package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.myapplication.model.ChatGroup
import com.example.myapplication.model.Message
import com.example.myapplication.model.Member
import com.example.myapplication.util.ImageUtils
import com.example.myapplication.ui.components.MessageAvatarImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    currentMember: Member,
    group: ChatGroup,
    messages: List<Message>,
    members: List<Member>,
    onSendMessage: (String) -> Unit,
    onDeleteMessage: (String) -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 消息列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = listState,
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageItem(
                    message = message,
                    isFromCurrentMember = message.senderId == currentMember.id,
                    sender = members.find { it.id == message.senderId },
                    onDeleteMessage = onDeleteMessage
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 输入框和发送按钮
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息") },
                    maxLines = 5
                )
                
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    isFromCurrentMember: Boolean,
    sender: Member?,
    onDeleteMessage: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var pressPosition by remember { mutableStateOf(DpOffset.Zero) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromCurrentMember) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = if (isFromCurrentMember) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isFromCurrentMember) {
                // 头像
                MessageAvatarImage(
                    avatarUrl = sender?.avatarUrl,
                    contentDescription = "发送者头像",
                    size = 40.dp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 成员名和消息内容
                Column {
                    // 成员名
                    Text(
                        text = sender?.name ?: "未知成员",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 消息气泡
                    Box {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            ),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { 
                                    showMenu = true
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                // 消息文本
                                Text(
                                    text = message.content,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                // 时间戳
                                Text(
                                    text = formatTimestamp(message.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        
                        MessageMenu(
                            showMenu = showMenu,
                            onDismiss = { showMenu = false },
                            onDelete = {
                                onDeleteMessage(message.id)
                                showMenu = false
                            }
                        )
                    }
                }
            } else {
                // 消息气泡
                Box {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 0.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { 
                                showMenu = true
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            // 消息文本
                            Text(
                                text = message.content,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            
                            // 时间戳
                            Text(
                                text = formatTimestamp(message.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    
                    MessageMenu(
                        showMenu = showMenu,
                        onDismiss = { showMenu = false },
                        onDelete = {
                            onDeleteMessage(message.id)
                            showMenu = false
                        }
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 当前成员头像
                MessageAvatarImage(
                    avatarUrl = sender?.avatarUrl,
                    contentDescription = "当前成员头像",
                    size = 40.dp
                )
            }
        }
    }
}

@Composable
fun MessageMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    if (showMenu) {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = true)
        ) {
            DropdownMenuItem(
                text = { Text("删除") },
                onClick = onDelete,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除"
                    )
                }
            )
        }
    }
}

@Composable
fun Modifier.combinedClickable(
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
): Modifier = composed {
    var isLongPress by remember { mutableStateOf(false) }
    
    pointerInput(Unit) {
        detectTapGestures(
            onLongPress = {
                isLongPress = true
                onLongClick()
            },
            onTap = {
                if (!isLongPress) {
                    onClick()
                }
                isLongPress = false
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val date = Date(timestamp)
    val sdf = when {
        // 今天的消息只显示时间
        isToday(timestamp) -> SimpleDateFormat("HH:mm", Locale.getDefault())
        // 昨天的消息显示"昨天"和时间
        isYesterday(timestamp) -> {
            return "昨天 " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        // 一周内的消息显示星期几和时间
        isWithinWeek(timestamp) -> {
            val weekDay = when(SimpleDateFormat("EEEE", Locale.CHINESE).format(date)) {
                "星期一" -> "周一"
                "星期二" -> "周二"
                "星期三" -> "周三"
                "星期四" -> "周四"
                "星期五" -> "周五"
                "星期六" -> "周六"
                "星期日" -> "周日"
                else -> ""
            }
            return "$weekDay " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        // 今年的消息显示月日和时间
        isThisYear(timestamp) -> SimpleDateFormat("M月d日 HH:mm", Locale.getDefault())
        // 其他显示完整日期
        else -> SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.getDefault())
    }
    return sdf.format(date)
}

private fun isToday(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    val today = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    return timestamp >= today
}

private fun isYesterday(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val yesterday = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    return timestamp >= yesterday && timestamp < yesterday + 24 * 60 * 60 * 1000
}

private fun isWithinWeek(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val weekAgo = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    return timestamp >= weekAgo
}

private fun isThisYear(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    val thisYear = calendar.get(Calendar.YEAR)
    calendar.timeInMillis = timestamp
    return calendar.get(Calendar.YEAR) == thisYear
} 