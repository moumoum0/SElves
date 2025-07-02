package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.MessageReadStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageReadStatusDao {
    
    @Query("SELECT * FROM message_read_status WHERE messageId = :messageId")
    suspend fun getMessageReadStatus(messageId: String): List<MessageReadStatusEntity>
    
    @Query("SELECT * FROM message_read_status WHERE messageId = :messageId AND memberId = :memberId")
    suspend fun getMessageReadStatusByMember(messageId: String, memberId: String): MessageReadStatusEntity?
    
    @Query("""
        SELECT COUNT(*) FROM messages m 
        WHERE m.groupId = :groupId 
        AND m.senderId != :currentMemberId
        AND NOT EXISTS (
            SELECT 1 FROM message_read_status mrs 
            WHERE mrs.messageId = m.id AND mrs.memberId = :currentMemberId
        )
    """)
    suspend fun getUnreadMessageCount(groupId: String, currentMemberId: String): Int
    
    @Query("""
        SELECT COUNT(*) FROM messages m 
        WHERE m.groupId = :groupId 
        AND m.senderId != :currentMemberId
        AND NOT EXISTS (
            SELECT 1 FROM message_read_status mrs 
            WHERE mrs.messageId = m.id AND mrs.memberId = :currentMemberId
        )
    """)
    fun getUnreadMessageCountFlow(groupId: String, currentMemberId: String): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadStatus(readStatus: MessageReadStatusEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadStatuses(readStatuses: List<MessageReadStatusEntity>)
    
    @Query("DELETE FROM message_read_status WHERE messageId = :messageId")
    suspend fun deleteReadStatusForMessage(messageId: String)
    
    @Query("DELETE FROM message_read_status WHERE memberId = :memberId")
    suspend fun deleteReadStatusForMember(memberId: String)
    
    @Query("""
        SELECT m.id FROM messages m 
        WHERE m.groupId = :groupId 
        AND m.senderId != :currentMemberId
        AND NOT EXISTS (
            SELECT 1 FROM message_read_status mrs 
            WHERE mrs.messageId = m.id AND mrs.memberId = :currentMemberId
        )
    """)
    suspend fun getUnreadMessageIds(groupId: String, currentMemberId: String): List<String>
} 