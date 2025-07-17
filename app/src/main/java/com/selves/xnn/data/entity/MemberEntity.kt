package com.selves.xnn.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val isDeleted: Boolean = false
) : Parcelable 