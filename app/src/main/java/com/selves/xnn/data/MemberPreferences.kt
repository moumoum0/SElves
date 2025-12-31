package com.selves.xnn.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.selves.xnn.model.ThemeMode
import com.selves.xnn.model.TrackingConfig
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
        private val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        
        // 轨迹记录配置
        private val TRACKING_RECORDING_INTERVAL = intPreferencesKey("tracking_recording_interval")
        private val TRACKING_AUTO_RESTART_DELAY = intPreferencesKey("tracking_auto_restart_delay")
        private val TRACKING_ENABLE_AUTO_START = booleanPreferencesKey("tracking_enable_auto_start")
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
} 