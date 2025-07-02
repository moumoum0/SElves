package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_read_status",
    indices = [
        Index("messageId"),
        Index("memberId"),
        Index(value = ["messageId", "memberId"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageReadStatusEntity(
    @PrimaryKey
    val id: String,
    val messageId: String,
    val memberId: String,
    val readAt: Long = System.currentTimeMillis()
) 