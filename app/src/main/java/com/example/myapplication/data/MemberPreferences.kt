package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "member_preferences")

/**
 * 成员偏好设置存储类
 */
class MemberPreferences(private val context: Context) {
    
    companion object {
        private val CURRENT_MEMBER_ID = stringPreferencesKey("current_member_id")
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
} 