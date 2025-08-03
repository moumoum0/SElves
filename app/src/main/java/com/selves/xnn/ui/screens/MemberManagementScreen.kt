package com.selves.xnn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.model.Member
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.CreateMemberDialog
import com.selves.xnn.ui.components.EditMemberDialog
import com.selves.xnn.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    members: List<Member>,
    currentMember: Member,
    onNavigateBack: () -> Unit,
    mainViewModel: MainViewModel // 移除默认的hiltViewModel()，强制使用传递的实例
) {
    var showDeleteConfirmation by remember { mutableStateOf<Member?>(null) }
    var showCreateMemberDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "成员管理",
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreateMemberDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加成员"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
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
    }
    
    // 删除确认对话框
    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = {
                Text("删除成员")
            },
            text = {
                Text("确定要删除成员「${showDeleteConfirmation!!.name}」吗？此操作不可撤销。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation?.let { mainViewModel.deleteMember(it) }
                        showDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = null }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 创建成员对话框
    if (showCreateMemberDialog) {
        CreateMemberDialog(
            existingMemberNames = members.map { it.name },
            onDismiss = { showCreateMemberDialog = false },
            onConfirm = { name, avatarUrl ->
                mainViewModel.createMember(name, avatarUrl, shouldSetAsCurrent = false)
                showCreateMemberDialog = false
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
                mainViewModel.updateMember(member.id, name, avatarUrl)
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
            contentDescription = "成员头像",
            size = 40.dp
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
                            text = "当前",
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
                    contentDescription = "更多操作",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // 编辑选项 - 所有成员都可以编辑
                DropdownMenuItem(
                    text = { Text("编辑") },
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
                        text = { Text("删除") },
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