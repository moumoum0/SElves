package com.selves.xnn.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "member_diaries",
    indices = [Index("memberId")]
)
data class MemberDiaryEntity(
    @PrimaryKey
    val id: String,
    val memberId: String,
    val title: String = "",
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
