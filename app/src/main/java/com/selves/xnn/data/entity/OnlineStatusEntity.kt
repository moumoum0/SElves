package com.selves.xnn.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "online_status",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memberId"])]
)
data class OnlineStatusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: String,
    val loginTime: Long,
    val logoutTime: Long? = null, // null表示仍在线
    val duration: Long = 0 // 在线时长（毫秒）
) 