package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "chat_groups")
data class ChatGroupEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val memberIds: String, // 存储为逗号分隔的成员ID
    val ownerId: String, // 群主ID
    val createdAt: Long
) : Parcelable 