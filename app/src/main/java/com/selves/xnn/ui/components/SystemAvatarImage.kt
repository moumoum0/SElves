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
 * 系统头像组件，使用与用户头像相同的默认图标
 */
@Composable
fun SystemAvatarImage(
    avatarUrl: String?,
    contentDescription: String = "系统头像",
    size: Dp = 60.dp,
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
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
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
        
        // 显示占位符图标（使用与用户头像相同的Person图标）
        if (isLoading || hasError || imageRequest == null) {
            val padding = when {
                size >= 80.dp -> 16.dp
                size >= 60.dp -> 12.dp
                else -> 8.dp
            }
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                modifier = Modifier.padding(padding),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 