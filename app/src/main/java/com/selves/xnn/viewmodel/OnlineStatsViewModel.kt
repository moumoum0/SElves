package com.selves.xnn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.repository.OnlineStatusRepository
import com.selves.xnn.data.repository.MemberRepository
import com.selves.xnn.model.Member
import com.selves.xnn.model.LoginLog
import com.selves.xnn.model.LoginLogSummary
import com.selves.xnn.ui.screens.OnlineStats
import com.selves.xnn.ui.screens.MemberOnlineStat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class OnlineStatsViewModel @Inject constructor(
    private val onlineStatusRepository: OnlineStatusRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _onlineStats = MutableStateFlow(OnlineStats(0, emptyList()))
    val onlineStats: StateFlow<OnlineStats> = _onlineStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _loginLogs = MutableStateFlow<List<LoginLog>>(emptyList())
    val loginLogs: StateFlow<List<LoginLog>> = _loginLogs.asStateFlow()
    
    private val _loginLogSummary = MutableStateFlow<LoginLogSummary?>(null)
    val loginLogSummary: StateFlow<LoginLogSummary?> = _loginLogSummary.asStateFlow()
    
    private val _isLoadingLogs = MutableStateFlow(false)
    val isLoadingLogs: StateFlow<Boolean> = _isLoadingLogs.asStateFlow()

    fun loadOnlineStats(currentMember: Member) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                memberRepository.getAllMembers().take(1).collect { members ->
                    val lastActiveTimeMap = onlineStatusRepository.getLastActiveTimeForAllMembers()
                        .associateBy { it.memberId }

                    val memberStats = members.map { member ->
                        val isOnline = onlineStatusRepository.isOnline(member.id)
                        val todayOnlineTimeMs = onlineStatusRepository.getTodayOnlineTime(member.id)
                        val todayOnlineMinutes = (todayOnlineTimeMs / (60 * 1000)).toInt()
                        
                        android.util.Log.d("OnlineStatsViewModel", "Member ${member.name}: isOnline=$isOnline, todayOnlineTimeMs=$todayOnlineTimeMs, todayOnlineMinutes=$todayOnlineMinutes")
                        
                        val lastActiveTime = lastActiveTimeMap[member.id]?.lastActiveTime 
                            ?: System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 默认24小时前

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

                    _onlineStats.value = OnlineStats(
                        onlineCount = onlineCount,
                        memberStats = sortedStats
                    )
                    
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // 处理错误
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun loginMember(memberId: String) {
        viewModelScope.launch {
            onlineStatusRepository.loginMember(memberId)
        }
    }

    fun logoutMember(memberId: String) {
        viewModelScope.launch {
            onlineStatusRepository.logoutMember(memberId)
        }
    }
    
    // 加载登录日志
    fun loadLoginLogs(filter: LoginLogFilter = LoginLogFilter.ALL) {
        viewModelScope.launch {
            _isLoadingLogs.value = true
            try {
                val entities = when (filter) {
                    LoginLogFilter.ALL -> onlineStatusRepository.getAllLoginLogs()
                    LoginLogFilter.TODAY -> onlineStatusRepository.getTodayLoginLogs()
                }
                
                val members = memberRepository.getAllMembers()
                members.collect { memberList ->
                    val memberMap = memberList.associateBy { it.id }
                    
                    val loginLogs = entities.map { entity ->
                        val member = memberMap[entity.memberId]
                        LoginLog(
                            id = entity.id,
                            memberId = entity.memberId,
                            memberName = member?.name ?: "未知用户",
                            memberAvatar = member?.avatarUrl,
                            loginTime = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(entity.loginTime),
                                ZoneId.systemDefault()
                            ),
                            logoutTime = entity.logoutTime?.let { 
                                LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(it),
                                    ZoneId.systemDefault()
                                )
                            },
                            duration = entity.duration,
                            isOnline = entity.logoutTime == null
                        )
                    }
                    
                    _loginLogs.value = loginLogs
                    _isLoadingLogs.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoadingLogs.value = false
            }
        }
    }
    
    // 加载登录统计摘要
    fun loadLoginLogSummary() {
        viewModelScope.launch {
            try {
                val summary = onlineStatusRepository.getLoginLogSummary()
                _loginLogSummary.value = summary
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

enum class LoginLogFilter {
    ALL, TODAY
} 