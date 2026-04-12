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
import androidx.compose.ui.res.stringResource
import com.selves.xnn.R
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
                title = { Text(stringResource(R.string.vote_create)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                            text = stringResource(R.string.btn_publish),
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
                title = stringResource(R.string.vote_info),
                subtitle = stringResource(R.string.vote_info_subtitle)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
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
                onValueChange = onDescriptionChange,
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
                            onOptionsChange(options.toMutableList().apply { this[index] = newValue })
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
                            onClick = { onOptionsChange(options.toMutableList().apply { removeAt(index) }) }
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
                    onClick = { onOptionsChange(options.toMutableList().apply { add("") }) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.vote_add_option))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.vote_add_option))
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
                title = stringResource(R.string.vote_settings),
                subtitle = stringResource(R.string.vote_settings_subtitle)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsRow(
                icon = Icons.Default.List,
                title = stringResource(R.string.vote_allow_multiple),
                subtitle = stringResource(R.string.vote_allow_multiple_desc),
                trailing = {
                    Switch(checked = allowMultipleChoice, onCheckedChange = onMultipleChoiceChange)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Default.VisibilityOff,
                title = stringResource(R.string.vote_anonymous),
                subtitle = stringResource(R.string.vote_anonymous_desc),
                trailing = {
                    Switch(checked = isAnonymous, onCheckedChange = onAnonymousChange)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Default.Schedule,
                title = stringResource(R.string.vote_end_time),
                subtitle = endTime?.let {
                    stringResource(R.string.vote_end_time_format, it.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                } ?: stringResource(R.string.vote_end_time_none),
                trailing = {
                    if (endTime != null) {
                        IconButton(onClick = { onEndTimeChange(null) }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_clear))
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
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowDatePickerChange(false) }) {
                    Text(stringResource(R.string.btn_cancel))
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
