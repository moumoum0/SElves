package com.selves.xnn.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.selves.xnn.ui.components.ImageViewer
import com.selves.xnn.ui.components.QuickMemberSwitch
import com.selves.xnn.data.MemberPreferences
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
    onUpdateGroupInfo: (String, String?) -> Unit = { _, _ -> },  // 修改为支持名称和头像
    onDeleteGroup: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onMemberSelected: (Member) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val memberPreferences = remember { MemberPreferences(context) }
    val quickMemberSwitchEnabled by memberPreferences.quickMemberSwitchEnabled.collectAsState(initial = false)
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var previewImagePosition by remember { mutableStateOf<Offset?>(null) }
    var previewImageSize by remember { mutableStateOf<DpSize?>(null) }
    var showGroupManagement by remember { mutableStateOf(false) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onSendImageMessage(it) }
    }
    
    // 当消息列表变化时，自动滚动到最新消息
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
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
            items(
                items = messages.reversed(),
                key = { it.id }
            ) { message ->
                MessageItem(
                    message = message,
                    isFromCurrentMember = message.senderId == currentMember.id,
                    sender = members.find { it.id == message.senderId },
                    onDeleteMessage = onDeleteMessage,
                    onImageClick = { imagePath, position, size ->
                        previewImagePath = imagePath
                        previewImagePosition = position
                        previewImageSize = size
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
                // 快捷成员切换（当启用时显示在最左边）
                if (quickMemberSwitchEnabled) {
                    QuickMemberSwitch(
                        currentMember = currentMember,
                        members = group.members, // 只显示当前群聊的成员
                        onMemberSelected = onMemberSelected,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                } else {
                    // 图片选择按钮（默认位置）
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "发送图片",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息") },
                    maxLines = 5
                )

                // 当启用快捷切换时，图片按钮移到这里
                if (quickMemberSwitchEnabled) {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "发送图片",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
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
        
        ImageViewer(
            imagePath = imagePath,
            onBack = { 
                previewImagePath = null
                previewImagePosition = null
                previewImageSize = null
            },
            senderName = sender?.name ?: "未知成员",
            timestamp = imageMessage?.timestamp ?: 0L,
            startPosition = previewImagePosition,
            startSize = previewImageSize
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
            onUpdateGroupInfo = onUpdateGroupInfo,
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
    onImageClick: (String, Offset, DpSize) -> Unit = { _, _, _ -> },
    currentMember: Member,
    members: List<Member>
) {
    var showMenu by remember { mutableStateOf(false) }
    var pressPosition by remember { mutableStateOf(DpOffset.Zero) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
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
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(message.content))
                                showMenu = false
                            },
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
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(message.content))
                            showMenu = false
                        },
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
    onImageClick: (String, Offset, DpSize) -> Unit = { _, _, _ -> }
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
    onImageClick: (String, Offset, DpSize) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    if (imagePath != null) {
        val imageRequest = remember(imagePath) {
            ImageUtils.createMessageImageRequest(context, imagePath)
        }
        
        if (imageRequest != null) {
            var imageSize by remember { mutableStateOf(DpSize.Zero) }
            var imagePosition by remember { mutableStateOf(Offset.Zero) }
            
            AsyncImage(
                model = imageRequest,
                contentDescription = "消息图片",
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .heightIn(max = 360.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .onSizeChanged { size ->
                        imageSize = with(density) {
                            DpSize(size.width.toDp(), size.height.toDp())
                        }
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        imagePosition = layoutCoordinates.positionInRoot()
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { 
                                // 传递图片的实际位置和大小，而不是点击位置
                                onImageClick(imagePath, imagePosition, imageSize)
                            }
                        )
                    },
                contentScale = ContentScale.Fit,
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
fun MessageMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    if (showMenu) {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = true)
        ) {
            DropdownMenuItem(
                text = { Text("复制") },
                onClick = onCopy,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制"
                    )
                }
            )
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
    return com.selves.xnn.util.TimeFormatter.formatTimestamp(timestamp)
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