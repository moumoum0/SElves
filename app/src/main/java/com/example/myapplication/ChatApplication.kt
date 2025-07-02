package com.example.myapplication

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
import com.example.myapplication.data.SQLiteConfig
import okhttp3.OkHttpClient

@HiltAndroidApp
class ChatApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化SQLite配置
        SQLiteConfig.init(this)
        
        // 配置Coil图片加载器
        setupCoilImageLoader()
    }
    
    private fun setupCoilImageLoader() {
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                // 设置更大的内存缓存
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30) // 使用30%的可用内存
                    .strongReferencesEnabled(true) // 启用强引用，防止频繁回收
                    .build()
            }
            .diskCache {
                // 启用磁盘缓存
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.08) // 增加磁盘缓存大小
                    .build()
            }
            .respectCacheHeaders(false) // 忽略服务器缓存控制
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            // 优化网络和线程池配置
            .okHttpClient {
                OkHttpClient.Builder()
                    .build()
            }
            // 启用预取功能
            .allowHardware(true) // 允许硬件加速
            .crossfade(false) // 禁用淡入动画，提升显示速度
            .build()
        
        Coil.setImageLoader(imageLoader)
    }
} 