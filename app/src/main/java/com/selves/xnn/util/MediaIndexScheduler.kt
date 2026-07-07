package com.selves.xnn.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.selves.xnn.service.ResourceCacheWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 媒体资源索引调度器
 * 
 * 负责定期更新媒体资源索引，优化应用性能
 */
object MediaIndexScheduler {

    /**
     * 启动媒体资源索引任务
     * 每天自动运行一次，维护资源索引的最新状态
     */
    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val initialDelay = calculateNextScheduleTime()

        val workRequest = PeriodicWorkRequestBuilder<ResourceCacheWorker>(
            1L, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            ResourceCacheWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * 计算下次执行时间的延迟（凌晨3点执行，避开用户活跃时段）
     */
    private fun calculateNextScheduleTime(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        return target.timeInMillis - now.timeInMillis
    }
}