package com.selves.xnn.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.selves.xnn.model.VoteStatus
import java.time.LocalDateTime

@Entity(
    tableName = "votes",
    indices = [
        Index("authorId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val createdAt: LocalDateTime,
    val endTime: LocalDateTime?,
    val status: VoteStatus,
    val allowMultipleChoice: Boolean = false,
    val isAnonymous: Boolean = false,
    val totalVotes: Int = 0
)

@Entity(
    tableName = "vote_options",
    indices = [
        Index("voteId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = VoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["voteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VoteOptionEntity(
    @PrimaryKey
    val id: String,
    val voteId: String,
    val content: String,
    val voteCount: Int = 0,
    val orderIndex: Int = 0
)

@Entity(
    tableName = "vote_records",
    indices = [
        Index("voteId"),
        Index("optionId"),
        Index("userId"),
        Index(value = ["voteId", "userId", "optionId"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = VoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["voteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VoteOptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["optionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VoteRecordEntity(
    @PrimaryKey
    val id: String,
    val voteId: String,
    val optionId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val votedAt: LocalDateTime
) 