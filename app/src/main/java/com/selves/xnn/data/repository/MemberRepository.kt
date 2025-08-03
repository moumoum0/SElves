package com.selves.xnn.data.repository

import com.selves.xnn.data.AppDatabase
import com.selves.xnn.data.entity.MemberEntity
import com.selves.xnn.data.Mappers.toDomain
import com.selves.xnn.data.Mappers.toEntity
import com.selves.xnn.model.Member
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * 成员仓库类，负责处理成员相关的数据操作
 */
@Singleton
class MemberRepository @Inject constructor(private val database: AppDatabase) {
    
    /**
     * 获取所有未删除的成员
     */
    fun getAllMembers(): Flow<List<Member>> {
        return database.memberDao().getAllMembers()
            .map { entities -> 
                entities
                    .filter { !it.isDeleted }  // 仅返回未被删除的成员
                    .map { it.toDomain() } 
            }
    }
    
    /**
     * 获取所有成员，包括已删除的成员（用于显示历史消息）
     */
    fun getAllMembersIncludingDeleted(): Flow<List<Member>> {
        return database.memberDao().getAllMembers()
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * 根据ID获取成员
     */
    suspend fun getMemberById(memberId: String): Member? {
        val member = database.memberDao().getMemberById(memberId)?.toDomain()
        // 如果成员已被删除，返回null
        return if (member?.isDeleted == true) null else member
    }
    
    /**
     * 根据ID获取成员（包括已删除的成员）
     */
    suspend fun getMemberByIdIncludingDeleted(memberId: String): Member? {
        return database.memberDao().getMemberById(memberId)?.toDomain()
    }
    
    /**
     * 保存成员
     */
    suspend fun saveMember(member: Member) {
        // 检查成员是否已存在
        val exists = database.memberDao().checkMemberExists(member.id)
        val entity = member.toEntity()
        
        // 不管成员是否存在，都保存/更新数据
        database.memberDao().insertMember(entity)
        
        // 如果是更新现有成员，同步更新相关表中的用户信息
        if (exists > 0) {
            Log.d("MemberRepository", "成功更新成员: ${member.id} - ${member.name}")
            
            // 同步更新动态表中的用户信息
            try {
                database.dynamicDao().updateAuthorInfo(member.id, member.name, member.avatarUrl)
                database.dynamicDao().updateCommentAuthorInfo(member.id, member.name, member.avatarUrl)
                Log.d("MemberRepository", "已同步更新动态表中的用户信息: ${member.id}")
            } catch (e: Exception) {
                Log.e("MemberRepository", "同步更新动态表用户信息失败: ${e.message}", e)
            }
            
            // 同步更新投票表中的用户信息
            try {
                database.voteDao().updateVoteAuthorInfo(member.id, member.name, member.avatarUrl)
                database.voteDao().updateVoteRecordUserInfo(member.id, member.name, member.avatarUrl)
                Log.d("MemberRepository", "已同步更新投票表中的用户信息: ${member.id}")
            } catch (e: Exception) {
                Log.e("MemberRepository", "同步更新投票表用户信息失败: ${e.message}", e)
            }
        } else {
            Log.d("MemberRepository", "成功保存新成员: ${member.id} - ${member.name}")
        }
    }
    
    /**
     * 标记成员为已删除状态
     */
    suspend fun deleteMember(memberId: String) {
        val member = database.memberDao().getMemberById(memberId)
        if (member != null) {
            // 更新为已删除状态
            val updatedMember = member.copy(isDeleted = true)
            database.memberDao().insertMember(updatedMember)
            Log.d("MemberRepository", "已标记成员为删除状态: ${member.id} - ${member.name}")
        }
    }
    
    /**
     * 物理删除成员（谨慎使用）
     */
    suspend fun hardDeleteMember(memberId: String) {
        database.memberDao().deleteMemberById(memberId)
        Log.d("MemberRepository", "已物理删除成员: $memberId")
    }
} 