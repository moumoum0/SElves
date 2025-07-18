package com.selves.xnn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.model.Dynamic
import com.selves.xnn.model.DynamicType
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.EditDynamicDialog
import com.selves.xnn.ui.components.DynamicImageGrid
import com.selves.xnn.ui.components.DynamicImagePreviewDialog
import com.selves.xnn.viewmodel.DynamicViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicScreen(
    currentMember: com.selves.xnn.model.Member?,
    onBackClick: () -> Unit = {},
    onDynamicClick: (String) -> Unit = {},
    dynamicViewModel: DynamicViewModel = hiltViewModel()
) {
    val uiState by dynamicViewModel.uiState.collectAsState()
    val dynamics by dynamicViewModel.dynamics.collectAsState()
    val searchQuery by dynamicViewModel.searchQuery.collectAsState()
    val filterType by dynamicViewModel.filterType.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    
    // 设置当前用户
    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { userId ->
            dynamicViewModel.setCurrentUser(userId)
        }
    }
    
    // 如果显示编辑对话框，则显示全屏编辑界面
    if (showEditDialog) {
        EditDynamicDialog(
            onDismiss = { showEditDialog = false },
            onConfirm = { content, images ->
                currentMember?.let { member ->
                    dynamicViewModel.createDynamic(
                        title = "",
                        content = content,
                        authorName = member.name,
                        authorAvatar = member.avatarUrl,
                        type = if (images.isNotEmpty()) DynamicType.IMAGE else DynamicType.TEXT,
                        images = images,
                        tags = emptyList()
                    )
                }
                showEditDialog = false
            }
        )
    } else {
        // 正常的动态列表界面
        Scaffold(
            topBar = {
                DynamicTopBar(
                    showSearchBar = showSearchBar,
                    searchQuery = searchQuery,
                    filterType = filterType,
                    onBackClick = onBackClick,
                    onSearchClick = { showSearchBar = !showSearchBar },
                    onSearchChange = { dynamicViewModel.searchDynamics(it) },
                    onFilterChange = { dynamicViewModel.setFilterType(it) },
                    onSearchClose = { 
                        showSearchBar = false
                        dynamicViewModel.searchDynamics("")
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showEditDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "创建动态")
                }
            }
        ) { paddingValues ->
            // 动态列表
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dynamics) { dynamic ->
                        DynamicCard(
                            dynamic = dynamic,
                            currentUserId = currentMember?.id,
                            onLikeClick = { dynamicViewModel.toggleLike(dynamic.id) },
                            onCommentClick = { onDynamicClick(dynamic.id) },
                            onDeleteClick = { dynamicViewModel.deleteDynamic(dynamic.id) },
                            onImageClick = { imagePath -> previewImagePath = imagePath },
                            onCardClick = { onDynamicClick(dynamic.id) }
                        )
                    }
                    
                    if (dynamics.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无动态",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 错误提示
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 可以在这里显示 SnackBar 或其他错误提示
            dynamicViewModel.clearError()
        }
    }
    
    // 图片预览对话框
    previewImagePath?.let { imagePath ->
        DynamicImagePreviewDialog(
            imagePath = imagePath,
            onDismiss = { previewImagePath = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicTopBar(
    showSearchBar: Boolean,
    searchQuery: String,
    filterType: DynamicType?,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (DynamicType?) -> Unit,
    onSearchClose: () -> Unit
) {
    Column {
        // 主导航栏
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            TopAppBar(
                title = {
                    // 使用Row来控制标题和搜索框的切换
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 标题（搜索栏未展开时显示）
                        AnimatedVisibility(
                            visible = !showSearchBar,
                            enter = slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300)),
                            exit = slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        ) {
                            Text(
                                text = "动态",
                                fontWeight = FontWeight.Normal
                            )
                        }
                        
                        // 搜索栏展开动画（从右侧向左侧展开）
                        AnimatedVisibility(
                            visible = showSearchBar,
                            enter = slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300)),
                            exit = slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                placeholder = { Text("搜索动态...") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = "搜索")
                                },
                                trailingIcon = {
                                    Row {
                                        // 过滤按钮
                                        IconButton(
                                            onClick = { 
                                                onFilterChange(
                                                    when (filterType) {
                                                        null -> DynamicType.IMAGE
                                                        DynamicType.IMAGE -> DynamicType.TEXT
                                                        DynamicType.TEXT -> null
                                                        else -> null
                                                    }
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = when (filterType) {
                                                    DynamicType.IMAGE -> Icons.Default.Image
                                                    DynamicType.TEXT -> Icons.Default.TextFormat
                                                    else -> Icons.Default.FilterList
                                                },
                                                contentDescription = "过滤",
                                                tint = if (filterType != null) MaterialTheme.colorScheme.primary 
                                                       else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        // 关闭搜索按钮
                                        IconButton(onClick = onSearchClose) {
                                            Icon(Icons.Default.Close, contentDescription = "关闭搜索")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 只在搜索栏未展开时显示搜索按钮
                    AnimatedVisibility(
                        visible = !showSearchBar,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    }
}

@Composable
fun DynamicCard(
    dynamic: Dynamic,
    currentUserId: String?,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onImageClick: (String) -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
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
                        size = 40.dp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = dynamic.authorName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = dynamic.createdAt.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 动态标题
            if (dynamic.title.isNotEmpty()) {
                Text(
                    text = dynamic.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 动态内容
            Text(
                text = dynamic.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 图片（如果有）
            if (dynamic.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                DynamicImageGrid(
                    imagePaths = dynamic.images,
                    onImageClick = onImageClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 标签
            if (dynamic.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dynamic.tags.forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text("#$tag", fontSize = 12.sp) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 互动按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 点赞按钮
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick() }
                    ) {
                        Icon(
                            imageVector = if (dynamic.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "点赞",
                            tint = if (dynamic.isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dynamic.likeCount.toString(),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 评论按钮
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCommentClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "评论",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dynamic.commentCount.toString(),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
} 