package com.selves.xnn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatGroup(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val members: List<Member>,
    val ownerId: String,
    val createdAt: Long = java.lang.System.currentTimeMillis()
) : Parcelable 