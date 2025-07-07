package com.example.myapplication.data.repository

import com.example.myapplication.data.Mappers.toDomain
import com.example.myapplication.data.Mappers.toEntity
import com.example.myapplication.data.dao.ChatGroupDao
import com.example.myapplication.data.dao.MemberDao
import com.example.myapplication.model.ChatGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatGroupRepository @Inject constructor(
    private val chatGroupDao: ChatGroupDao,
    private val memberDao: MemberDao
) {
    /**
     * 获取所有群聊
     */
    fun getAllGroups(): Flow<List<ChatGroup>> {
        return chatGroupDao.getAllGroups().map { entities ->
            entities.map { entity -> entity.toDomain(memberDao) }
        }
    }

    /**
     * 根据成员ID获取该成员所属的群聊
     */
    fun getGroupsByMemberId(memberId: String): Flow<List<ChatGroup>> {
        return chatGroupDao.getGroupsByMemberId(memberId).map { entities ->
            entities.map { entity -> entity.toDomain(memberDao) }
                .filter { group -> 
                    // 进一步过滤，确保成员确实在群聊中
                    group.members.any { it.id == memberId }
                }
        }
    }

    /**
     * 通过ID获取特定群聊
     */
    suspend fun getGroupById(groupId: String): ChatGroup? {
        return chatGroupDao.getGroupById(groupId)?.toDomain(memberDao)
    }

    /**
     * 保存一个群聊
     */
    suspend fun saveGroup(group: ChatGroup) {
        chatGroupDao.insertGroup(group.toEntity())
    }

    /**
     * 保存多个群聊
     */
    suspend fun saveGroups(groups: List<ChatGroup>) {
        chatGroupDao.insertGroups(groups.map { it.toEntity() })
    }

    /**
     * 更新群聊信息
     */
    suspend fun updateGroup(group: ChatGroup) {
        chatGroupDao.updateGroup(group.toEntity())
    }

    /**
     * 删除群聊
     */
    suspend fun deleteGroup(group: ChatGroup) {
        chatGroupDao.deleteGroup(group.toEntity())
    }

    /**
     * 通过ID删除群聊
     */
    suspend fun deleteGroupById(groupId: String) {
        chatGroupDao.deleteGroupById(groupId)
    }
} 