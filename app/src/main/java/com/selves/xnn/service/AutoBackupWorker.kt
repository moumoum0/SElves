package com.selves.xnn.service

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.selves.xnn.data.BackupService
import com.selves.xnn.data.BackupResult
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.util.SmartBackupRetention
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 自动备份 Worker
 * 使用 WorkManager 定时执行备份任务
 */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupService: BackupService,
    private val memberPreferences: MemberPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 检查自动备份是否启用
            val isEnabled = memberPreferences.autoBackupEnabled.first()
            
            if (!isEnabled) {
                return Result.success()
            }

            // 创建备份目录
            val backupDir = File(applicationContext.getExternalFilesDir(null), "auto_backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // 生成备份文件名
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val backupFile = File(backupDir, "selves_auto_backup_$timestamp.zip")

            // 执行备份
            val uri = FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.fileprovider",
                backupFile
            )
            
            when (backupService.exportBackup(uri)) {
                is BackupResult.Success -> {
                    // 使用智能保留策略清理旧备份
                    SmartBackupRetention.cleanBackupDirectory(backupDir)
                    Result.success()
                }
                is BackupResult.Error -> Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "auto_backup_work"
    }
}