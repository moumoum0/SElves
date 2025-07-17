package com.selves.xnn.data.repository

import com.selves.xnn.data.dao.VoteDao
import com.selves.xnn.data.entity.VoteEntity
import com.selves.xnn.data.entity.VoteOptionEntity
import com.selves.xnn.data.entity.VoteRecordEntity
import com.selves.xnn.model.Vote
import com.selves.xnn.model.VoteOption
import com.selves.xnn.model.VoteRecord
import com.selves.xnn.model.VoteStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteRepository @Inject constructor(
    private val voteDao: VoteDao
) {
    
    private var currentUserId: String? = null
    
    fun setCurrentUserId(userId: String?) {
        currentUserId = userId
    }
    
    // 获取所有投票
    fun getAllVotes(): Flow<List<Vote>> {
        return voteDao.getAllVotes().map { entities ->
            entities.map { entity ->
                val options = voteDao.getVoteOptionsSync(entity.id)
                entity.toDomain(options)
            }
        }
    }
    
    // 获取投票详情（包含选项）
    fun getVoteWithOptions(voteId: String): Flow<Vote?> {
        return combine(
            voteDao.getVoteOptions(voteId),
            voteDao.getVoteRecords(voteId)
        ) { options, records ->
            val voteEntity = voteDao.getVoteById(voteId)
            voteEntity?.let { entity ->
                val userRecords = this.currentUserId?.let { userId ->
                    records.filter { it.userId == userId }
                } ?: emptyList()
                
                val voteOptions = options.map { option ->
                    val optionRecords = records.filter { it.optionId == option.id }
                    VoteOption(
                        id = option.id,
                        voteId = option.voteId,
                        content = option.content,
                        voteCount = optionRecords.size,
                        percentage = if (records.isNotEmpty()) {
                            (optionRecords.size.toFloat() / records.size) * 100
                        } else 0f,
                        isSelected = userRecords.any { it.optionId == option.id }
                    )
                }
                
                Vote(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    authorId = entity.authorId,
                    authorName = entity.authorName,
                    authorAvatar = entity.authorAvatar,
                    createdAt = entity.createdAt,
                    endTime = entity.endTime,
                    isActive = entity.status == VoteStatus.ACTIVE && 
                              (entity.endTime == null || entity.endTime.isAfter(LocalDateTime.now())),
                    allowMultipleChoice = entity.allowMultipleChoice,
                    isAnonymous = entity.isAnonymous,
                    options = voteOptions,
                    totalVotes = records.size,
                    hasVoted = userRecords.isNotEmpty()
                )
            }
        }
    }
    
    // 获取活跃投票
    fun getActiveVotes(): Flow<List<Vote>> {
        val currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        return voteDao.getActiveVotes(currentTime).map { entities ->
            entities.map { entity ->
                val options = voteDao.getVoteOptionsSync(entity.id)
                entity.toDomain(options)
            }
        }
    }
    
    // 获取已结束投票
    fun getEndedVotes(): Flow<List<Vote>> {
        val currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        return voteDao.getEndedVotes(currentTime).map { entities ->
            entities.map { entity ->
                val options = voteDao.getVoteOptionsSync(entity.id)
                entity.toDomain(options)
            }
        }
    }
    
    // 创建投票
    suspend fun createVote(
        title: String,
        description: String,
        authorId: String,
        authorName: String,
        authorAvatar: String?,
        options: List<String>,
        endTime: LocalDateTime? = null,
        allowMultipleChoice: Boolean = false,
        isAnonymous: Boolean = false
    ): String {
        val voteId = UUID.randomUUID().toString()
        
        // 创建投票
        val voteEntity = VoteEntity(
            id = voteId,
            title = title,
            description = description,
            authorId = authorId,
            authorName = authorName,
            authorAvatar = authorAvatar,
            createdAt = LocalDateTime.now(),
            endTime = endTime,
            status = VoteStatus.ACTIVE,
            allowMultipleChoice = allowMultipleChoice,
            isAnonymous = isAnonymous,
            totalVotes = 0
        )
        
        voteDao.insertVote(voteEntity)
        
        // 创建选项
        val optionEntities = options.mapIndexed { index, content ->
            VoteOptionEntity(
                id = UUID.randomUUID().toString(),
                voteId = voteId,
                content = content,
                voteCount = 0,
                orderIndex = index
            )
        }
        
        voteDao.insertVoteOptions(optionEntities)
        
        return voteId
    }
    
    // 投票
    suspend fun vote(voteId: String, optionIds: List<String>, userId: String, userName: String, userAvatar: String?): Boolean {
        val voteEntity = voteDao.getVoteById(voteId) ?: return false
        
        // 检查投票是否仍然活跃
        if (voteEntity.status != VoteStatus.ACTIVE) return false
        if (voteEntity.endTime != null && voteEntity.endTime.isBefore(LocalDateTime.now())) return false
        
        // 检查用户是否已经投票
        val existingRecords = voteDao.getUserVoteRecords(voteId, userId)
        if (existingRecords.isNotEmpty() && !voteEntity.allowMultipleChoice) {
            // 如果不允许多选且已投票，先删除之前的投票
            voteDao.deleteUserVoteRecords(voteId, userId)
        }
        
        // 添加新的投票记录
        val voteRecords = optionIds.map { optionId ->
            VoteRecordEntity(
                id = UUID.randomUUID().toString(),
                voteId = voteId,
                optionId = optionId,
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                votedAt = LocalDateTime.now()
            )
        }
        
        voteDao.insertVoteRecords(voteRecords)
        
        // 更新选项投票数
        optionIds.forEach { optionId ->
            val count = voteDao.getOptionVoteCount(optionId)
            voteDao.updateVoteOptionCount(optionId, count)
        }
        
        // 更新投票总数
        val totalCount = voteDao.getVoteTotalCount(voteId)
        voteDao.updateVoteTotalVotes(voteId, totalCount)
        
        return true
    }
    
    // 删除投票
    suspend fun deleteVote(voteId: String) {
        voteDao.deleteVoteById(voteId)
    }
    
    // 结束投票
    suspend fun endVote(voteId: String) {
        voteDao.updateVoteStatus(voteId, VoteStatus.ENDED)
    }
    
    // 获取投票记录
    fun getVoteRecords(voteId: String): Flow<List<VoteRecord>> {
        return voteDao.getVoteRecords(voteId).map { entities ->
            entities.map { entity ->
                VoteRecord(
                    id = entity.id,
                    voteId = entity.voteId,
                    optionId = entity.optionId,
                    userId = entity.userId,
                    userName = entity.userName,
                    userAvatar = entity.userAvatar,
                    votedAt = entity.votedAt
                )
            }
        }
    }
    
    // 搜索投票
    fun searchVotes(query: String): Flow<List<Vote>> {
        return voteDao.searchVotes("%$query%").map { entities ->
            entities.map { entity ->
                val options = voteDao.getVoteOptionsSync(entity.id)
                entity.toDomain(options)
            }
        }
    }
    
    // 获取用户参与的投票
    fun getUserVotes(userId: String): Flow<List<Vote>> {
        return voteDao.getVotesByAuthor(userId).map { entities ->
            entities.map { entity ->
                val options = voteDao.getVoteOptionsSync(entity.id)
                entity.toDomain(options)
            }
        }
    }
    
    // 扩展函数：将实体转换为领域模型
    private suspend fun VoteEntity.toDomain(options: List<VoteOptionEntity>): Vote {
        val voteOptions = options.map { option ->
            VoteOption(
                id = option.id,
                voteId = option.voteId,
                content = option.content,
                voteCount = option.voteCount,
                percentage = if (this.totalVotes > 0) {
                    (option.voteCount.toFloat() / this.totalVotes) * 100
                } else 0f,
                isSelected = false // 需要在具体使用时根据用户ID判断
            )
        }
        
        return Vote(
            id = this.id,
            title = this.title,
            description = this.description,
            authorId = this.authorId,
            authorName = this.authorName,
            authorAvatar = this.authorAvatar,
            createdAt = this.createdAt,
            endTime = this.endTime,
            isActive = this.status == VoteStatus.ACTIVE && 
                      (this.endTime == null || this.endTime.isAfter(LocalDateTime.now())),
            allowMultipleChoice = this.allowMultipleChoice,
            isAnonymous = this.isAnonymous,
            options = voteOptions,
            totalVotes = this.totalVotes,
            hasVoted = false // 需要在具体使用时根据用户ID判断
        )
    }
} 