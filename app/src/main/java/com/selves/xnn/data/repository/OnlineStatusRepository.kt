package com.selves.xnn.data.repository

import com.selves.xnn.data.dao.OnlineStatusDao
import com.selves.xnn.data.entity.OnlineStatusEntity
import com.selves.xnn.data.dao.LastActiveTime
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnlineStatusRepository @Inject constructor(
    private val onlineStatusDao: OnlineStatusDao
) {
    
    fun getOnlineStatusByMember(memberId: String): Flow<List<OnlineStatusEntity>> {
        return onlineStatusDao.getOnlineStatusByMember(memberId)
    }
    
    suspend fun getCurrentOnlineStatus(memberId: String): OnlineStatusEntity? {
        return onlineStatusDao.getCurrentOnlineStatus(memberId)
    }
    
    suspend fun getTodayOnlineTime(memberId: String): Long {
        val currentTime = System.currentTimeMillis()
        
        // 计算今天的开始时间（00:00:00）
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentTime
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        
        val result = onlineStatusDao.getTodayOnlineTime(memberId, currentTime, todayStart) ?: 0L
        android.util.Log.d("OnlineStatusRepository", "getTodayOnlineTime for $memberId: $result ms (todayStart: $todayStart, currentTime: $currentTime)")
        return result
    }
    
    suspend fun loginMember(memberId: String): Long {
        // 检查是否已经在线
        val currentStatus = onlineStatusDao.getCurrentOnlineStatus(memberId)
        if (currentStatus != null) {
            android.util.Log.d("OnlineStatusRepository", "用户 $memberId 已经在线，返回现有记录ID: ${currentStatus.id}")
            return currentStatus.id
        }
        
        // 创建新的在线状态记录
        val currentTime = System.currentTimeMillis()
        val onlineStatus = OnlineStatusEntity(
            memberId = memberId,
            loginTime = currentTime
        )
        val recordId = onlineStatusDao.insertOnlineStatus(onlineStatus)
        android.util.Log.d("OnlineStatusRepository", "用户 $memberId 上线，登录时间: $currentTime，记录ID: $recordId")
        
        return recordId
    }
    
    suspend fun logoutMember(memberId: String) {
        val currentStatus = onlineStatusDao.getCurrentOnlineStatus(memberId)
        if (currentStatus != null) {
            val logoutTime = System.currentTimeMillis()
            val duration = logoutTime - currentStatus.loginTime
            onlineStatusDao.updateLogoutTime(currentStatus.id, logoutTime, duration)
        }
    }
    
    suspend fun isOnline(memberId: String): Boolean {
        return onlineStatusDao.getCurrentOnlineStatus(memberId) != null
    }
    
    suspend fun getLastActiveTimeForAllMembers(): List<LastActiveTime> {
        return onlineStatusDao.getLastActiveTimeForAllMembers()
    }
    
    suspend fun getTodayOnlineStatus(memberId: String): List<OnlineStatusEntity> {
        val currentTime = System.currentTimeMillis()
        
        // 计算今天的开始时间（00:00:00）
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentTime
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        
        return onlineStatusDao.getTodayOnlineStatus(memberId, todayStart)
    }
    
    // 获取所有登录日志
    suspend fun getAllLoginLogs(limit: Int = 100): List<OnlineStatusEntity> {
        return onlineStatusDao.getAllLoginLogs(limit)
    }
    
    // 获取今日登录日志
    suspend fun getTodayLoginLogs(): List<OnlineStatusEntity> {
        val todayStart = getTodayStartTime()
        return onlineStatusDao.getTodayLoginLogs(todayStart)
    }
    
    // 获取指定日期范围的登录日志
    suspend fun getLoginLogsByDateRange(startTime: Long, endTime: Long): List<OnlineStatusEntity> {
        return onlineStatusDao.getLoginLogsByDateRange(startTime, endTime)
    }
    
    // 获取登录统计摘要
    suspend fun getLoginLogSummary(): com.selves.xnn.model.LoginLogSummary {
        val currentTime = System.currentTimeMillis()
        val todayStart = getTodayStartTime()
        
        val totalLogins = onlineStatusDao.getTotalLoginCount()
        val todayLogins = onlineStatusDao.getTodayLoginCount(todayStart)
        val currentOnlineCount = onlineStatusDao.getCurrentOnlineCount()
        val averageOnlineTime = onlineStatusDao.getAverageOnlineTime(currentTime, todayStart) ?: 0L
        
        return com.selves.xnn.model.LoginLogSummary(
            totalLogins = totalLogins,
            todayLogins = todayLogins,
            currentOnlineCount = currentOnlineCount,
            averageOnlineTime = averageOnlineTime
        )
    }
    
    private fun getTodayStartTime(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
} 