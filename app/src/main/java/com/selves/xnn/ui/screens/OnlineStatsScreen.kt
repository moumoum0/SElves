package com.selves.xnn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import com.selves.xnn.R
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
    val tabs = listOf(stringResource(R.string.online_stats_tabs_1), stringResource(R.string.online_stats_tabs_2), stringResource(R.string.online_stats_tabs_3))
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.online_stats_title),
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        AvatarImage(
            avatarUrl = memberStat.member.avatarUrl,
            contentDescription = stringResource(R.string.cd_member_avatar),
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
                text = stringResource(R.string.online_stats_today, formatOnlineTime(memberStat.todayOnlineMinutes, context)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = if (memberStat.isOnline) {
                    stringResource(R.string.online_status_online)
                } else {
                    stringResource(R.string.online_status_last_active, formatLastActiveTime(memberStat.lastActiveTime))
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (memberStat.isOnline) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        
        // 在线状态指示器
        Surface(
            shape = CircleShape,
            color = if (memberStat.isOnline) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
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



private fun formatOnlineTime(minutes: Int, context: Context): String {
    return when {
        minutes == 0 -> context.getString(R.string.online_stats_never_online)
        minutes < 60 -> context.getString(R.string.online_time_minutes, minutes)
        minutes < 1440 -> {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            if (remainingMinutes == 0) {
                context.getString(R.string.online_time_hours, hours)
            } else {
                context.getString(R.string.online_time_hours_minutes, hours, remainingMinutes)
            }
        }
        else -> {
            val days = minutes / 1440
            val remainingHours = (minutes % 1440) / 60
            if (remainingHours == 0) {
                context.getString(R.string.online_time_days, days)
            } else {
                context.getString(R.string.online_time_days_hours, days, remainingHours)
            }
        }
    }
}

private fun formatLastActiveTime(timestamp: Long): String {
    return com.selves.xnn.util.TimeFormatter.formatTimestamp(timestamp)
}

@Composable
fun LoginLogSummaryCard(summary: LoginLogSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.online_stats_login_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    title = stringResource(R.string.login_stats_total),
                    value = summary.totalLogins.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                SummaryItem(
                    title = stringResource(R.string.login_stats_today),
                    value = summary.todayLogins.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
                
                SummaryItem(
                    title = stringResource(R.string.login_stats_current_online),
                    value = summary.currentOnlineCount.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.online_stats_avg_today, formatDuration(summary.averageOnlineTime, LocalContext.current)),
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
            label = { Text(stringResource(R.string.filter_all)) },
            selected = selectedFilter == LoginLogFilter.ALL,
            leadingIcon = if (selectedFilter == LoginLogFilter.ALL) {
                { Icon(Icons.Default.FilterList, contentDescription = null) }
            } else null
        )
        
        FilterChip(
            onClick = { onFilterChanged(LoginLogFilter.TODAY) },
            label = { Text(stringResource(R.string.filter_today)) },
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
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
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
                    text = stringResource(R.string.online_stats_login, com.selves.xnn.util.TimeFormatter.formatDetailDateTime(loginLog.loginTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 登出时间或在线时长
                if (loginLog.isOnline) {
                    Text(
                        text = stringResource(R.string.online_stats_online),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    loginLog.logoutTime?.let { logoutTime ->
                        Text(
                            text = stringResource(R.string.online_stats_logout, com.selves.xnn.util.TimeFormatter.formatDetailDateTime(logoutTime), formatDuration(loginLog.duration, LocalContext.current)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long, context: Context): String {
    val minutes = durationMs / (60 * 1000)
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    
    return when {
        hours > 0 -> context.getString(R.string.online_time_hours_minutes, hours.toInt(), remainingMinutes.toInt())
        minutes > 0 -> context.getString(R.string.online_time_minutes, minutes.toInt())
        else -> context.getString(R.string.online_time_less_than_minute)
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
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.online_stats_no_logs),
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
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
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
                contentDescription = stringResource(R.string.cd_member_avatar),
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
                    text = stringResource(R.string.online_stats_today_duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 在线时长显示
            Text(
                text = formatOnlineTime(memberStat.todayOnlineMinutes, context),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}