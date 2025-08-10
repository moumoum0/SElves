package com.selves.xnn.util

import com.selves.xnn.data.entity.OnlineStatusEntity
import com.selves.xnn.model.Member
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.exp
import kotlin.math.ln

/**
 * 成员排序工具类 - 基于时间衰减加权频次排序
 * 
 * 核心算法：
 * 1. 时间衰减：越近的登录权重越高，使用指数衰减 exp(-λ * days)
 * 2. 时段权重：不同时段的登录价值不同（工作时间 > 普通时间 > 深夜时间）
 * 3. 最终分数：Score = Σ[exp(-λ * days_i) * w_timeSlot_i]
 */
object MemberSortingUtils {
    
    // 时间衰减参数
    private const val HALF_LIFE_DAYS = 14.0 // 半衰期：14天
    private val LAMBDA = ln(2.0) / HALF_LIFE_DAYS // 衰减系数 λ ≈ 0.0495
    
    // 时段权重配置（24小时制）
    private val TIME_SLOT_WEIGHTS = mapOf(
        6 to 1.0,   // 06:00-06:59 早晨
        7 to 1.2,   // 07:00-07:59 上班前
        8 to 1.3,   // 08:00-08:59 上班时间
        9 to 1.5,   // 09:00-09:59 工作高峰
        10 to 1.5,  // 10:00-10:59 工作高峰
        11 to 1.4,  // 11:00-11:59 工作时间
        12 to 1.2,  // 12:00-12:59 午休
        13 to 1.3,  // 13:00-13:59 午后
        14 to 1.5,  // 14:00-14:59 工作高峰
        15 to 1.5,  // 15:00-15:59 工作高峰
        16 to 1.4,  // 16:00-16:59 工作时间
        17 to 1.3,  // 17:00-17:59 下班前
        18 to 1.2,  // 18:00-18:59 下班后
        19 to 1.1,  // 19:00-19:59 晚饭时间
        20 to 1.2,  // 20:00-20:59 晚间
        21 to 1.3,  // 21:00-21:59 晚间活跃
        22 to 1.1,  // 22:00-22:59 晚间
        23 to 0.9,  // 23:00-23:59 深夜
        0 to 0.7,   // 00:00-00:59 深夜
        1 to 0.6,   // 01:00-01:59 深夜
        2 to 0.5,   // 02:00-02:59 深夜
        3 to 0.5,   // 03:00-03:59 深夜
        4 to 0.6,   // 04:00-04:59 深夜
        5 to 0.8    // 05:00-05:59 早晨
    )
    
    /**
     * 计算成员的时间衰减加权分数
     * 
     * @param member 成员信息
     * @param loginRecords 该成员的登录记录列表
     * @return 加权分数，分数越高优先级越高
     */
    fun calculateMemberScore(
        member: Member,
        loginRecords: List<OnlineStatusEntity>
    ): Double {
        if (loginRecords.isEmpty()) {
            return 0.0
        }
        
        val currentTime = System.currentTimeMillis()
        var totalScore = 0.0
        
        loginRecords.forEach { record ->
            // 计算天数差
            val daysDiff = (currentTime - record.loginTime) / (24.0 * 60 * 60 * 1000)
            
            // 时间衰减系数
            val timeDecay = exp(-LAMBDA * daysDiff)
            
            // 获取登录时间的时段权重
            val loginDateTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(record.loginTime),
                ZoneId.systemDefault()
            )
            val hour = loginDateTime.hour
            val timeSlotWeight = TIME_SLOT_WEIGHTS[hour] ?: 1.0
            
            // 累加分数：时间衰减 × 时段权重
            totalScore += timeDecay * timeSlotWeight
        }
        
        return totalScore
    }
    
    /**
     * 对成员列表进行排序
     * 
     * @param members 成员列表
     * @param loginRecordsMap 成员ID -> 登录记录列表的映射
     * @param currentMemberId 当前选中的成员ID（会排在最前面）
     * @return 排序后的成员列表
     */
    fun sortMembersByLoginActivity(
        members: List<Member>,
        loginRecordsMap: Map<String, List<OnlineStatusEntity>>,
        currentMemberId: String
    ): List<Member> {
        // 分离当前成员和其他成员
        val currentMember = members.find { it.id == currentMemberId }
        val otherMembers = members.filter { it.id != currentMemberId }
        
        // 计算每个成员的分数并排序
        val scoredMembers = otherMembers.map { member ->
            val loginRecords = loginRecordsMap[member.id] ?: emptyList()
            val score = calculateMemberScore(member, loginRecords)
            val lastLoginTime = loginRecords.maxOfOrNull { it.loginTime } ?: 0L
            
            Triple(member, score, lastLoginTime)
        }
        
        // 排序规则：
        // 1. 主排序：分数降序
        // 2. 分数相同时：最近登录时间降序
        // 3. 再相同时：按姓名升序
        val sortedOtherMembers = scoredMembers.sortedWith { a, b ->
            when {
                a.second != b.second -> b.second.compareTo(a.second) // 分数降序
                a.third != b.third -> b.third.compareTo(a.third)     // 最近登录时间降序
                else -> a.first.name.compareTo(b.first.name)         // 姓名升序
            }
        }.map { it.first }
        
        // 返回结果：当前成员在前，其他成员按分数排序
        return if (currentMember != null) {
            listOf(currentMember) + sortedOtherMembers
        } else {
            sortedOtherMembers
        }
    }
    
    /**
     * 获取成员的排序摘要信息（用于调试）
     */
    fun getMemberScoreSummary(
        member: Member,
        loginRecords: List<OnlineStatusEntity>
    ): MemberScoreSummary {
        val score = calculateMemberScore(member, loginRecords)
        val loginCount = loginRecords.size
        val lastLoginTime = loginRecords.maxOfOrNull { it.loginTime }
        val recentLoginCount = loginRecords.count { 
            (System.currentTimeMillis() - it.loginTime) <= (7 * 24 * 60 * 60 * 1000L) // 最近7天
        }
        
        return MemberScoreSummary(
            memberId = member.id,
            memberName = member.name,
            score = score,
            totalLoginCount = loginCount,
            recentLoginCount = recentLoginCount,
            lastLoginTime = lastLoginTime
        )
    }
}

/**
 * 成员分数摘要
 */
data class MemberScoreSummary(
    val memberId: String,
    val memberName: String,
    val score: Double,
    val totalLoginCount: Int,
    val recentLoginCount: Int,
    val lastLoginTime: Long?
)
