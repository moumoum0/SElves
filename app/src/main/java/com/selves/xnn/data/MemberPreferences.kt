package com.selves.xnn.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.selves.xnn.model.ThemeMode
import com.selves.xnn.model.TrackingConfig
import com.selves.xnn.model.ColorScheme
import androidx.datastore.preferences.core.intPreferencesKey

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "member_preferences")

/**
 * 成员偏好设置存储类
 */
class MemberPreferences(private val context: Context) {
    
    companion object {
        private val CURRENT_MEMBER_ID = stringPreferencesKey("current_member_id")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val QUICK_MEMBER_SWITCH_ENABLED = booleanPreferencesKey("quick_member_switch_enabled")
        private val CHAT_LAYOUT_OPTIMIZATION_ENABLED = booleanPreferencesKey("chat_layout_optimization_enabled")
        private val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        private val COLOR_SCHEME = stringPreferencesKey("color_scheme")
        private val LANGUAGE = stringPreferencesKey("language")
        
        // 轨迹记录配置
        private val TRACKING_RECORDING_INTERVAL = intPreferencesKey("tracking_recording_interval")
        private val TRACKING_AUTO_RESTART_DELAY = intPreferencesKey("tracking_auto_restart_delay")
        private val TRACKING_ENABLE_AUTO_START = booleanPreferencesKey("tracking_enable_auto_start")
        
        // Web 访问服务
        private val WEB_SERVER_ENABLED = booleanPreferencesKey("web_server_enabled")
        private val WEB_API_TOKEN = stringPreferencesKey("web_api_token")
        
        // 自动备份配置
        private val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        private val AUTO_BACKUP_FREQUENCY = stringPreferencesKey("auto_backup_frequency")
        private val AUTO_BACKUP_HOUR = intPreferencesKey("auto_backup_hour")
        private val AUTO_BACKUP_PATH = stringPreferencesKey("auto_backup_path")
    }
    
    /**
     * 获取当前成员ID
     */
    val currentMemberId: Flow<String?> = context.dataStore.data
        .map { preferences -> 
            preferences[CURRENT_MEMBER_ID]
        }
    
    /**
     * 保存当前成员ID
     */
    suspend fun saveCurrentMemberId(memberId: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_MEMBER_ID] = memberId
        }
    }
    
    /**
     * 清除当前成员ID
     */
    suspend fun clearCurrentMemberId() {
        context.dataStore.edit { preferences ->
            preferences.remove(CURRENT_MEMBER_ID)
        }
    }
    
    /**
     * 获取主题模式
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val themeModeString = preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(themeModeString)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }
    
    /**
     * 保存主题模式
     */
    suspend fun saveThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }
    
    /**
     * 获取快捷切换成员是否启用
     */
    val quickMemberSwitchEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[QUICK_MEMBER_SWITCH_ENABLED] ?: false
        }
    
    /**
     * 保存快捷切换成员启用状态
     */
    suspend fun saveQuickMemberSwitchEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[QUICK_MEMBER_SWITCH_ENABLED] = enabled
        }
    }
    
    /**
     * 获取群聊排版优化是否启用
     */
    val chatLayoutOptimizationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[CHAT_LAYOUT_OPTIMIZATION_ENABLED] ?: false
        }
    
    /**
     * 保存群聊排版优化启用状态
     */
    suspend fun saveChatLayoutOptimizationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_LAYOUT_OPTIMIZATION_ENABLED] = enabled
        }
    }
    
    /**
     * 获取轨迹记录配置
     */
    val trackingConfig: Flow<TrackingConfig> = context.dataStore.data
        .map { preferences ->
            TrackingConfig(
                recordingInterval = preferences[TRACKING_RECORDING_INTERVAL] ?: 60,
                autoRestartDelay = preferences[TRACKING_AUTO_RESTART_DELAY] ?: 300,
                enableAutoStart = preferences[TRACKING_ENABLE_AUTO_START] ?: false
            )
        }
    
    /**
     * 保存轨迹记录配置
     */
    suspend fun saveTrackingConfig(config: TrackingConfig) {
        context.dataStore.edit { preferences ->
            preferences[TRACKING_RECORDING_INTERVAL] = config.recordingInterval
            preferences[TRACKING_AUTO_RESTART_DELAY] = config.autoRestartDelay
            preferences[TRACKING_ENABLE_AUTO_START] = config.enableAutoStart
        }
    }
    
    /**
     * 获取动态颜色是否启用
     */
    val dynamicColorEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DYNAMIC_COLOR_ENABLED] ?: false
        }
    
    /**
     * 保存动态颜色启用状态
     */
    suspend fun saveDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_ENABLED] = enabled
        }
    }
    
    /**
     * 获取配色方案
     */
    val colorScheme: Flow<ColorScheme> = context.dataStore.data
        .map { preferences ->
            val colorSchemeString = preferences[COLOR_SCHEME]
            if (colorSchemeString != null) {
                try {
                    ColorScheme.valueOf(colorSchemeString)
                } catch (e: IllegalArgumentException) {
                    ColorScheme.APP_DEFAULT
                }
            } else {
                val dynamicColorEnabled = preferences[DYNAMIC_COLOR_ENABLED] ?: false
                if (dynamicColorEnabled) ColorScheme.WALLPAPER else ColorScheme.APP_DEFAULT
            }
        }
    
    /**
     * 保存配色方案
     */
    suspend fun saveColorScheme(colorScheme: ColorScheme) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_SCHEME] = colorScheme.name
        }
    }
    
    /**
     * 获取语言设置
     */
    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE] ?: "system" // 默认跟随系统
        }
    
    /**
     * 保存语言设置
     */
    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }

    /**
     * 获取 Web 服务器启用状态
     */
    val webServerEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[WEB_SERVER_ENABLED] ?: false }

    /**
     * 保存 Web 服务器启用状态
     */
    suspend fun saveWebServerEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WEB_SERVER_ENABLED] = enabled
        }
    }

    /**
     * 获取 Web API Token
     */
    val webApiToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[WEB_API_TOKEN] }

    /**
     * 获取 Web API Token（同步阻塞，仅用于 Ktor 路由鉴权）
     */
    suspend fun getWebApiToken(): String? {
        return context.dataStore.data.first()[WEB_API_TOKEN]
    }

    /**
     * 保存 Web API Token
     */
    suspend fun saveWebApiToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[WEB_API_TOKEN] = token
        }
    }

    /**
     * 生成并保存新的 Web API Token
     * 格式：6位大写字母+数字组合（如 A3B7K9）
     */
    suspend fun generateWebApiToken(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // 排除易混淆字符 0O1I
        val token = (1..6).map { chars.random() }.joinToString("")
        saveWebApiToken(token)
        return token
    }
    
    /**
     * 获取自动备份启用状态
     */
    val autoBackupEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[AUTO_BACKUP_ENABLED] ?: false }
    
    /**
     * 保存自动备份启用状态
     */
    suspend fun saveAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_BACKUP_ENABLED] = enabled
        }
    }
    
    /**
     * 获取自动备份频率（daily, weekly, monthly）
     */
    val autoBackupFrequency: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[AUTO_BACKUP_FREQUENCY] ?: "daily" }
    
    /**
     * 保存自动备份频率
     */
    suspend fun saveAutoBackupFrequency(frequency: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_BACKUP_FREQUENCY] = frequency
        }
    }
    
    /**
     * 获取自动备份时间（小时，0-23）
     */
    val autoBackupHour: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[AUTO_BACKUP_HOUR] ?: 3 }
    
    /**
     * 保存自动备份时间
     */
    suspend fun saveAutoBackupHour(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_BACKUP_HOUR] = hour
        }
    }
    
    /**
     * 获取自动备份路径
     */
    val autoBackupPath: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[AUTO_BACKUP_PATH] }
    
    /**
     * 保存自动备份路径
     */
    suspend fun saveAutoBackupPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path != null) {
                preferences[AUTO_BACKUP_PATH] = path
            } else {
                preferences.remove(AUTO_BACKUP_PATH)
            }
        }
    }
}