package com.selves.xnn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Member(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val isDeleted: Boolean = false,
    val bio: String = "",
    val pronouns: String = "",
    val groups: List<String> = emptyList()
) : Parcelable 