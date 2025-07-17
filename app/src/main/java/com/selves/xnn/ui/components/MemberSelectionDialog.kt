package com.selves.xnn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.selves.xnn.model.Member

@Composable
fun MemberSelectionDialog(
    availableMembers: List<Member>,
    currentMember: Member,
    onDismiss: () -> Unit,
    onConfirm: (selectedMembers: List<Member>) -> Unit
) {
    var selectedMembers by remember { 
        mutableStateOf(setOf(currentMember)) // 默认选中当前成员（群主）
    }

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
                    text = "选择群成员",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "创建者将自动成为群主",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 成员列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 300.dp)
                ) {
                    items(availableMembers) { member ->
                        val isSelected = selectedMembers.contains(member)
                        val isCurrentMember = member.id == currentMember.id
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        if (isCurrentMember) {
                                            // 当前成员（群主）不能取消选择
                                            return@selectable
                                        }
                                        selectedMembers = if (isSelected) {
                                            selectedMembers - member
                                        } else {
                                            selectedMembers + member
                                        }
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null, // 通过Row的点击处理
                                enabled = !isCurrentMember
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            AvatarImage(
                                avatarUrl = member.avatarUrl,
                                contentDescription = "成员头像",
                                size = 40.dp
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (isCurrentMember) {
                                    Text(
                                        text = "群主",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            onConfirm(selectedMembers.toList())
                        },
                        enabled = selectedMembers.isNotEmpty()
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
} 