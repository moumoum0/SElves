package com.selves.xnn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<String>, LocalDateTime?, Boolean, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(mutableListOf("", "")) }
    var endTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var allowMultipleChoice by remember { mutableStateOf(false) }
    var isAnonymous by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDateTime?>(null) }
    
    val context = LocalContext.current
    // 不修改系统UI颜色，保持与主应用一致
    
    // 使用Box替代Dialog来实现全屏效果
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = { Text("创建投票") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                if (title.isNotBlank() && description.isNotBlank() && 
                                    options.filter { it.isNotBlank() }.size >= 2) {
                                    onConfirm(
                                        title,
                                        description,
                                        options.filter { it.isNotBlank() },
                                        endTime,
                                        allowMultipleChoice,
                                        isAnonymous
                                    )
                                }
                            },
                            enabled = title.isNotBlank() && description.isNotBlank() && 
                                     options.filter { it.isNotBlank() }.size >= 2
                        ) {
                            Text(
                                text = "发布",
                                color = if (title.isNotBlank() && description.isNotBlank() && 
                                           options.filter { it.isNotBlank() }.size >= 2) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
            
            // 内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 投票信息卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DialogSectionHeader(
                            icon = Icons.Default.Description,
                            title = "投票信息",
                            subtitle = "填写投票的基本信息"
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("投票标题") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("投票描述") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
                
                // 投票选项卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DialogSectionHeader(
                            icon = Icons.Default.Notes,
                            title = "投票选项",
                            subtitle = "至少添加2个选项"
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = option,
                                    onValueChange = { newValue ->
                                        options = options.toMutableList().apply { this[index] = newValue }
                                    },
                                    label = { Text("选项 ${index + 1}") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )
                                
                                if (options.size > 2) {
                                    IconButton(
                                        onClick = { options = options.toMutableList().apply { removeAt(index) } }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除选项", tint = MaterialTheme.colorScheme.error)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(48.dp))
                                }
                            }
                            
                            if (index < options.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (options.size < 10) {
                            OutlinedButton(
                                onClick = { options = options.toMutableList().apply { add("") } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "添加选项")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("添加选项")
                            }
                        }
                    }
                }
                
                // 投票设置卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DialogSectionHeader(
                            icon = Icons.Default.Settings,
                            title = "投票设置",
                            subtitle = "配置投票的附加选项"
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DialogSettingsRow(
                            icon = Icons.Default.List,
                            title = "允许多选",
                            subtitle = "允许用户选择多个选项",
                            trailing = {
                                Switch(checked = allowMultipleChoice, onCheckedChange = { allowMultipleChoice = it })
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DialogSettingsRow(
                            icon = Icons.Default.VisibilityOff,
                            title = "匿名投票",
                            subtitle = "投票结果不显示投票者",
                            trailing = {
                                Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DialogSettingsRow(
                            icon = Icons.Default.Schedule,
                            title = "投票截止时间",
                            subtitle = endTime?.let {
                                "截止: ${it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
                            } ?: "不限制",
                            trailing = {
                                if (endTime != null) {
                                    IconButton(onClick = { endTime = null }) {
                                        Icon(Icons.Default.Clear, contentDescription = "清除")
                                    }
                                }
                            },
                            onClick = { showDatePicker = true }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime()
                        }
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) {
                    Text("下一步")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showTimePicker && selectedDate != null) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endTime = selectedDate?.withHour(timePickerState.hour)?.withMinute(timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            },
            title = { Text("选择时间") },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
private fun DialogSectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DialogSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        trailing()
    }
}
