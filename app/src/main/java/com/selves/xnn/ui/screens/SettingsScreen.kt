package com.selves.xnn.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.selves.xnn.ui.viewmodels.SettingsViewModel
import com.selves.xnn.ui.components.ThemeModeDialog
import com.selves.xnn.ui.components.ColorSchemeDialog
import com.selves.xnn.ui.components.BackupProgressDialog
import com.selves.xnn.ui.components.ImportBackupWarningDialog
import com.selves.xnn.data.ImportMode
import com.selves.xnn.ui.components.LanguageDialog
import com.selves.xnn.model.getDisplayName
import androidx.compose.ui.res.stringResource
import com.selves.xnn.R
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
    val language by viewModel.language.collectAsState()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsState()
    val webServerEnabled by viewModel.webServerEnabled.collectAsState()
    val webServerIp by viewModel.webServerIp.collectAsState()
    val isBackupInProgress by viewModel.isBackupInProgress.collectAsState()
    val backupMessage by viewModel.backupMessage.collectAsState()
    val showBackupProgressDialog by viewModel.showBackupProgressDialog.collectAsState()
    val showImportWarningDialog by viewModel.showImportWarningDialog.collectAsState()
    val backupProgress by viewModel.backupProgress.collectAsState()
    val backupProgressMessage by viewModel.backupProgressMessage.collectAsState()
    val spImportInProgress by viewModel.spImportInProgress.collectAsState()
    val spImportProgress by viewModel.spImportProgress.collectAsState()
    val spImportProgressMessage by viewModel.spImportProgressMessage.collectAsState()
    val spImportMessage by viewModel.spImportMessage.collectAsState()
    val showSpModeDialog by viewModel.showSpModeDialog.collectAsState()
    
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

    // 文件选择器 - 从 SP 导入
    val spImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.showSpImportDialog(it) }
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
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
                SettingsGroupTitle(title = stringResource(R.string.settings_general))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = when (language) {
                        "zh" -> stringResource(R.string.settings_language_zh)
                        "en" -> stringResource(R.string.settings_language_en)
                        else -> stringResource(R.string.settings_theme_system)
                    },
                    onClick = { viewModel.showLanguageDialog() }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.settings_theme),
                    subtitle = themeMode.getDisplayName(context),
                    onClick = { viewModel.showThemeModeDialog() }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.settings_color),
                    subtitle = colorScheme.getDisplayName(context),
                    onClick = { viewModel.showColorSchemeDialog() }
                )
            }
            
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.SwapHoriz,
                    title = stringResource(R.string.member_quick_switch),
                    subtitle = stringResource(R.string.member_quick_switch_desc),
                    checked = quickMemberSwitchEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setQuickMemberSwitchEnabled(enabled)
                    }
                )
            }
            
            // 数据与备份分组
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsGroupTitle(title = stringResource(R.string.settings_data_backup))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Schedule,
                    title = stringResource(R.string.settings_backup_auto),
                    subtitle = stringResource(R.string.settings_backup_auto_desc),
                    onClick = { /* TODO: 实现定时备份设置 */ }
                )
            }
            
            item {
                SettingsItemWithProgress(
                    icon = Icons.Default.FileUpload,
                    title = stringResource(R.string.settings_backup_export),
                    subtitle = stringResource(R.string.settings_backup_export_desc),
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
                    title = stringResource(R.string.settings_backup_import),
                    subtitle = stringResource(R.string.settings_backup_import_desc),
                    isLoading = isBackupInProgress,
                    onClick = {
                        importBackupLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                    }
                )
            }

            item {
                SettingsItemWithProgress(
                    icon = Icons.Default.FileDownload,
                    title = stringResource(R.string.sp_import_settings_title),
                    subtitle = stringResource(R.string.sp_import_settings_subtitle),
                    isLoading = spImportInProgress,
                    onClick = {
                        spImportLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                    }
                )
            }
            
            // Web 访问分组
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsGroupTitle(title = stringResource(R.string.settings_web_access))
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Wifi,
                    title = stringResource(R.string.settings_web_access_toggle),
                    subtitle = stringResource(R.string.settings_web_access_toggle_desc),
                    checked = webServerEnabled,
                    onCheckedChange = { viewModel.setWebServerEnabled(it) }
                )
            }

            if (webServerEnabled) {
                item {
                    WebAccessInfoCard(
                        url = viewModel.webServerUrl,
                        context = context
                    )
                }
            }

            // 其他分组
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsGroupTitle(title = stringResource(R.string.settings_other))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_about),
                    subtitle = stringResource(R.string.settings_about_desc),
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
        
        // 语言选择对话框
        LanguageDialog(
            isOpen = showLanguageDialog,
            selectedLanguage = language,
            onLanguageSelected = { selectedLanguage ->
                viewModel.setLanguage(selectedLanguage)
            },
            onDismiss = { viewModel.hideLanguageDialog() }
        )
        
        // 备份进度对话框
        BackupProgressDialog(
            isVisible = showBackupProgressDialog,
            title = if (backupProgress != null && backupProgress!! >= 1.0f) stringResource(R.string.settings_backup_complete) else stringResource(R.string.settings_backup_progress),
            message = backupProgressMessage,
            progress = backupProgress
        )
        
        // 导入备份警告对话框
        ImportBackupWarningDialog(
            isVisible = showImportWarningDialog,
            onConfirm = { viewModel.confirmImportBackup() },
            onDismiss = { viewModel.cancelImportBackup() }
        )

        // SP 导入进度对话框
        BackupProgressDialog(
            isVisible = spImportInProgress,
            title = stringResource(R.string.sp_import_title),
            message = spImportProgressMessage,
            progress = spImportProgress
        )

        // SP 导入模式选择对话框
        if (showSpModeDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSpImportDialog() },
                title = { Text(stringResource(R.string.sp_import_mode_title)) },
                text = { Text(stringResource(R.string.sp_import_mode_message)) },
                confirmButton = {
                    Button(onClick = { viewModel.confirmSpImport(ImportMode.OVERWRITE) }) {
                        Text(stringResource(R.string.sp_import_mode_overwrite))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.confirmSpImport(ImportMode.MERGE) }) {
                        Text(stringResource(R.string.sp_import_mode_merge))
                    }
                }
            )
        }

        // SP 导入结果对话框
        spImportMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { viewModel.clearSpImportMessage() },
                title = { Text(stringResource(R.string.sp_import_title)) },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearSpImportMessage() }) {
                        Text(stringResource(R.string.btn_confirm))
                    }
                }
            )
        }
        
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

@Composable
private fun WebAccessInfoCard(
    url: String,
    context: Context
) {
    val qrBitmap = remember(url) { generateQrCodeBitmap(url, 240) }
    val copiedText = stringResource(R.string.settings_web_access_copied)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_web_access_url),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    clipboard.setPrimaryClip(ClipData.newPlainText("Selves URL", url))
                    Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.settings_web_access_url),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (qrBitmap != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.settings_web_access_qr),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.settings_web_access_qr),
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_web_access_tip),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun generateQrCodeBitmap(text: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}