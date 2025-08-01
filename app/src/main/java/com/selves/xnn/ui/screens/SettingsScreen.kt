package com.selves.xnn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.ui.viewmodels.SettingsViewModel
import com.selves.xnn.ui.components.ThemeModeDialog
import com.selves.xnn.model.getDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val showThemeModeDialog by viewModel.showThemeModeDialog.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "设置",
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 通用设置分组
            item {
                SettingsGroupTitle(title = "通用")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "语言",
                    subtitle = "简体中文",
                    onClick = { /* TODO: 实现语言设置 */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "深色模式",
                    subtitle = themeMode.getDisplayName(),
                    onClick = { viewModel.showThemeModeDialog() }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.SwapHoriz,
                    title = "快捷切换成员",
                    subtitle = "设置默认成员和快捷切换",
                    onClick = { /* TODO: 实现快捷切换成员设置 */ }
                )
            }
            
            // 数据与备份分组
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsGroupTitle(title = "数据与备份")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Schedule,
                    title = "定时备份",
                    subtitle = "设置自动备份频率和时间",
                    onClick = { /* TODO: 实现定时备份设置 */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = "导入备份",
                    subtitle = "从文件恢复应用数据",
                    onClick = { /* TODO: 实现导入备份功能 */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FileUpload,
                    title = "导出备份",
                    subtitle = "备份应用数据到文件",
                    onClick = { /* TODO: 实现导出备份功能 */ }
                )
            }
            
            // 其他分组
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsGroupTitle(title = "其他")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "关于",
                    subtitle = "应用信息和版本",
                    onClick = onNavigateToAbout
                )
            }
        }
        
        // 主题模式选择对话框
        ThemeModeDialog(
            isOpen = showThemeModeDialog,
            selectedThemeMode = themeMode,
            onThemeModeSelected = { selectedMode ->
                viewModel.setThemeMode(selectedMode)
            },
            onDismiss = { viewModel.hideThemeModeDialog() }
        )
    }
}

@Composable
fun SettingsGroupTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
} 