package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.selves.xnn.model.ChatGroup
import com.selves.xnn.model.Member
import com.selves.xnn.model.MessageType
import com.selves.xnn.ui.components.CreateGroupDialog
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.UserInfoHeader
import com.selves.xnn.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    viewModel: MainViewModel,
    currentMember: Member,
    groups: List<ChatGroup>,
    members: List<Member>,
    onMemberSwitch: () -> Unit,
    onGroupClick: (ChatGroup) -> Unit,
    onCreateGroup: (String, String?, List<Member>) -> Unit  // 增加avatarUrl参数
) {
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 创建头像默认图标
    val defaultIcon = @Composable {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "成员头像默认图标",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(8.dp)
        )
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            availableMembers = members,
            currentMember = currentMember,
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { groupName, avatarUrl, selectedMembers ->
                showCreateGroupDialog = false
                onCreateGroup(groupName, avatarUrl, selectedMembers)
            }
        )
    }

    // 在列表层级统一收集状态，避免每个 GroupItem 独立 collectAsState
    val messages by viewModel.messages.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val unreadCounts by viewModel.unreadCounts.collectAsState()
    
    // 用 remember 缓存排序结果，仅在 groups 或 messages 变化时重新排序
    val sortedGroups = remember(groups, messages) {
        groups.sortedByDescending { group ->
            val groupMessages = messages[group.id] ?: emptyList()
            val latestMessage = groupMessages.lastOrNull()
            latestMessage?.timestamp ?: group.createdAt
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // 顶部成员信息栏
            UserInfoHeader(
                currentMember = currentMember,
                onMemberSwitch = onMemberSwitch
            )

            // 群聊列表
            if (sortedGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无群聊，请点击右下角创建")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(sortedGroups.size * 2 - 1) { index ->
                        if (index % 2 == 0) {
                            val groupIndex = index / 2
                            val group = sortedGroups[groupIndex]
                            val groupMessages = remember(messages, group.id) {
                                messages[group.id] ?: emptyList()
                            }
                            val latestMessage = remember(groupMessages) {
                                groupMessages.lastOrNull()
                            }
                            val sender = remember(latestMessage, allMembers) {
                                latestMessage?.let { msg -> allMembers.find { it.id == msg.senderId } }
                            }
                            val unreadCount = remember(unreadCounts, group.id) {
                                unreadCounts[group.id] ?: 0
                            }
                            GroupItem(
                                group = group,
                                currentMember = currentMember,
                                latestMessage = latestMessage,
                                sender = sender,
                                unreadCount = unreadCount,
                                formattedTime = remember(latestMessage, group.createdAt) {
                                    viewModel.formatMessageTime(
                                        latestMessage?.timestamp ?: group.createdAt
                                    )
                                },
                                onClick = { onGroupClick(group) }
                            )
                        } else {
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(start = 72.dp)
                                    .fillMaxWidth(),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        // 右下角的添加按钮
        FloatingActionButton(
            onClick = { showCreateGroupDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "创建群聊"
            )
        }
    }
}

@Composable
fun GroupItem(
    group: ChatGroup,
    currentMember: Member,
    latestMessage: com.selves.xnn.model.Message?,
    sender: Member?,
    unreadCount: Int,
    formattedTime: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧头像
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (group.avatarUrl != null) {
                        Color.Transparent
                    } else if (group.name.isNotEmpty()) {
                        // 根据群聊名称生成颜色
                        val hash = group.name.hashCode().let { if (it < 0) -it else it }
                        val hue = (hash % 360).toFloat()
                        androidx.compose.ui.graphics.Color.hsl(hue, 0.6f, 0.5f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (group.avatarUrl != null) {
                AvatarImage(
                    avatarUrl = group.avatarUrl,
                    contentDescription = "群聊头像",
                    size = 52.dp
                )
            } else {
                Text(
                    text = if (group.name.isNotEmpty()) group.name.first().toString().uppercase() else "G",
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 中间内容区域
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 群聊名称
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // 最新消息
            if (latestMessage != null) {
                val messageContent = when (latestMessage.type) {
                    MessageType.IMAGE -> "[图片]"
                    MessageType.TEXT -> latestMessage.content
                }
                
                val messageText = if (sender?.id == currentMember.id) {
                    "你: $messageContent"
                } else {
                    "${sender?.name ?: "未知用户"}: $messageContent"
                }
                
                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "暂无消息",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 右侧时间和未读数量
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 时间
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            // 未读消息数量
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .defaultMinSize(minWidth = 20.dp, minHeight = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        color = MaterialTheme.colorScheme.surface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}