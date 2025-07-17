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
        // 先检查成员是否已存在
        val exists = database.memberDao().checkMemberExists(member.id)
        if (exists > 0) {
            // 成员已存在，记录日志
            Log.d("MemberRepository", "成员已存在，不重复保存: ${member.id} - ${member.name}")
            return
        }
        
        val entity = member.toEntity()
        database.memberDao().insertMember(entity)
        Log.d("MemberRepository", "成功保存成员: ${member.id} - ${member.name}")
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