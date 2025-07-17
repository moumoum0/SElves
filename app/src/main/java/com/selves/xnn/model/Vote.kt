package com.selves.xnn.model

import java.time.LocalDateTime

data class Vote(
    val id: String,
    val title: String,
    val description: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val createdAt: LocalDateTime,
    val endTime: LocalDateTime?,
    val isActive: Boolean = true,
    val allowMultipleChoice: Boolean = false,
    val isAnonymous: Boolean = false,
    val options: List<VoteOption> = emptyList(),
    val totalVotes: Int = 0,
    val hasVoted: Boolean = false
)

data class VoteOption(
    val id: String,
    val voteId: String,
    val content: String,
    val voteCount: Int = 0,
    val percentage: Float = 0f,
    val isSelected: Boolean = false
)

data class VoteRecord(
    val id: String,
    val voteId: String,
    val optionId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val votedAt: LocalDateTime
)

enum class VoteStatus {
    ACTIVE,     // 进行中
    ENDED,      // 已结束
    DRAFT       // 草稿
}

data class VoteStats(
    val totalVotes: Int = 0,
    val activeVotes: Int = 0,
    val endedVotes: Int = 0,
    val myVotes: Int = 0
) 