package com.selves.xnn.util

import android.os.Environment
import java.io.File

/**
 * 图片资源缓存目录管理
 *
 * 负责管理缩略图缓存目录的创建与路径提供。
 * 缓存目录位于公共相册下，便于系统媒体扫描器统一管理。
 */
object ImageCacheDirManager {

    /** 缓存子目录名 */
    private const val CACHE_FOLDER_NAME = "Camera_Raw_Cache"

    /**
     * 获取缓存目录。
     * 如果目录不存在则自动创建。
     *
     * @return 缓存目录 File，若无法创建则返回 null
     */
    fun getCacheLocation(): File? {
        val dcimRoot = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM
        )
        val cacheDir = File(dcimRoot, CACHE_FOLDER_NAME)
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) return null
        }
        return cacheDir.takeIf { it.isDirectory && it.canWrite() }
    }

    /**
     * 清理过期的缓存文件。
     * 保留策略：
     * 1. 保留最新的 maxCount 个文件
     * 2. 额外保留历史最大的一个文件（防止数据丢失后被覆盖）
     *
     * @param maxCount 最大保留数量
     * @return 清理掉的文件数量
     */
    fun cleanExpiredCache(maxCount: Int = 5): Int {
        val dir = getCacheLocation() ?: return 0
        val files = dir.listFiles()?.toList() ?: return 0
        if (files.isEmpty()) return 0

        // 找到历史最大的文件（按文件大小）
        val largestFile = files.maxByOrNull { it.length() }

        // 按时间排序，最新的在前
        val sortedByTime = files.sortedByDescending { it.lastModified() }
        
        if (sortedByTime.size <= maxCount) return 0

        // 保留最新的 maxCount 个
        val toKeep = sortedByTime.take(maxCount).toMutableSet()
        
        // 额外保留历史最大的文件
        if (largestFile != null && largestFile !in toKeep) {
            toKeep.add(largestFile)
        }

        // 删除不需要保留的文件
        val toDelete = files.filter { it !in toKeep }
        var deleted = 0
        toDelete.forEach { f ->
            if (f.delete()) deleted++
        }
        return deleted
    }
}