package com.selves.xnn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.selves.xnn.util.ImageUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * 独立的图片查看器组件
 * 
 * @param imagePath 图片路径
 * @param onBack 返回按钮点击事件
 * @param onDismiss 点击空白区域关闭事件
 * @param showAsDialog 是否以Dialog形式显示，默认为true
 * @param senderName 发送者名称，可选
 * @param timestamp 时间戳，可选
 */
@Composable
fun ImageViewer(
    imagePath: String,
    onBack: () -> Unit,
    onDismiss: () -> Unit = onBack,
    showAsDialog: Boolean = true,
    senderName: String? = null,
    timestamp: Long? = null
) {
    val content = @Composable {
        ImageViewerContent(
            imagePath = imagePath,
            onBack = onBack,
            onDismiss = onDismiss,
            senderName = senderName,
            timestamp = timestamp
        )
    }
    
    if (showAsDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = true
            )
        ) {
            content()
        }
    } else {
        content()
    }
}

@Composable
private fun ImageViewerContent(
    imagePath: String,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    senderName: String? = null,
    timestamp: Long? = null
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onDismiss() }
                )
            }
    ) {
        // 图片内容 - 全屏显示
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val imageRequest = remember(imagePath) {
                ImageUtils.createMessageImageRequest(context, imagePath)
            }
            
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "预览图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "图片加载失败",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        // 左上角返回按钮
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 右上角用户信息（如果提供了的话）
        if (senderName != null && timestamp != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = senderName,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatTimestamp(timestamp),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Long): String {
    return com.selves.xnn.util.TimeFormatter.formatDetailDateTime(timestamp)
} 