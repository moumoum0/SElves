package com.selves.xnn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.selves.xnn.model.*
import com.selves.xnn.viewmodel.LocationTrackingViewModel
import java.time.format.DateTimeFormatter

@Composable
fun LocationTrackingPreview(
    currentMemberId: String,
    onNavigateToLocationPage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val trackingConfig by viewModel.trackingConfig.collectAsState()
    val trackingStats by viewModel.trackingStats.collectAsState()
    
    // 设置当前成员ID
    LaunchedEffect(currentMemberId) {
        viewModel.setCurrentMemberId(currentMemberId)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToLocationPage() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 状态指示器
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (uiState.trackingStatus) {
                                    TrackingStatus.RECORDING -> MaterialTheme.colorScheme.primary
                                    TrackingStatus.STOPPED -> MaterialTheme.colorScheme.outline
                                }
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when (uiState.trackingStatus) {
                            TrackingStatus.RECORDING -> "记录中"
                            TrackingStatus.STOPPED -> "未记录"
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "查看详情",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatPreviewItem(
                    label = "今日记录",
                    value = trackingStats.todayRecords.toString(),
                    icon = Icons.Default.Today
                )
                
                StatPreviewItem(
                    label = "总记录数",
                    value = trackingStats.totalRecords.toString(),
                    icon = Icons.Default.Timeline
                )
                
                StatPreviewItem(
                    label = "最后记录",
                    value = trackingStats.lastRecordTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--",
                    icon = Icons.Default.Schedule
                )
            }
            
            // 显示快捷控制
            Spacer(modifier = Modifier.height(12.dp))
            
            when (uiState.trackingStatus) {
                TrackingStatus.RECORDING -> {
                    Button(
                        onClick = { viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.StopTracking) },
                        modifier = Modifier.fillMaxWidth(),
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
                TrackingStatus.STOPPED -> {
                    Button(
                        onClick = { viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.StartTracking) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始记录轨迹")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPreviewItem(
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
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
