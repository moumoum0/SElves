package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.model.Member
import com.selves.xnn.viewmodel.VoteViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoteScreen(
    currentMember: Member?,
    onNavigateBack: () -> Unit,
    voteViewModel: VoteViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(mutableListOf("", "")) }
    var endTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var allowMultipleChoice by remember { mutableStateOf(false) }
    var isAnonymous by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { voteViewModel.setCurrentUser(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建投票") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val validOptions = options.filter { it.isNotBlank() }
                            if (title.isNotBlank() && description.isNotBlank() && validOptions.size >= 2 && currentMember != null) {
                                voteViewModel.createVote(
                                    title = title,
                                    description = description,
                                    authorName = currentMember.name,
                                    authorAvatar = currentMember.avatarUrl,
                                    options = validOptions,
                                    endTime = endTime,
                                    allowMultipleChoice = allowMultipleChoice,
                                    isAnonymous = isAnonymous
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank() && description.isNotBlank() && options.count { it.isNotBlank() } >= 2
                    ) {
                        Text(
                            text = "发布",
                            color = if (title.isNotBlank() && description.isNotBlank() && options.count { it.isNotBlank() } >= 2) {
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 投票信息卡片
            item {
                VoteInfoCard(
                    title = title,
                    description = description,
                    onTitleChange = { title = it },
                    onDescriptionChange = { description = it }
                )
            }

            // 投票选项卡片
            item {
                VoteOptionsCard(
                    options = options,
                    onOptionsChange = { options = it }
                )
            }

            // 投票设置卡片
            item {
                VoteSettingsCard(
                    endTime = endTime,
                    allowMultipleChoice = allowMultipleChoice,
                    isAnonymous = isAnonymous,
                    onEndTimeChange = { endTime = it },
                    onMultipleChoiceChange = { allowMultipleChoice = it },
                    onAnonymousChange = { isAnonymous = it },
                    showDatePicker = showDatePicker,
                    onShowDatePickerChange = { showDatePicker = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun VoteInfoCard(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.Title,
                title = "投票信息",
                subtitle = "填写投票的基本信息"
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
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
                onValueChange = onDescriptionChange,
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
}

@Composable
private fun VoteOptionsCard(
    options: MutableList<String>,
    onOptionsChange: (MutableList<String>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.List,
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
                            onOptionsChange(options.toMutableList().apply { this[index] = newValue })
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
                            onClick = { onOptionsChange(options.toMutableList().apply { removeAt(index) }) }
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
                    onClick = { onOptionsChange(options.toMutableList().apply { add("") }) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加选项")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加选项")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoteSettingsCard(
    endTime: LocalDateTime?,
    allowMultipleChoice: Boolean,
    isAnonymous: Boolean,
    onEndTimeChange: (LocalDateTime?) -> Unit,
    onMultipleChoiceChange: (Boolean) -> Unit,
    onAnonymousChange: (Boolean) -> Unit,
    showDatePicker: Boolean,
    onShowDatePickerChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.Settings,
                title = "投票设置",
                subtitle = "配置投票的附加选项"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsRow(
                icon = Icons.Default.List,
                title = "允许多选",
                subtitle = "允许用户选择多个选项",
                trailing = {
                    Switch(checked = allowMultipleChoice, onCheckedChange = onMultipleChoiceChange)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Default.VisibilityOff,
                title = "匿名投票",
                subtitle = "投票结果不显示投票者",
                trailing = {
                    Switch(checked = isAnonymous, onCheckedChange = onAnonymousChange)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Default.Schedule,
                title = "投票截止时间",
                subtitle = endTime?.let {
                    "截止: ${it.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
                } ?: "不限制",
                trailing = {
                    if (endTime != null) {
                        IconButton(onClick = { onEndTimeChange(null) }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                onClick = { onShowDatePickerChange(true) }
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { onShowDatePickerChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime()
                                .plusDays(1)
                                .minusSeconds(1)
                            onEndTimeChange(selectedDate)
                        }
                        onShowDatePickerChange(false)
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowDatePickerChange(false) }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SectionHeader(
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
private fun SettingsRow(
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
