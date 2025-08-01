package com.selves.xnn.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

import com.selves.xnn.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDynamicDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var savedImagePaths by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        // 限制最多选择9张图片
        val maxImages = 9
        val currentImageCount = selectedImageUris.size + savedImagePaths.size
        val availableSlots = maxImages - currentImageCount
        
        if (availableSlots > 0) {
            val newUris = uris.take(availableSlots)
            selectedImageUris = selectedImageUris + newUris
            
            // 保存选中的图片
            coroutineScope.launch {
                val newPaths = mutableListOf<String>()
                newUris.forEach { uri ->
                    val savedPath = ImageUtils.saveMessageImageToInternalStorage(context, uri)
                    savedPath?.let { newPaths.add(it) }
                }
                savedImagePaths = savedImagePaths + newPaths
            }
        }
    }
    
    // 获取系统UI控制器
    // 不修改系统UI颜色，保持与主应用一致
    
    // 使用Box替代Dialog来实现全屏效果
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部导航栏
            TopAppBar(
                title = { Text("编辑动态") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (content.isNotBlank() || savedImagePaths.isNotEmpty()) {
                                onConfirm(content, savedImagePaths)
                            }
                        },
                        enabled = content.isNotBlank() || savedImagePaths.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "发布")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 文本输入区域
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("分享你的想法...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    maxLines = 10
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 分隔线
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 图片区域
                ImageSection(
                    imagePaths = savedImagePaths,
                    onImagesChange = { newPaths ->
                        savedImagePaths = newPaths
                        // 同步更新选中的URI列表
                        selectedImageUris = selectedImageUris.take(newPaths.size)
                    },
                    onAddImageClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    maxImages = 9
                )
            }
        }
    }
}

@Composable
fun ImageSection(
    imagePaths: List<String>,
    onImagesChange: (List<String>) -> Unit,
    onAddImageClick: () -> Unit,
    maxImages: Int = 9
) {
    // 创建包含图片和添加按钮的完整列表
    val allItems = mutableListOf<String>().apply {
        addAll(imagePaths)
        if (imagePaths.size < maxImages) {
            add("ADD_BUTTON")
        }
    }
    
    // 按每行3个分组
    val chunkedItems = allItems.chunked(3)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedItems.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    if (item == "ADD_BUTTON") {
                        AddImageButton(
                            onClick = onAddImageClick,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        ImageItem(
                            imagePath = item,
                            onRemove = {
                                onImagesChange(imagePaths - item)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // 如果这一行不满3个，添加空白占位符
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AddImageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "添加图片",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun ImageItem(
    imagePath: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier.aspectRatio(1f)
    ) {
        // 图片预览
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            val imageRequest = remember(imagePath) {
                ImageUtils.createMessageImageRequest(context, imagePath)
            }
            
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "预览图片",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 图片加载失败时显示占位符
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "图片加载失败",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 删除按钮
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除图片",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier
                        .padding(2.dp)
                        .size(16.dp)
                )
            }
        }
    }
} 

/**
 * 动态图片网格显示组件
 */
@Composable
fun DynamicImageGrid(
    imagePaths: List<String>,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    if (imagePaths.isEmpty()) return
    
    when {
        imagePaths.size == 1 -> {
            // 单张图片 - 显示最大尺寸
            SingleImageItem(
                imagePath = imagePaths[0],
                onClick = { onImageClick(imagePaths[0]) },
                modifier = modifier.height(300.dp) // 增加高度，显示更大
            )
        }
        imagePaths.size in 2..4 -> {
            // 2-4张图片 - 2×2网格
            val rows = if (imagePaths.size <= 2) 1 else 2
            val itemsPerRow = 2
            
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (rowIndex in 0 until rows) {
                    val startIndex = rowIndex * itemsPerRow
                    val endIndex = minOf(startIndex + itemsPerRow, imagePaths.size)
                    val rowImages = imagePaths.subList(startIndex, endIndex)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowImages.forEach { imagePath ->
                            GridImageItem(
                                imagePath = imagePath,
                                onClick = { onImageClick(imagePath) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 如果这一行不满2个，添加空白占位符
                        repeat(itemsPerRow - rowImages.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        imagePaths.size in 5..9 -> {
            // 5-9张图片 - 3×3网格
            val rows = when (imagePaths.size) {
                5, 6 -> 2
                7, 8, 9 -> 3
                else -> 3
            }
            val itemsPerRow = 3
            
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (rowIndex in 0 until rows) {
                    val startIndex = rowIndex * itemsPerRow
                    val endIndex = minOf(startIndex + itemsPerRow, imagePaths.size)
                    val rowImages = imagePaths.subList(startIndex, endIndex)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowImages.forEach { imagePath ->
                            GridImageItem(
                                imagePath = imagePath,
                                onClick = { onImageClick(imagePath) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 如果这一行不满3个，添加空白占位符
                        repeat(itemsPerRow - rowImages.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleImageItem(
    imagePath: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val imageRequest = remember(imagePath) {
            ImageUtils.createMessageImageRequest(context, imagePath)
        }
        
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "动态图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "图片加载失败",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun GridImageItem(
    imagePath: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val imageRequest = remember(imagePath) {
            ImageUtils.createMessageImageRequest(context, imagePath)
        }
        
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "动态图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "图片\n加载失败",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

 