package com.example.myapplication.data

import com.example.myapplication.data.dao.MemberDao
import com.example.myapplication.data.entity.ChatGroupEntity
import com.example.myapplication.data.entity.MessageEntity
import com.example.myapplication.data.entity.MemberEntity
import com.example.myapplication.data.entity.TodoEntity
import com.example.myapplication.model.ChatGroup
import com.example.myapplication.model.Message
import com.example.myapplication.model.Member
import com.example.myapplication.model.MessageType
import com.example.myapplication.model.Todo
import com.example.myapplication.model.TodoPriority
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
            members = members,
            ownerId = ownerId,
            createdAt = createdAt
        )
    }
    
    // 群聊映射
    fun ChatGroup.toEntity(): ChatGroupEntity = ChatGroupEntity(
        id = id,
        name = name,
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
} 