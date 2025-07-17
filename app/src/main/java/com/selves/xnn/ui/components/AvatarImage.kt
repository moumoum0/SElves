package com.selves.xnn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.selves.xnn.util.ImageUtils

/**
 * 优化的头像组件，支持快速加载和占位符
 */
@Composable
fun AvatarImage(
    avatarUrl: String?,
    contentDescription: String = "头像",
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    // 创建优化的图片请求
    val imageRequest = remember(avatarUrl) {
        ImageUtils.createOptimizedImageRequest(context, avatarUrl)
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), // 始终显示圆形背景
        contentAlignment = Alignment.Center
    ) {
        if (imageRequest != null && !hasError) {
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onState = { state ->
                    isLoading = state is AsyncImagePainter.State.Loading
                    hasError = state is AsyncImagePainter.State.Error
                }
            )
        }
        
        // 显示占位符图标（当加载中、出错或没有头像时）
        if (isLoading || hasError || imageRequest == null) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                modifier = Modifier.padding(8.dp) // 使用固定的8.dp内边距，与原来一致
            )
        }
    }
}

/**
 * 带背景的头像组件，适用于聊天消息
 */
@Composable
fun MessageAvatarImage(
    avatarUrl: String?,
    contentDescription: String = "头像",
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    AvatarImage(
        avatarUrl = avatarUrl,
        contentDescription = contentDescription,
        size = size,
        modifier = modifier
    )
}

/**
 * 大尺寸头像组件，适用于用户资料
 */
@Composable
fun ProfileAvatarImage(
    avatarUrl: String?,
    contentDescription: String = "头像",
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    // 创建优化的图片请求
    val imageRequest = remember(avatarUrl) {
        ImageUtils.createOptimizedImageRequest(context, avatarUrl)
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), // 始终显示圆形背景
        contentAlignment = Alignment.Center
    ) {
        if (imageRequest != null && !hasError) {
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onState = { state ->
                    isLoading = state is AsyncImagePainter.State.Loading
                    hasError = state is AsyncImagePainter.State.Error
                }
            )
        }
        
        // 显示占位符图标 - 大尺寸头像使用16.dp内边距
        if (isLoading || hasError || imageRequest == null) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                modifier = Modifier.padding(16.dp) // 大尺寸头像用16.dp内边距
            )
        }
    }
} 