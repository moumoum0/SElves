package com.selves.xnn.data.repository

import com.selves.xnn.data.dao.DynamicDao
import com.selves.xnn.data.entity.DynamicEntity
import com.selves.xnn.data.entity.DynamicCommentEntity
import com.selves.xnn.data.entity.DynamicLikeEntity
import com.selves.xnn.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicRepository @Inject constructor(
    private val dynamicDao: DynamicDao
) {
    
    // 获取所有动态
    fun getAllDynamics(): Flow<List<Dynamic>> {
        return dynamicDao.getAllDynamics().map { entities ->
            entities.map { entity ->
                entity.toDynamic()
            }
        }
    }
    
    // 根据ID获取动态
    suspend fun getDynamicById(id: String): Dynamic? {
        return dynamicDao.getDynamicById(id)?.toDynamic()
    }
    
    // 根据作者获取动态
    fun getDynamicsByAuthor(authorId: String): Flow<List<Dynamic>> {
        return dynamicDao.getDynamicsByAuthor(authorId).map { entities ->
            entities.map { it.toDynamic() }
        }
    }
    
    // 根据类型获取动态
    fun getDynamicsByType(type: DynamicType): Flow<List<Dynamic>> {
        return dynamicDao.getDynamicsByType(type).map { entities ->
            entities.map { it.toDynamic() }
        }
    }
    
    // 创建动态
    suspend fun createDynamic(
        title: String,
        content: String,
        authorId: String,
        authorName: String,
        authorAvatar: String?,
        type: DynamicType,
        images: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ): String {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        
        val entity = DynamicEntity(
            id = id,
            title = title,
            content = content,
            authorId = authorId,
            authorName = authorName,
            authorAvatar = authorAvatar,
            createdAt = now,
            updatedAt = now,
            type = type,
            images = images,
            likeCount = 0,
            commentCount = 0,
            tags = tags
        )
        
        dynamicDao.insertDynamic(entity)
        return id
    }
    
    // 更新动态
    suspend fun updateDynamic(
        id: String,
        title: String? = null,
        content: String? = null,
        images: List<String>? = null,
        tags: List<String>? = null
    ) {
        val existing = dynamicDao.getDynamicById(id) ?: return
        val updated = existing.copy(
            title = title ?: existing.title,
            content = content ?: existing.content,
            images = images ?: existing.images,
            tags = tags ?: existing.tags,
            updatedAt = LocalDateTime.now()
        )
        dynamicDao.updateDynamic(updated)
    }
    
    // 删除动态
    suspend fun deleteDynamic(id: String) {
        dynamicDao.deleteDynamicById(id)
    }
    
    // 点赞/取消点赞
    suspend fun toggleLike(dynamicId: String, userId: String): Boolean {
        val existingLike = dynamicDao.getLike(dynamicId, userId)
        
        return if (existingLike != null) {
            // 取消点赞
            dynamicDao.deleteLike(existingLike)
            val newCount = dynamicDao.getLikeCount(dynamicId)
            dynamicDao.updateLikeCount(dynamicId, newCount)
            false
        } else {
            // 点赞
            val like = DynamicLikeEntity(
                id = UUID.randomUUID().toString(),
                dynamicId = dynamicId,
                userId = userId,
                createdAt = LocalDateTime.now()
            )
            dynamicDao.insertLike(like)
            val newCount = dynamicDao.getLikeCount(dynamicId)
            dynamicDao.updateLikeCount(dynamicId, newCount)
            true
        }
    }
    
    // 添加评论
    suspend fun addComment(
        dynamicId: String,
        content: String,
        authorId: String,
        authorName: String,
        authorAvatar: String?,
        parentCommentId: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val comment = DynamicCommentEntity(
            id = id,
            dynamicId = dynamicId,
            content = content,
            authorId = authorId,
            authorName = authorName,
            authorAvatar = authorAvatar,
            createdAt = LocalDateTime.now(),
            parentCommentId = parentCommentId
        )
        
        dynamicDao.insertComment(comment)
        
        // 更新评论数
        val newCount = dynamicDao.getCommentCount(dynamicId)
        dynamicDao.updateCommentCount(dynamicId, newCount)
        
        return id
    }
    
    // 获取评论
    fun getCommentsByDynamicId(dynamicId: String): Flow<List<DynamicComment>> {
        return dynamicDao.getCommentsByDynamicId(dynamicId).map { entities ->
            entities.map { it.toDynamicComment() }
        }
    }
    
    // 删除评论
    suspend fun deleteComment(commentId: String) {
        dynamicDao.deleteCommentById(commentId)
    }
    
    // 搜索动态
    fun searchDynamics(query: String): Flow<List<Dynamic>> {
        return dynamicDao.searchDynamics("%$query%").map { entities ->
            entities.map { it.toDynamic() }
        }
    }
    
    // 检查用户是否点赞了某个动态
    suspend fun isLikedByUser(dynamicId: String, userId: String): Boolean {
        return dynamicDao.getLike(dynamicId, userId) != null
    }
}

// 扩展函数：实体转换为模型
private suspend fun DynamicEntity.toDynamic(): Dynamic {
    return Dynamic(
        id = id,
        title = title,
        content = content,
        authorId = authorId,
        authorName = authorName,
        authorAvatar = authorAvatar,
        createdAt = createdAt,
        updatedAt = updatedAt,
        type = type,
        images = images,
        likeCount = likeCount,
        commentCount = commentCount,
        isLiked = false, // 需要根据当前用户动态设置
        tags = tags
    )
}

private fun DynamicCommentEntity.toDynamicComment(): DynamicComment {
    return DynamicComment(
        id = id,
        dynamicId = dynamicId,
        content = content,
        authorId = authorId,
        authorName = authorName,
        authorAvatar = authorAvatar,
        createdAt = createdAt,
        parentCommentId = parentCommentId
    )
} 