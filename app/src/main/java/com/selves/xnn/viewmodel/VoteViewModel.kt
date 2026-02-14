package com.selves.xnn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.repository.VoteRepository
import com.selves.xnn.model.Vote
import com.selves.xnn.model.VoteRecord
import com.selves.xnn.model.VoteStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class VoteUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isVoting: Boolean = false
)

@HiltViewModel
class VoteViewModel @Inject constructor(
    private val voteRepository: VoteRepository
) : ViewModel() {
    
    private val TAG = "VoteViewModel"
    
    // UI状态
    private val _uiState = MutableStateFlow(VoteUiState())
    val uiState: StateFlow<VoteUiState> = _uiState.asStateFlow()
    
    // 当前用户ID
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 过滤类型
    private val _filterActive = MutableStateFlow(true)
    val filterActive: StateFlow<Boolean> = _filterActive.asStateFlow()
    
    // 投票列表
    private val _votes = MutableStateFlow<List<Vote>>(emptyList())
    val votes: StateFlow<List<Vote>> = _votes.asStateFlow()
    
    // 活跃投票
    val activeVotes: StateFlow<List<Vote>> = voteRepository.getActiveVotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 已结束投票
    val endedVotes: StateFlow<List<Vote>> = voteRepository.getEndedVotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 过滤后的投票列表
    val filteredVotes: StateFlow<List<Vote>> = combine(
        activeVotes,
        endedVotes,
        _searchQuery,
        _filterActive
    ) { active, ended, query, showActive ->
        val allVotes = if (showActive) active else ended
        
        if (query.isBlank()) {
            allVotes
        } else {
            allVotes.filter { vote ->
                vote.title.contains(query, ignoreCase = true) ||
                vote.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 投票统计
    val voteStats: StateFlow<VoteStats> = combine(
        activeVotes,
        endedVotes,
        _currentUserId
    ) { active, ended, userId ->
        val myVotes = if (userId != null) {
            (active + ended).count { it.authorId == userId }
        } else 0
        
        VoteStats(
            totalVotes = active.size + ended.size,
            activeVotes = active.size,
            endedVotes = ended.size,
            myVotes = myVotes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VoteStats())
    
    init {
        loadVotes()
    }
    
    // 设置当前用户
    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
        voteRepository.setCurrentUserId(userId)
    }
    
    // 加载投票
    private fun loadVotes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                voteRepository.getAllVotes().collect { votes ->
                    _votes.value = votes
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载投票失败", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载投票失败: ${e.message}"
                )
            }
        }
    }
    
    // 创建投票
    fun createVote(
        title: String,
        description: String,
        authorName: String,
        authorAvatar: String?,
        options: List<String>,
        endTime: LocalDateTime? = null,
        allowMultipleChoice: Boolean = false,
        isAnonymous: Boolean = false
    ) {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "请先选择用户")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)
            
            try {
                val voteId = voteRepository.createVote(
                    title = title,
                    description = description,
                    authorId = userId,
                    authorName = authorName,
                    authorAvatar = authorAvatar,
                    options = options,
                    endTime = endTime,
                    allowMultipleChoice = allowMultipleChoice,
                    isAnonymous = isAnonymous
                )
                
                _uiState.value = _uiState.value.copy(isCreating = false)
                
            } catch (e: Exception) {
                Log.e(TAG, "创建投票失败", e)
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = "创建投票失败: ${e.message}"
                )
            }
        }
    }
    
    // 投票
    fun vote(voteId: String, optionIds: List<String>, userName: String, userAvatar: String?) {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "请先选择用户")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVoting = true)
            
            try {
                val success = voteRepository.vote(
                    voteId = voteId,
                    optionIds = optionIds,
                    userId = userId,
                    userName = userName,
                    userAvatar = userAvatar
                )
                
                if (!success) {
                    _uiState.value = _uiState.value.copy(error = "投票失败")
                }
                
                _uiState.value = _uiState.value.copy(isVoting = false)
                
            } catch (e: Exception) {
                Log.e(TAG, "投票失败", e)
                _uiState.value = _uiState.value.copy(
                    isVoting = false,
                    error = "投票失败: ${e.message}"
                )
            }
        }
    }
    
    // 删除投票
    fun deleteVote(voteId: String) {
        viewModelScope.launch {
            try {
                voteRepository.deleteVote(voteId)
            } catch (e: Exception) {
                Log.e(TAG, "删除投票失败", e)
                _uiState.value = _uiState.value.copy(
                    error = "删除投票失败: ${e.message}"
                )
            }
        }
    }
    
    // 结束投票
    fun endVote(voteId: String) {
        viewModelScope.launch {
            try {
                voteRepository.endVote(voteId)
            } catch (e: Exception) {
                Log.e(TAG, "结束投票失败", e)
                _uiState.value = _uiState.value.copy(
                    error = "结束投票失败: ${e.message}"
                )
            }
        }
    }
    
    // 获取投票详情
    fun getVoteWithOptions(voteId: String): Flow<Vote?> {
        return voteRepository.getVoteWithOptions(voteId)
    }
    
    // 获取投票记录
    fun getVoteRecords(voteId: String): Flow<List<VoteRecord>> {
        return voteRepository.getVoteRecords(voteId)
    }
    
    // 搜索投票
    fun searchVotes(query: String) {
        _searchQuery.value = query
    }
    
    // 设置过滤类型
    fun setFilterActive(active: Boolean) {
        _filterActive.value = active
    }
    
    // 清除错误
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 