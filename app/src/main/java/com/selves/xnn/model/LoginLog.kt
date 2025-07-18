package com.selves.xnn.model

import java.time.LocalDateTime

data class LoginLog(
    val id: Long,
    val memberId: String,
    val memberName: String,
    val memberAvatar: String?,
    val loginTime: LocalDateTime,
    val logoutTime: LocalDateTime? = null,
    val duration: Long = 0, // 在线时长（毫秒）
    val isOnline: Boolean = false // 是否仍在线
)

data class LoginLogSummary(
    val totalLogins: Int,
    val todayLogins: Int,
    val currentOnlineCount: Int,
    val averageOnlineTime: Long
) 