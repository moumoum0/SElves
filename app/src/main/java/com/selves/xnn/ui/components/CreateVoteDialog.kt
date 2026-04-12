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
import androidx.compose.ui.res.stringResource
import com.selves.xnn.R
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
                    title = { Text(stringResource(R.string.vote_create)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                                text = stringResource(R.string.btn_publish),
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
                            title = stringResource(R.string.vote_info),
                            subtitle = stringResource(R.string.vote_info_subtitle)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(R.string.vote_title_label)) },
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
                            label = { Text(stringResource(R.string.vote_description)) },
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
                            title = stringResource(R.string.vote_options),
                            subtitle = stringResource(R.string.vote_options_subtitle)
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
                                    label = { Text(stringResource(R.string.vote_option_label, index + 1)) },
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
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.vote_delete_option), tint = MaterialTheme.colorScheme.error)
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
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.vote_add_option))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.vote_add_option)) // replace_all
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
                            title = stringResource(R.string.vote_settings),
                            subtitle = stringResource(R.string.vote_settings_subtitle)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DialogSettingsRow(
                            icon = Icons.Default.List,
                            title = stringResource(R.string.vote_allow_multiple),
                            subtitle = stringResource(R.string.vote_allow_multiple_desc),
                            trailing = {
                                Switch(checked = allowMultipleChoice, onCheckedChange = { allowMultipleChoice = it })
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DialogSettingsRow(
                            icon = Icons.Default.VisibilityOff,
                            title = stringResource(R.string.vote_anonymous),
                            subtitle = stringResource(R.string.vote_anonymous_desc),
                            trailing = {
                                Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DialogSettingsRow(
                            icon = Icons.Default.Schedule,
                            title = stringResource(R.string.vote_end_time),
                            subtitle = endTime?.let {
                                stringResource(R.string.vote_end_time_format, it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                            } ?: stringResource(R.string.vote_end_time_none),
                            trailing = {
                                if (endTime != null) {
                                    IconButton(onClick = { endTime = null }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_clear))
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
                    Text(stringResource(R.string.btn_next))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
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
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
            title = { Text(stringResource(R.string.vote_select_time)) },
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
