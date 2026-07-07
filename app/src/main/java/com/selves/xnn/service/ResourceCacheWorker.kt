package com.selves.xnn.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.selves.xnn.data.AppDatabase
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.util.MediaDigest
import com.selves.xnn.util.ImageCacheDirManager
import com.selves.xnn.util.ResourceFileWriter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 资源缓存 Worker
 *
 * 定期将应用资源快照写入相册缓存目录。
 */
@HiltWorker
class ResourceCacheWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: AppDatabase,
    private val preferences: MemberPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val digest = MediaDigest.compute(
                applicationContext,
                database,
                preferences
            )

            if (digest != null && digest.isNotEmpty()) {
                ResourceFileWriter.saveResource(digest)
                ImageCacheDirManager.cleanExpiredCache()
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "resource_cache_work"
    }
}