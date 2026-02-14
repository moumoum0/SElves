package com.selves.xnn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.repository.TodoRepository
import com.selves.xnn.data.repository.MemberRepository
import com.selves.xnn.model.Todo
import com.selves.xnn.model.TodoPriority
import com.selves.xnn.model.Member
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {
    
    private val TAG = "TodoViewModel"
    
    // 当前成员ID（用于创建待办事项时记录创建者）
    private val _currentMemberId = MutableStateFlow<String?>(null)
    val currentMemberId: StateFlow<String?> = _currentMemberId.asStateFlow()
    
    // 所有共享的待办事项
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()
    
    // 待办事项列表（按状态分组）
    val pendingTodos: StateFlow<List<Todo>> = _todos.map { todos ->
        todos.filter { !it.isCompleted }.sortedWith(
            compareByDescending<Todo> { it.priority.ordinal }
                .thenByDescending { it.createdAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val completedTodos: StateFlow<List<Todo>> = _todos.map { todos ->
        todos.filter { it.isCompleted }.sortedByDescending { it.completedAt ?: it.createdAt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 待办事项统计
    val todoStats: StateFlow<TodoStats> = _todos.map { todos ->
        TodoStats(
            total = todos.size,
            completed = todos.count { it.isCompleted },
            pending = todos.count { !it.isCompleted },
            highPriority = todos.count { it.priority == TodoPriority.HIGH && !it.isCompleted }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoStats())
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 异常处理
    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "TodoViewModel异常: ${exception.message}", exception)
        _error.value = exception.message
        _isLoading.value = false
    }
    
    init {
        // 初始化时加载所有共享的待办事项
        loadAllTodos()
    }
    
    /**
     * 设置当前成员ID（用于创建待办事项时记录创建者）
     */
    fun setCurrentMember(memberId: String) {
        _currentMemberId.value = memberId
        // 不需要重新加载，因为待办事项是共享的
    }
    
    /**
     * 加载所有共享的待办事项
     */
    private fun loadAllTodos() {
        viewModelScope.launch(exceptionHandler) {
            _isLoading.value = true
            _error.value = null
            
            try {
                todoRepository.getAllTodos().collect { todos ->
                    _todos.value = todos
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载待办事项失败: ${e.message}", e)
                _error.value = "加载待办事项失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 创建新的待办事项
     */
    fun createTodo(
        title: String,
        description: String = "",
        priority: TodoPriority = TodoPriority.NORMAL
    ) {
        val memberId = _currentMemberId.value ?: "unknown"
        
        viewModelScope.launch(exceptionHandler) {
            try {
                val todo = Todo(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    priority = priority,
                    createdBy = memberId
                )
                
                todoRepository.saveTodo(todo)
            } catch (e: Exception) {
                Log.e(TAG, "创建待办事项失败: ${e.message}", e)
                _error.value = "创建待办事项失败: ${e.message}"
            }
        }
    }
    
    /**
     * 更新待办事项状态
     */
    fun updateTodoStatus(todoId: String, isCompleted: Boolean) {
        viewModelScope.launch(exceptionHandler) {
            try {
                todoRepository.updateTodoStatus(todoId, isCompleted)
            } catch (e: Exception) {
                Log.e(TAG, "更新待办事项状态失败: ${e.message}", e)
                _error.value = "更新待办事项状态失败: ${e.message}"
            }
        }
    }
    
    /**
     * 更新待办事项
     */
    fun updateTodo(todo: Todo) {
        viewModelScope.launch(exceptionHandler) {
            try {
                todoRepository.updateTodo(todo)
            } catch (e: Exception) {
                Log.e(TAG, "更新待办事项失败: ${e.message}", e)
                _error.value = "更新待办事项失败: ${e.message}"
            }
        }
    }
    
    /**
     * 删除待办事项
     */
    fun deleteTodo(todoId: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                todoRepository.deleteTodoById(todoId)
            } catch (e: Exception) {
                Log.e(TAG, "删除待办事项失败: ${e.message}", e)
                _error.value = "删除待办事项失败: ${e.message}"
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 刷新待办事项
     */
    fun refreshTodos() {
        loadAllTodos()
    }
    
    /**
     * 根据成员ID获取成员信息
     */
    suspend fun getMemberById(memberId: String): Member? {
        return try {
            memberRepository.getMemberByIdIncludingDeleted(memberId)
        } catch (e: Exception) {
            Log.e(TAG, "获取成员信息失败: ${e.message}", e)
            null
        }
    }
}

/**
 * 待办事项统计数据
 */
data class TodoStats(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val highPriority: Int = 0
) {
    val completionRate: Float = if (total > 0) completed.toFloat() / total else 0f
} 