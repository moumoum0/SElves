package com.selves.xnn.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.selves.xnn.data.repository.LocationRecordRepository
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.model.*
import com.selves.xnn.service.LocationTrackingService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class LocationTrackingViewModel @Inject constructor(
    private val locationRecordRepository: LocationRecordRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val memberPreferences = MemberPreferences(context)

    // UI状态
    private val _uiState = MutableStateFlow(LocationTrackingUiState())
    val uiState: StateFlow<LocationTrackingUiState> = _uiState.asStateFlow()

    // 轨迹记录配置（从持久化存储加载）
    val trackingConfig: StateFlow<TrackingConfig> = memberPreferences.trackingConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrackingConfig()
        )

    // 当前选择的日期
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // 当前成员ID（从MainViewModel获取）
    private val _currentMemberId = MutableStateFlow("")
    private val currentMemberId: StateFlow<String> = _currentMemberId.asStateFlow()

    // 获取指定日期的位置记录
    val locationRecords = combine(
        selectedDate,
        currentMemberId
    ) { date, memberId ->
        if (memberId.isNotEmpty()) {
            val startDateTime = date.atStartOfDay()
            val endDateTime = date.atTime(LocalTime.MAX)
            locationRecordRepository.getLocationRecordsByDateRange(startDateTime, endDateTime, memberId)
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 轨迹统计信息
    private val _trackingStats = MutableStateFlow(TrackingStats())
    val trackingStats: StateFlow<TrackingStats> = _trackingStats.asStateFlow()

    // 广播接收器，用于监听轨迹记录状态变化
    private val trackingStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationTrackingService.ACTION_TRACKING_STATUS_CHANGED) {
                val isTracking = intent.getBooleanExtra(LocationTrackingService.EXTRA_IS_TRACKING, false)
                val trackingStatus = if (isTracking) TrackingStatus.RECORDING else TrackingStatus.STOPPED
                
                _uiState.update { it.copy(trackingStatus = trackingStatus) }
                
                // 同步配置状态到持久化存储
                viewModelScope.launch {
                    val currentConfig = trackingConfig.value
                    if (currentConfig.isEnabled != isTracking) {
                        memberPreferences.saveTrackingConfig(currentConfig.copy(isEnabled = isTracking))
                    }
                }
                
                android.util.Log.d("LocationTrackingViewModel", "Received tracking status change: $trackingStatus")
            }
        }
    }

    init {
        // 初始化时加载轨迹统计信息
        loadTrackingStats()
        
        // 注册广播接收器
        val intentFilter = IntentFilter(LocationTrackingService.ACTION_TRACKING_STATUS_CHANGED)
        androidx.core.content.ContextCompat.registerReceiver(
            context,
            trackingStatusReceiver,
            intentFilter,
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )
        
        // 初始化时检查服务状态
        checkAndUpdateServiceStatus()
    }

    // 检查并更新服务状态
    private fun checkAndUpdateServiceStatus() {
        val isCurrentlyTracking = LocationTrackingService.isTrackingActive()
        val initialStatus = if (isCurrentlyTracking) TrackingStatus.RECORDING else TrackingStatus.STOPPED
        _uiState.update { it.copy(trackingStatus = initialStatus) }
        
        // 如果服务正在运行但配置显示未启用，需要同步配置状态
        if (isCurrentlyTracking && !trackingConfig.value.isEnabled) {
            viewModelScope.launch {
                val currentConfig = trackingConfig.value
                memberPreferences.saveTrackingConfig(currentConfig.copy(isEnabled = true))
            }
        }
        
        android.util.Log.d("LocationTrackingViewModel", "Initial service status check: isTracking=$isCurrentlyTracking, status=$initialStatus")
    }

    // 设置当前成员ID
    fun setCurrentMemberId(memberId: String) {
        _currentMemberId.value = memberId
        loadTrackingStats()
        // 重新检查服务状态，以防状态不同步
        checkAndUpdateServiceStatus()
    }

    // 处理UI事件
    fun handleEvent(event: LocationTrackingEvent) {
        when (event) {
            is LocationTrackingEvent.StartTracking -> startTracking()
            is LocationTrackingEvent.StopTracking -> stopTracking()
            is LocationTrackingEvent.UpdateConfig -> updateTrackingConfig(event.config)
            is LocationTrackingEvent.SelectDate -> selectDate(event.date)
            is LocationTrackingEvent.NavigateToLocation -> navigateToLocation(event.locationRecord, event.mapApp)
            is LocationTrackingEvent.AddLocationRecord -> addLocationRecord(event.latitude, event.longitude, event.address, event.note)
            is LocationTrackingEvent.DeleteLocationRecord -> deleteLocationRecord(event.recordId)
            is LocationTrackingEvent.ShowConfigDialog -> showConfigDialog()
            is LocationTrackingEvent.HideConfigDialog -> hideConfigDialog()
        }
    }

    private fun startTracking() {
        viewModelScope.launch {
            try {
                // 更新配置为启用状态
                val currentConfig = trackingConfig.value
                memberPreferences.saveTrackingConfig(currentConfig.copy(isEnabled = true))
                _uiState.update { it.copy(trackingStatus = TrackingStatus.RECORDING) }
                
                // 启动位置跟踪服务
                LocationTrackingService.startTracking(
                    context = context,
                    memberId = currentMemberId.value,
                    interval = currentConfig.recordingInterval * 1000L // 转换为毫秒
                )

                // 取消可能存在的自动重启闹钟
                LocationTrackingService.cancelAutoRestart(context)
                
                // 延迟检查确保服务状态同步
                kotlinx.coroutines.delay(500)
                checkAndUpdateServiceStatus()
                
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "启动轨迹记录失败: ${e.message}") }
            }
        }
    }

    private fun stopTracking() {
        viewModelScope.launch {
            try {
                // 更新配置为停用状态
                val currentConfig = trackingConfig.value
                memberPreferences.saveTrackingConfig(currentConfig.copy(isEnabled = false))
                _uiState.update { it.copy(trackingStatus = TrackingStatus.STOPPED) }
                
                // 停止位置跟踪服务
                LocationTrackingService.stopTracking(context)

                // 如果开启了自动开启，则在指定延迟后自动重启
                if (currentConfig.enableAutoStart && currentMemberId.value.isNotEmpty()) {
                    LocationTrackingService.scheduleAutoRestart(
                        context = context,
                        memberId = currentMemberId.value,
                        interval = currentConfig.recordingInterval * 1000L,
                        delaySeconds = currentConfig.autoRestartDelay.toLong()
                    )
                    android.util.Log.d("LocationTrackingViewModel", "Auto-restart scheduled: enableAutoStart=${currentConfig.enableAutoStart}, delay=${currentConfig.autoRestartDelay}s")
                } else {
                    LocationTrackingService.cancelAutoRestart(context)
                    android.util.Log.d("LocationTrackingViewModel", "Auto-restart not scheduled: enableAutoStart=${currentConfig.enableAutoStart}, memberId=${currentMemberId.value}")
                }
                
                // 延迟检查确保服务状态同步
                kotlinx.coroutines.delay(500)
                checkAndUpdateServiceStatus()
                
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "停止轨迹记录失败: ${e.message}") }
            }
        }
    }

    private fun updateTrackingConfig(config: TrackingConfig) {
        viewModelScope.launch {
            // 保存配置到持久化存储
            memberPreferences.saveTrackingConfig(config)
            _uiState.update { it.copy(showConfigDialog = false) }
            
            android.util.Log.d("LocationTrackingViewModel", "Tracking config updated: enableAutoStart=${config.enableAutoStart}, autoRestartDelay=${config.autoRestartDelay}s")
        }
    }

    private fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    private fun navigateToLocation(locationRecord: LocationRecord, mapApp: MapApp) {
        // 这里会在UI层处理地图跳转
        _uiState.update { 
            it.copy(navigationRequest = NavigationRequest(locationRecord, mapApp))
        }
    }

    private fun addLocationRecord(latitude: Double, longitude: Double, address: String?, note: String?) {
        viewModelScope.launch {
            try {
                locationRecordRepository.addLocationRecord(
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    memberId = currentMemberId.value,
                    note = note
                )
                loadTrackingStats()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun deleteLocationRecord(recordId: String) {
        viewModelScope.launch {
            try {
                locationRecordRepository.deleteLocationRecordById(recordId)
                loadTrackingStats()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun showConfigDialog() {
        _uiState.update { it.copy(showConfigDialog = true) }
    }

    private fun hideConfigDialog() {
        _uiState.update { it.copy(showConfigDialog = false) }
    }

    private fun loadTrackingStats() {
        if (currentMemberId.value.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val stats = locationRecordRepository.getTrackingStats(currentMemberId.value)
                _trackingStats.value = stats
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // 清除导航请求
    fun clearNavigationRequest() {
        _uiState.update { it.copy(navigationRequest = null) }
    }

    // 清除错误信息
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        // 注销广播接收器
        try {
            context.unregisterReceiver(trackingStatusReceiver)
        } catch (e: Exception) {
            android.util.Log.e("LocationTrackingViewModel", "Error unregistering receiver: ${e.message}")
        }
    }
}

// UI状态数据类
data class LocationTrackingUiState(
    val trackingStatus: TrackingStatus = TrackingStatus.STOPPED,
    val showConfigDialog: Boolean = false,
    val navigationRequest: NavigationRequest? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)

// 导航请求数据类
data class NavigationRequest(
    val locationRecord: LocationRecord,
    val mapApp: MapApp
)

// UI事件封装
sealed class LocationTrackingEvent {
    object StartTracking : LocationTrackingEvent()
    object StopTracking : LocationTrackingEvent()
    data class UpdateConfig(val config: TrackingConfig) : LocationTrackingEvent()
    data class SelectDate(val date: LocalDate) : LocationTrackingEvent()
    data class NavigateToLocation(val locationRecord: LocationRecord, val mapApp: MapApp) : LocationTrackingEvent()
    data class AddLocationRecord(val latitude: Double, val longitude: Double, val address: String?, val note: String?) : LocationTrackingEvent()
    data class DeleteLocationRecord(val recordId: String) : LocationTrackingEvent()
    object ShowConfigDialog : LocationTrackingEvent()
    object HideConfigDialog : LocationTrackingEvent()
}

