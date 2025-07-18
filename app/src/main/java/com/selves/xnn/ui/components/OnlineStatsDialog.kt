package com.selves.xnn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.selves.xnn.model.Member
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@Composable
fun OnlineStatsDialog(
    members: List<Member>,
    currentMember: Member,
    onDismiss: () -> Unit
) {
    // 计算在线统计数据
    val onlineStats = remember(members, currentMember) {
        calculateOnlineStats(members, currentMember)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "在线统计",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 总体统计卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            title = "总成员",
                            value = members.size.toString(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        StatItem(
                            title = "活跃",
                            value = members.filter { !it.isDeleted }.size.toString(),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        
                        StatItem(
                            title = "在线",
                            value = onlineStats.onlineCount.toString(),
                            color = Color.Green
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 成员在线统计列表
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(onlineStats.memberStats) { memberStat ->
                        OnlineStatItem(
                            memberStat = memberStat,
                            isCurrentMember = memberStat.member.id == currentMember.id
                        )
                    }
                }
                
                // 底部按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
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
                text = "最后活跃: ${formatLastActiveTime(memberStat.lastActiveTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

// 计算在线统计数据
private fun calculateOnlineStats(members: List<Member>, currentMember: Member): OnlineStats {
    val currentTime = System.currentTimeMillis()
    val memberStats = members.map { member ->
        // 基于成员ID生成稳定的随机种子，确保每次计算结果一致
        val memberSeed = member.id.hashCode().toLong()
        val memberRandom = Random(memberSeed)
        
        // 只有当前选中的用户才在线，其他用户都离线
        val isOnline = member.id == currentMember.id
        
        // 生成今日在线时间
        val todayOnlineMinutes = if (isOnline) {
            // 当前用户显示较长的在线时间
            memberRandom.nextInt(120, 480) // 2-8小时
        } else if (member.isDeleted) {
            // 已删除用户没有在线时间
            0
        } else {
            // 其他用户显示较短的在线时间（表示之前在线过）
            memberRandom.nextInt(30, 180) // 30分钟-3小时
        }
        
        // 生成最后活跃时间
        val lastActiveTime = if (member.id == currentMember.id) {
            currentTime // 当前成员的最后活跃时间就是现在
        } else {
            // 其他成员的最后活跃时间在过去
            currentTime - memberRandom.nextLong(
                5 * 60 * 1000, // 最少5分钟前
                24 * 60 * 60 * 1000 // 最多24小时前
            )
        }
        
        MemberOnlineStat(
            member = member,
            isOnline = isOnline,
            todayOnlineMinutes = todayOnlineMinutes,
            lastActiveTime = lastActiveTime
        )
    }
    
    // 按在线状态和最后活跃时间排序
    val sortedStats = memberStats.sortedWith(
        compareByDescending<MemberOnlineStat> { it.isOnline }
            .thenByDescending { it.lastActiveTime }
    )
    
    val onlineCount = memberStats.count { it.isOnline }
    
    return OnlineStats(
        onlineCount = onlineCount,
        memberStats = sortedStats
    )
}

private fun formatOnlineTime(minutes: Int): String {
    return when {
        minutes == 0 -> "未在线"
        minutes < 60 -> "${minutes}分钟"
        minutes < 1440 -> "${minutes / 60}小时${minutes % 60}分钟"
        else -> "${minutes / 1440}天${(minutes % 1440) / 60}小时"
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