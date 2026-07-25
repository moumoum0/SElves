package com.selves.xnn.data

/**
 * 面向 Web 端的 DTO 定义，字段名/类型与 web/src/types/models.ts 完全对齐。
 * LocalDateTime 统一转为 epoch milliseconds (Long)。
 * 图片路径统一转为 base64 data URI 或 null。
 */

// ===== Member =====
data class MemberDto(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val isDeleted: Boolean = false,
    val bio: String = "",
    val pronouns: String = "",
    val groups: List<String> = emptyList(),
    val isAdmin: Boolean = false
)

// ===== SystemInfo =====
data class SystemInfoDto(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val description: String = "",
    val createdAt: Long,
    val updatedAt: Long
)

// ===== ChatGroup =====
data class ChatGroupDto(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val members: List<MemberDto>,
    val ownerId: String,
    val createdAt: Long
)

// ===== Message =====
data class MessageDto(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val type: String, // "TEXT" | "IMAGE"
    val imagePath: String? = null,
    val imageData: String? = null, // base64 data URI for image messages
    val groupId: String? = null   // 所属群聊 ID，WebSocket 广播时携带，供前端归属消息
)

// ===== Todo =====
data class TodoDto(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val completedAt: Long? = null,
    val priority: String, // "LOW" | "NORMAL" | "HIGH"
    val createdBy: String
)

// ===== Dynamic =====
data class DynamicDto(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val type: String, // "TEXT" | "IMAGE" | "MIXED"
    val images: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val tags: List<String> = emptyList()
)

// ===== DynamicComment =====
data class DynamicCommentDto(
    val id: String,
    val dynamicId: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val createdAt: Long,
    val parentCommentId: String? = null
)

// ===== Vote =====
data class VoteDto(
    val id: String,
    val title: String,
    val description: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val createdAt: Long,
    val endTime: String, // ISO-8601 string or empty
    val isActive: Boolean,
    val allowMultipleChoice: Boolean = false,
    val isAnonymous: Boolean = false,
    val options: List<VoteOptionDto> = emptyList(),
    val totalVotes: Int = 0,
    val hasVoted: Boolean = false
)

data class VoteOptionDto(
    val id: String,
    val voteId: String,
    val content: String,
    val voteCount: Int = 0,
    val percentage: Double = 0.0,
    val isSelected: Boolean = false
)

data class VoteRecordDto(
    val id: String,
    val voteId: String,
    val optionId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val votedAt: Long
)

// ===== MemberDiary =====
data class MemberDiaryDto(
    val id: String,
    val memberId: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

// ===== TrackingSummary =====
data class TrackingSummaryDto(
    val status: String, // "RECORDING" | "STOPPED"
    val todayRecords: Int = 0,
    val totalRecords: Int = 0,
    val lastRecordTime: String = ""
)
