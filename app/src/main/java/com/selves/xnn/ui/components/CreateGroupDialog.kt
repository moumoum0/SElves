package com.selves.xnn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.selves.xnn.model.Member

@Composable
fun CreateGroupDialog(
    availableMembers: List<Member>,
    currentMember: Member,
    onDismiss: () -> Unit,
    onConfirm: (String, List<Member>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showMemberSelection by remember { mutableStateOf(false) }

    if (showMemberSelection) {
        MemberSelectionDialog(
            availableMembers = availableMembers,
            currentMember = currentMember,
            onDismiss = { showMemberSelection = false },
            onConfirm = { selectedMembers ->
                showMemberSelection = false
                if (groupName.isNotBlank()) {
                    onConfirm(groupName, selectedMembers)
                }
            }
        )
    } else {
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
                        text = "创建新群聊",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { 
                            groupName = it
                            showError = false
                        },
                        label = { Text("群聊名称") },
                        isError = showError,
                        supportingText = if (showError) {
                            { Text("群聊名称不能为空") }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

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
                                if (groupName.isBlank()) {
                                    showError = true
                                } else {
                                    showMemberSelection = true
                                }
                            }
                        ) {
                            Text("下一步")
                        }
                    }
                }
            }
        }
    }
} 