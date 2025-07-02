package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.Mappers.toDomain
import com.example.myapplication.data.Mappers.toEntity
import com.example.myapplication.data.dao.MessageDao
import com.example.myapplication.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {
    private val TAG = "MessageRepository"

    suspend fun saveMessage(message: Message, groupId: String) {
        try {
            val entity = message.toEntity(groupId)
            messageDao.insertMessage(entity)
            Log.d(TAG, "已保存消息: ${message.id}")
        } catch (e: Exception) {
            Log.e(TAG, "保存消息失败: ${e.message}", e)
            throw e
        }
    }

    suspend fun saveMessages(messages: List<Message>, groupId: String) {
        try {
            val entities = messages.map { it.toEntity(groupId) }
            messageDao.insertMessages(entities)
            Log.d(TAG, "已批量保存 ${messages.size} 条消息")
        } catch (e: Exception) {
            Log.e(TAG, "批量保存消息失败: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteMessage(message: Message, groupId: String) {
        try {
            val entity = message.toEntity(groupId)
            messageDao.deleteMessage(entity)
            Log.d(TAG, "已删除消息: ${message.id}")
        } catch (e: Exception) {
            Log.e(TAG, "删除消息失败: ${e.message}", e)
            throw e
        }
    }

    suspend fun clearGroupMessages(groupId: String) {
        try {
            messageDao.deleteMessagesByGroupId(groupId)
            Log.d(TAG, "已清空群组 $groupId 的消息")
        } catch (e: Exception) {
            Log.e(TAG, "清空群组消息失败: ${e.message}", e)
            throw e
        }
    }

    fun getGroupMessages(groupId: String): Flow<List<Message>> {
        return messageDao.getMessagesByGroupId(groupId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getRecentGroupMessages(groupId: String, limit: Int = 100): Flow<List<Message>> {
        return messageDao.getRecentMessagesByGroupId(groupId, limit)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun getMessageCount(groupId: String): Int {
        return messageDao.getMessageCountByGroupId(groupId)
    }
} 