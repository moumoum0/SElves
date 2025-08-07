package com.selves.xnn.data

import com.selves.xnn.data.dao.MemberDao
import com.selves.xnn.data.entity.ChatGroupEntity
import com.selves.xnn.data.entity.MessageEntity
import com.selves.xnn.data.entity.MemberEntity
import com.selves.xnn.data.entity.TodoEntity
import com.selves.xnn.data.entity.SystemEntity
import com.selves.xnn.model.ChatGroup
import com.selves.xnn.model.Message
import com.selves.xnn.model.Member
import com.selves.xnn.model.MessageType
import com.selves.xnn.model.Todo
import com.selves.xnn.model.TodoPriority
import com.selves.xnn.model.System
import kotlinx.coroutines.flow.first

/**
 * 数据映射工具类，用于在数据库实体和领域模型之间进行转换
 */
object Mappers {
    // 成员映射
    fun MemberEntity.toDomain(): Member = Member(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        isDeleted = isDeleted
    )

    fun Member.toEntity(): MemberEntity = MemberEntity(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        isDeleted = isDeleted
    )

    suspend fun ChatGroupEntity.toDomain(memberDao: MemberDao): ChatGroup {
        val memberIds = memberIds.split(",").filter { it.isNotEmpty() }
        val members = memberDao.getMembersByIds(memberIds).first().map { it.toDomain() }
        
        return ChatGroup(
            id = id,
            name = name,
            avatarUrl = avatarUrl,
            members = members,
            ownerId = ownerId,
            createdAt = createdAt
        )
    }
    
    // 群聊映射
    fun ChatGroup.toEntity(): ChatGroupEntity = ChatGroupEntity(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        memberIds = members.map { it.id }.joinToString(","),
        ownerId = ownerId,
        createdAt = createdAt
    )
    
    // 消息映射
    fun MessageEntity.toDomain(): Message = Message(
        id = id,
        senderId = senderId,
        content = content,
        timestamp = timestamp,
        type = when(type) {
            0 -> MessageType.TEXT
            1 -> MessageType.IMAGE
            else -> MessageType.TEXT // 默认为文本消息
        },
        imagePath = imagePath
    )
    
    fun Message.toEntity(groupId: String): MessageEntity = MessageEntity(
        id = id,
        groupId = groupId,
        senderId = senderId,
        content = content,
        timestamp = timestamp,
        type = when(type) {
            MessageType.TEXT -> 0
            MessageType.IMAGE -> 1
        },
        imagePath = imagePath
    )
    
    // 待办事项映射
    fun TodoEntity.toDomain(): Todo = Todo(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt,
        completedAt = completedAt,
        priority = when(priority) {
            0 -> TodoPriority.LOW
            1 -> TodoPriority.NORMAL
            2 -> TodoPriority.HIGH
            else -> TodoPriority.NORMAL
        },
        createdBy = createdBy
    )
    
    fun Todo.toEntity(): TodoEntity = TodoEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt,
        completedAt = completedAt,
        priority = when(priority) {
            TodoPriority.LOW -> 0
            TodoPriority.NORMAL -> 1
            TodoPriority.HIGH -> 2
        },
        createdBy = createdBy
    )
    
    // 系统映射
    fun SystemEntity.toDomain(): System = System(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    fun System.toEntity(): SystemEntity = SystemEntity(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
} 