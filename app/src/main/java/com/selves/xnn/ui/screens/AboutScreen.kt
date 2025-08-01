package com.selves.xnn.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "关于",
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
                        // 应用图标
            item {
                // 同时显示背景层和前景层，重现完整应用图标
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 背景层
                    AsyncImage(
                        model = com.selves.xnn.R.mipmap.ic_launcher_background,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    // 前景层
                    AsyncImage(
                        model = com.selves.xnn.R.mipmap.ic_launcher_foreground,
                        contentDescription = "应用图标",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 应用名称
            item {
                Text(
                    text = "selves",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 版本信息
            item {
                Text(
                    text = "版本 0.1.0",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // 开发者信息
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "开发者",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "moumoum",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 第三方库标题
            item {
                Text(
                    text = "使用的第三方库",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // 第三方库列表
            items(getThirdPartyLibraries()) { library ->
                LibraryItem(library = library)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 底部间距
            item {
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "感谢所有开源项目的贡献者",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LibraryItem(library: ThirdPartyLibrary) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 库名称
            Text(
                text = library.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 版本信息（如果有）
            if (library.version.isNotEmpty()) {
                Text(
                    text = "版本 ${library.version}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // 分隔间距
            Spacer(modifier = Modifier.height(8.dp))
            
            // 描述信息
            Text(
                text = library.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

data class ThirdPartyLibrary(
    val name: String,
    val version: String,
    val description: String
)

private fun getThirdPartyLibraries(): List<ThirdPartyLibrary> {
    return listOf(
        ThirdPartyLibrary(
            name = "Jetpack Compose",
            version = "2023.08.00",
            description = "现代Android UI工具包，用于构建原生用户界面"
        ),
        ThirdPartyLibrary(
            name = "Room",
            version = "2.6.1",
            description = "SQLite对象映射库，用于本地数据存储"
        ),
        ThirdPartyLibrary(
            name = "Dagger Hilt",
            version = "2.48",
            description = "依赖注入框架，简化Android开发中的依赖管理"
        ),
        ThirdPartyLibrary(
            name = "Navigation Compose",
            version = "2.7.6",
            description = "Jetpack导航组件的Compose版本"
        ),
        ThirdPartyLibrary(
            name = "Coil",
            version = "2.5.0",
            description = "基于Kotlin协程的Android图片加载库"
        ),
        ThirdPartyLibrary(
            name = "DataStore",
            version = "1.0.0",
            description = "数据存储解决方案，替代SharedPreferences"
        ),
        ThirdPartyLibrary(
            name = "Android Image Cropper",
            version = "4.3.2",
            description = "功能强大的Android图片裁剪库"
        ),
        ThirdPartyLibrary(
            name = "Accompanist System UI Controller",
            version = "0.32.0",
            description = "用于控制系统UI的实用工具库"
        ),
        ThirdPartyLibrary(
            name = "Material Icons Extended",
            version = "",
            description = "Material Design扩展图标库"
        ),
        ThirdPartyLibrary(
            name = "Kotlin Coroutines",
            version = "",
            description = "Kotlin异步编程库，用于处理并发操作"
        )
    )
} 