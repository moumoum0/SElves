package com.selves.xnn.data

import android.util.Base64
import com.selves.xnn.data.dao.MemberDao
import com.selves.xnn.data.entity.ChatGroupEntity
import com.selves.xnn.data.entity.DynamicCommentEntity
import com.selves.xnn.data.entity.DynamicEntity
import com.selves.xnn.data.entity.MessageEntity
import com.selves.xnn.data.entity.MemberEntity
import com.selves.xnn.data.entity.MemberGroupEntity
import com.selves.xnn.data.entity.MemberDiaryEntity
import com.selves.xnn.data.entity.TodoEntity
import com.selves.xnn.data.entity.SystemEntity
import com.selves.xnn.data.entity.VoteEntity
import com.selves.xnn.data.entity.VoteOptionEntity
import com.selves.xnn.data.entity.VoteRecordEntity
import com.selves.xnn.model.ChatGroup
import com.selves.xnn.model.Message
import com.selves.xnn.model.Member
import com.selves.xnn.model.MemberGroup
import com.selves.xnn.model.MessageType
import com.selves.xnn.model.Todo
import com.selves.xnn.model.TodoPriority
import com.selves.xnn.model.MemberDiary
import com.selves.xnn.model.System
import com.selves.xnn.model.VoteStatus
import kotlinx.coroutines.flow.first
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 数据映射工具类，用于在数据库实体和领域模型之间进行转换
 */
object Mappers {
    // 成员映射
    fun MemberEntity.toDomain(): Member = Member(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        isDeleted = isDeleted,
        bio = bio,
        pronouns = pronouns,
        groups = groups
    )

    fun Member.toEntity(): MemberEntity = MemberEntity(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        isDeleted = isDeleted,
        bio = bio,
        pronouns = pronouns,
        groups = groups
    )

    fun MemberGroupEntity.toDomain(): MemberGroup = MemberGroup(
        name = name,
        description = description,
        parentName = parentName
    )

    fun MemberGroup.toEntity(): MemberGroupEntity = MemberGroupEntity(
        name = name,
        description = description,
        parentName = parentName
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
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    fun System.toEntity(): SystemEntity = SystemEntity(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    // 成员日记映射
    fun MemberDiaryEntity.toDomain(): MemberDiary = MemberDiary(
        id = id,
        memberId = memberId,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun MemberDiary.toEntity(): MemberDiaryEntity = MemberDiaryEntity(
        id = id,
        memberId = memberId,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    // ===== Entity → Dto 转换（面向 Web API） =====

    private fun readFileAsBase64(path: String): String? = try {
        val file = File(path)
        if (file.exists() && file.length() < 10 * 1024 * 1024) { // 限制 10MB
            val extension = file.extension.lowercase()
            val mime = when (extension) {
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }
            "data:$mime;base64," + Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        } else null
    } catch (_: Exception) { null }

    private fun LocalDateTime.toEpochMs(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun MemberEntity.toDto(): MemberDto = MemberDto(
        id = id,
        name = name,
        avatarUrl = avatarUrl?.let { readFileAsBase64(it) ?: it },
        isDeleted = isDeleted,
        bio = bio,
        pronouns = pronouns,
        groups = groups
    )

    fun SystemEntity.toDto(): SystemInfoDto = SystemInfoDto(
        id = id,
        name = name,
        avatarUrl = avatarUrl?.let { readFileAsBase64(it) ?: it },
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    suspend fun ChatGroupEntity.toDto(memberDao: MemberDao): ChatGroupDto {
        val memberIdList = memberIds.split(",").filter { it.isNotEmpty() }
        val memberEntities = memberDao.getMembersByIds(memberIdList).first()
        return ChatGroupDto(
            id = id,
            name = name,
            avatarUrl = avatarUrl?.let { readFileAsBase64(it) ?: it },
            members = memberEntities.map { it.toDto() },
            ownerId = ownerId,
            createdAt = createdAt
        )
    }

    fun MessageEntity.toDto(groupId: String? = null): MessageDto = MessageDto(
        id = id,
        senderId = senderId,
        content = content,
        timestamp = timestamp,
        type = when (type) {
            1 -> "IMAGE"
            else -> "TEXT"
        },
        imagePath = null, // 不暴露服务器本地路径
        imageData = if (type == 1 && imagePath != null) readFileAsBase64(imagePath) else null,
        groupId = groupId
    )

    fun TodoEntity.toDto(): TodoDto = TodoDto(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt,
        completedAt = completedAt,
        priority = when (priority) {
            0 -> "LOW"
            2 -> "HIGH"
            else -> "NORMAL"
        },
        createdBy = createdBy
    )

    fun DynamicEntity.toDto(): DynamicDto = DynamicDto(
        id = id,
        title = title,
        content = content,
        authorId = authorId,
        authorName = authorName,
        authorAvatar = authorAvatar?.let { readFileAsBase64(it) ?: it },
        createdAt = createdAt.toEpochMs(),
        updatedAt = updatedAt.toEpochMs(),
        type = type.name,
        images = images.mapNotNull { readFileAsBase64(it) ?: it },
        likeCount = likeCount,
        commentCount = commentCount,
        tags = tags
    )

    fun DynamicCommentEntity.toDto(): DynamicCommentDto = DynamicCommentDto(
        id = id,
        dynamicId = dynamicId,
        content = content,
        authorId = authorId,
        authorName = authorName,
        authorAvatar = authorAvatar?.let { readFileAsBase64(it) ?: it },
        createdAt = createdAt.toEpochMs(),
        parentCommentId = parentCommentId
    )

    fun VoteEntity.toDto(
        options: List<VoteOptionDto> = emptyList(),
        hasVoted: Boolean = false
    ): VoteDto = VoteDto(
        id = id,
        title = title,
        description = description,
        authorId = authorId,
        authorName = authorName,
        authorAvatar = authorAvatar?.let { readFileAsBase64(it) ?: it },
        createdAt = createdAt.toEpochMs(),
        endTime = endTime?.toString() ?: "",
        isActive = status == VoteStatus.ACTIVE,
        allowMultipleChoice = allowMultipleChoice,
        isAnonymous = isAnonymous,
        options = options,
        totalVotes = totalVotes,
        hasVoted = hasVoted
    )

    fun VoteOptionEntity.toDto(totalVotes: Int, isSelected: Boolean = false): VoteOptionDto =
        VoteOptionDto(
            id = id,
            voteId = voteId,
            content = content,
            voteCount = voteCount,
            percentage = if (totalVotes > 0) (voteCount * 100.0 / totalVotes) else 0.0,
            isSelected = isSelected
        )

    fun VoteRecordEntity.toDto(): VoteRecordDto = VoteRecordDto(
        id = id,
        voteId = voteId,
        optionId = optionId,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar?.let { readFileAsBase64(it) ?: it },
        votedAt = votedAt.toEpochMs()
    )

    fun MemberDiaryEntity.toDto(): MemberDiaryDto = MemberDiaryDto(
        id = id,
        memberId = memberId,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
} 