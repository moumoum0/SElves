package com.selves.xnn.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.selves.xnn.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "未知"
        } catch (e: Exception) {
            "未知"
        }
    }

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
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
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
                    text = "版本 $versionName",
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://b23.tv/59njutf")))
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = com.selves.xnn.R.drawable.bilibili,
                                contentDescription = "Bilibili",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Bilibili",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moumoum0/SElves")))
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = com.selves.xnn.R.drawable.ic_github,
                                contentDescription = "GitHub",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "GitHub",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
            version = BuildConfig.COMPOSE_BOM_VERSION,
            description = "现代Android UI工具包，用于构建原生用户界面"
        ),
        ThirdPartyLibrary(
            name = "Material3",
            version = BuildConfig.COMPOSE_BOM_VERSION,
            description = "Material Design 3组件库"
        ),
        ThirdPartyLibrary(
            name = "Material Icons Extended",
            version = BuildConfig.COMPOSE_BOM_VERSION,
            description = "Material Design扩展图标库"
        ),
        ThirdPartyLibrary(
            name = "Navigation Compose",
            version = BuildConfig.NAVIGATION_COMPOSE_VERSION,
            description = "Jetpack导航组件的Compose版本"
        ),
        ThirdPartyLibrary(
            name = "Room",
            version = BuildConfig.ROOM_VERSION,
            description = "SQLite对象映射库，用于本地数据存储"
        ),
        ThirdPartyLibrary(
            name = "Dagger Hilt",
            version = BuildConfig.HILT_VERSION,
            description = "依赖注入框架，简化Android开发中的依赖管理"
        ),
        ThirdPartyLibrary(
            name = "Hilt Navigation Compose",
            version = BuildConfig.HILT_NAVIGATION_COMPOSE_VERSION,
            description = "Hilt与Navigation Compose的集成库"
        ),
        ThirdPartyLibrary(
            name = "Coil",
            version = BuildConfig.COIL_VERSION,
            description = "基于Kotlin协程的Android图片加载库"
        ),
        ThirdPartyLibrary(
            name = "DataStore",
            version = BuildConfig.DATASTORE_VERSION,
            description = "数据存储解决方案，替代SharedPreferences"
        ),
        ThirdPartyLibrary(
            name = "Android Image Cropper",
            version = BuildConfig.IMAGE_CROPPER_VERSION,
            description = "功能强大的Android图片裁剪库"
        ),
        ThirdPartyLibrary(
            name = "Accompanist System UI Controller",
            version = BuildConfig.ACCOMPANIST_VERSION,
            description = "用于控制系统UI的实用工具库"
        ),
        ThirdPartyLibrary(
            name = "Gson",
            version = BuildConfig.GSON_VERSION,
            description = "Google JSON序列化/反序列化库"
        ),
        ThirdPartyLibrary(
            name = "TinyPinyin",
            version = BuildConfig.TINYPINYIN_VERSION,
            description = "中文转拼音工具库"
        ),
        ThirdPartyLibrary(
            name = "Core KTX",
            version = BuildConfig.CORE_KTX_VERSION,
            description = "Android核心Kotlin扩展库"
        ),
        ThirdPartyLibrary(
            name = "Lifecycle Runtime KTX",
            version = BuildConfig.LIFECYCLE_VERSION,
            description = "生命周期感知组件的Kotlin扩展"
        ),
        ThirdPartyLibrary(
            name = "Activity Compose",
            version = BuildConfig.ACTIVITY_COMPOSE_VERSION,
            description = "Activity与Jetpack Compose的集成库"
        ),
        ThirdPartyLibrary(
            name = "AppCompat",
            version = BuildConfig.APPCOMPAT_VERSION,
            description = "Android向后兼容支持库"
        ),
        ThirdPartyLibrary(
            name = "Splash Screen",
            version = BuildConfig.SPLASH_SCREEN_VERSION,
            description = "Android 12+启动画面API兼容库"
        ),
        ThirdPartyLibrary(
            name = "Kotlin",
            version = BuildConfig.KOTLIN_BOM_VERSION,
            description = "Kotlin语言及协程支持"
        )
    )
} 