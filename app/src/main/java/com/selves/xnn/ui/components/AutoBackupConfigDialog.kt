package com.selves.xnn.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.selves.xnn.R

/**
 * 自动备份配置对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoBackupConfigDialog(
    isOpen: Boolean,
    enabled: Boolean,
    frequency: String,
    hour: Int,
    onConfirm: (enabled: Boolean, frequency: String, hour: Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var tempEnabled by remember { mutableStateOf(enabled) }
    var tempFrequency by remember { mutableStateOf(frequency) }
    var tempHour by remember { mutableStateOf(hour) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showFrequencyMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(text = stringResource(R.string.settings_backup_auto)) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 启用/禁用开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_backup_auto_enable),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = tempEnabled,
                        onCheckedChange = { tempEnabled = it }
                    )
                }

                if (tempEnabled) {
                    // 频率选择（下拉菜单）
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFrequencyMenu = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_backup_auto_frequency),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (tempFrequency) {
                                        "daily" -> stringResource(R.string.settings_backup_auto_daily)
                                        "weekly" -> stringResource(R.string.settings_backup_auto_weekly)
                                        "monthly" -> stringResource(R.string.settings_backup_auto_monthly)
                                        else -> stringResource(R.string.settings_backup_auto_daily)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showFrequencyMenu,
                            onDismissRequest = { showFrequencyMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_backup_auto_daily)) },
                                onClick = {
                                    tempFrequency = "daily"
                                    showFrequencyMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_backup_auto_weekly)) },
                                onClick = {
                                    tempFrequency = "weekly"
                                    showFrequencyMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_backup_auto_monthly)) },
                                onClick = {
                                    tempFrequency = "monthly"
                                    showFrequencyMenu = false
                                }
                            )
                        }
                    }

                    // 时间选择
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_backup_auto_time),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = String.format("%02d:00", tempHour),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 说明文本
                    Text(
                        text = stringResource(R.string.settings_backup_auto_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onConfirm(tempEnabled, tempFrequency, tempHour)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )

    // TimePicker 对话框
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = tempHour,
            initialMinute = 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempHour = timePickerState.hour
                    showTimePicker = false
                }) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}