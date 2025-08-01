package com.selves.xnn.data.dao

import androidx.room.*
import com.selves.xnn.data.entity.OnlineStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OnlineStatusDao {
    
    @Query("SELECT * FROM online_status WHERE memberId = :memberId ORDER BY loginTime DESC")
    fun getOnlineStatusByMember(memberId: String): Flow<List<OnlineStatusEntity>>
    
    @Query("SELECT * FROM online_status WHERE memberId = :memberId AND logoutTime IS NULL LIMIT 1")
    suspend fun getCurrentOnlineStatus(memberId: String): OnlineStatusEntity?
    
    @Query("""
        SELECT * FROM online_status 
        WHERE memberId = :memberId 
        AND loginTime >= :todayStart
        ORDER BY loginTime DESC
    """)
    suspend fun getTodayOnlineStatus(memberId: String, todayStart: Long): List<OnlineStatusEntity>
    
    @Query("""
        SELECT SUM(
            CASE 
                WHEN logoutTime IS NULL THEN :currentTime - loginTime
                ELSE duration
            END
        ) FROM online_status 
        WHERE memberId = :memberId 
        AND loginTime >= :todayStart
    """)
    suspend fun getTodayOnlineTime(memberId: String, currentTime: Long, todayStart: Long): Long?
    
    @Insert
    suspend fun insertOnlineStatus(onlineStatus: OnlineStatusEntity): Long
    
    @Update
    suspend fun updateOnlineStatus(onlineStatus: OnlineStatusEntity)
    
    @Query("UPDATE online_status SET logoutTime = :logoutTime, duration = :duration WHERE id = :id")
    suspend fun updateLogoutTime(id: Long, logoutTime: Long, duration: Long)
    
    @Query("DELETE FROM online_status WHERE memberId = :memberId")
    suspend fun deleteOnlineStatusByMember(memberId: String)
    
    @Query("""
        SELECT memberId, MAX(
            CASE 
                WHEN logoutTime IS NULL THEN loginTime
                ELSE logoutTime
            END
        ) as lastActiveTime
        FROM online_status 
        GROUP BY memberId
    """)
    suspend fun getLastActiveTimeForAllMembers(): List<LastActiveTime>
    
    // 获取所有登录日志，按登录时间降序
    @Query("SELECT * FROM online_status ORDER BY loginTime DESC LIMIT :limit")
    suspend fun getAllLoginLogs(limit: Int = 100): List<OnlineStatusEntity>
    
    // 获取今日所有登录日志
    @Query("""
        SELECT * FROM online_status 
        WHERE loginTime >= :todayStart 
        ORDER BY loginTime DESC
    """)
    suspend fun getTodayLoginLogs(todayStart: Long): List<OnlineStatusEntity>
    
    // 获取指定日期范围的登录日志
    @Query("""
        SELECT * FROM online_status 
        WHERE loginTime >= :startTime AND loginTime <= :endTime 
        ORDER BY loginTime DESC
    """)
    suspend fun getLoginLogsByDateRange(startTime: Long, endTime: Long): List<OnlineStatusEntity>
    
    // 获取登录统计信息
    @Query("SELECT COUNT(*) FROM online_status")
    suspend fun getTotalLoginCount(): Int
    
    @Query("SELECT COUNT(*) FROM online_status WHERE loginTime >= :todayStart")
    suspend fun getTodayLoginCount(todayStart: Long): Int
    
    @Query("SELECT COUNT(*) FROM online_status WHERE logoutTime IS NULL")
    suspend fun getCurrentOnlineCount(): Int
    
    @Query("""
        SELECT AVG(
            CASE 
                WHEN logoutTime IS NULL THEN :currentTime - loginTime
                ELSE duration
            END
        ) FROM online_status 
        WHERE loginTime >= :todayStart
    """)
    suspend fun getAverageOnlineTime(currentTime: Long, todayStart: Long): Long?

    // 备份用的同步查询方法
    @Query("SELECT * FROM online_status ORDER BY loginTime ASC")
    suspend fun getAllOnlineStatusSync(): List<OnlineStatusEntity>

    @Query("DELETE FROM online_status")
    suspend fun deleteAll()
}

data class LastActiveTime(
    val memberId: String,
    val lastActiveTime: Long
) 