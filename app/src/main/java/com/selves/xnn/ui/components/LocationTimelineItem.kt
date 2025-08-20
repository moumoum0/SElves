package com.selves.xnn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.selves.xnn.model.LocationRecord
import com.selves.xnn.model.MapApp
import com.selves.xnn.util.MapNavigationUtils
import java.time.format.DateTimeFormatter

@Composable
fun LocationTimelineItem(
    locationRecord: LocationRecord,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onNavigate: (MapApp) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMapSelection by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 时间线左侧
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 上方连接线
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
            
            // 时间点圆圈
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            
            // 下方连接线
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))

        // 中间内容与右侧导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间与地址
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = locationRecord.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when {
                            !locationRecord.address.isNullOrEmpty() -> locationRecord.address!!
                            else -> "${String.format("%.6f", locationRecord.latitude)}, ${String.format("%.6f", locationRecord.longitude)}"
                        },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(onClick = { showMapSelection = true }) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "导航",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    // 地图选择对话框
    if (showMapSelection) {
        MapSelectionDialog(
            onMapSelected = { mapApp ->
                onNavigate(mapApp)
                showMapSelection = false
            },
            onDismiss = { showMapSelection = false }
        )
    }
}

@Composable
fun MapSelectionDialog(
    onMapSelected: (MapApp) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val availableApps = remember(context) { MapNavigationUtils.getAvailableMapApps(context) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("选择地图应用")
        },
        text = {
            Column {
                if (availableApps.isEmpty()) {
                    Text("未检测到已安装的地图应用")
                } else {
                    availableApps.forEach { mapApp ->
                        TextButton(
                            onClick = { onMapSelected(mapApp) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = mapApp.displayName,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

