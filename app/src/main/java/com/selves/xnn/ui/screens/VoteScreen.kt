package com.selves.xnn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.selves.xnn.model.Member
import com.selves.xnn.model.Vote
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.CreateVoteDialog
import com.selves.xnn.viewmodel.VoteViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteScreen(
    currentMember: Member?,
    onBackClick: () -> Unit = {},
    onVoteClick: (String) -> Unit = {},
    voteViewModel: VoteViewModel = hiltViewModel()
) {
    val uiState by voteViewModel.uiState.collectAsState()
    val filteredVotes by voteViewModel.filteredVotes.collectAsState()
    val searchQuery by voteViewModel.searchQuery.collectAsState()
    val filterActive by voteViewModel.filterActive.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    // 设置当前用户
    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { userId ->
            voteViewModel.setCurrentUser(userId)
        }
    }
    
    // 如果显示创建对话框，则显示全屏创建界面
    if (showCreateDialog) {
        CreateVoteDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, description, options, endTime, allowMultipleChoice, isAnonymous ->
                currentMember?.let { member ->
                    voteViewModel.createVote(
                        title = title,
                        description = description,
                        authorName = member.name,
                        authorAvatar = member.avatarUrl,
                        options = options,
                        endTime = endTime,
                        allowMultipleChoice = allowMultipleChoice,
                        isAnonymous = isAnonymous
                    )
                }
                showCreateDialog = false
            }
        )
    } else {
        // 正常的投票列表界面
        Scaffold(
            topBar = {
                VoteTopBar(
                    showSearchBar = showSearchBar,
                    searchQuery = searchQuery,
                    filterActive = filterActive,
                    onBackClick = onBackClick,
                    onSearchClick = { showSearchBar = !showSearchBar },
                    onSearchChange = { voteViewModel.searchVotes(it) },
                    onFilterChange = { voteViewModel.setFilterActive(it) },
                    onSearchClose = { 
                        showSearchBar = false
                        voteViewModel.searchVotes("")
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "创建投票")
                }
            }
        ) { paddingValues ->
            // 投票列表
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
                    items(filteredVotes) { vote ->
                        VoteCard(
                            vote = vote,
                            currentUserId = currentMember?.id,
                            onVoteClick = { onVoteClick(vote.id) },
                            onDeleteClick = { voteViewModel.deleteVote(vote.id) },
                            onEndClick = { voteViewModel.endVote(vote.id) },
                            onCardClick = { onVoteClick(vote.id) }
                        )
                    }
                    
                    if (filteredVotes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (filterActive) "暂无进行中的投票" else "暂无已结束的投票",
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
            voteViewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteTopBar(
    showSearchBar: Boolean,
    searchQuery: String,
    filterActive: Boolean,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (Boolean) -> Unit,
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
                                text = "投票",
                                fontWeight = FontWeight.Normal
                            )
                        }
                        
                        // 搜索栏展开动画
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
                                placeholder = { Text("搜索投票...") },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { onSearchChange("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "清除")
                                        }
                                    } else {
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
        
        // 过滤标签栏
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onFilterChange(true) },
                    label = { Text("进行中") },
                    selected = filterActive,
                    leadingIcon = if (filterActive) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                
                FilterChip(
                    onClick = { onFilterChange(false) },
                    label = { Text("已结束") },
                    selected = !filterActive,
                    leadingIcon = if (!filterActive) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun VoteCard(
    vote: Vote,
    currentUserId: String?,
    onVoteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEndClick: () -> Unit,
    onCardClick: () -> Unit
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
                        avatarUrl = vote.authorAvatar,
                        contentDescription = "作者头像",
                        size = 40.dp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = vote.authorName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = vote.createdAt.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // 操作按钮（只有作者可以操作）
                if (currentUserId == vote.authorId) {
                    Row {
                        if (vote.isActive) {
                            IconButton(onClick = onEndClick) {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = "结束投票",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 投票标题
            Text(
                text = vote.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 投票描述
            Text(
                text = vote.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 投票选项预览（显示前2个选项）
            vote.options.take(2).forEach { option ->
                VoteOptionPreview(
                    option = option,
                    isActive = vote.isActive,
                    showPercentage = !vote.isActive || vote.hasVoted
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            if (vote.options.size > 2) {
                Text(
                    text = "还有 ${vote.options.size - 2} 个选项",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 投票状态和统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 投票状态
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (vote.isActive) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(
                            text = if (vote.isActive) "进行中" else "已结束",
                            fontSize = 12.sp,
                            color = if (vote.isActive) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 投票统计
                    Text(
                        text = "${vote.totalVotes} 票",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 结束时间
                vote.endTime?.let { endTime ->
                    Text(
                        text = "截止 ${endTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 投票设置标签
            if (vote.allowMultipleChoice || vote.isAnonymous) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (vote.allowMultipleChoice) {
                        AssistChip(
                            onClick = { },
                            label = { Text("多选", fontSize = 10.sp) }
                        )
                    }
                    if (vote.isAnonymous) {
                        AssistChip(
                            onClick = { },
                            label = { Text("匿名", fontSize = 10.sp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoteOptionPreview(
    option: com.selves.xnn.model.VoteOption,
    isActive: Boolean,
    showPercentage: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 选项内容
        Text(
            text = option.content,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // 投票数或百分比
        if (showPercentage) {
            Text(
                text = "${option.percentage.toInt()}% (${option.voteCount})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // 进度条（只在显示百分比时显示）
    if (showPercentage) {
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = option.percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
} 