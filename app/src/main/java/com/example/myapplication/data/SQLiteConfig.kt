package com.example.myapplication.data

import android.content.Context
import android.util.Log
import java.io.File

/**
 * SQLite配置类
 * 注意：由于在Android上使用org.sqlite.JDBC可能会遇到本地库加载问题，
 * 改为直接使用Android内置的SQLite实现
 */
object SQLiteConfig {
    private const val TAG = "SQLiteConfig"
    
    fun init(context: Context) {
        try {
            // 确保应用数据库目录存在
            val dbDir = File(context.filesDir, "databases").apply { 
                if (!exists()) mkdirs() 
            }
            Log.d(TAG, "数据库目录已初始化: ${dbDir.absolutePath}")
            
            // 不再尝试加载SQLite JDBC本地库，直接使用Android内置SQLite
            Log.d(TAG, "使用Android内置SQLite，跳过JDBC库加载")
        } catch (e: Exception) {
            Log.e(TAG, "SQLite初始化失败: ${e.message}", e)
        }
    }
} 