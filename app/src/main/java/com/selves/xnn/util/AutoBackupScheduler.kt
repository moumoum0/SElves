package com.selves.xnn.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.selves.xnn.service.AutoBackupWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 自动备份调度器
 * 负责安排和取消自动备份任务
 */
object AutoBackupScheduler {

    /**
     * 安排自动备份任务
     * @param context Context
     * @param frequency 频率：daily, weekly, monthly
     * @param hour 执行时间（小时，0-23）
     * @param replaceExisting 是否替换已有任务；用户修改配置时替换，应用启动恢复时保留
     */
    fun scheduleAutoBackup(
        context: Context,
        frequency: String,
        hour: Int,
        replaceExisting: Boolean = true
    ) {
        val workManager = WorkManager.getInstance(context)

        // 计算初始延迟时间
        val initialDelay = calculateInitialDelay(hour)

        // 根据频率创建周期性任务
        val repeatInterval = when (frequency) {
            "daily" -> 1L to TimeUnit.DAYS
            "weekly" -> 7L to TimeUnit.DAYS
            "monthly" -> 30L to TimeUnit.DAYS
            else -> 1L to TimeUnit.DAYS
        }

        val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            repeatInterval.first, 
            repeatInterval.second
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        val policy = if (replaceExisting) {
            ExistingPeriodicWorkPolicy.REPLACE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }

        workManager.enqueueUniquePeriodicWork(
            AutoBackupWorker.WORK_NAME,
            policy,
            workRequest
        )
    }

    /**
     * 取消自动备份任务
     */
    fun cancelAutoBackup(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(AutoBackupWorker.WORK_NAME)
    }

    /**
     * 计算到指定小时的初始延迟时间（毫秒）
     */
    private fun calculateInitialDelay(targetHour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // 如果目标时间已经过了，设置为明天
            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        return target.timeInMillis - now.timeInMillis
    }
}