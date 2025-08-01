package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.model.Dynamic
import com.selves.xnn.model.DynamicComment
import com.selves.xnn.model.Member
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.DynamicImageGrid
import com.selves.xnn.ui.components.ImageViewer
import com.selves.xnn.viewmodel.DynamicViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicDetailScreen(
    dynamicId: String,
    currentMember: Member?,
    onBackClick: () -> Unit = {},
    dynamicViewModel: DynamicViewModel = hiltViewModel()
) {
    val uiState by dynamicViewModel.uiState.collectAsState()
    val dynamics by dynamicViewModel.dynamics.collectAsState()
    val comments by dynamicViewModel.getComments(dynamicId).collectAsState(initial = emptyList())
    
    var commentText by remember { mutableStateOf("") }
    var replyToComment by remember { mutableStateOf<DynamicComment?>(null) }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var previewImagePosition by remember { mutableStateOf<Offset?>(null) }
    var previewImageSize by remember { mutableStateOf<DpSize?>(null) }
    
    // 查找当前动态
    val currentDynamic = dynamics.find { it.id == dynamicId }
    
    // 设置当前用户
    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { userId ->
            dynamicViewModel.setCurrentUser(userId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "动态详情",
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (currentDynamic == null) {
            // 动态不存在或加载中
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("动态不存在或已被删除")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 动态内容区域
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 动态详情卡片
                    item {
                        DynamicDetailCard(
                            dynamic = currentDynamic,
                            currentUserId = currentMember?.id,
                            onLikeClick = { dynamicViewModel.toggleLike(currentDynamic.id) },
                            onImageClick = { imagePath, position, size ->
                                previewImagePath = imagePath
                                previewImagePosition = position
                                previewImageSize = size
                            },
                            onDeleteClick = { 
                                dynamicViewModel.deleteDynamic(currentDynamic.id)
                                onBackClick()
                            }
                        )
                    }
                    
                    // 评论分隔线
                    item {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                    
                    // 评论标题
                    item {
                        Text(
                            text = "评论 (${comments.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // 评论列表
                    items(comments) { comment ->
                        CommentItem(
                            comment = comment,
                            currentUserId = currentMember?.id,
                            onReplyClick = { replyToComment = comment },
                            onDeleteClick = { 
                                dynamicViewModel.deleteComment(comment.id)
                            }
                        )
                    }
                    
                    // 如果没有评论
                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无评论，快来抢沙发吧！",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                // 评论输入区域
                CommentInputSection(
                    commentText = commentText,
                    onCommentTextChange = { commentText = it },
                    replyToComment = replyToComment,
                    onCancelReply = { replyToComment = null },
                    onSendComment = {
                        if (commentText.isNotBlank() && currentMember != null) {
                            dynamicViewModel.addComment(
                                dynamicId = dynamicId,
                                content = commentText,
                                authorName = currentMember.name,
                                authorAvatar = currentMember.avatarUrl,
                                parentCommentId = replyToComment?.id
                            )
                            commentText = ""
                            replyToComment = null
                        }
                    }
                )
            }
        }
    }
    
    // 图片预览对话框
    previewImagePath?.let { imagePath ->
        ImageViewer(
            imagePath = imagePath,
            onBack = { 
                previewImagePath = null
                previewImagePosition = null
                previewImageSize = null
            },
            startPosition = previewImagePosition,
            startSize = previewImageSize
        )
    }
    
    // 错误提示
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 可以在这里显示 SnackBar 或其他错误提示
            dynamicViewModel.clearError()
        }
    }
}

@Composable
fun DynamicDetailCard(
    dynamic: Dynamic,
    currentUserId: String?,
    onLikeClick: () -> Unit,
    onImageClick: (String, Offset, DpSize) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 作者信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarImage(
                        avatarUrl = dynamic.authorAvatar,
                        contentDescription = "作者头像",
                        size = 48.dp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = dynamic.authorName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = com.selves.xnn.util.TimeFormatter.formatDetailDateTime(dynamic.createdAt),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // 删除按钮（只有作者可以删除）
                if (currentUserId == dynamic.authorId) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 动态标题
            if (dynamic.title.isNotEmpty()) {
                Text(
                    text = dynamic.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 动态内容
            Text(
                text = dynamic.content,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
            
            // 图片（如果有）
            if (dynamic.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                DynamicImageGrid(
                    imagePaths = dynamic.images,
                    onImageClick = onImageClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 标签
            if (dynamic.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dynamic.tags.forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text("#$tag", fontSize = 12.sp) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 点赞按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClick() }
            ) {
                Icon(
                    imageVector = if (dynamic.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "点赞",
                    tint = if (dynamic.isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${dynamic.likeCount} 个赞",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: DynamicComment,
    currentUserId: String?,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 评论者信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarImage(
                        avatarUrl = comment.authorAvatar,
                        contentDescription = "评论者头像",
                        size = 32.dp
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = comment.authorName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            text = com.selves.xnn.util.TimeFormatter.formatDetailDateTime(comment.createdAt),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // 操作按钮
                Row {
                    TextButton(
                        onClick = onReplyClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "回复",
                            fontSize = 12.sp
                        )
                    }
                    
                    // 删除按钮（只有评论者可以删除）
                    if (currentUserId == comment.authorId) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除评论",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 评论内容
            Text(
                text = comment.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun CommentInputSection(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    replyToComment: DynamicComment?,
    onCancelReply: () -> Unit,
    onSendComment: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 回复提示
            if (replyToComment != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "回复 ${replyToComment.authorName}:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = replyToComment.content,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    IconButton(
                        onClick = onCancelReply,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "取消回复",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 评论输入框
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChange,
                    placeholder = { 
                        Text(
                            if (replyToComment != null) "回复评论..." else "写评论..."
                        ) 
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 发送按钮
                IconButton(
                    onClick = onSendComment,
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "发送",
                        tint = if (commentText.isNotBlank()) MaterialTheme.colorScheme.onPrimary 
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
} 