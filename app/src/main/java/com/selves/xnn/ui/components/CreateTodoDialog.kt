package com.selves.xnn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.selves.xnn.R
import com.selves.xnn.model.TodoPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTodoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, TodoPriority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TodoPriority.NORMAL) }
    var showTitleError by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.todo_create_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        showTitleError = false
                    },
                    label = { Text(stringResource(R.string.label_title)) },
                    isError = showTitleError,
                    supportingText = if (showTitleError) {
                        { Text(stringResource(R.string.error_title_empty)) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // 描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.label_description_optional)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // 优先级选择
                ExposedDropdownMenuBox(
                    expanded = showPriorityDropdown,
                    onExpandedChange = { showPriorityDropdown = !showPriorityDropdown }
                ) {
                    OutlinedTextField(
                        value = getPriorityText(priority),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_priority)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.todo_priority_select)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = getPriorityColor(priority),
                            unfocusedTextColor = getPriorityColor(priority)
                        ),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showPriorityDropdown,
                        onDismissRequest = { showPriorityDropdown = false }
                    ) {
                        TodoPriority.values().forEach { priorityOption ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = getPriorityText(priorityOption),
                                        color = getPriorityColor(priorityOption)
                                    )
                                },
                                onClick = {
                                    priority = priorityOption
                                    showPriorityDropdown = false
                                }
                            )
                        }
                    }
                }

                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.todo_cancel_action))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                showTitleError = true
                            } else {
                                onConfirm(title, description, priority)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.todo_create_action))
                    }
                }
            }
        }
    }
}

@Composable
private fun getPriorityText(priority: TodoPriority): String {
    return when (priority) {
        TodoPriority.LOW -> stringResource(R.string.todo_priority_low_full)
        TodoPriority.NORMAL -> stringResource(R.string.todo_priority_normal_full)
        TodoPriority.HIGH -> stringResource(R.string.todo_priority_high_full)
    }
}

@Composable
private fun getPriorityColor(priority: TodoPriority): Color {
    return when (priority) {
        TodoPriority.LOW -> MaterialTheme.colorScheme.outline
        TodoPriority.NORMAL -> MaterialTheme.colorScheme.onSurface
        TodoPriority.HIGH -> MaterialTheme.colorScheme.error
    }
} 