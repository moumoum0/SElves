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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.R
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
                    text = stringResource(R.string.location_preview_title),
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
                            TrackingStatus.RECORDING -> stringResource(R.string.location_preview_status_recording)
                            TrackingStatus.STOPPED -> stringResource(R.string.location_preview_status_stopped)
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.cd_view_history),
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
                    label = stringResource(R.string.location_preview_today_records),
                    value = trackingStats.todayRecords.toString(),
                    icon = Icons.Default.Today
                )
                
                StatPreviewItem(
                    label = stringResource(R.string.location_preview_total_records),
                    value = trackingStats.totalRecords.toString(),
                    icon = Icons.Default.Timeline
                )
                
                StatPreviewItem(
                    label = stringResource(R.string.location_preview_last_record),
                    value = trackingStats.lastRecordTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--",
                    icon = Icons.Default.Schedule
                )
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
