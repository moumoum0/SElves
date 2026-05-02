package com.selves.xnn.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.selves.xnn.data.entity.MemberDiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDiaryDao {
    @Query("SELECT * FROM member_diaries WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getDiariesByMember(memberId: String): Flow<List<MemberDiaryEntity>>

    @Query("SELECT * FROM member_diaries WHERE memberId = :memberId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentDiariesByMember(memberId: String, limit: Int): Flow<List<MemberDiaryEntity>>

    @Query("SELECT * FROM member_diaries ORDER BY createdAt DESC")
    suspend fun getAllDiariesSync(): List<MemberDiaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiary(diary: MemberDiaryEntity)

    @Query("DELETE FROM member_diaries WHERE id = :id")
    suspend fun deleteDiaryById(id: String)

    @Query("DELETE FROM member_diaries WHERE memberId = :memberId")
    suspend fun deleteDiariesByMember(memberId: String)

    @Query("DELETE FROM member_diaries")
    suspend fun deleteAll()
}
