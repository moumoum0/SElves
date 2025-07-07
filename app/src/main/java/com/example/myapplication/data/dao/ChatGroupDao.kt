package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.entity.ChatGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatGroupDao {
    @Query("SELECT * FROM chat_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<ChatGroupEntity>>
    
    @Query("SELECT * FROM chat_groups WHERE memberIds LIKE '%' || :memberId || '%' ORDER BY createdAt DESC")
    fun getGroupsByMemberId(memberId: String): Flow<List<ChatGroupEntity>>
    
    @Query("SELECT * FROM chat_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): ChatGroupEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ChatGroupEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<ChatGroupEntity>)
    
    @Update
    suspend fun updateGroup(group: ChatGroupEntity)
    
    @Delete
    suspend fun deleteGroup(group: ChatGroupEntity)
    
    @Query("DELETE FROM chat_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: String)
} 