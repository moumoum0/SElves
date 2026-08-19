package com.selves.xnn.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val isDeleted: Boolean = false,
    val bio: String = "",
    val pronouns: String = "",
    val groups: List<String> = emptyList(),
    val isAdmin: Boolean = false
)