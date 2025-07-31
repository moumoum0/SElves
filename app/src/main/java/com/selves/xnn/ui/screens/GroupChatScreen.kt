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
    onCreateGroup: (String, List<Member>) -> Unit
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
            onConfirm = { groupName, selectedMembers ->
                showCreateGroupDialog = false
                onCreateGroup(groupName, selectedMembers)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // 顶部成员信息栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onMemberSwitch() }
                ) {
                    AvatarImage(
                        avatarUrl = currentMember.avatarUrl,
                        contentDescription = "成员头像",
                        size = 40.dp
                    )
                    Text(
                        text = currentMember.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                IconButton(onClick = onMemberSwitch) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "切换成员",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 群聊列表
            if (groups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无群聊，请点击右下角创建")
                }
            } else {
                // 获取消息状态用于排序
                val messages by viewModel.messages.collectAsState()
                
                // 按最新消息时间排序群聊列表
                val sortedGroups = groups.sortedByDescending { group ->
                    val groupMessages = messages[group.id] ?: emptyList()
                    val latestMessage = groupMessages.lastOrNull()
                    latestMessage?.timestamp ?: group.createdAt
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(sortedGroups.size * 2 - 1) { index ->
                        if (index % 2 == 0) {
                            // 群聊项目
                            val groupIndex = index / 2
                            val group = sortedGroups[groupIndex]
                            GroupItem(
                                viewModel = viewModel,
                                group = group,
                                currentMember = currentMember,
                                onClick = { onGroupClick(group) }
                            )
                        } else {
                            // 分隔线
                            Divider(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupItem(
    viewModel: MainViewModel,
    group: ChatGroup,
    currentMember: Member,
    onClick: () -> Unit
) {
    // 获取消息状态
    val messages by viewModel.messages.collectAsState()
    val groupMessages = messages[group.id] ?: emptyList()
    
    // 获取最新消息和未读数量
    val latestMessage = groupMessages.lastOrNull()
    val unreadCount = viewModel.getUnreadCount(group.id)
    val allMembers by viewModel.allMembers.collectAsState()
    
    // 获取发送者信息
    val sender = latestMessage?.let { message ->
        allMembers.find { it.id == message.senderId }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧头像
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (group.name.isNotEmpty()) {
                        // 根据群聊名称生成颜色
                        val colors = listOf(
                            Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800),
                            Color(0xFF9C27B0), Color(0xFFF44336), Color(0xFF00BCD4)
                        )
                        colors[group.name.hashCode().rem(colors.size).let { if (it < 0) -it else it }]
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (group.name.isNotEmpty()) group.name.first().toString().uppercase() else "G",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
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
            if (latestMessage != null) {
                Text(
                    text = viewModel.formatMessageTime(latestMessage.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = viewModel.formatMessageTime(group.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
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
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}