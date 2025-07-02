package com.example.myapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatGroup(
    val id: String,
    val name: String,
    val members: List<Member>,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable 