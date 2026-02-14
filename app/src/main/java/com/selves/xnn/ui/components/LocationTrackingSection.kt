package com.selves.xnn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.selves.xnn.model.*
import com.selves.xnn.viewmodel.LocationTrackingViewModel

import com.selves.xnn.util.MapNavigationUtils
import com.selves.xnn.util.LocationPermissionHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LocationTrackingSection(
    currentMemberId: String,
    modifier: Modifier = Modifier,
    viewModel: LocationTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val trackingConfig by viewModel.trackingConfig.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val locationRecords by viewModel.locationRecords.collectAsState()
    val trackingStats by viewModel.trackingStats.collectAsState()
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    // 设置当前成员ID
    LaunchedEffect(currentMemberId) {
        viewModel.setCurrentMemberId(currentMemberId)
    }
    
    // 处理导航请求
    LaunchedEffect(uiState.navigationRequest) {
        uiState.navigationRequest?.let { request ->
            MapNavigationUtils.navigateToLocation(
                context = context,
                latitude = request.locationRecord.latitude,
                longitude = request.locationRecord.longitude,
                mapApp = request.mapApp,
                name = request.locationRecord.address
            )
            viewModel.clearNavigationRequest()
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "轨迹记录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row {
                    // 设置按钮
                    IconButton(
                        onClick = { viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.ShowConfigDialog) }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 记录控制按钮
                    when (uiState.trackingStatus) {
                        TrackingStatus.STOPPED -> {
                            IconButton(
                                onClick = {
                                    if (!LocationPermissionHelper.hasLocationPermission(context)) {
                                        (context as? Activity)?.let { activity ->
                                            LocationPermissionHelper.requestLocationPermission(activity)
                                        }
                                        return@IconButton
                                    }
                                    viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.StartTracking)
                                }
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "开始记录",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        TrackingStatus.RECORDING -> {
                            IconButton(
                                onClick = { viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.StopTracking) }
                            ) {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = "停止记录",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 状态指示器
            StatusIndicator(
                status = uiState.trackingStatus,
                config = trackingConfig,
                stats = trackingStats
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 日期选择器
            DateSelector(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.SelectDate(date))
                },
                onShowDatePicker = { showDatePicker = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 位置记录列表
            if (locationRecords.isEmpty()) {
                EmptyLocationRecords(selectedDate = selectedDate)
            } else {
                LocationRecordsList(
                    locationRecords = locationRecords,
                    onNavigate = { record, mapApp ->
                        viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.NavigateToLocation(record, mapApp))
                    }
                )
            }
        }
    }
    
    // 配置对话框
    if (uiState.showConfigDialog) {
        LocationTrackingConfigDialog(
            config = trackingConfig,
            onConfigUpdate = { config ->
                viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.UpdateConfig(config))
            },
            onDismiss = { viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.HideConfigDialog) }
        )
    }
    
    // 日期选择器对话框
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.SelectDate(date))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // 错误提示
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 显示错误提示，这里可以使用SnackBar
            viewModel.clearError()
        }
    }
}

@Composable
private fun StatusIndicator(
    status: TrackingStatus,
    config: TrackingConfig,
    stats: TrackingStats
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when (status) {
                    TrackingStatus.RECORDING -> MaterialTheme.colorScheme.primaryContainer
                    TrackingStatus.STOPPED -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (status) {
                TrackingStatus.RECORDING -> Icons.Default.FiberManualRecord
                TrackingStatus.STOPPED -> Icons.Default.Stop
            },
            contentDescription = null,
            tint = when (status) {
                TrackingStatus.RECORDING -> MaterialTheme.colorScheme.primary
                TrackingStatus.STOPPED -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (status) {
                    TrackingStatus.RECORDING -> "记录中"
                    TrackingStatus.STOPPED -> "未记录"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (status == TrackingStatus.RECORDING) {
                Text(
                    text = "每${config.recordingInterval}秒记录一次",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 统计信息
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "今日: ${stats.todayRecords}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "总计: ${stats.totalRecords}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "查看日期",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 前一天
            IconButton(
                onClick = { onDateSelected(selectedDate.minusDays(1)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "前一天",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // 日期显示
            TextButton(
                onClick = onShowDatePicker,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // 后一天
            IconButton(
                onClick = { 
                    if (selectedDate.isBefore(LocalDate.now())) {
                        onDateSelected(selectedDate.plusDays(1))
                    }
                },
                enabled = selectedDate.isBefore(LocalDate.now()),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "后一天",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyLocationRecords(selectedDate: LocalDate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (selectedDate.isEqual(LocalDate.now())) {
                "今日暂无轨迹记录"
            } else {
                "该日期无轨迹记录"
            },
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (selectedDate.isEqual(LocalDate.now())) {
                "开启记录功能开始记录轨迹"
            } else {
                "选择其他日期查看轨迹"
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LocationRecordsList(
    locationRecords: List<LocationRecord>,
    onNavigate: (LocationRecord, MapApp) -> Unit
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(locationRecords) { index, record ->
            LocationTimelineItem(
                locationRecord = record,
                isFirst = index == 0,
                isLast = index == locationRecords.lastIndex,
                onNavigate = { mapApp -> onNavigate(record, mapApp) }
            )
        }
    }
}
