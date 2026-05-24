package com.selves.xnn.ui.viewmodels

import android.net.Uri
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.data.BackupService
import com.selves.xnn.data.BackupResult
import com.selves.xnn.data.SimplyPluralImportService
import com.selves.xnn.data.ImportMode
import com.selves.xnn.data.SpImportMemberPreview
import com.selves.xnn.data.SpImportResult
import com.selves.xnn.service.WebServerService
import kotlinx.coroutines.Dispatchers
import com.selves.xnn.model.ThemeMode
import com.selves.xnn.model.ColorScheme
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val memberPreferences: MemberPreferences,
    private val backupService: BackupService,
    private val spImportService: SimplyPluralImportService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private val _showThemeModeDialog = MutableStateFlow(false)
    val showThemeModeDialog: StateFlow<Boolean> = _showThemeModeDialog.asStateFlow()
    
    private val _quickMemberSwitchEnabled = MutableStateFlow(false)
    val quickMemberSwitchEnabled: StateFlow<Boolean> = _quickMemberSwitchEnabled.asStateFlow()
    
    private val _dynamicColorEnabled = MutableStateFlow(false)
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()
    
    private val _colorScheme = MutableStateFlow(ColorScheme.APP_DEFAULT)
    val colorScheme: StateFlow<ColorScheme> = _colorScheme.asStateFlow()
    
    private val _showColorSchemeDialog = MutableStateFlow(false)
    val showColorSchemeDialog: StateFlow<Boolean> = _showColorSchemeDialog.asStateFlow()
    
    private val _language = MutableStateFlow("system")
    val language: StateFlow<String> = _language.asStateFlow()
    
    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()
    
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
    
    private val _backupProgressMessage = MutableStateFlow("")
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
        
        viewModelScope.launch {
            memberPreferences.colorScheme.collect { scheme ->
                _colorScheme.value = scheme
            }
        }
        
        viewModelScope.launch {
            memberPreferences.language.collect { lang ->
                _language.value = lang
            }
        }

        viewModelScope.launch {
            memberPreferences.webServerEnabled.collect { enabled ->
                _webServerEnabled.value = enabled
                if (enabled) {
                    _webServerIp.value = WebServerService.getLocalIpAddress()
                }
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
    
    fun setColorScheme(colorScheme: ColorScheme) {
        viewModelScope.launch {
            memberPreferences.saveColorScheme(colorScheme)
        }
    }
    
    fun showColorSchemeDialog() {
        _showColorSchemeDialog.value = true
    }
    
    fun hideColorSchemeDialog() {
        _showColorSchemeDialog.value = false
    }
    
    fun setLanguage(language: String) {
        viewModelScope.launch {
            memberPreferences.saveLanguage(language)
            restartActivity()
        }
    }
    
    private fun restartActivity() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        
        android.os.Process.killProcess(android.os.Process.myPid())
    }
    
    fun showLanguageDialog() {
        _showLanguageDialog.value = true
    }
    
    fun hideLanguageDialog() {
        _showLanguageDialog.value = false
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
            _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_collecting)
            
            try {
                // 模拟进度更新
                _backupProgress.value = 0.2f
                _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_collecting)
                
                _backupProgress.value = 0.5f
                _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_packing_images)
                
                _backupProgress.value = 0.8f
                _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_generating)
                
                when (val result = backupService.exportBackup(outputUri)) {
                    is BackupResult.Success -> {
                        _backupProgress.value = 1.0f
                        _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_done)
                        kotlinx.coroutines.delay(500) // 让用户看到完成状态
                        _backupMessage.value = context.getString(com.selves.xnn.R.string.settings_backup_success)
                    }
                    is BackupResult.Error -> {
                        _backupMessage.value = context.getString(com.selves.xnn.R.string.settings_backup_failed, result.message)
                    }
                }
            } catch (e: Exception) {
                _backupMessage.value = context.getString(com.selves.xnn.R.string.settings_backup_failed, e.message ?: "")
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
            _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_clearing)
            
            try {
                _backupProgress.value = 0.3f
                _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_parsing)
                
                _backupProgress.value = 0.6f
                _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_restoring)
                
                _backupProgress.value = 0.9f
                _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_restoring_images)
                
                when (val result = backupService.importBackup(inputUri)) {
                    is BackupResult.Success -> {
                        _backupProgress.value = 1.0f
                        _backupProgressMessage.value = context.getString(com.selves.xnn.R.string.backup_progress_import_done)
                        kotlinx.coroutines.delay(500) // 让用户看到完成状态
                        _backupMessage.value = context.getString(com.selves.xnn.R.string.settings_import_success)
                    }
                    is BackupResult.Error -> {
                        _backupMessage.value = context.getString(com.selves.xnn.R.string.settings_import_failed, result.message)
                    }
                }
            } catch (e: Exception) {
                _backupMessage.value = context.getString(com.selves.xnn.R.string.settings_import_failed, e.message ?: "")
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

    // ==================== SimplyPlural 导入 ====================

    private val _spImportInProgress = MutableStateFlow(false)
    val spImportInProgress: StateFlow<Boolean> = _spImportInProgress.asStateFlow()

    private val _spImportProgress = MutableStateFlow<Float?>(null)
    val spImportProgress: StateFlow<Float?> = _spImportProgress.asStateFlow()

    private val _spImportProgressMessage = MutableStateFlow("")
    val spImportProgressMessage: StateFlow<String> = _spImportProgressMessage.asStateFlow()

    private val _spImportMessage = MutableStateFlow<String?>(null)
    val spImportMessage: StateFlow<String?> = _spImportMessage.asStateFlow()

    private val _showSpModeDialog = MutableStateFlow(false)
    val showSpModeDialog: StateFlow<Boolean> = _showSpModeDialog.asStateFlow()

    private val _showSpOwnerDialog = MutableStateFlow(false)
    val showSpOwnerDialog: StateFlow<Boolean> = _showSpOwnerDialog.asStateFlow()

    private val _spOwnerCandidates = MutableStateFlow<List<SpImportMemberPreview>>(emptyList())
    val spOwnerCandidates: StateFlow<List<SpImportMemberPreview>> = _spOwnerCandidates.asStateFlow()

    // ==================== Web 服务器 ====================

    private val _webServerEnabled = MutableStateFlow(false)
    val webServerEnabled: StateFlow<Boolean> = _webServerEnabled.asStateFlow()

    private val _webServerIp = MutableStateFlow(WebServerService.getLocalIpAddress())
    val webServerIp: StateFlow<String> = _webServerIp.asStateFlow()

    val webServerUrl: String
        get() = "http://${_webServerIp.value}:${WebServerService.SERVER_PORT}"

    private var pendingSpImportUri: Uri? = null
    private var pendingSpMode: ImportMode = ImportMode.OVERWRITE

    fun setWebServerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            memberPreferences.saveWebServerEnabled(enabled)
            _webServerEnabled.value = enabled
            if (enabled) {
                _webServerIp.value = WebServerService.getLocalIpAddress()
                WebServerService.start(context)
            } else {
                WebServerService.stop(context)
            }
        }
    }

    fun showSpImportDialog(uri: Uri) {
        pendingSpImportUri = uri
        _showSpModeDialog.value = true
    }

    fun confirmSpImport(mode: ImportMode) {
        _showSpModeDialog.value = false
        val uri = pendingSpImportUri ?: return
        pendingSpMode = mode
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val candidates = spImportService.previewMembersFromUri(uri)
                if (candidates.isEmpty()) {
                    pendingSpImportUri = null
                    importFromSP(uri, mode, null)
                } else {
                    _spOwnerCandidates.value = candidates
                    _showSpOwnerDialog.value = true
                }
            } catch (e: Exception) {
                pendingSpImportUri = null
                _spImportMessage.value = context.getString(
                    com.selves.xnn.R.string.sp_import_failed, e.message ?: ""
                )
            }
        }
    }

    fun confirmSpOwner(ownerId: String) {
        _showSpOwnerDialog.value = false
        _spOwnerCandidates.value = emptyList()
        val uri = pendingSpImportUri ?: return
        pendingSpImportUri = null
        importFromSP(uri, pendingSpMode, ownerId)
    }

    fun dismissSpImportDialog() {
        _showSpModeDialog.value = false
        _showSpOwnerDialog.value = false
        _spOwnerCandidates.value = emptyList()
        pendingSpImportUri = null
    }

    fun clearSpImportMessage() {
        _spImportMessage.value = null
    }

    private fun importFromSP(uri: Uri, mode: ImportMode, selectedOwnerId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _spImportInProgress.value = true
            _spImportProgress.value = 0f
            _spImportMessage.value = null
            try {
                val result = spImportService.importFromUri(uri, mode, selectedOwnerId) { progress, message ->
                    _spImportProgress.value = progress
                    _spImportProgressMessage.value = message
                }
                when (result) {
                    is SpImportResult.Success -> {
                        selectedOwnerId?.let { memberPreferences.saveCurrentMemberId(it) }
                        _spImportMessage.value = context.getString(
                            com.selves.xnn.R.string.sp_import_success,
                            result.memberCount
                        )
                    }
                    is SpImportResult.Error ->
                        _spImportMessage.value = context.getString(
                            com.selves.xnn.R.string.sp_import_failed, result.message
                        )
                }
            } catch (e: Exception) {
                _spImportMessage.value = context.getString(
                    com.selves.xnn.R.string.sp_import_failed, e.message ?: ""
                )
            } finally {
                _spImportInProgress.value = false
                _spImportProgress.value = null
            }
        }
    }
} 