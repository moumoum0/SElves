package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.Mappers.toDomain
import com.example.myapplication.data.Mappers.toEntity
import com.example.myapplication.data.dao.TodoDao
import com.example.myapplication.model.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao
) {
    private val TAG = "TodoRepository"

    // 获取所有共享的待办事项
    fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllPendingTodos(): Flow<List<Todo>> {
        return todoDao.getAllPendingTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllCompletedTodos(): Flow<List<Todo>> {
        return todoDao.getAllCompletedTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getAllPendingTodoCount(): Int {
        return todoDao.getAllPendingTodoCount()
    }

    // 保留原有的按成员ID查询的方法（向后兼容）
    fun getTodosByMemberId(memberId: String): Flow<List<Todo>> {
        return todoDao.getTodosByMemberId(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getPendingTodosByMemberId(memberId: String): Flow<List<Todo>> {
        return todoDao.getPendingTodosByMemberId(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getCompletedTodosByMemberId(memberId: String): Flow<List<Todo>> {
        return todoDao.getCompletedTodosByMemberId(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getPendingTodoCount(memberId: String): Int {
        return todoDao.getPendingTodoCount(memberId)
    }

    // 基本操作方法
    suspend fun getTodoById(todoId: String): Todo? {
        return todoDao.getTodoById(todoId)?.toDomain()
    }

    suspend fun saveTodo(todo: Todo) {
        try {
            val entity = todo.toEntity()
            todoDao.insertTodo(entity)
            Log.d(TAG, "已保存待办事项: ${todo.title}")
        } catch (e: Exception) {
            Log.e(TAG, "保存待办事项失败: ${e.message}", e)
            throw e
        }
    }

    suspend fun saveTodos(todos: List<Todo>) {
        try {
            val entities = todos.map { it.toEntity() }
            todoDao.insertTodos(entities)
            Log.d(TAG, "已批量保存 ${todos.size} 个待办事项")
        } catch (e: Exception) {
            Log.e(TAG, "批量保存待办事项失败: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateTodo(todo: Todo) {
        try {
            val entity = todo.toEntity()
            todoDao.updateTodo(entity)
            Log.d(TAG, "已更新待办事项: ${todo.title}")
        } catch (e: Exception) {
            Log.e(TAG, "更新待办事项失败: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteTodo(todo: Todo) {
        try {
            val entity = todo.toEntity()
            todoDao.deleteTodo(entity)
            Log.d(TAG, "已删除待办事项: ${todo.title}")
        } catch (e: Exception) {
            Log.e(TAG, "删除待办事项失败: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteTodoById(todoId: String) {
        try {
            todoDao.deleteTodoById(todoId)
            Log.d(TAG, "已删除待办事项: $todoId")
        } catch (e: Exception) {
            Log.e(TAG, "删除待办事项失败: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateTodoStatus(todoId: String, isCompleted: Boolean) {
        try {
            val completedAt = if (isCompleted) System.currentTimeMillis() else null
            todoDao.updateTodoStatus(todoId, isCompleted, completedAt)
            Log.d(TAG, "已更新待办事项状态: $todoId -> $isCompleted")
        } catch (e: Exception) {
            Log.e(TAG, "更新待办事项状态失败: ${e.message}", e)
            throw e
        }
    }
} 