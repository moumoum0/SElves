package com.selves.xnn.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonRemove
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
import com.canhub.cropper.CropImageContract
import com.selves.xnn.model.ChatGroup
import com.selves.xnn.model.Member
import com.selves.xnn.util.ImageUtils

@Composable
fun GroupManagementDialog(
    group: ChatGroup,
    currentMember: Member,
    allMembers: List<Member>,
    onDismiss: () -> Unit,
    onAddMembers: (List<Member>) -> Unit,
    onRemoveMembers: (List<Member>) -> Unit,
    onUpdateGroupInfo: (String, String?) -> Unit,  // 修改为支持名称和头像
    onDeleteGroup: () -> Unit
) {
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showRemoveMemberDialog by remember { mutableStateOf(false) }
    var showEditInfoDialog by remember { mutableStateOf(false) }  // 重命名变量
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    val isOwner = group.ownerId == currentMember.id

    if (showAddMemberDialog) {
        AddMemberDialog(
            group = group,
            allMembers = allMembers,
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { selectedMembers ->
                showAddMemberDialog = false
                onAddMembers(selectedMembers)
            }
        )
    }

    if (showRemoveMemberDialog) {
        RemoveMemberDialog(
            group = group,
            currentMember = currentMember,
            onDismiss = { showRemoveMemberDialog = false },
            onConfirm = { selectedMembers ->
                showRemoveMemberDialog = false
                onRemoveMembers(selectedMembers)
            }
        )
    }

    if (showEditInfoDialog) {
        EditGroupInfoDialog(  // 重命名组件
            group = group,
            onDismiss = { showEditInfoDialog = false },
            onConfirm = { newName, newAvatarUrl ->
                showEditInfoDialog = false
                onUpdateGroupInfo(newName, newAvatarUrl)
            }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteGroupConfirmDialog(
            groupName = group.name,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                showDeleteConfirmDialog = false
                onDeleteGroup()
            }
        )
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
                    text = "群聊管理",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 管理选项列表
                ManagementOption(
                    icon = Icons.Default.GroupAdd,
                    title = "添加成员",
                    subtitle = "邀请新成员加入群聊",
                    onClick = { showAddMemberDialog = true }
                )

                if (isOwner) {
                    ManagementOption(
                        icon = Icons.Default.PersonRemove,
                        title = "移除成员",
                        subtitle = "将成员移出群聊",
                        onClick = { showRemoveMemberDialog = true }
                    )
                }

                if (isOwner) {
                    ManagementOption(
                        icon = Icons.Default.Edit,
                        title = "修改群聊信息",
                        subtitle = "更改群聊的名称和头像",
                        onClick = { showEditInfoDialog = true }
                    )
                }

                if (isOwner) {
                    ManagementOption(
                        icon = Icons.Default.Delete,
                        title = "解散群聊",
                        subtitle = "永久删除此群聊",
                        onClick = { showDeleteConfirmDialog = true },
                        isDestructive = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}

@Composable
fun ManagementOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = if (isDestructive) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddMemberDialog(
    group: ChatGroup,
    allMembers: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (List<Member>) -> Unit
) {
    // 过滤出不在群聊中的成员
    val availableMembers = allMembers.filter { member ->
        !group.members.any { it.id == member.id }
    }
    
    var selectedMembers by remember { mutableStateOf(setOf<Member>()) }

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
                    text = "添加成员",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (availableMembers.isEmpty()) {
                    Text(
                        text = "没有可添加的成员",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 300.dp)
                    ) {
                        items(availableMembers) { member ->
                            val isSelected = selectedMembers.contains(member)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
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
                                    onCheckedChange = null
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                AvatarImage(
                                    avatarUrl = member.avatarUrl,
                                    contentDescription = "成员头像",
                                    size = 40.dp
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
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

@Composable
fun RemoveMemberDialog(
    group: ChatGroup,
    currentMember: Member,
    onDismiss: () -> Unit,
    onConfirm: (List<Member>) -> Unit
) {
    // 过滤出可以被移除的成员（不包括群主和当前成员）
    val removableMembers = group.members.filter { member ->
        member.id != group.ownerId && member.id != currentMember.id
    }
    
    var selectedMembers by remember { mutableStateOf(setOf<Member>()) }

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
                    text = "移除成员",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "选择要移除的成员",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (removableMembers.isEmpty()) {
                    Text(
                        text = "没有可移除的成员",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 300.dp)
                    ) {
                        items(removableMembers) { member ->
                            val isSelected = selectedMembers.contains(member)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
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
                                    onCheckedChange = null
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                AvatarImage(
                                    avatarUrl = member.avatarUrl,
                                    contentDescription = "成员头像",
                                    size = 40.dp
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
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
                        enabled = selectedMembers.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("移除")
                    }
                }
            }
        }
    }
}

@Composable
fun EditGroupInfoDialog(
    group: ChatGroup,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var newName by remember { mutableStateOf(group.name) }
    var newAvatarUrl by remember { mutableStateOf<String?>(group.avatarUrl) }
    var showError by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // 头像裁剪启动器
    val avatarCropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                val savedPath = ImageUtils.saveAvatarToInternalStorage(context, uri)
                newAvatarUrl = savedPath
            }
        }
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
                    text = "修改群聊信息",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 群聊头像修改区域
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (newAvatarUrl != null) {
                                    Color.Transparent
                                } else {
                                    // 根据群聊名称生成背景色
                                    val colors = listOf(
                                        Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800),
                                        Color(0xFF9C27B0), Color(0xFFF44336), Color(0xFF00BCD4)
                                    )
                                    colors[group.name.hashCode().rem(colors.size).let { if (it < 0) -it else it }]
                                }
                            )
                            .clickable {
                                val cropOptions = ImageUtils.createAvatarCropOptions()
                                avatarCropLauncher.launch(cropOptions)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (newAvatarUrl != null) {
                            AvatarImage(
                                avatarUrl = newAvatarUrl,
                                contentDescription = "群聊头像",
                                size = 80.dp
                            )
                        } else {
                            // 显示首字母或相机图标
                            if (group.avatarUrl == null) {
                                Text(
                                    text = if (group.name.isNotEmpty()) group.name.first().toString().uppercase() else "G",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "更换头像",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "点击更换群聊头像",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newName,
                    onValueChange = { 
                        newName = it
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
                            if (newName.isBlank()) {
                                showError = true
                            } else {
                                onConfirm(newName, newAvatarUrl)
                            }
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteGroupConfirmDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "解散群聊",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "确定要解散群聊「$groupName」吗？此操作无法撤销，所有聊天记录将被永久删除。",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("解散")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 