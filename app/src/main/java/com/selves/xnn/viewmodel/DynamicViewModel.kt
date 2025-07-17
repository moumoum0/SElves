package com.selves.xnn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.repository.DynamicRepository
import com.selves.xnn.model.Dynamic
import com.selves.xnn.model.DynamicComment
import com.selves.xnn.model.DynamicType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DynamicViewModel @Inject constructor(
    private val dynamicRepository: DynamicRepository
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(DynamicUiState())
    val uiState: StateFlow<DynamicUiState> = _uiState.asStateFlow()
    
    // 动态列表
    private val _dynamics = MutableStateFlow<List<Dynamic>>(emptyList())
    val dynamics: StateFlow<List<Dynamic>> = _dynamics.asStateFlow()
    
    // 当前用户ID
    private val _currentUserId = MutableStateFlow<String?>(null)
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 过滤类型
    private val _filterType = MutableStateFlow<DynamicType?>(null)
    val filterType: StateFlow<DynamicType?> = _filterType.asStateFlow()
    
    init {
        loadDynamics()
    }
    
    // 设置当前用户
    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
        loadDynamics()
    }
    
    // 加载动态列表
    private fun loadDynamics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 根据搜索查询和过滤类型加载动态
                val dynamicsFlow = when {
                    _searchQuery.value.isNotEmpty() -> {
                        dynamicRepository.searchDynamics(_searchQuery.value)
                    }
                    _filterType.value != null -> {
                        dynamicRepository.getDynamicsByType(_filterType.value!!)
                    }
                    else -> {
                        dynamicRepository.getAllDynamics()
                    }
                }
                
                dynamicsFlow.collect { dynamicList ->
                    // 更新点赞状态
                    val updatedDynamics = dynamicList.map { dynamic ->
                        val isLiked = _currentUserId.value?.let { userId ->
                            dynamicRepository.isLikedByUser(dynamic.id, userId)
                        } ?: false
                        
                        dynamic.copy(isLiked = isLiked)
                    }
                    
                    _dynamics.value = updatedDynamics
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "加载动态失败: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    // 创建动态
    fun createDynamic(
        title: String,
        content: String,
        authorName: String,
        authorAvatar: String?,
        type: DynamicType,
        images: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            try {
                dynamicRepository.createDynamic(
                    title = title,
                    content = content,
                    authorId = userId,
                    authorName = authorName,
                    authorAvatar = authorAvatar,
                    type = type,
                    images = images,
                    tags = tags
                )
                
                _uiState.update { it.copy(showCreateDialog = false) }
                clearError()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "创建动态失败: ${e.message}") 
                }
            }
        }
    }
    
    // 删除动态
    fun deleteDynamic(dynamicId: String) {
        viewModelScope.launch {
            try {
                dynamicRepository.deleteDynamic(dynamicId)
                clearError()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "删除动态失败: ${e.message}") 
                }
            }
        }
    }
    
    // 切换点赞状态
    fun toggleLike(dynamicId: String) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            try {
                dynamicRepository.toggleLike(dynamicId, userId)
                clearError()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "操作失败: ${e.message}") 
                }
            }
        }
    }
    
    // 添加评论
    fun addComment(
        dynamicId: String,
        content: String,
        authorName: String,
        authorAvatar: String?,
        parentCommentId: String? = null
    ) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            try {
                dynamicRepository.addComment(
                    dynamicId = dynamicId,
                    content = content,
                    authorId = userId,
                    authorName = authorName,
                    authorAvatar = authorAvatar,
                    parentCommentId = parentCommentId
                )
                
                clearError()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "添加评论失败: ${e.message}") 
                }
            }
        }
    }
    
    // 获取评论
    fun getComments(dynamicId: String): Flow<List<DynamicComment>> {
        return dynamicRepository.getCommentsByDynamicId(dynamicId)
    }
    
    // 删除评论
    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                dynamicRepository.deleteComment(commentId)
                clearError()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "删除评论失败: ${e.message}") 
                }
            }
        }
    }
    
    // 搜索动态
    fun searchDynamics(query: String) {
        _searchQuery.value = query
        loadDynamics()
    }
    
    // 设置过滤类型
    fun setFilterType(type: DynamicType?) {
        _filterType.value = type
        loadDynamics()
    }
    
    // 清除搜索和过滤
    fun clearFilters() {
        _searchQuery.value = ""
        _filterType.value = null
        loadDynamics()
    }
    
    // 显示创建对话框
    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }
    
    // 隐藏创建对话框
    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }
    
    // 清除错误
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // 刷新动态
    fun refresh() {
        loadDynamics()
    }
}

// UI状态数据类
data class DynamicUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false
) 