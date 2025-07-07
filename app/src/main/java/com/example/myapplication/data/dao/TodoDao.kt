package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    
    // 获取所有共享的待办事项
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun getAllPendingTodos(): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getAllCompletedTodos(): Flow<List<TodoEntity>>
    
    // 获取所有待办事项的统计信息
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0")
    suspend fun getAllPendingTodoCount(): Int
    
    // 保留原有的按成员ID查询的方法（可能其他地方还需要用到）
    @Query("SELECT * FROM todos WHERE createdBy = :memberId ORDER BY createdAt DESC")
    fun getTodosByMemberId(memberId: String): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE createdBy = :memberId AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getPendingTodosByMemberId(memberId: String): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE createdBy = :memberId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTodosByMemberId(memberId: String): Flow<List<TodoEntity>>
    
    @Query("SELECT COUNT(*) FROM todos WHERE createdBy = :memberId AND isCompleted = 0")
    suspend fun getPendingTodoCount(memberId: String): Int
    
    // 基本操作
    @Query("SELECT * FROM todos WHERE id = :todoId")
    suspend fun getTodoById(todoId: String): TodoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<TodoEntity>)
    
    @Update
    suspend fun updateTodo(todo: TodoEntity)
    
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
    
    @Query("DELETE FROM todos WHERE id = :todoId")
    suspend fun deleteTodoById(todoId: String)
    
    @Query("UPDATE todos SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :todoId")
    suspend fun updateTodoStatus(todoId: String, isCompleted: Boolean, completedAt: Long?)
} 