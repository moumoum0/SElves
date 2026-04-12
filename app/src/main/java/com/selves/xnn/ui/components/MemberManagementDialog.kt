package com.selves.xnn.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.selves.xnn.R
import com.selves.xnn.model.Member

@Composable
fun MemberManagementDialog(
    members: List<Member>,
    currentMember: Member,
    onDismiss: () -> Unit,
    onCreateNewMember: () -> Unit,
    onDeleteMember: (Member) -> Unit,
    onEditMember: (Member) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf<Member?>(null) }
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = stringResource(R.string.dialog_member_management),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(
                        onClick = onCreateNewMember,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_add_member)
                        )
                    }
                }
                
                // 成员列表
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(members) { member ->
                        MemberItem(
                            member = member,
                            isCurrentMember = member.id == currentMember.id,
                            onDeleteMember = { showDeleteConfirmation = member },
                            onEditMember = { memberToEdit = member }
                        )
                    }
                }
                
                // 底部按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = {
                Text(stringResource(R.string.dialog_delete_member))
            },
            text = {
                Text(stringResource(R.string.dialog_delete_member_confirm, showDeleteConfirmation!!.name))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation?.let { onDeleteMember(it) }
                        showDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = null }
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
    
    // 编辑成员对话框
    memberToEdit?.let { member ->
        EditMemberDialog(
            member = member,
            existingMemberNames = members.map { it.name },
            onDismiss = { memberToEdit = null },
            onConfirm = { name, avatarUrl ->
                onEditMember(member.copy(name = name, avatarUrl = avatarUrl))
                memberToEdit = null
            }
        )
    }
}

@Composable
fun MemberItem(
    member: Member,
    isCurrentMember: Boolean,
    onDeleteMember: () -> Unit,
    onEditMember: (Member) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 可以添加点击事件 */ }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        AvatarImage(
            avatarUrl = member.avatarUrl,
            contentDescription = stringResource(R.string.cd_member_avatar),
            size =40.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (isCurrentMember) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = stringResource(R.string.member_current),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Text(
                text = "ID: ${member.id.take(8)}...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 操作菜单 - 所有成员都显示菜单，但当前成员只能编辑，不能删除
        Box {
            IconButton(
                onClick = { showMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.cd_more_actions),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // 编辑选项 - 所有成员都可以编辑
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.btn_edit)) },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    },
                    onClick = {
                        showMenu = false
                        onEditMember(member)
                    }
                )
                
                // 删除选项 - 只有非当前成员才能删除
                if (!isCurrentMember) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_delete)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeleteMember()
                        }
                    )
                }
            }
        }
    }
} 