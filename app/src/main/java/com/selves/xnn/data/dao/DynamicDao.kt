package com.selves.xnn.data.dao

import androidx.room.*
import com.selves.xnn.data.entity.DynamicEntity
import com.selves.xnn.data.entity.DynamicCommentEntity
import com.selves.xnn.data.entity.DynamicLikeEntity
import com.selves.xnn.model.DynamicType
import kotlinx.coroutines.flow.Flow

@Dao
interface DynamicDao {
    
    // 动态相关操作
    @Query("SELECT * FROM dynamics ORDER BY createdAt DESC")
    fun getAllDynamics(): Flow<List<DynamicEntity>>
    
    @Query("SELECT * FROM dynamics WHERE id = :id")
    suspend fun getDynamicById(id: String): DynamicEntity?
    
    @Query("SELECT * FROM dynamics WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getDynamicsByAuthor(authorId: String): Flow<List<DynamicEntity>>
    
    @Query("SELECT * FROM dynamics WHERE type = :type ORDER BY createdAt DESC")
    fun getDynamicsByType(type: DynamicType): Flow<List<DynamicEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDynamic(dynamic: DynamicEntity)
    
    @Update
    suspend fun updateDynamic(dynamic: DynamicEntity)
    
    @Delete
    suspend fun deleteDynamic(dynamic: DynamicEntity)
    
    @Query("DELETE FROM dynamics WHERE id = :id")
    suspend fun deleteDynamicById(id: String)
    
    // 更新点赞数
    @Query("UPDATE dynamics SET likeCount = :likeCount WHERE id = :dynamicId")
    suspend fun updateLikeCount(dynamicId: String, likeCount: Int)
    
    // 更新评论数
    @Query("UPDATE dynamics SET commentCount = :commentCount WHERE id = :dynamicId")
    suspend fun updateCommentCount(dynamicId: String, commentCount: Int)
    
    // 评论相关操作
    @Query("SELECT * FROM dynamic_comments WHERE dynamicId = :dynamicId ORDER BY createdAt ASC")
    fun getCommentsByDynamicId(dynamicId: String): Flow<List<DynamicCommentEntity>>
    
    @Query("SELECT * FROM dynamic_comments WHERE parentCommentId = :parentId ORDER BY createdAt ASC")
    fun getRepliesByParentId(parentId: String): Flow<List<DynamicCommentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: DynamicCommentEntity)
    
    @Delete
    suspend fun deleteComment(comment: DynamicCommentEntity)
    
    @Query("DELETE FROM dynamic_comments WHERE id = :id")
    suspend fun deleteCommentById(id: String)
    
    @Query("SELECT COUNT(*) FROM dynamic_comments WHERE dynamicId = :dynamicId")
    suspend fun getCommentCount(dynamicId: String): Int
    
    // 点赞相关操作
    @Query("SELECT * FROM dynamic_likes WHERE dynamicId = :dynamicId AND userId = :userId")
    suspend fun getLike(dynamicId: String, userId: String): DynamicLikeEntity?
    
    @Query("SELECT * FROM dynamic_likes WHERE dynamicId = :dynamicId")
    fun getLikesByDynamicId(dynamicId: String): Flow<List<DynamicLikeEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: DynamicLikeEntity)
    
    @Delete
    suspend fun deleteLike(like: DynamicLikeEntity)
    
    @Query("DELETE FROM dynamic_likes WHERE dynamicId = :dynamicId AND userId = :userId")
    suspend fun deleteLikeByIds(dynamicId: String, userId: String)
    
    @Query("SELECT COUNT(*) FROM dynamic_likes WHERE dynamicId = :dynamicId")
    suspend fun getLikeCount(dynamicId: String): Int
    
    // 搜索功能
    @Query("SELECT * FROM dynamics WHERE title LIKE :query OR content LIKE :query ORDER BY createdAt DESC")
    fun searchDynamics(query: String): Flow<List<DynamicEntity>>

    // 备份用的同步查询方法
    @Query("SELECT * FROM dynamics ORDER BY createdAt ASC")
    suspend fun getAllDynamicsSync(): List<DynamicEntity>

    @Query("SELECT * FROM dynamic_comments ORDER BY createdAt ASC")
    suspend fun getAllCommentsSync(): List<DynamicCommentEntity>

    @Query("SELECT * FROM dynamic_likes ORDER BY createdAt ASC")
    suspend fun getAllLikesSync(): List<DynamicLikeEntity>

    @Query("DELETE FROM dynamics")
    suspend fun deleteAllDynamics()

    @Query("DELETE FROM dynamic_comments")
    suspend fun deleteAllComments()

    @Query("DELETE FROM dynamic_likes")
    suspend fun deleteAllLikes()

    // 用户信息同步更新相关方法
    @Query("UPDATE dynamics SET authorName = :newName, authorAvatar = :newAvatar WHERE authorId = :userId")
    suspend fun updateAuthorInfo(userId: String, newName: String, newAvatar: String?)

    @Query("UPDATE dynamic_comments SET authorName = :newName, authorAvatar = :newAvatar WHERE authorId = :userId")  
    suspend fun updateCommentAuthorInfo(userId: String, newName: String, newAvatar: String?)
} 