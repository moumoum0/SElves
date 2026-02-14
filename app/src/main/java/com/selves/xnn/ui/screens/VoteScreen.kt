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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.selves.xnn.viewmodel.VoteViewModel
import com.selves.xnn.data.MemberPreferences
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteScreen(
    currentMember: Member?,
    members: List<Member> = emptyList(),
    onBackClick: () -> Unit = {},
    onVoteClick: (String) -> Unit = {},
    onMemberSelected: (Member) -> Unit = {},
    onNavigateToCreateVote: () -> Unit = {},
    voteViewModel: VoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val memberPreferences = remember { MemberPreferences(context) }
    val quickMemberSwitchEnabled by memberPreferences.quickMemberSwitchEnabled.collectAsState(initial = false)
    val uiState by voteViewModel.uiState.collectAsState()
    val filteredVotes by voteViewModel.filteredVotes.collectAsState()
    val searchQuery by voteViewModel.searchQuery.collectAsState()
    val filterActive by voteViewModel.filterActive.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    
    // 设置当前用户
    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { userId ->
            voteViewModel.setCurrentUser(userId)
        }
    }
    
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
                onClick = onNavigateToCreateVote
            ) {
                Icon(Icons.Default.Add, contentDescription = "创建投票")
            }
        }
    ) { paddingValues ->
            // 投票列表（根据设置显示快捷切换成员）
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Poll,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (filterActive) "暂无进行中的投票" else "暂无已结束的投票",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "点击右下角按钮创建第一个投票",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 14.sp
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
            color = MaterialTheme.colorScheme.surface
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        
        // 过滤标签栏
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 顶部栏：作者信息 + 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 作者头像
                    AvatarImage(
                        avatarUrl = vote.authorAvatar,
                        contentDescription = "作者头像",
                        size = 44.dp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = vote.authorName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = com.selves.xnn.util.TimeFormatter.formatDetailDateTime(vote.createdAt),
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
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 投票标题
            Text(
                text = vote.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 投票描述
            if (vote.description.isNotEmpty()) {
                Text(
                    text = vote.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 投票选项预览
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vote.options.take(4).forEachIndexed { index, option ->
                    VoteOptionPreview(
                        option = option,
                        index = index,
                        isActive = vote.isActive,
                        showPercentage = !vote.isActive || vote.hasVoted,
                        totalVotes = vote.totalVotes
                    )
                }
                
                if (vote.options.size > 4) {
                    Text(
                        text = "还有 ${vote.options.size - 4} 个选项",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 底部栏：状态 + 统计 + 时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 投票状态
                VoteStatusChip(
                    isActive = vote.isActive,
                    remainingTime = vote.endTime?.let { getRemainingTime(it) }
                )
                
                // 投票统计
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${vote.totalVotes} 人参与",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 投票设置标签
            if (vote.allowMultipleChoice || vote.isAnonymous) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (vote.allowMultipleChoice) {
                        VoteTag(
                            icon = Icons.Default.List,
                            text = "多选"
                        )
                    }
                    if (vote.isAnonymous) {
                        VoteTag(
                            icon = Icons.Default.VisibilityOff,
                            text = "匿名"
                        )
                    }
                }
            }
            
            // 参与状态提示
            if (vote.hasVoted && vote.isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "您已参与投票",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoteStatusChip(
    isActive: Boolean,
    remainingTime: String?
) {
    Surface(
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示灯
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isActive) "进行中" else "已结束",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isActive && remainingTime != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "· $remainingTime",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun VoteOptionPreview(
    option: com.selves.xnn.model.VoteOption,
    index: Int,
    isActive: Boolean,
    showPercentage: Boolean,
    totalVotes: Int
) {
    val percentage = if (totalVotes > 0) option.percentage else 0f
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选项序号
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // 选项内容
            Text(
                text = option.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // 投票数和百分比
            if (showPercentage) {
                Text(
                    text = "${option.voteCount} 票",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${percentage.toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 进度条
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 34.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 进度条
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // 百分比条（右侧）
            if (showPercentage && percentage > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width((40 * percentage / 100).dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun VoteTag(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getRemainingTime(endTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(now, endTime)
    
    return when {
        duration.toDays() > 0 -> "${duration.toDays()}天"
        duration.toHours() > 0 -> "${duration.toHours()}小时"
        duration.toMinutes() > 0 -> "${duration.toMinutes()}分钟"
        else -> "即将结束"
    }
}
