package com.selves.xnn.util

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
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

/**
 * 图片工具类
 */
object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val AVATAR_DIR = "avatars"
    private const val MESSAGE_IMAGES_DIR = "message_images" // 消息图片目录
    
    // 添加内存缓存，避免频繁的文件系统访问
    private val avatarFileCache = LruCache<String, File>(50) // 增加缓存大小
    private val avatarExistsCache = LruCache<String, Boolean>(100) // 增加缓存大小
    private val messageImageFileCache = LruCache<String, File>(100) // 消息图片缓存
    private val messageImageExistsCache = LruCache<String, Boolean>(200) // 消息图片存在性缓存
    
    // 预加载状态缓存
    private val preloadedAvatars = mutableSetOf<String>()
    private val preloadedMessageImages = mutableSetOf<String>()
    
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
     * 保存消息图片到应用内部存储
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 保存成功返回文件路径，失败返回null
     */
    suspend fun saveMessageImageToInternalStorage(context: Context, imageUri: Uri?): String? = withContext(Dispatchers.IO) {
        if (imageUri == null) return@withContext null
        
        try {
            // 创建消息图片目录
            val messageImageDir = File(context.filesDir, MESSAGE_IMAGES_DIR).apply { 
                if (!exists()) mkdirs() 
            }
            
            // 创建唯一文件名
            val fileName = "msg_img_${UUID.randomUUID()}.jpg"
            val destinationFile = File(messageImageDir, fileName)
            
            // 复制文件
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // 返回文件路径
            val filePath = destinationFile.absolutePath
            
            // 保存到缓存
            messageImageFileCache.put(filePath, destinationFile)
            messageImageExistsCache.put(filePath, true)
            
            Log.d(TAG, "消息图片已保存到: $filePath")
            return@withContext filePath
        } catch (e: Exception) {
            Log.e(TAG, "保存消息图片失败: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * 获取消息图片文件
     * @param imagePath 图片文件路径
     * @return 图片文件，如果路径为空或文件不存在则返回null
     */
    fun getMessageImageFile(imagePath: String?): File? {
        if (imagePath.isNullOrEmpty()) return null
        
        // 先从缓存中获取
        messageImageFileCache.get(imagePath)?.let { return it }
        
        // 检查文件是否存在的缓存
        if (messageImageExistsCache.get(imagePath) == false) return null
        
        val file = File(imagePath)
        val exists = file.exists()
        
        // 更新缓存
        messageImageExistsCache.put(imagePath, exists)
        if (exists) {
            messageImageFileCache.put(imagePath, file)
            return file
        }
        
        return null
    }
    
    /**
     * 获取消息图片加载模型
     * @param imagePath 图片文件路径
     * @return 适合AsyncImage加载的模型，如果路径无效则返回null
     */
    fun getMessageImageModel(imagePath: String?): Any? {
        return getMessageImageFile(imagePath)
    }
    
    /**
     * 创建消息图片的优化请求
     * @param context 上下文
     * @param imagePath 图片文件路径
     * @return ImageRequest对象
     */
    fun createMessageImageRequest(context: Context, imagePath: String?): ImageRequest? {
        val file = getMessageImageFile(imagePath) ?: return null
        
        return ImageRequest.Builder(context)
            .data(file)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true) // 消息图片使用淡入效果
            .build()
    }
    
    /**
     * 预加载消息图片到内存缓存中
     * @param context 上下文
     * @param imagePaths 需要预加载的图片路径列表
     * @param coroutineScope 协程作用域
     */
    fun preloadMessageImages(
        context: Context, 
        imagePaths: List<String>, 
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val imageLoader = coil.Coil.imageLoader(context)
            
            imagePaths.forEach { path ->
                if (path.isNotEmpty() && !preloadedMessageImages.contains(path)) {
                    try {
                        // 缓存文件是否存在信息
                        val file = File(path)
                        val exists = file.exists()
                        messageImageExistsCache.put(path, exists)
                        
                        if (exists) {
                            // 缓存File对象
                            messageImageFileCache.put(path, file)
                            
                            // 预加载到Coil的内存缓存中
                            val request = ImageRequest.Builder(context)
                                .data(file)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build()
                            
                            // 执行预加载
                            imageLoader.execute(request)
                            preloadedMessageImages.add(path)
                            Log.d(TAG, "预加载消息图片: $path")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "预加载消息图片失败: $path, ${e.message}")
                    }
                }
            }
        }
    }
    
    /**
     * 清理预加载缓存
     */
    fun clearPreloadCache() {
        preloadedAvatars.clear()
        preloadedMessageImages.clear()
    }
    
    /**
     * 创建头像裁剪选项
     * @return 头像裁剪的配置选项
     */
    fun createAvatarCropOptions(): CropImageContractOptions {
        return CropImageContractOptions(
            uri = null,
            cropImageOptions = CropImageOptions().apply {
                imageSourceIncludeGallery = true
                imageSourceIncludeCamera = false
                guidelines = com.canhub.cropper.CropImageView.Guidelines.ON_TOUCH
                aspectRatioX = 1
                aspectRatioY = 1
                fixAspectRatio = true
                cropShape = com.canhub.cropper.CropImageView.CropShape.OVAL
                activityTitle = "裁剪头像"
                activityMenuIconColor = android.graphics.Color.WHITE
                activityBackgroundColor = android.graphics.Color.BLACK
                toolbarColor = android.graphics.Color.BLACK
                cropMenuCropButtonTitle = "✓"
                allowRotation = true
                allowFlipping = false
                allowCounterRotation = false
                showCropOverlay = true
                autoZoomEnabled = true
                maxZoom = 4
                initialCropWindowPaddingRatio = 0.1f
                borderLineThickness = 3f
                borderLineColor = android.graphics.Color.WHITE
                borderCornerThickness = 5f
                borderCornerOffset = 5f
                borderCornerLength = 14f
                borderCornerColor = android.graphics.Color.WHITE
                guidelinesThickness = 1f
                guidelinesColor = android.graphics.Color.argb(170, 255, 255, 255)
                backgroundColor = android.graphics.Color.argb(119, 0, 0, 0)
                minCropWindowWidth = 40
                minCropWindowHeight = 40
                minCropResultWidth = 200
                minCropResultHeight = 200
                maxCropResultWidth = 999
                maxCropResultHeight = 999
                outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG
                outputCompressQuality = 90
                outputRequestWidth = 400
                outputRequestHeight = 400
                outputRequestSizeOptions = com.canhub.cropper.CropImageView.RequestSizeOptions.RESIZE_INSIDE
                noOutputImage = false
                customOutputUri = null
            }
        )
    }
    
    /**
     * 创建从 Uri 开始的头像裁剪选项
     * @param uri 要裁剪的图片 Uri
     * @return 头像裁剪的配置选项
     */
    fun createAvatarCropOptionsFromUri(uri: Uri): CropImageContractOptions {
        return CropImageContractOptions(
            uri = uri,
            cropImageOptions = CropImageOptions().apply {
                imageSourceIncludeGallery = false
                imageSourceIncludeCamera = false
                guidelines = com.canhub.cropper.CropImageView.Guidelines.ON_TOUCH
                aspectRatioX = 1
                aspectRatioY = 1
                fixAspectRatio = true
                cropShape = com.canhub.cropper.CropImageView.CropShape.OVAL
                activityTitle = "裁剪头像"
                activityMenuIconColor = android.graphics.Color.WHITE
                activityBackgroundColor = android.graphics.Color.BLACK
                toolbarColor = android.graphics.Color.BLACK
                cropMenuCropButtonTitle = "✓"
                allowRotation = true
                allowFlipping = false
                allowCounterRotation = false
                showCropOverlay = true
                autoZoomEnabled = true
                maxZoom = 4
                initialCropWindowPaddingRatio = 0.1f
                borderLineThickness = 3f
                borderLineColor = android.graphics.Color.WHITE
                borderCornerThickness = 5f
                borderCornerOffset = 5f
                borderCornerLength = 14f
                borderCornerColor = android.graphics.Color.WHITE
                guidelinesThickness = 1f
                guidelinesColor = android.graphics.Color.argb(170, 255, 255, 255)
                backgroundColor = android.graphics.Color.argb(119, 0, 0, 0)
                minCropWindowWidth = 40
                minCropWindowHeight = 40
                minCropResultWidth = 200
                minCropResultHeight = 200
                maxCropResultWidth = 999
                maxCropResultHeight = 999
                outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG
                outputCompressQuality = 90
                outputRequestWidth = 400
                outputRequestHeight = 400
                outputRequestSizeOptions = com.canhub.cropper.CropImageView.RequestSizeOptions.RESIZE_INSIDE
                noOutputImage = false
                customOutputUri = null
            }
        )
    }
} 