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
import androidx.compose.ui.window.Dialog
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
                    text = "新建待办事项",
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
                    label = { Text("标题") },
                    isError = showTitleError,
                    supportingText = if (showTitleError) {
                        { Text("标题不能为空") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // 描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
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
                        label = { Text("优先级") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "选择优先级"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = getPriorityColor(priority),
                            unfocusedTextColor = getPriorityColor(priority)
                        ),
                        modifier = Modifier
                            .menuAnchor()
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
                        Text("取消")
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
                        Text("创建")
                    }
                }
            }
        }
    }
}

@Composable
private fun getPriorityText(priority: TodoPriority): String {
    return when (priority) {
        TodoPriority.LOW -> "低优先级"
        TodoPriority.NORMAL -> "普通优先级"
        TodoPriority.HIGH -> "高优先级"
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