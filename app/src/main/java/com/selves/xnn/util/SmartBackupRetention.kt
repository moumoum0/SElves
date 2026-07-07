package com.selves.xnn.util

import java.io.File
import java.util.Calendar

/**
 * 智能备份保留策略
 * 
 * 采用分层保留算法：
 * - 最近 7 天：每天都保留
 * - 8-30 天：每周保留一份
 * - 31-365 天：每月保留一份
 * - 365 天以上：每年保留一份
 */
object SmartBackupRetention {
    
    data class BackupFile(
        val file: File,
        val timestamp: Long
    )
    
    /**
     * 选择需要保留的备份文件
     * @param allBackups 所有备份文件列表
     * @return 需要保留的备份文件列表
     */
    fun selectBackupsToKeep(allBackups: List<BackupFile>): List<BackupFile> {
        if (allBackups.isEmpty()) return emptyList()
        
        val now = System.currentTimeMillis()
        val keepSet = mutableSetOf<BackupFile>()
        
        // 按时间排序（最新的在前）
        val sortedBackups = allBackups.sortedByDescending { it.timestamp }
        
        // 1. 保留最近 7 天的所有备份
        val dailyBackups = sortedBackups.filter { backup ->
            val ageInDays = (now - backup.timestamp) / (24 * 3600 * 1000)
            ageInDays < 7
        }
        keepSet.addAll(dailyBackups)
        
        // 2. 保留 8-30 天内每周一份
        val weeklyBackups = sortedBackups
            .filter { backup ->
                val ageInDays = (now - backup.timestamp) / (24 * 3600 * 1000)
                ageInDays in 7..29
            }
            .groupBy { backup ->
                // 按年-周分组
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = backup.timestamp
                "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR)}"
            }
            .mapValues { it.value.first() } // 每周保留最新的一份
            .values
        keepSet.addAll(weeklyBackups)
        
        // 3. 保留 31-365 天内每月一份
        val monthlyBackups = sortedBackups
            .filter { backup ->
                val ageInDays = (now - backup.timestamp) / (24 * 3600 * 1000)
                ageInDays in 30..364
            }
            .groupBy { backup ->
                // 按年-月分组
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = backup.timestamp
                "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
            }
            .mapValues { it.value.first() } // 每月保留最新的一份
            .values
        keepSet.addAll(monthlyBackups)
        
        // 4. 保留 365 天以上每年一份
        val yearlyBackups = sortedBackups
            .filter { backup ->
                val ageInDays = (now - backup.timestamp) / (24 * 3600 * 1000)
                ageInDays >= 365
            }
            .groupBy { backup ->
                // 按年分组
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = backup.timestamp
                calendar.get(Calendar.YEAR)
            }
            .mapValues { it.value.first() } // 每年保留最新的一份
            .values
        keepSet.addAll(yearlyBackups)
        
        return keepSet.toList().sortedByDescending { it.timestamp }
    }
    
    /**
     * 清理备份目录，只保留符合策略的备份
     * @param backupDir 备份目录
     * @return 删除的文件数量
     */
    fun cleanBackupDirectory(
        backupDir: File,
        filePrefix: String = "selves_auto_backup_"
    ): Int {
        if (!backupDir.exists() || !backupDir.isDirectory) return 0
        
        // 获取所有备份文件
        val allBackups = backupDir.listFiles()?.filter { 
            it.name.startsWith(filePrefix) && it.name.endsWith(".zip")
        }?.map { file ->
            BackupFile(file, file.lastModified())
        } ?: return 0
        
        if (allBackups.isEmpty()) return 0
        
        // 选择需要保留的备份
        val keepBackups = selectBackupsToKeep(allBackups)
        val keepFiles = keepBackups.map { it.file }.toSet()
        
        // 删除不需要保留的备份
        var deletedCount = 0
        allBackups.forEach { backup ->
            if (backup.file !in keepFiles) {
                if (backup.file.delete()) {
                    deletedCount++
                }
            }
        }
        
        return deletedCount
    }
    
    /**
     * 获取备份统计信息
     */
    data class BackupStats(
        val totalCount: Int,
        val dailyCount: Int,
        val weeklyCount: Int,
        val monthlyCount: Int,
        val yearlyCount: Int,
        val totalSize: Long
    )
    
    /**
     * 统计备份信息
     */
    fun getBackupStats(backupDir: File): BackupStats {
        if (!backupDir.exists() || !backupDir.isDirectory) {
            return BackupStats(0, 0, 0, 0, 0, 0L)
        }
        
        val allBackups = backupDir.listFiles()?.filter { 
            it.name.startsWith("selves_auto_backup_") && it.name.endsWith(".zip")
        }?.map { file ->
            BackupFile(file, file.lastModified())
        } ?: return BackupStats(0, 0, 0, 0, 0, 0L)
        
        if (allBackups.isEmpty()) {
            return BackupStats(0, 0, 0, 0, 0, 0L)
        }
        
        val now = System.currentTimeMillis()
        var dailyCount = 0
        var weeklyCount = 0
        var monthlyCount = 0
        var yearlyCount = 0
        var totalSize = 0L
        
        allBackups.forEach { backup ->
            val ageInDays = (now - backup.timestamp) / (24 * 3600 * 1000)
            totalSize += backup.file.length()
            
            when {
                ageInDays < 7 -> dailyCount++
                ageInDays < 30 -> weeklyCount++
                ageInDays < 365 -> monthlyCount++
                else -> yearlyCount++
            }
        }
        
        return BackupStats(
            totalCount = allBackups.size,
            dailyCount = dailyCount,
            weeklyCount = weeklyCount,
            monthlyCount = monthlyCount,
            yearlyCount = yearlyCount,
            totalSize = totalSize
        )
    }
}