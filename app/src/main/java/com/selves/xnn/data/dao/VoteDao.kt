package com.selves.xnn.data.dao

import androidx.room.*
import com.selves.xnn.data.entity.VoteEntity
import com.selves.xnn.data.entity.VoteOptionEntity
import com.selves.xnn.data.entity.VoteRecordEntity
import com.selves.xnn.model.VoteStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface VoteDao {
    
    // 投票相关操作
    @Query("SELECT * FROM votes ORDER BY createdAt DESC")
    fun getAllVotes(): Flow<List<VoteEntity>>
    
    @Query("SELECT * FROM votes WHERE id = :id")
    suspend fun getVoteById(id: String): VoteEntity?
    
    @Query("SELECT * FROM votes WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getVotesByAuthor(authorId: String): Flow<List<VoteEntity>>
    
    @Query("SELECT * FROM votes WHERE status = :status ORDER BY createdAt DESC")
    fun getVotesByStatus(status: VoteStatus): Flow<List<VoteEntity>>
    
    @Query("SELECT * FROM votes WHERE endTime > :currentTime OR endTime IS NULL ORDER BY createdAt DESC")
    fun getActiveVotes(currentTime: Long): Flow<List<VoteEntity>>
    
    @Query("SELECT * FROM votes WHERE endTime <= :currentTime AND endTime IS NOT NULL ORDER BY createdAt DESC")
    fun getEndedVotes(currentTime: Long): Flow<List<VoteEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: VoteEntity)
    
    @Update
    suspend fun updateVote(vote: VoteEntity)
    
    @Delete
    suspend fun deleteVote(vote: VoteEntity)
    
    @Query("DELETE FROM votes WHERE id = :id")
    suspend fun deleteVoteById(id: String)
    
    @Query("UPDATE votes SET totalVotes = :totalVotes WHERE id = :voteId")
    suspend fun updateVoteTotalVotes(voteId: String, totalVotes: Int)
    
    @Query("UPDATE votes SET status = :status WHERE id = :voteId")
    suspend fun updateVoteStatus(voteId: String, status: VoteStatus)
    
    // 投票选项相关操作
    @Query("SELECT * FROM vote_options WHERE voteId = :voteId ORDER BY orderIndex ASC")
    fun getVoteOptions(voteId: String): Flow<List<VoteOptionEntity>>
    
    @Query("SELECT * FROM vote_options WHERE voteId = :voteId ORDER BY orderIndex ASC")
    suspend fun getVoteOptionsSync(voteId: String): List<VoteOptionEntity>
    
    @Query("SELECT * FROM vote_options WHERE id = :id")
    suspend fun getVoteOptionById(id: String): VoteOptionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoteOption(option: VoteOptionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoteOptions(options: List<VoteOptionEntity>)
    
    @Update
    suspend fun updateVoteOption(option: VoteOptionEntity)
    
    @Delete
    suspend fun deleteVoteOption(option: VoteOptionEntity)
    
    @Query("DELETE FROM vote_options WHERE voteId = :voteId")
    suspend fun deleteVoteOptionsByVoteId(voteId: String)
    
    @Query("UPDATE vote_options SET voteCount = :voteCount WHERE id = :optionId")
    suspend fun updateVoteOptionCount(optionId: String, voteCount: Int)
    
    // 投票记录相关操作
    @Query("SELECT * FROM vote_records WHERE voteId = :voteId ORDER BY votedAt DESC")
    fun getVoteRecords(voteId: String): Flow<List<VoteRecordEntity>>
    
    @Query("SELECT * FROM vote_records WHERE voteId = :voteId AND userId = :userId")
    suspend fun getUserVoteRecords(voteId: String, userId: String): List<VoteRecordEntity>
    
    @Query("SELECT * FROM vote_records WHERE optionId = :optionId ORDER BY votedAt DESC")
    fun getVoteRecordsByOption(optionId: String): Flow<List<VoteRecordEntity>>
    
    @Query("SELECT * FROM vote_records WHERE userId = :userId ORDER BY votedAt DESC")
    fun getVoteRecordsByUser(userId: String): Flow<List<VoteRecordEntity>>
    
    @Query("SELECT COUNT(*) FROM vote_records WHERE voteId = :voteId AND userId = :userId")
    suspend fun hasUserVoted(voteId: String, userId: String): Int
    
    @Query("SELECT COUNT(*) FROM vote_records WHERE optionId = :optionId")
    suspend fun getOptionVoteCount(optionId: String): Int
    
    @Query("SELECT COUNT(*) FROM vote_records WHERE voteId = :voteId")
    suspend fun getVoteTotalCount(voteId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoteRecord(record: VoteRecordEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoteRecords(records: List<VoteRecordEntity>)
    
    @Delete
    suspend fun deleteVoteRecord(record: VoteRecordEntity)
    
    @Query("DELETE FROM vote_records WHERE voteId = :voteId AND userId = :userId")
    suspend fun deleteUserVoteRecords(voteId: String, userId: String)
    
    @Query("DELETE FROM vote_records WHERE optionId = :optionId")
    suspend fun deleteVoteRecordsByOption(optionId: String)
    
    // 统计查询
    @Query("SELECT COUNT(*) FROM votes")
    suspend fun getTotalVoteCount(): Int
    
    @Query("SELECT COUNT(*) FROM votes WHERE status = :status")
    suspend fun getVoteCountByStatus(status: VoteStatus): Int
    
    @Query("SELECT COUNT(*) FROM votes WHERE authorId = :authorId")
    suspend fun getVoteCountByAuthor(authorId: String): Int
    
    @Query("SELECT COUNT(DISTINCT voteId) FROM vote_records WHERE userId = :userId")
    suspend fun getParticipatedVoteCount(userId: String): Int
    
    // 搜索功能
    @Query("SELECT * FROM votes WHERE title LIKE :query OR description LIKE :query ORDER BY createdAt DESC")
    fun searchVotes(query: String): Flow<List<VoteEntity>>

    // 备份用的同步查询方法
    @Query("SELECT * FROM votes ORDER BY createdAt ASC")
    suspend fun getAllVotesSync(): List<VoteEntity>

    @Query("SELECT * FROM vote_options ORDER BY voteId ASC, orderIndex ASC")
    suspend fun getAllVoteOptionsSync(): List<VoteOptionEntity>

    @Query("SELECT * FROM vote_records ORDER BY voteId ASC, votedAt ASC")
    suspend fun getAllVoteRecordsSync(): List<VoteRecordEntity>

    @Query("DELETE FROM votes")
    suspend fun deleteAllVotes()

    @Query("DELETE FROM vote_options")
    suspend fun deleteAllVoteOptions()

    @Query("DELETE FROM vote_records")
    suspend fun deleteAllVoteRecords()
} 