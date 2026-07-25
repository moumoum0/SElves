package com.selves.xnn.data

import com.selves.xnn.util.AdminPinCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 系统级管理员数字 PIN（6 位）。
 *
 * - 哈希存 DataStore；进程内会话解锁（验证一次后同进程内敏感操作免重复输入）
 * - 恢复：后续可在此增加 recovery 校验入口，不改 PIN 哈希格式
 */
@Singleton
class AdminPinRepository @Inject constructor(
    private val memberPreferences: MemberPreferences
) {
    val hasAdminPin: Flow<Boolean> = memberPreferences.hasAdminPin

    private val _sessionUnlocked = MutableStateFlow(false)
    val sessionUnlocked: StateFlow<Boolean> = _sessionUnlocked.asStateFlow()

    suspend fun hasAdminPin(): Boolean = memberPreferences.hasAdminPin()

    fun isSessionUnlocked(): Boolean = _sessionUnlocked.value

    fun unlockSession() {
        _sessionUnlocked.value = true
    }

    fun lockSession() {
        _sessionUnlocked.value = false
    }

    /**
     * 设置 / 重置 PIN。成功后自动解锁会话。
     * @return false 若格式非法
     */
    suspend fun setPin(pin: String): Boolean {
        if (!AdminPinCrypto.isValidPinFormat(pin)) return false
        val salt = AdminPinCrypto.generateSalt()
        val hash = AdminPinCrypto.hashPin(pin, salt)
        memberPreferences.saveAdminPin(salt, hash)
        unlockSession()
        return true
    }

    /** 校验 PIN；正确则解锁会话 */
    suspend fun verifyPin(pin: String): Boolean {
        if (!AdminPinCrypto.isValidPinFormat(pin)) return false
        val salt = memberPreferences.getAdminPinSalt() ?: return false
        val hash = memberPreferences.getAdminPinHash() ?: return false
        val ok = AdminPinCrypto.verifyPin(pin, salt, hash)
        if (ok) unlockSession()
        return ok
    }

    /**
     * 修改 PIN：须先验证旧 PIN。
     * @return false 若旧 PIN 错误或新 PIN 格式非法
     */
    suspend fun changePin(oldPin: String, newPin: String): Boolean {
        if (!verifyPin(oldPin)) return false
        return setPin(newPin)
    }
}