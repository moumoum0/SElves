package com.selves.xnn.model

import java.time.LocalDateTime

data class Dynamic(
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
    val isLiked: Boolean = false,
    val tags: List<String> = emptyList()
)

enum class DynamicType {
    TEXT,       // 纯文本动态
    IMAGE,      // 图片动态
    MIXED,      // 图文混合
    LINK,       // 链接分享
    SYSTEM      // 系统通知
}

data class DynamicComment(
    val id: String,
    val dynamicId: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val createdAt: LocalDateTime,
    val parentCommentId: String? = null  // 用于回复评论
)

data class DynamicLike(
    val id: String,
    val dynamicId: String,
    val userId: String,
    val createdAt: LocalDateTime
) 