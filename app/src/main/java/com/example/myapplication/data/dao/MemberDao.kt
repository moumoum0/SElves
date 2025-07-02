package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.MemberEntity
import kotlinx.coroutines.flow.Flow
import android.util.Log

@Dao
interface MemberDao {
    @Query("SELECT * FROM members")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :memberId")
    suspend fun getMemberById(memberId: String): MemberEntity?

    @Query("SELECT * FROM members WHERE id IN (:memberIds)")
    fun getMembersByIds(memberIds: List<String>): Flow<List<MemberEntity>>

    @Query("SELECT COUNT(*) FROM members WHERE id = :memberId")
    suspend fun checkMemberExists(memberId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun deleteMemberById(memberId: String)
} 