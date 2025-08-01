package com.selves.xnn.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.selves.xnn.util.ImageUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 全屏图片查看器组件
 * 
 * @param imagePath 图片路径
 * @param onBack 返回按钮点击事件
 * @param onDismiss 点击空白区域关闭事件
 * @param senderName 发送者名称，可选
 * @param timestamp 时间戳，可选
 * @param startPosition 动画起始位置，可选
 * @param startSize 动画起始大小，可选
 */
@Composable
fun ImageViewer(
    imagePath: String,
    onBack: () -> Unit,
    onDismiss: () -> Unit = onBack,
    senderName: String? = null,
    timestamp: Long? = null,
    startPosition: Offset? = null,
    startSize: androidx.compose.ui.unit.DpSize? = null
) {
    val systemUiController = rememberSystemUiController()
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    
    // 记住原始的系统UI颜色
    val originalStatusBarColor = remember { Color.White }
    val originalNavigationBarColor = remember { surfaceVariantColor }
    
    // 动画状态
    var animationStarted by remember { mutableStateOf(false) }
    
    // 设置全屏透明模式
    LaunchedEffect(Unit) {
        // 进入时设置透明
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            // 退出时恢复原色
            systemUiController.setStatusBarColor(
                color = originalStatusBarColor,
                darkIcons = true
            )
            systemUiController.setNavigationBarColor(
                color = originalNavigationBarColor,
                darkIcons = false
            )
        }
    }
    
    // 启动动画
    LaunchedEffect(Unit) {
        delay(50) // 短暂延迟确保初始状态设置完成
        animationStarted = true
    }
    
    ImageViewerContent(
        imagePath = imagePath,
        onBack = onBack,
        onDismiss = onDismiss,
        senderName = senderName,
        timestamp = timestamp,
        startPosition = startPosition,
        startSize = startSize,
        animationStarted = animationStarted
    )
}

@Composable
private fun ImageViewerContent(
    imagePath: String,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    senderName: String? = null,
    timestamp: Long? = null,
    startPosition: Offset? = null,
    startSize: androidx.compose.ui.unit.DpSize? = null,
    animationStarted: Boolean
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    
    // 屏幕尺寸
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val screenCenterX = screenWidth / 2
    val screenCenterY = screenHeight / 2
    
    // 计算初始位置偏移（相对于屏幕中心）
    val initialOffsetX = startPosition?.let { 
        // 从图片中心开始动画，所以要加上图片宽度的一半
        val imageCenterX = with(density) { it.x.toDp() } + (startSize?.width ?: 0.dp) / 2
        imageCenterX - screenCenterX 
    } ?: 0.dp
    
    val initialOffsetY = startPosition?.let { 
        // 从图片中心开始动画，所以要加上图片高度的一半
        val imageCenterY = with(density) { it.y.toDp() } + (startSize?.height ?: 0.dp) / 2
        imageCenterY - screenCenterY 
    } ?: 0.dp
    
    // 计算初始缩放比例
    val initialScale = startSize?.let { 
        // 使用较小的维度来计算缩放比例，确保图片完全可见
        minOf(
            it.width / screenWidth, 
            it.height / screenHeight
        )
    } ?: 0.1f
    
    // 位置动画
    val animatedOffsetX by animateDpAsState(
        targetValue = if (animationStarted) 0.dp else initialOffsetX,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "offsetX"
    )
    
    val animatedOffsetY by animateDpAsState(
        targetValue = if (animationStarted) 0.dp else initialOffsetY,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "offsetY"
    )
    
    // 缩放动画
    val animatedScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else initialScale,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    // 背景透明度动画
    val animatedBackgroundAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "backgroundAlpha"
    )
    
    // 内容透明度动画（返回按钮和用户信息）
    val animatedContentAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = animatedBackgroundAlpha))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onDismiss() }
                )
            }
    ) {
        // 图片内容 - 带动画的显示
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = animatedOffsetX, y = animatedOffsetY)
                .scale(animatedScale),
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
        
        // 左上角返回按钮 - 带淡入动画
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 8.dp, top = 8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f * animatedContentAlpha))
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White.copy(alpha = animatedContentAlpha),
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 右上角用户信息（如果提供了的话）- 带淡入动画
        if (senderName != null && timestamp != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f * animatedContentAlpha))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = senderName,
                        color = Color.White.copy(alpha = animatedContentAlpha),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatTimestamp(timestamp),
                        color = Color.White.copy(alpha = 0.7f * animatedContentAlpha),
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