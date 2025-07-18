package com.selves.xnn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long = java.lang.System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val imagePath: String? = null // 图片消息的本地路径
) : Parcelable

enum class MessageType {
    TEXT,
    IMAGE
    // 后续可以添加文件等类型
} 