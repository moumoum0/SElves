package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.R
import com.selves.xnn.model.Member
import com.selves.xnn.model.Vote
import com.selves.xnn.model.VoteOption
import com.selves.xnn.model.VoteRecord
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.QuickMemberSwitch
import com.selves.xnn.viewmodel.VoteViewModel
import com.selves.xnn.data.MemberPreferences
import androidx.compose.ui.platform.LocalContext
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteDetailScreen(
    voteId: String,
    currentMember: Member?,
    members: List<Member> = emptyList(),
    onBackClick: () -> Unit = {},
    onMemberSelected: (Member) -> Unit = {},
    voteViewModel: VoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val memberPreferences = remember { MemberPreferences(context) }
    val quickMemberSwitchEnabled by memberPreferences.quickMemberSwitchEnabled.collectAsState(initial = false)
    val uiState by voteViewModel.uiState.collectAsState()
    val vote by voteViewModel.getVoteWithOptions(voteId).collectAsState(initial = null)
    val voteRecords by voteViewModel.getVoteRecords(voteId).collectAsState(initial = emptyList())
    
    var selectedOptions by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showVoteRecords by remember { mutableStateOf(false) }
    
    // 设置当前用户
    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { userId ->
            voteViewModel.setCurrentUser(userId)
        }
    }
    
    // 初始化已选择的选项
    LaunchedEffect(vote) {
        vote?.let { v ->
            selectedOptions = v.options.filter { it.isSelected }.map { it.id }.toSet()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.vote_detail_title),
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    // 快捷成员切换组件
                    if (quickMemberSwitchEnabled && currentMember != null) {
                        QuickMemberSwitch(
                            currentMember = currentMember,
                            members = members,
                            onMemberSelected = onMemberSelected,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    
                    vote?.let { currentVote ->
                        if (currentVote.isActive && !currentVote.hasVoted) {
                            TextButton(
                                onClick = {
                                    if (selectedOptions.isNotEmpty() && currentMember != null) {
                                        voteViewModel.vote(
                                            voteId = voteId,
                                            optionIds = selectedOptions.toList(),
                                            userName = currentMember.name,
                                            userAvatar = currentMember.avatarUrl
                                        )
                                    }
                                },
                                enabled = selectedOptions.isNotEmpty() && !uiState.isVoting
                            ) {
                                if (uiState.isVoting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(R.string.vote_action))
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (vote == null) {
            // 投票不存在或加载中
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(R.string.vote_not_found))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 投票详情卡片
                item {
                    vote?.let { currentVote ->
                        VoteDetailCard(
                            vote = currentVote,
                            currentUserId = currentMember?.id,
                            onDeleteClick = { 
                                voteViewModel.deleteVote(currentVote.id)
                                onBackClick()
                            },
                            onEndClick = { voteViewModel.endVote(currentVote.id) }
                        )
                    }
                }
                
                // 投票选项
                item {
                    vote?.let { currentVote ->
                        VoteOptionsSection(
                            vote = currentVote,
                            selectedOptions = selectedOptions,
                            onOptionSelect = { optionId ->
                                if (currentVote.isActive && !currentVote.hasVoted) {
                                    selectedOptions = if (currentVote.allowMultipleChoice) {
                                        if (selectedOptions.contains(optionId)) {
                                            selectedOptions - optionId
                                        } else {
                                            selectedOptions + optionId
                                        }
                                    } else {
                                        setOf(optionId)
                                    }
                                }
                            }
                        )
                    }
                }
                
                // 投票记录
                vote?.let { currentVote ->
                    if (!currentVote.isAnonymous) {
                        item {
                            VoteRecordsSection(
                                voteRecords = voteRecords,
                                showRecords = showVoteRecords,
                                onToggleRecords = { showVoteRecords = !showVoteRecords }
                            )
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

@Composable
fun VoteDetailCard(
    vote: Vote,
    currentUserId: String?,
    onDeleteClick: () -> Unit,
    onEndClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
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
                        contentDescription = stringResource(R.string.cd_author_avatar),
                        size = 48.dp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = vote.authorName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
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
                                    contentDescription = stringResource(R.string.cd_end_vote),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 投票标题
            Text(
                text = vote.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 投票描述
            Text(
                text = vote.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                            text = if (vote.isActive) stringResource(R.string.vote_status_active_text) else stringResource(R.string.vote_status_ended_text),
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
                        text = stringResource(R.string.vote_detail_total_votes, vote.totalVotes),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 结束时间
                vote.endTime?.let { endTime ->
                    Text(
                        text = stringResource(R.string.vote_detail_deadline, com.selves.xnn.util.TimeFormatter.formatDetailDateTime(endTime)),
                        fontSize = 12.sp,
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
                        AssistChip(
                            onClick = { },
                            label = { Text(stringResource(R.string.vote_multiple_choice), fontSize = 12.sp) }
                        )
                    }
                    if (vote.isAnonymous) {
                        AssistChip(
                            onClick = { },
                            label = { Text(stringResource(R.string.vote_anonymous), fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoteOptionsSection(
    vote: Vote,
    selectedOptions: Set<String>,
    onOptionSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.vote_detail_options_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            vote.options.forEach { option ->
                VoteOptionItem(
                    option = option,
                    isSelected = selectedOptions.contains(option.id),
                    isActive = vote.isActive && !vote.hasVoted,
                    allowMultipleChoice = vote.allowMultipleChoice,
                    showResults = !vote.isActive || vote.hasVoted,
                    onSelect = { onOptionSelect(option.id) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun VoteOptionItem(
    option: VoteOption,
    isSelected: Boolean,
    isActive: Boolean,
    allowMultipleChoice: Boolean,
    showResults: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        showResults -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isActive) {
                    Modifier.clickable { onSelect() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 选项内容
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 选择指示器
                    if (isActive) {
                        if (allowMultipleChoice) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onSelect() }
                            )
                        } else {
                            RadioButton(
                                selected = isSelected,
                                onClick = onSelect
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = option.content,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // 投票结果
                if (showResults) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(R.string.vote_option_votes, option.voteCount),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${option.percentage.toInt()}%",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // 进度条
            if (showResults) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { option.percentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun VoteRecordsSection(
    voteRecords: List<VoteRecord>,
    showRecords: Boolean,
    onToggleRecords: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleRecords() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.vote_detail_records_title, voteRecords.size),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Icon(
                    imageVector = if (showRecords) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (showRecords) stringResource(R.string.cd_collapse) else stringResource(R.string.cd_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (showRecords) {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (voteRecords.isEmpty()) {
                    Text(
                        text = stringResource(R.string.vote_detail_no_records),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    voteRecords.forEach { record ->
                        VoteRecordItem(record = record)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VoteRecordItem(
    record: VoteRecord
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            avatarUrl = record.userAvatar,
            contentDescription = stringResource(R.string.cd_voter_avatar),
            size = 32.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = record.userName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = com.selves.xnn.util.TimeFormatter.formatDetailDateTime(record.votedAt),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 