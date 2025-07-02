package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.collection.LruCache
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * 图片工具类
 */
object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val AVATAR_DIR = "avatars"
    
    // 添加内存缓存，避免频繁的文件系统访问
    private val avatarFileCache = LruCache<String, File>(50) // 增加缓存大小
    private val avatarExistsCache = LruCache<String, Boolean>(100) // 增加缓存大小
    
    // 预加载状态缓存
    private val preloadedAvatars = mutableSetOf<String>()
    
    /**
     * 保存头像到应用内部存储
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 保存成功返回文件路径，失败返回null
     */
    fun saveAvatarToInternalStorage(context: Context, imageUri: Uri?): String? {
        if (imageUri == null) return null
        
        try {
            // 创建头像目录
            val avatarDir = File(context.filesDir, AVATAR_DIR).apply { 
                if (!exists()) mkdirs() 
            }
            
            // 创建唯一文件名
            val fileName = "avatar_${UUID.randomUUID()}.jpg"
            val destinationFile = File(avatarDir, fileName)
            
            // 复制文件
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // 返回文件路径
            val filePath = destinationFile.absolutePath
            
            // 保存到缓存
            avatarFileCache.put(filePath, destinationFile)
            avatarExistsCache.put(filePath, true)
            
            Log.d(TAG, "头像已保存到: $filePath")
            return filePath
        } catch (e: Exception) {
            Log.e(TAG, "保存头像失败: ${e.message}", e)
            return null
        }
    }
    
    /**
     * 获取头像文件
     * @param avatarPath 头像文件路径
     * @return 头像文件，如果路径为空或文件不存在则返回null
     */
    fun getAvatarFile(avatarPath: String?): File? {
        if (avatarPath.isNullOrEmpty()) return null
        
        // 先从缓存中获取
        avatarFileCache.get(avatarPath)?.let { return it }
        
        // 检查文件是否存在的缓存
        if (avatarExistsCache.get(avatarPath) == false) return null
        
        val file = File(avatarPath)
        val exists = file.exists()
        
        // 更新缓存
        avatarExistsCache.put(avatarPath, exists)
        if (exists) {
            avatarFileCache.put(avatarPath, file)
            return file
        }
        
        return null
    }
    
    /**
     * 获取头像加载模型，优化版本
     * @param avatarPath 头像文件路径
     * @return 适合AsyncImage加载的模型，如果路径无效则返回null
     */
    fun getAvatarModel(avatarPath: String?): Any? {
        return getAvatarFile(avatarPath)
    }
    
    /**
     * 创建优化的图片请求，支持占位符和快速加载
     * @param context 上下文
     * @param avatarPath 头像文件路径
     * @return ImageRequest对象
     */
    fun createOptimizedImageRequest(context: Context, avatarPath: String?): ImageRequest? {
        val file = getAvatarFile(avatarPath) ?: return null
        
        return ImageRequest.Builder(context)
            .data(file)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(false) // 禁用交叉淡入动画，加快显示速度
            .build()
    }
    
    /**
     * 真正的预加载头像 - 将图片加载到内存缓存中
     * @param context 上下文
     * @param avatarPaths 需要预加载的头像路径列表
     * @param coroutineScope 协程作用域
     */
    fun preloadAvatarsToMemory(
        context: Context, 
        avatarPaths: List<String>, 
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val imageLoader = coil.Coil.imageLoader(context)
            
            avatarPaths.forEach { path ->
                if (!path.isNullOrEmpty() && !preloadedAvatars.contains(path)) {
                    try {
                        // 缓存文件是否存在信息
                        val file = File(path)
                        val exists = file.exists()
                        avatarExistsCache.put(path, exists)
                        
                        if (exists) {
                            // 缓存File对象
                            avatarFileCache.put(path, file)
                            
                            // 预加载到Coil的内存缓存中
                            val request = ImageRequest.Builder(context)
                                .data(file)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build()
                            
                            // 执行预加载
                            imageLoader.execute(request)
                            preloadedAvatars.add(path)
                            Log.d(TAG, "预加载头像: $path")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "预加载头像失败: $path, ${e.message}")
                    }
                }
            }
        }
    }
    
    /**
     * 预加载一组成员的头像 - 保持向后兼容
     * @param context 上下文
     * @param avatarPaths 需要预加载的头像路径列表
     */
    @Deprecated("使用 preloadAvatarsToMemory 替代", ReplaceWith("preloadAvatarsToMemory(context, avatarPaths, coroutineScope)"))
    fun preloadAvatars(context: Context, avatarPaths: List<String>) {
        avatarPaths.forEach { path ->
            if (!path.isNullOrEmpty()) {
                // 缓存文件是否存在信息
                val file = File(path)
                val exists = file.exists()
                avatarExistsCache.put(path, exists)
                
                if (exists) {
                    // 缓存File对象
                    avatarFileCache.put(path, file)
                }
            }
        }
    }
    
    /**
     * 清理预加载缓存
     */
    fun clearPreloadCache() {
        preloadedAvatars.clear()
    }
} 