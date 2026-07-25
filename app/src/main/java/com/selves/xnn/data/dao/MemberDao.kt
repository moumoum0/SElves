package com.selves.xnn.data.dao

import androidx.room.*
import com.selves.xnn.data.entity.MemberEntity
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

    @Query("SELECT COUNT(*) FROM members WHERE isDeleted = 0")
    suspend fun countActiveMembers(): Int

    @Query("SELECT COUNT(*) FROM members WHERE isAdmin = 1 AND isDeleted = 0")
    suspend fun countActiveAdmins(): Int

    /** 将指定活跃成员设为管理员 */
    @Query("UPDATE members SET isAdmin = 1 WHERE id = :memberId AND isDeleted = 0")
    suspend fun setMemberAsAdmin(memberId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun deleteMemberById(memberId: String)

    // 备份用的同步查询方法
    @Query("SELECT * FROM members")
    suspend fun getAllMembersSync(): List<MemberEntity>

    @Query("DELETE FROM members")
    suspend fun deleteAll()
} 