package com.selves.xnn.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "member_groups")
data class MemberGroupEntity(
    @PrimaryKey
    val name: String,
    val description: String = ""
)
