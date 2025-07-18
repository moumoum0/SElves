package com.selves.xnn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selves.xnn.model.Member
import com.selves.xnn.model.LoginLog
import com.selves.xnn.model.LoginLogSummary
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.viewmodel.LoginLogFilter
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineStatsScreen(
    members: List<Member>,
    currentMember: Member,
    onlineStats: OnlineStats,
    isLoading: Boolean,
    loginLogs: List<LoginLog>,
    loginLogSummary: LoginLogSummary?,
    isLoadingLogs: Boolean,
    onNavigateBack: () -> Unit,
    onLoadLoginLogs: (LoginLogFilter) -> Unit = {},
    onLoadLoginLogSummary: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("在线状况", "在线时间统计", "登录日志")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "在线统计",
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 选项卡
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // 内容区域
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIndex) {
                    0 -> OnlineStatusTab(
                        members = members,
                        currentMember = currentMember,
                        onlineStats = onlineStats
                    )
                    1 -> OnlineTimeStatsTab(
                        members = members,
                        currentMember = currentMember,
                        onlineStats = onlineStats
                    )
                    2 -> LoginLogTab(
                        loginLogs = loginLogs,
                        loginLogSummary = loginLogSummary,
                        isLoadingLogs = isLoadingLogs,
                        onLoadLoginLogs = onLoadLoginLogs,
                        onLoadLoginLogSummary = onLoadLoginLogSummary
                    )
                }
            }
        }
    }
}



@Composable
fun OnlineStatItem(
    memberStat: MemberOnlineStat,
    isCurrentMember: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        AvatarImage(
            avatarUrl = memberStat.member.avatarUrl,
            contentDescription = "成员头像",
            size = 40.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = memberStat.member.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (isCurrentMember) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(6.dp)
                    ) {}
                }
            }
            
            Text(
                text = "今日在线: ${formatOnlineTime(memberStat.todayOnlineMinutes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = if (memberStat.isOnline) {
                    "在线中"
                } else {
                    "最后活跃: ${formatLastActiveTime(memberStat.lastActiveTime)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (memberStat.isOnline) {
                    Color.Green
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        
        // 在线状态指示器
        Surface(
            shape = CircleShape,
            color = if (memberStat.isOnline) Color.Green else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(8.dp)
        ) {}
    }
}

// 数据类定义
data class OnlineStats(
    val onlineCount: Int,
    val memberStats: List<MemberOnlineStat>
)

data class MemberOnlineStat(
    val member: Member,
    val isOnline: Boolean,
    val todayOnlineMinutes: Int,
    val lastActiveTime: Long
)



private fun formatOnlineTime(minutes: Int): String {
    return when {
        minutes == 0 -> "未在线"
        minutes < 60 -> "${minutes}分钟"
        minutes < 1440 -> {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            if (remainingMinutes == 0) {
                "${hours}小时"
            } else {
                "${hours}小时${remainingMinutes}分钟"
            }
        }
        else -> {
            val days = minutes / 1440
            val remainingHours = (minutes % 1440) / 60
            if (remainingHours == 0) {
                "${days}天"
            } else {
                "${days}天${remainingHours}小时"
            }
        }
    }
}

private fun formatLastActiveTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        else -> {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(calendar.time)
        }
    }
}

@Composable
fun LoginLogSummaryCard(summary: LoginLogSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "登录统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    title = "总登录次数",
                    value = summary.totalLogins.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                SummaryItem(
                    title = "今日登录",
                    value = summary.todayLogins.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
                
                SummaryItem(
                    title = "当前在线",
                    value = summary.currentOnlineCount.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "今日平均在线时长: ${formatDuration(summary.averageOnlineTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SummaryItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedFilter: LoginLogFilter,
    onFilterChanged: (LoginLogFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            onClick = { onFilterChanged(LoginLogFilter.ALL) },
            label = { Text("全部") },
            selected = selectedFilter == LoginLogFilter.ALL,
            leadingIcon = if (selectedFilter == LoginLogFilter.ALL) {
                { Icon(Icons.Default.FilterList, contentDescription = null) }
            } else null
        )
        
        FilterChip(
            onClick = { onFilterChanged(LoginLogFilter.TODAY) },
            label = { Text("今日") },
            selected = selectedFilter == LoginLogFilter.TODAY,
            leadingIcon = if (selectedFilter == LoginLogFilter.TODAY) {
                { Icon(Icons.Default.FilterList, contentDescription = null) }
            } else null
        )
    }
}

@Composable
fun LoginLogItem(loginLog: LoginLog) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像
            AvatarImage(
                avatarUrl = loginLog.memberAvatar,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 用户信息和时间
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = loginLog.memberName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // 在线状态指示器
                    if (loginLog.isOnline) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 登录时间
                Text(
                    text = "登录: ${loginLog.loginTime.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 登出时间或在线时长
                if (loginLog.isOnline) {
                    Text(
                        text = "在线中",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    loginLog.logoutTime?.let { logoutTime ->
                        Text(
                            text = "登出: ${logoutTime.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))} • 时长: ${formatDuration(loginLog.duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = durationMs / (60 * 1000)
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    
    return when {
        hours > 0 -> "${hours}小时${remainingMinutes}分钟"
        minutes > 0 -> "${minutes}分钟"
        else -> "不到1分钟"
    }
}

@Composable
fun OnlineStatusTab(
    members: List<Member>,
    currentMember: Member,
    onlineStats: OnlineStats
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 成员在线统计列表
        items(onlineStats.memberStats) { memberStat ->
            OnlineStatItem(
                memberStat = memberStat,
                isCurrentMember = memberStat.member.id == currentMember.id
            )
        }
    }
}

@Composable
fun OnlineTimeStatsTab(
    members: List<Member>,
    currentMember: Member,
    onlineStats: OnlineStats
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 按在线时间排序的成员列表
        items(onlineStats.memberStats.sortedByDescending { it.todayOnlineMinutes }) { memberStat ->
            OnlineTimeStatItem(
                memberStat = memberStat,
                isCurrentMember = memberStat.member.id == currentMember.id
            )
        }
    }
}

@Composable
fun LoginLogTab(
    loginLogs: List<LoginLog>,
    loginLogSummary: LoginLogSummary?,
    isLoadingLogs: Boolean,
    onLoadLoginLogs: (LoginLogFilter) -> Unit,
    onLoadLoginLogSummary: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(LoginLogFilter.ALL) }
    
    LaunchedEffect(Unit) {
        onLoadLoginLogSummary()
        onLoadLoginLogs(selectedFilter)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 统计摘要卡片
        item {
            loginLogSummary?.let { summary ->
                LoginLogSummaryCard(summary = summary)
            }
        }
        
        // 筛选器
        item {
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterChanged = { filter ->
                    selectedFilter = filter
                    onLoadLoginLogs(filter)
                }
            )
        }
        
        // 登录日志列表
        if (isLoadingLogs) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(loginLogs) { loginLog ->
                LoginLogItem(loginLog = loginLog)
            }
            
            if (loginLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无登录日志",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnlineTimeStatItem(
    memberStat: MemberOnlineStat,
    isCurrentMember: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            AvatarImage(
                avatarUrl = memberStat.member.avatarUrl,
                contentDescription = "成员头像",
                size = 40.dp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = memberStat.member.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (isCurrentMember) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(6.dp)
                        ) {}
                    }
                }
                
                Text(
                    text = "今日在线时长",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 在线时长显示
            Text(
                text = formatOnlineTime(memberStat.todayOnlineMinutes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 