package com.example.myapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Todo(
    val id: String,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val priority: TodoPriority = TodoPriority.NORMAL,
    val createdBy: String // 创建者ID
) : Parcelable

enum class TodoPriority {
    LOW,
    NORMAL,
    HIGH
} 