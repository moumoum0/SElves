package com.selves.xnn.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "systems")
data class SystemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val description: String = "",
    val createdAt: Long = java.lang.System.currentTimeMillis(),
    val updatedAt: Long = java.lang.System.currentTimeMillis()
)