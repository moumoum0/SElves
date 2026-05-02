package com.selves.xnn.model

data class MemberDiary(
    val id: String,
    val memberId: String,
    val title: String = "",
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
