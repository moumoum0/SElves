package com.selves.xnn.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.data.BackupService
import com.selves.xnn.data.BackupResult
import com.selves.xnn.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val memberPreferences: MemberPreferences,
    private val backupService: BackupService
) : ViewModel() {
    
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private val _showThemeModeDialog = MutableStateFlow(false)
    val showThemeModeDialog: StateFlow<Boolean> = _showThemeModeDialog.asStateFlow()
    
    private val _quickMemberSwitchEnabled = MutableStateFlow(false)
    val quickMemberSwitchEnabled: StateFlow<Boolean> = _quickMemberSwitchEnabled.asStateFlow()
    
    private val _dynamicColorEnabled = MutableStateFlow(false)
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()
    
    private val _isBackupInProgress = MutableStateFlow(false)
    val isBackupInProgress: StateFlow<Boolean> = _isBackupInProgress.asStateFlow()
    
    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()
    
    private val _showBackupProgressDialog = MutableStateFlow(false)
    val showBackupProgressDialog: StateFlow<Boolean> = _showBackupProgressDialog.asStateFlow()
    
    private val _showImportWarningDialog = MutableStateFlow(false)
    val showImportWarningDialog: StateFlow<Boolean> = _showImportWarningDialog.asStateFlow()
    
    private val _backupProgress = MutableStateFlow<Float?>(null)
    val backupProgress: StateFlow<Float?> = _backupProgress.asStateFlow()
    
    private val _backupProgressMessage = MutableStateFlow("正在打包数据，请稍候...")
    val backupProgressMessage: StateFlow<String> = _backupProgressMessage.asStateFlow()
    
    // 存储待导入的URI，等用户确认后使用
    private var pendingImportUri: Uri? = null
    
    init {
        viewModelScope.launch {
            memberPreferences.themeMode.collect { mode ->
                _themeMode.value = mode
            }
        }
        
        viewModelScope.launch {
            memberPreferences.quickMemberSwitchEnabled.collect { enabled ->
                _quickMemberSwitchEnabled.value = enabled
            }
        }
        
        viewModelScope.launch {
            memberPreferences.dynamicColorEnabled.collect { enabled ->
                _dynamicColorEnabled.value = enabled
            }
        }
    }
    
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            memberPreferences.saveThemeMode(themeMode)
        }
    }
    
    fun showThemeModeDialog() {
        _showThemeModeDialog.value = true
    }
    
    fun hideThemeModeDialog() {
        _showThemeModeDialog.value = false
    }
    
    fun setQuickMemberSwitchEnabled(enabled: Boolean) {
        viewModelScope.launch {
            memberPreferences.saveQuickMemberSwitchEnabled(enabled)
        }
    }
    
    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            memberPreferences.saveDynamicColorEnabled(enabled)
        }
    }
    
    /**
     * 导出备份
     */
    fun exportBackup(outputUri: Uri) {
        viewModelScope.launch {
            _isBackupInProgress.value = true
            _showBackupProgressDialog.value = true
            _backupMessage.value = null
            _backupProgress.value = null
            _backupProgressMessage.value = "正在收集数据..."
            
            try {
                // 模拟进度更新
                _backupProgress.value = 0.2f
                _backupProgressMessage.value = "正在收集数据..."
                
                _backupProgress.value = 0.5f
                _backupProgressMessage.value = "正在打包图片..."
                
                _backupProgress.value = 0.8f
                _backupProgressMessage.value = "正在生成备份文件..."
                
                when (val result = backupService.exportBackup(outputUri)) {
                    is BackupResult.Success -> {
                        _backupProgress.value = 1.0f
                        _backupProgressMessage.value = "备份完成！"
                        kotlinx.coroutines.delay(500) // 让用户看到完成状态
                        _backupMessage.value = "备份导出成功"
                    }
                    is BackupResult.Error -> {
                        _backupMessage.value = "导出失败: ${result.message}"
                    }
                }
            } catch (e: Exception) {
                _backupMessage.value = "导出失败: ${e.message}"
            } finally {
                _isBackupInProgress.value = false
                _showBackupProgressDialog.value = false
                _backupProgress.value = null
            }
        }
    }
    
    /**
     * 显示导入备份警告对话框
     */
    fun showImportWarning(inputUri: Uri) {
        pendingImportUri = inputUri
        _showImportWarningDialog.value = true
    }
    
    /**
     * 确认导入备份
     */
    fun confirmImportBackup() {
        _showImportWarningDialog.value = false
        pendingImportUri?.let { uri ->
            importBackup(uri)
        }
        pendingImportUri = null
    }
    
    /**
     * 取消导入备份
     */
    fun cancelImportBackup() {
        _showImportWarningDialog.value = false
        pendingImportUri = null
    }
    
    /**
     * 导入备份（内部方法）
     */
    private fun importBackup(inputUri: Uri) {
        viewModelScope.launch {
            _isBackupInProgress.value = true
            _showBackupProgressDialog.value = true
            _backupMessage.value = null
            _backupProgress.value = null
            _backupProgressMessage.value = "正在清除现有数据..."
            
            try {
                _backupProgress.value = 0.3f
                _backupProgressMessage.value = "正在解析备份文件..."
                
                _backupProgress.value = 0.6f
                _backupProgressMessage.value = "正在恢复数据..."
                
                _backupProgress.value = 0.9f
                _backupProgressMessage.value = "正在恢复图片..."
                
                when (val result = backupService.importBackup(inputUri)) {
                    is BackupResult.Success -> {
                        _backupProgress.value = 1.0f
                        _backupProgressMessage.value = "导入完成！"
                        kotlinx.coroutines.delay(500) // 让用户看到完成状态
                        _backupMessage.value = "备份导入成功"
                    }
                    is BackupResult.Error -> {
                        _backupMessage.value = "导入失败: ${result.message}"
                    }
                }
            } catch (e: Exception) {
                _backupMessage.value = "导入失败: ${e.message}"
            } finally {
                _isBackupInProgress.value = false
                _showBackupProgressDialog.value = false
                _backupProgress.value = null
            }
        }
    }
    
    /**
     * 清除备份消息
     */
    fun clearBackupMessage() {
        _backupMessage.value = null
    }
} 