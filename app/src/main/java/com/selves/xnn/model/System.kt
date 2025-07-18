package com.selves.xnn.model

data class System(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val description: String = "",
    val createdAt: Long = java.lang.System.currentTimeMillis(),
    val updatedAt: Long = java.lang.System.currentTimeMillis()
) 