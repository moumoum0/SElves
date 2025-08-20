package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.selves.xnn.model.*
import com.selves.xnn.ui.components.*
import com.selves.xnn.viewmodel.LocationTrackingViewModel
import com.selves.xnn.util.MapNavigationUtils
import com.selves.xnn.util.LocationPermissionHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationTrackingScreen(
    currentMemberId: String,
    onNavigateBack: (() -> Unit)? = null,
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
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部应用栏
        TopAppBar(
            title = {
                Text(
                    text = "轨迹记录",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                if (onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            },
            actions = {
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
            }
        )
        
        // 主要内容
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 轨迹控制面板
            item {
                TrackingControlPanel(
                    uiState = uiState,
                    trackingConfig = trackingConfig,
                    trackingStats = trackingStats,
                    onEvent = viewModel::handleEvent
                )
            }
            
            // 日期选择器
            item {
                DateSelectorCard(
                    selectedDate = selectedDate,
                    onDateSelected = { date ->
                        viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.SelectDate(date))
                    },
                    onShowDatePicker = { showDatePicker = true }
                )
            }
            
            // 轨迹统计卡片
            item {
                TrackingStatsCard(
                    stats = trackingStats,
                    selectedDate = selectedDate
                )
            }
            
            // 位置记录列表
            item {
                LocationRecordsCard(
                    locationRecords = locationRecords,
                    selectedDate = selectedDate,
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
private fun TrackingControlPanel(
    uiState: com.selves.xnn.viewmodel.LocationTrackingUiState,
    trackingConfig: TrackingConfig,
    trackingStats: TrackingStats,
    onEvent: (com.selves.xnn.viewmodel.LocationTrackingEvent) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 状态指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (uiState.trackingStatus) {
                            TrackingStatus.RECORDING -> "正在记录轨迹"
                            TrackingStatus.STOPPED -> "轨迹记录已停止"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (uiState.trackingStatus) {
                            TrackingStatus.RECORDING -> MaterialTheme.colorScheme.primary
                            TrackingStatus.STOPPED -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    if (uiState.trackingStatus == TrackingStatus.RECORDING) {
                        Text(
                            text = "每${trackingConfig.recordingInterval}秒记录一次位置",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // 状态图标
                Icon(
                    imageVector = when (uiState.trackingStatus) {
                        TrackingStatus.RECORDING -> Icons.Default.FiberManualRecord
                        TrackingStatus.STOPPED -> Icons.Default.Stop
                    },
                    contentDescription = null,
                    tint = when (uiState.trackingStatus) {
                        TrackingStatus.RECORDING -> MaterialTheme.colorScheme.primary
                        TrackingStatus.STOPPED -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (uiState.trackingStatus) {
                    TrackingStatus.STOPPED -> {
                        Button(
                            onClick = {
                                if (!LocationPermissionHelper.hasLocationPermission(context)) {
                                    (context as? Activity)?.let { activity ->
                                        LocationPermissionHelper.requestLocationPermission(activity)
                                    }
                                    return@Button
                                }
                                onEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.StartTracking)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("开始记录")
                        }
                    }
                    TrackingStatus.RECORDING -> {
                        Button(
                            onClick = { onEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.StopTracking) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("停止记录")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelectorCard(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "查看日期",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 前一天
                IconButton(
                    onClick = { onDateSelected(selectedDate.minusDays(1)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "前一天",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // 日期显示
                TextButton(
                    onClick = onShowDatePicker,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
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
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "后一天",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingStatsCard(
    stats: TrackingStats,
    selectedDate: LocalDate
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "轨迹统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "总记录数",
                    value = stats.totalRecords.toString(),
                    icon = Icons.Default.Timeline
                )
                
                StatItem(
                    label = if (selectedDate.isEqual(LocalDate.now())) "今日记录" else "当日记录",
                    value = stats.todayRecords.toString(),
                    icon = Icons.Default.Today
                )
                
                StatItem(
                    label = "最后记录",
                    value = stats.lastRecordTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--",
                    icon = Icons.Default.Schedule
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocationRecordsCard(
    locationRecords: List<LocationRecord>,
    selectedDate: LocalDate,
    onNavigate: (LocationRecord, MapApp) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedDate.isEqual(LocalDate.now())) "今日轨迹" else "当日轨迹",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${locationRecords.size} 个记录点",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (locationRecords.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (selectedDate.isEqual(LocalDate.now())) {
                            "今日暂无轨迹记录"
                        } else {
                            "该日期无轨迹记录"
                        },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = if (selectedDate.isEqual(LocalDate.now())) {
                            "开启记录功能开始记录轨迹"
                        } else {
                            "选择其他日期查看轨迹"
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    locationRecords.forEachIndexed { index, record ->
                        LocationTimelineItem(
                            locationRecord = record,
                            isFirst = index == 0,
                            isLast = index == locationRecords.lastIndex,
                            onNavigate = { mapApp -> onNavigate(record, mapApp) }
                        )
                    }
                }
            }
        }
    }
}
