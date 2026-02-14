package com.selves.xnn.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.ui.viewmodels.SettingsViewModel
import com.selves.xnn.ui.components.ThemeModeDialog
import com.selves.xnn.ui.components.ColorSchemeDialog
import com.selves.xnn.ui.components.BackupProgressDialog
import com.selves.xnn.ui.components.ImportBackupWarningDialog
import com.selves.xnn.model.getDisplayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val showThemeModeDialog by viewModel.showThemeModeDialog.collectAsState()
    val quickMemberSwitchEnabled by viewModel.quickMemberSwitchEnabled.collectAsState()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
    val colorScheme by viewModel.colorScheme.collectAsState()
    val showColorSchemeDialog by viewModel.showColorSchemeDialog.collectAsState()
    val isBackupInProgress by viewModel.isBackupInProgress.collectAsState()
    val backupMessage by viewModel.backupMessage.collectAsState()
    val showBackupProgressDialog by viewModel.showBackupProgressDialog.collectAsState()
    val showImportWarningDialog by viewModel.showImportWarningDialog.collectAsState()
    val backupProgress by viewModel.backupProgress.collectAsState()
    val backupProgressMessage by viewModel.backupProgressMessage.collectAsState()
    
    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 处理权限结果
    }
    
    // 文件选择器 - 导出备份
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportBackup(it) }
    }
    
    // 文件选择器 - 导入备份
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.showImportWarning(it) }
    }
    
    // 显示备份消息
    LaunchedEffect(backupMessage) {
        backupMessage?.let {
            // 这里可以显示Toast或Snackbar
            viewModel.clearBackupMessage()
        }
    }
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
                    icon = Icons.Default.Palette,
                    title = "颜色与个性化",
                    subtitle = colorScheme.getDisplayName(),
                    onClick = { viewModel.showColorSchemeDialog() }
                )
            }
            
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.SwapHoriz,
                    title = "快捷切换成员",
                    subtitle = "在投票和聊天界面显示快捷成员切换",
                    checked = quickMemberSwitchEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setQuickMemberSwitchEnabled(enabled)
                    }
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
                SettingsItemWithProgress(
                    icon = Icons.Default.FileUpload,
                    title = "导出备份",
                    subtitle = "备份应用数据到文件",
                    isLoading = isBackupInProgress,
                    onClick = {
                        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val timestamp = dateFormat.format(Date())
                        val fileName = "selves_backup_$timestamp.zip"
                        exportBackupLauncher.launch(fileName)
                    }
                )
            }
            
            item {
                SettingsItemWithProgress(
                    icon = Icons.Default.FileDownload,
                    title = "导入备份",
                    subtitle = "从文件恢复应用数据",
                    isLoading = isBackupInProgress,
                    onClick = {
                        importBackupLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                    }
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
        
        // 配色方案选择对话框
        ColorSchemeDialog(
            isOpen = showColorSchemeDialog,
            selectedColorScheme = colorScheme,
            onColorSchemeSelected = { selectedScheme ->
                viewModel.setColorScheme(selectedScheme)
            },
            onDismiss = { viewModel.hideColorSchemeDialog() }
        )
        
        // 备份进度对话框
        BackupProgressDialog(
            isVisible = showBackupProgressDialog,
            title = if (backupProgress != null && backupProgress!! >= 1.0f) "备份完成" else "正在备份",
            message = backupProgressMessage,
            progress = backupProgress
        )
        
        // 导入备份警告对话框
        ImportBackupWarningDialog(
            isVisible = showImportWarningDialog,
            onConfirm = { viewModel.confirmImportBackup() },
            onDismiss = { viewModel.cancelImportBackup() }
        )
        
        // 备份消息Snackbar
        backupMessage?.let { message ->
            LaunchedEffect(message) {
                // 这里可以显示Snackbar
            }
        }
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
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) 
                MaterialTheme.colorScheme.onSurfaceVariant
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
        
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsItemWithProgress(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isLoading) 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isLoading)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (isLoading) 0.5f else 1f
                )
            )
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        }
    }
} 