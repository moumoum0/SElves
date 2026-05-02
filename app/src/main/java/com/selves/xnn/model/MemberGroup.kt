package com.selves.xnn.model

data class MemberGroup(
    val name: String,
    val description: String = "",
    val parentName: String? = null
)
