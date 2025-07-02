package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.MessageReadStatusDao
import com.example.myapplication.data.entity.MessageReadStatusEntity
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageReadStatusRepository @Inject constructor(
    private val messageReadStatusDao: MessageReadStatusDao
) {
    
    suspend fun markMessageAsRead(messageId: String, memberId: String) {
        val readStatus = MessageReadStatusEntity(
            id = UUID.randomUUID().toString(),
            messageId = messageId,
            memberId = memberId
        )
        messageReadStatusDao.insertReadStatus(readStatus)
    }
    
    suspend fun markMessagesAsRead(messageIds: List<String>, memberId: String) {
        val readStatuses = messageIds.map { messageId ->
            MessageReadStatusEntity(
                id = UUID.randomUUID().toString(),
                messageId = messageId,
                memberId = memberId
            )
        }
        messageReadStatusDao.insertReadStatuses(readStatuses)
    }
    
    suspend fun markAllGroupMessagesAsRead(groupId: String, memberId: String) {
        val unreadMessageIds = messageReadStatusDao.getUnreadMessageIds(groupId, memberId)
        if (unreadMessageIds.isNotEmpty()) {
            markMessagesAsRead(unreadMessageIds, memberId)
        }
    }
    
    suspend fun getUnreadMessageCount(groupId: String, memberId: String): Int {
        return messageReadStatusDao.getUnreadMessageCount(groupId, memberId)
    }
    
    fun getUnreadMessageCountFlow(groupId: String, memberId: String): Flow<Int> {
        return messageReadStatusDao.getUnreadMessageCountFlow(groupId, memberId)
    }
    
    suspend fun isMessageRead(messageId: String, memberId: String): Boolean {
        return messageReadStatusDao.getMessageReadStatusByMember(messageId, memberId) != null
    }
    
    suspend fun getMessageReadStatus(messageId: String): List<MessageReadStatusEntity> {
        return messageReadStatusDao.getMessageReadStatus(messageId)
    }
    
    suspend fun deleteReadStatusForMessage(messageId: String) {
        messageReadStatusDao.deleteReadStatusForMessage(messageId)
    }
    
    suspend fun deleteReadStatusForMember(memberId: String) {
        messageReadStatusDao.deleteReadStatusForMember(memberId)
    }
} 