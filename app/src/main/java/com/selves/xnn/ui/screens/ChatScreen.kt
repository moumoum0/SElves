package com.selves.xnn.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.selves.xnn.model.ChatGroup
import com.selves.xnn.model.Message
import com.selves.xnn.model.MessageType
import com.selves.xnn.model.Member
import com.selves.xnn.util.ImageUtils
import com.selves.xnn.ui.components.MessageAvatarImage
import com.selves.xnn.ui.components.GroupManagementDialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    currentMember: Member,
    group: ChatGroup,
    messages: List<Message>,
    members: List<Member>,
    onSendMessage: (String) -> Unit,
    onSendImageMessage: (Uri) -> Unit = {}, // 发送图片消息的回调
    onDeleteMessage: (String) -> Unit = {},
    onAddMembers: (List<Member>) -> Unit = {},
    onRemoveMembers: (List<Member>) -> Unit = {},
    onUpdateGroupName: (String) -> Unit = {},
    onDeleteGroup: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var showGroupManagement by remember { mutableStateOf(false) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onSendImageMessage(it) }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏
        ChatTopBar(
            group = group,
            onNavigateBack = onNavigateBack,
            onMenuClick = { showGroupManagement = true }
        )
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
                    onDeleteMessage = onDeleteMessage,
                    onImageClick = { imagePath ->
                        previewImagePath = imagePath
                    },
                    currentMember = currentMember,
                    members = members
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
                // 图片选择按钮
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "发送图片",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
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
    
    // 图片预览对话框
    previewImagePath?.let { imagePath ->
        // 找到发送这张图片的消息
        val imageMessage = messages.find { it.imagePath == imagePath }
        val sender = imageMessage?.let { message ->
            members.find { it.id == message.senderId }
        }
        
        ImagePreviewDialog(
            imagePath = imagePath,
            senderName = sender?.name ?: "未知成员",
            timestamp = imageMessage?.timestamp ?: 0L,
            onDismiss = { previewImagePath = null }
        )
    }
    
    // 群聊管理对话框
    if (showGroupManagement) {
        GroupManagementDialog(
            group = group,
            currentMember = currentMember,
            allMembers = members,
            onDismiss = { showGroupManagement = false },
            onAddMembers = onAddMembers,
            onRemoveMembers = onRemoveMembers,
            onUpdateGroupName = onUpdateGroupName,
            onDeleteGroup = onDeleteGroup
        )
    }
}

@Composable
fun MessageItem(
    message: Message,
    isFromCurrentMember: Boolean,
    sender: Member?,
    onDeleteMessage: (String) -> Unit,
    onImageClick: (String) -> Unit = {},
    currentMember: Member,
    members: List<Member>
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
                        MessageBubble(
                            message = message,
                            isFromCurrentMember = false,
                            onLongClick = { showMenu = true },
                            onImageClick = onImageClick
                        )
                        
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
                    MessageBubble(
                        message = message,
                        isFromCurrentMember = true,
                        onLongClick = { showMenu = true },
                        onImageClick = onImageClick
                    )
                    
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
fun MessageBubble(
    message: Message,
    isFromCurrentMember: Boolean,
    onLongClick: () -> Unit,
    onImageClick: (String) -> Unit = {}
) {
    val bubbleColor = if (isFromCurrentMember) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isFromCurrentMember) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val bubbleShape = if (isFromCurrentMember) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 0.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    }
    
    Surface(
        shape = bubbleShape,
        color = bubbleColor,
        modifier = Modifier.combinedClickable(
            onClick = {},
            onLongClick = onLongClick
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            when (message.type) {
                MessageType.TEXT -> {
                    // 文本消息
                    Text(
                        text = message.content,
                        color = contentColor
                    )
                }
                MessageType.IMAGE -> {
                    // 图片消息
                    ImageMessage(
                        imagePath = message.imagePath,
                        contentColor = contentColor,
                        onImageClick = onImageClick
                    )
                    
                    // 如果图片消息还有文本内容，也显示出来
                    if (message.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message.content,
                            color = contentColor
                        )
                    }
                }
            }
            
            // 时间戳
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ImageMessage(
    imagePath: String?,
    contentColor: Color,
    onImageClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    if (imagePath != null) {
        val imageRequest = remember(imagePath) {
            ImageUtils.createMessageImageRequest(context, imagePath)
        }
        
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "消息图片",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onImageClick(imagePath)
                            }
                        )
                    },
                contentScale = ContentScale.Crop,
                onError = {
                    // 图片加载失败时的处理
                }
            )
        } else {
            // 图片加载失败时显示错误信息
            Text(
                text = "图片加载失败",
                color = contentColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    } else {
        // 没有图片路径时显示错误信息
        Text(
            text = "图片不可用",
            color = contentColor.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ImagePreviewDialog(
    imagePath: String,
    senderName: String,
    timestamp: Long,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() }
                    )
                }
        ) {
            // 图片内容 - 全屏显示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val imageRequest = remember(imagePath) {
                    ImageUtils.createMessageImageRequest(context, imagePath)
                }
                
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "预览图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "图片加载失败",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // 顶部信息栏 - 带半透明背景
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.3f)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.offset(x = (-8).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 发送人信息
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = senderName,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatTimestamp(timestamp),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    group: ChatGroup,
    onNavigateBack: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${group.members.size} 人",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多选项"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
} 