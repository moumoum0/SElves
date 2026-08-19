package com.selves.xnn.model

data class Member(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val isDeleted: Boolean = false,
    val bio: String = "",
    val pronouns: String = "",
    val groups: List<String> = emptyList(),
    /** 系统管理员：可删成员、导入备份等 */
    val isAdmin: Boolean = false
)