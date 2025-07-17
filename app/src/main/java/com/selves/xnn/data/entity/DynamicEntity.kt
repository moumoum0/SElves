package com.selves.xnn.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.selves.xnn.model.DynamicType
import java.time.LocalDateTime

@Entity(tableName = "dynamics")
data class DynamicEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val type: DynamicType,
    val images: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val tags: List<String> = emptyList()
)

@Entity(tableName = "dynamic_comments")
data class DynamicCommentEntity(
    @PrimaryKey
    val id: String,
    val dynamicId: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val createdAt: LocalDateTime,
    val parentCommentId: String? = null
)

@Entity(tableName = "dynamic_likes")
data class DynamicLikeEntity(
    @PrimaryKey
    val id: String,
    val dynamicId: String,
    val userId: String,
    val createdAt: LocalDateTime
)

 