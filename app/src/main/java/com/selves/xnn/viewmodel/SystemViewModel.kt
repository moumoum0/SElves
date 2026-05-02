package com.selves.xnn.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.R
import com.selves.xnn.data.repository.SystemRepository
import com.selves.xnn.model.System
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SystemViewModel @Inject constructor(
    private val systemRepository: SystemRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val TAG = "SystemViewModel"
    
    // 当前系统
    private val _currentSystem = MutableStateFlow<System?>(null)
    val currentSystem: StateFlow<System?> = _currentSystem.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 异常处理器，用于处理协程取消
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is CancellationException) {
            Log.d(TAG, context.getString(R.string.error_system_coroutine_cancelled) + ": ${throwable.message}")
        } else {
            Log.e(TAG, context.getString(R.string.error_system_coroutine_exception) + ": ${throwable.message}", throwable)
            _error.value = "${context.getString(R.string.error_system_operation_failed)}: ${throwable.message}"
            _isLoading.value = false
        }
    }
    
    init {
        loadCurrentSystem()
    }
    
    /**
     * 加载当前系统
     */
    private fun loadCurrentSystem() {
        viewModelScope.launch(exceptionHandler) {
            try {
                _isLoading.value = true
                systemRepository.getCurrentSystem()
                    .catch { e ->
                        if (e is CancellationException) {
                            Log.d(TAG, context.getString(R.string.error_system_load_stream_cancelled))
                        } else {
                            Log.e(TAG, context.getString(R.string.error_system_load_failed) + ": ${e.message}", e)
                            _error.value = "${context.getString(R.string.error_system_load_failed)}: ${e.message}"
                        }
                        _isLoading.value = false
                    }
                    .collect { system ->
                        _currentSystem.value = system
                        _isLoading.value = false
                        Log.d(TAG, "${context.getString(R.string.msg_system_loaded)}: ${system?.name}")
                    }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, context.getString(R.string.error_system_load_failed) + ": ${e.message}", e)
                    _error.value = "${context.getString(R.string.error_system_load_failed)}: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * 创建系统
     */
    fun createSystem(name: String, avatarUrl: String?, description: String = "") {
        viewModelScope.launch(exceptionHandler) {
            try {
                val system = System(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    avatarUrl = avatarUrl,
                    description = description,
                    createdAt = java.lang.System.currentTimeMillis(),
                    updatedAt = java.lang.System.currentTimeMillis()
                )
                
                systemRepository.saveSystem(system)
                _currentSystem.value = system
                Log.d(TAG, "${context.getString(R.string.msg_system_created)}: $name")
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, context.getString(R.string.error_system_create_failed) + ": ${e.message}", e)
                    _error.value = "${context.getString(R.string.error_system_create_failed)}: ${e.message}"
                }
            }
        }
    }
    
    /**
     * 更新系统
     */
    fun updateSystem(system: System) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val updatedSystem = system.copy(updatedAt = java.lang.System.currentTimeMillis())
                systemRepository.updateSystem(updatedSystem)
                _currentSystem.value = updatedSystem
                Log.d(TAG, "${context.getString(R.string.msg_system_updated)}: ${system.name}")
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, context.getString(R.string.error_system_update_failed) + ": ${e.message}", e)
                    _error.value = "${context.getString(R.string.error_system_update_failed)}: ${e.message}"
                }
            }
        }
    }
    
    /**
     * 检查系统是否存在
     */
    suspend fun hasSystem(): Boolean {
        return try {
            systemRepository.hasSystem()
        } catch (e: Exception) {
            if (e !is CancellationException) {
                Log.e(TAG, context.getString(R.string.error_system_check_exists_failed) + ": ${e.message}", e)
            }
            false
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }
} 