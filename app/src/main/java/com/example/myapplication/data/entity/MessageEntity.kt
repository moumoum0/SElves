package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index("groupId"),
        Index("senderId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val type: Int = 0, // 0表示文本消息，1表示图片消息
    val imagePath: String? = null // 图片消息的本地路径
) 