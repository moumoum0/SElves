package com.selves.xnn.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.selves.xnn.data.entity.*
import com.selves.xnn.data.Mappers.toEntity
import com.selves.xnn.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.gson.*
import java.lang.reflect.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LocalDateTime的Gson适配器
 */
class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        Log.d("LocalDateTimeAdapter", "开始序列化LocalDateTime: $src (类型: ${src?.javaClass?.name})")
        return try {
            if (src == null) {
                Log.d("LocalDateTimeAdapter", "LocalDateTime为null，返回JsonNull")
                JsonNull.INSTANCE
            } else {
                // 检查LocalDateTime对象的完整性
                try {
                    val year = src.year
                    val month = src.monthValue
                    val day = src.dayOfMonth
                    Log.v("LocalDateTimeAdapter", "LocalDateTime组件: $year-$month-$day")
                } catch (e: Exception) {
                    Log.e("LocalDateTimeAdapter", "LocalDateTime对象内部状态异常", e)
                    return JsonNull.INSTANCE
                }
                
                val formattedDate = src.format(formatter)
                Log.d("LocalDateTimeAdapter", "序列化LocalDateTime成功: $src -> $formattedDate")
                JsonPrimitive(formattedDate)
            }
        } catch (e: Exception) {
            Log.e("LocalDateTimeAdapter", "序列化LocalDateTime失败: $src, ${e.message}", e)
            // 序列化失败时返回null而不是空对象
            JsonNull.INSTANCE
        }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime? {
        return try {
            when {
                json == null || json.isJsonNull -> null
                json.isJsonPrimitive -> {
                    val dateString = json.asString
                    if (dateString.isNullOrBlank()) {
                        null
                    } else {
                        LocalDateTime.parse(dateString, formatter)
                    }
                }
                json.isJsonObject -> {
                    // 处理错误序列化的情况，JSON是空对象{}
                    Log.w("LocalDateTimeAdapter", "LocalDateTime被序列化为JsonObject，使用当前时间作为fallback")
                    LocalDateTime.now()
                }
                else -> {
                    Log.w("LocalDateTimeAdapter", "未知的JSON类型: ${json.javaClass.simpleName}")
                    LocalDateTime.now()
                }
            }
        } catch (e: Exception) {
            Log.e("LocalDateTimeAdapter", "解析LocalDateTime失败: ${json?.toString()}, ${json?.javaClass?.simpleName}, ${e.message}", e)
            // 如果解析失败，返回当前时间作为fallback
            LocalDateTime.now()
        }
    }
}

/**
 * 用户偏好设置备份数据
 */
data class PreferencesBackupData(
    val currentMemberId: String? = null,
    val themeMode: String = "SYSTEM",
    val quickMemberSwitchEnabled: Boolean = false
)

/**
 * 备份数据类，包含所有需要备份的数据
 */
data class BackupData(
    val version: Int = BACKUP_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val members: List<MemberEntity> = emptyList(),
    val chatGroups: List<ChatGroupEntity> = emptyList(),
    val messages: List<MessageEntity> = emptyList(),
    val messageReadStatus: List<MessageReadStatusEntity> = emptyList(),
    val todos: List<TodoEntity> = emptyList(),
    val dynamics: List<DynamicEntity> = emptyList(),
    val dynamicComments: List<DynamicCommentEntity> = emptyList(),
    val dynamicLikes: List<DynamicLikeEntity> = emptyList(),
    val votes: List<VoteEntity> = emptyList(),
    val voteOptions: List<VoteOptionEntity> = emptyList(),
    val voteRecords: List<VoteRecordEntity> = emptyList(),
    val systems: List<SystemEntity> = emptyList(),
    val onlineStatus: List<OnlineStatusEntity> = emptyList(),
    val preferences: PreferencesBackupData = PreferencesBackupData()
) {
    companion object {
        const val BACKUP_VERSION = 2  // 增加版本号，因为添加了新字段
        // 如果后续需要扩展其他静态常量，可在此处添加
    }
}

/**
 * 备份结果类
 */
sealed class BackupResult {
    object Success : BackupResult()
    data class Error(val message: String, val exception: Throwable? = null) : BackupResult()
}

/**
 * 备份服务类，负责数据的导出和导入
 */
@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val memberPreferences: MemberPreferences
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .serializeNulls() // 确保null值被正确序列化
        .create().also {
            Log.d(TAG, "Gson配置完成，已注册LocalDateTimeAdapter")
        }

    companion object {
        private const val TAG = "BackupService"
        private const val BACKUP_FILE_NAME = "backup_data.json"
        private const val IMAGES_FOLDER = "images/"
        private const val DATABASE_FOLDER = "database/"
        private const val CACHE_FOLDER = "cache/"
        private const val OTHER_FILES_FOLDER = "files/"
        // 兼容旧版本备份所需的字段常量
        private val REQUIRED_ARRAY_FIELDS = listOf(
            "members",
            "chatGroups",
            "messages",
            "messageReadStatus",
            "todos",
            "dynamics",
            "dynamicComments",
            "dynamicLikes",
            "votes",
            "voteOptions",
            "voteRecords",
            "systems",
            "onlineStatus"
        )
        private const val PREFERENCES_FIELD = "preferences"
    }

    /**
     * 导出备份到ZIP文件
     */
    suspend fun exportBackup(outputUri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始导出备份...")
            
            // 收集所有数据
            val backupData = collectAllData()
            
            // 创建ZIP文件
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->
                    // 添加备份数据JSON文件
                    Log.d(TAG, "开始JSON序列化...")
                    val jsonData = gson.toJson(backupData)
                    Log.d(TAG, "JSON序列化完成，大小: ${jsonData.length} 字符")
                    Log.d(TAG, "JSON片段预览: ${jsonData.take(500)}...")
                    
                    // 检查JSON中是否包含空对象
                    val emptyObjectCount = jsonData.split("{}").size - 1
                    if (emptyObjectCount > 0) {
                        Log.w(TAG, "JSON中发现 $emptyObjectCount 个空对象 {}")
                        // 找到第一个空对象的上下文
                        val emptyObjectIndex = jsonData.indexOf("{}")
                        if (emptyObjectIndex >= 0) {
                            val start = maxOf(0, emptyObjectIndex - 100)
                            val end = minOf(jsonData.length, emptyObjectIndex + 100)
                            Log.w(TAG, "空对象上下文: ${jsonData.substring(start, end)}")
                        }
                    }
                    
                    val jsonEntry = ZipEntry(BACKUP_FILE_NAME)
                    zipOut.putNextEntry(jsonEntry)
                    zipOut.write(jsonData.toByteArray())
                    zipOut.closeEntry()

                    // 添加图片文件
                    addImagesToZip(zipOut, backupData)
                    
                    // 添加其他重要文件
                    addOtherFilesToZip(zipOut)
                }
            }
            
            Log.d(TAG, "备份导出成功")
            BackupResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "导出备份失败", e)
            BackupResult.Error("导出失败: ${e.message}", e)
        }
    }

    /**
     * 兼容旧版本备份：为缺失的字段填充默认值，避免Gson因非空参数为null抛异常。
     */
    private fun upgradeLegacyBackupJson(originalJson: String): String {
        return try {
            val jsonElement = JsonParser.parseString(originalJson)
            if (!jsonElement.isJsonObject) return originalJson // 非对象，直接返回
            val root = jsonElement.asJsonObject

            // 设置默认版本号
            if (!root.has("version")) {
                root.addProperty("version", 1)
            }

            // 确保所有必需的数组字段存在
            REQUIRED_ARRAY_FIELDS.forEach { field ->
                if (!root.has(field) || root.get(field).isJsonNull) {
                    root.add(field, JsonArray())
                }
            }

            // 确保preferences字段存在
            if (!root.has(PREFERENCES_FIELD) || root.get(PREFERENCES_FIELD).isJsonNull) {
                val prefObj = JsonObject()
                prefObj.add("currentMemberId", JsonNull.INSTANCE)
                prefObj.addProperty("themeMode", ThemeMode.SYSTEM.name)
                prefObj.addProperty("quickMemberSwitchEnabled", false)
                root.add(PREFERENCES_FIELD, prefObj)
            }

            root.toString()
        } catch (e: Exception) {
            Log.w(TAG, "升级旧版本备份JSON失败，使用原始JSON: ${e.message}")
            originalJson
        }
    }

    /**
     * 从ZIP文件导入备份
     */
    suspend fun importBackup(inputUri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始导入备份...")
            
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var backupData: BackupData? = null
                    val foundEntries = mutableListOf<String>()
                    var jsonParseError: Exception? = null
                    var jsonContent: String? = null
                    
                    // 读取ZIP文件内容
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        foundEntries.add(entry.name)
                        Log.d(TAG, "发现ZIP条目: ${entry.name}, 大小: ${entry.size}")
                        
                        when {
                            entry.name == BACKUP_FILE_NAME -> {
                                // 读取备份数据
                                val jsonData = zipIn.readBytes().toString(Charsets.UTF_8)
                                jsonContent = jsonData
                                Log.d(TAG, "读取JSON数据，大小: ${jsonData.length} 字符")
                                
                                if (jsonData.isEmpty()) {
                                    Log.e(TAG, "JSON文件为空")
                                } else {
                                    Log.d(TAG, "JSON片段预览: ${jsonData.take(500)}...")
                                    
                                    val upgradedJson = upgradeLegacyBackupJson(jsonData)
                                    try {
                                        backupData = gson.fromJson(upgradedJson, BackupData::class.java)
                                        Log.d(TAG, "JSON反序列化成功 (升级后)")
                                        
                                        // 验证备份数据的基本结构
                                        if (backupData != null) {
                                            Log.d(TAG, "备份数据验证:")
                                            Log.d(TAG, "  - 版本: ${backupData.version}")
                                            Log.d(TAG, "  - 时间戳: ${backupData.timestamp}")
                                            Log.d(TAG, "  - 成员数: ${backupData.members.size}")
                                            Log.d(TAG, "  - 群组数: ${backupData.chatGroups.size}")
                                            Log.d(TAG, "  - 消息数: ${backupData.messages.size}")
                                            Log.d(TAG, "  - 动态数: ${backupData.dynamics.size}")
                                            Log.d(TAG, "  - 投票数: ${backupData.votes.size}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "JSON反序列化失败: ${e.message}", e)
                                        jsonParseError = e
                                        
                                        // 尝试分析JSON内容问题
                                        if (jsonData.startsWith("{") && jsonData.endsWith("}")) {
                                            Log.d(TAG, "JSON格式看起来正确（以{}包围）")
                                        } else {
                                            Log.e(TAG, "JSON格式可能有问题，开头: ${jsonData.take(10)}, 结尾: ${jsonData.takeLast(10)}")
                                        }
                                    }
                                }
                            }
                            entry.name.startsWith(IMAGES_FOLDER) -> {
                                // 恢复图片文件
                                restoreImageFile(entry.name, zipIn.readBytes())
                            }
                            entry.name.startsWith(OTHER_FILES_FOLDER) -> {
                                // 恢复其他文件
                                restoreOtherFile(entry.name, zipIn.readBytes())
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                    
                    Log.d(TAG, "ZIP文件解析完成，共找到 ${foundEntries.size} 个条目:")
                    foundEntries.forEach { entryName ->
                        Log.d(TAG, "  - $entryName")
                    }
                    
                    // 导入数据到数据库
                    when {
                        backupData != null -> {
                            Log.d(TAG, "找到有效的备份数据，开始导入...")
                            importDataToDatabase(backupData)
                        }
                        jsonParseError != null -> {
                            val errorMsg = "备份文件JSON解析失败: ${jsonParseError.message}"
                            Log.e(TAG, errorMsg)
                            Log.e(TAG, "JSON内容长度: ${jsonContent?.length ?: 0}")
                            throw IllegalStateException(errorMsg, jsonParseError)
                        }
                        !foundEntries.contains(BACKUP_FILE_NAME) -> {
                            val errorMsg = "备份文件中未找到数据文件 '$BACKUP_FILE_NAME'。找到的文件: ${foundEntries.joinToString()}"
                            Log.e(TAG, errorMsg)
                            throw IllegalStateException(errorMsg)
                        }
                        jsonContent.isNullOrEmpty() -> {
                            val errorMsg = "备份数据文件 '$BACKUP_FILE_NAME' 为空"
                            Log.e(TAG, errorMsg)
                            throw IllegalStateException(errorMsg)
                        }
                        else -> {
                            val errorMsg = "备份文件中未找到有效的备份数据，原因未知"
                            Log.e(TAG, errorMsg)
                            throw IllegalStateException(errorMsg)
                        }
                    }
                }
            } ?: throw IllegalStateException("无法打开备份文件输入流")
            
            Log.d(TAG, "备份导入成功")
            BackupResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "导入备份失败", e)
            BackupResult.Error("导入失败: ${e.message}", e)
        }
    }

    /**
     * 收集所有需要备份的数据
     */
    private suspend fun collectAllData(): BackupData {
        Log.d(TAG, "开始收集备份数据...")
        
        // 收集用户偏好设置
        val preferences = PreferencesBackupData(
            currentMemberId = memberPreferences.currentMemberId.first(),
            themeMode = memberPreferences.themeMode.first().name,
            quickMemberSwitchEnabled = memberPreferences.quickMemberSwitchEnabled.first()
        )
        Log.d(TAG, "收集用户偏好设置完成: currentMemberId=${preferences.currentMemberId}")
        
        val members = database.memberDao().getAllMembersSync()
        val chatGroups = database.chatGroupDao().getAllGroupsSync()
        val messages = database.messageDao().getAllMessagesSync()
        val messageReadStatus = database.messageReadStatusDao().getAllReadStatusSync()
        val todos = database.todoDao().getAllTodosSync()
        val dynamics = database.dynamicDao().getAllDynamicsSync()
        val dynamicComments = database.dynamicDao().getAllCommentsSync()
        val dynamicLikes = database.dynamicDao().getAllLikesSync()
        val votes = database.voteDao().getAllVotesSync()
        val voteOptions = database.voteDao().getAllVoteOptionsSync()
        val voteRecords = database.voteDao().getAllVoteRecordsSync()
        val systems = database.systemDao().getAllSystemsSync()
        val onlineStatus = database.onlineStatusDao().getAllOnlineStatusSync()
        
        Log.d(TAG, "数据收集统计:")
        Log.d(TAG, "  - 成员: ${members.size}")
        Log.d(TAG, "  - 群组: ${chatGroups.size}")
        Log.d(TAG, "  - 消息: ${messages.size}")
        Log.d(TAG, "  - 已读状态: ${messageReadStatus.size}")
        Log.d(TAG, "  - 待办事项: ${todos.size}")
        Log.d(TAG, "  - 动态: ${dynamics.size}")
        Log.d(TAG, "  - 动态评论: ${dynamicComments.size}")
        Log.d(TAG, "  - 动态点赞: ${dynamicLikes.size}")
        Log.d(TAG, "  - 投票: ${votes.size}")
        Log.d(TAG, "  - 投票选项: ${voteOptions.size}")
        Log.d(TAG, "  - 投票记录: ${voteRecords.size}")
        Log.d(TAG, "  - 系统: ${systems.size}")
        Log.d(TAG, "  - 在线状态: ${onlineStatus.size}")
        
        // 检查LocalDateTime字段
        dynamics.forEach { dynamic ->
            Log.v(TAG, "动态 ${dynamic.id}: createdAt=${dynamic.createdAt}, updatedAt=${dynamic.updatedAt}")
            Log.v(TAG, "  createdAt类型: ${dynamic.createdAt?.javaClass?.name}")
            Log.v(TAG, "  updatedAt类型: ${dynamic.updatedAt?.javaClass?.name}")
        }
        votes.forEach { vote ->
            Log.v(TAG, "投票 ${vote.id}: createdAt=${vote.createdAt}, endTime=${vote.endTime}")
            Log.v(TAG, "  createdAt类型: ${vote.createdAt?.javaClass?.name}")
            Log.v(TAG, "  endTime类型: ${vote.endTime?.javaClass?.name}")
        }
        
        return BackupData(
            members = members,
            chatGroups = chatGroups,
            messages = messages,
            messageReadStatus = messageReadStatus,
            todos = todos,
            dynamics = dynamics,
            dynamicComments = dynamicComments,
            dynamicLikes = dynamicLikes,
            votes = votes,
            voteOptions = voteOptions,
            voteRecords = voteRecords,
            systems = systems,
            onlineStatus = onlineStatus,
            preferences = preferences
        )
    }

    /**
     * 添加图片文件到ZIP
     */
    private fun addImagesToZip(zipOut: ZipOutputStream, backupData: BackupData) {
        // 收集所有图片路径
        val imagePaths = mutableSetOf<String>()
        
        // 从消息中收集图片路径
        backupData.messages.forEach { message ->
            message.imagePath?.let { path ->
                if (path.isNotBlank()) imagePaths.add(path)
            }
        }
        
        // 从成员头像中收集图片路径
        backupData.members.forEach { member ->
            member.avatarUrl?.let { path ->
                if (path.isNotBlank() && !path.startsWith("http")) {
                    imagePaths.add(path)
                }
            }
        }
        
        // 从动态中收集图片路径
        backupData.dynamics.forEach { dynamic ->
            // images字段已经是List<String>类型
            imagePaths.addAll(dynamic.images.filter { it.isNotBlank() && !it.startsWith("http") })
            
            // 动态作者头像
            dynamic.authorAvatar?.let { path ->
                if (path.isNotBlank() && !path.startsWith("http")) {
                    imagePaths.add(path)
                }
            }
        }
        
        // 从动态评论中收集作者头像
        backupData.dynamicComments.forEach { comment ->
            comment.authorAvatar?.let { path ->
                if (path.isNotBlank() && !path.startsWith("http")) {
                    imagePaths.add(path)
                }
            }
        }
        
        // 从投票中收集作者头像
        backupData.votes.forEach { vote ->
            vote.authorAvatar?.let { path ->
                if (path.isNotBlank() && !path.startsWith("http")) {
                    imagePaths.add(path)
                }
            }
        }
        
        // 从投票记录中收集用户头像
        backupData.voteRecords.forEach { record ->
            record.userAvatar?.let { path ->
                if (path.isNotBlank() && !path.startsWith("http")) {
                    imagePaths.add(path)
                }
            }
        }
        
        // 从系统中收集头像
        backupData.systems.forEach { system ->
            system.avatarUrl?.let { path ->
                if (path.isNotBlank() && !path.startsWith("http")) {
                    imagePaths.add(path)
                }
            }
        }
        
        Log.d(TAG, "收集到 ${imagePaths.size} 个图片路径准备备份")
        if (imagePaths.isNotEmpty()) {
            Log.d(TAG, "图片路径列表: ${imagePaths.take(10).joinToString()}")
        }
        
        // 添加图片文件到ZIP
        var successCount = 0
        imagePaths.forEach { imagePath ->
            try {
                // 处理绝对路径和相对路径
                val imageFile = if (imagePath.startsWith("/")) {
                    // 绝对路径，直接使用
                    File(imagePath)
                } else {
                    // 相对路径，相对于files目录
                    File(context.filesDir, imagePath)
                }
                
                if (imageFile.exists()) {
                    // 获取相对于files目录的路径用于ZIP中的路径
                    val relativeImagePath = if (imagePath.startsWith("/")) {
                        // 如果是绝对路径，获取相对于filesDir的路径
                        val filesDir = context.filesDir.absolutePath
                        if (imagePath.startsWith(filesDir)) {
                            imagePath.substring(filesDir.length + 1) // +1 去掉开头的 /
                        } else {
                            // 如果不在filesDir下，使用文件名
                            imageFile.name
                        }
                    } else {
                        imagePath
                    }
                    
                    val entry = ZipEntry("$IMAGES_FOLDER$relativeImagePath")
                    zipOut.putNextEntry(entry)
                    imageFile.inputStream().use { input ->
                        input.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                    successCount++
                    Log.d(TAG, "成功备份图片: $relativeImagePath")
                } else {
                    Log.w(TAG, "图片文件不存在: $imagePath")
                }
            } catch (e: Exception) {
                Log.e(TAG, "添加图片文件失败: $imagePath, ${e.message}", e)
            }
        }
        
        Log.d(TAG, "图片备份完成，成功备份 $successCount/${imagePaths.size} 个文件")
    }
    
    /**
     * 添加其他重要文件到ZIP
     */
    private fun addOtherFilesToZip(zipOut: ZipOutputStream) {
        try {
            val filesDir = context.filesDir
            val filesToBackup = mutableListOf<File>()
            
            // 递归收集所有文件（除了已经处理的图片）
            collectImportantFiles(filesDir, filesToBackup)
            
            Log.d(TAG, "准备备份 ${filesToBackup.size} 个其他文件")
            
            var successCount = 0
            filesToBackup.forEach { file ->
                try {
                    if (file.exists() && file.isFile) {
                        val relativePath = file.relativeTo(filesDir).path
                        val entry = ZipEntry("$OTHER_FILES_FOLDER$relativePath")
                        zipOut.putNextEntry(entry)
                        file.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                        successCount++
                        Log.d(TAG, "成功备份文件: $relativePath")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "备份文件失败: ${file.path}, ${e.message}", e)
                }
            }
            
            Log.d(TAG, "其他文件备份完成，成功备份 $successCount/${filesToBackup.size} 个文件")
        } catch (e: Exception) {
            Log.e(TAG, "备份其他文件时发生错误: ${e.message}", e)
        }
    }
    
    /**
     * 递归收集重要文件（排除图片文件）
     */
    private fun collectImportantFiles(dir: File, fileList: MutableList<File>) {
        try {
            if (!dir.exists() || !dir.isDirectory) return
            
            dir.listFiles()?.forEach { file ->
                when {
                    file.isDirectory -> {
                        // 跳过已经处理的图片目录和缓存目录
                        if (file.name !in listOf("avatars", "message_images", "image_cache")) {
                            collectImportantFiles(file, fileList)
                        }
                    }
                    file.isFile -> {
                        // 跳过临时文件和系统文件
                        if (!file.name.startsWith(".") && 
                            !file.name.endsWith(".tmp") &&
                            !file.name.endsWith(".lock")) {
                            fileList.add(file)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "收集文件时出错: ${dir.path}, ${e.message}")
        }
    }
    
    /**
     * 恢复其他文件
     */
    private fun restoreOtherFile(filePath: String, fileData: ByteArray) {
        try {
            val relativePath = filePath.removePrefix(OTHER_FILES_FOLDER)
            val targetFile = File(context.filesDir, relativePath)
            
            // 确保父目录存在
            targetFile.parentFile?.mkdirs()
            
            // 写入文件数据
            targetFile.writeBytes(fileData)
            
            Log.d(TAG, "成功恢复文件: $relativePath")
        } catch (e: Exception) {
            Log.e(TAG, "恢复文件失败: $filePath, ${e.message}", e)
        }
    }

    /**
     * 恢复图片文件
     */
    private fun restoreImageFile(imagePath: String, imageData: ByteArray) {
        try {
            val relativePath = imagePath.removePrefix(IMAGES_FOLDER)
            val imageFile = File(context.filesDir, relativePath)
            
            // 确保父目录存在
            imageFile.parentFile?.mkdirs()
            
            // 写入文件数据
            imageFile.writeBytes(imageData)
            
            Log.d(TAG, "成功恢复图片文件: $relativePath")
        } catch (e: Exception) {
            Log.e(TAG, "恢复图片文件失败: $imagePath, ${e.message}", e)
        }
    }

    /**
     * 将备份数据导入到数据库
     */
    private suspend fun importDataToDatabase(backupData: BackupData) {
        // 清空现有数据
        clearAllData()
        
        // 导入数据
        Log.d(TAG, "开始导入数据到数据库...")
        
        Log.d(TAG, "导入 ${backupData.members.size} 个成员")
        backupData.members.forEach { member ->
            database.memberDao().insertMember(member)
        }
        
        Log.d(TAG, "导入 ${backupData.chatGroups.size} 个群组")
        backupData.chatGroups.forEach { group ->
            database.chatGroupDao().insertGroup(group)
        }
        
        Log.d(TAG, "导入 ${backupData.messages.size} 个消息")
        backupData.messages.forEach { message ->
            database.messageDao().insertMessage(message)
        }
        
        Log.d(TAG, "导入 ${backupData.messageReadStatus.size} 个已读状态")
        backupData.messageReadStatus.forEach { status ->
            database.messageReadStatusDao().insertReadStatus(status)
        }
        
        Log.d(TAG, "导入 ${backupData.todos.size} 个待办事项")
        backupData.todos.forEach { todo ->
            database.todoDao().insertTodo(todo)
        }
        
        Log.d(TAG, "导入 ${backupData.dynamics.size} 个动态")
        backupData.dynamics.forEach { dynamic ->
            try {
                Log.d(TAG, "导入动态: ${dynamic.id}, createdAt=${dynamic.createdAt}, updatedAt=${dynamic.updatedAt}")
                database.dynamicDao().insertDynamic(dynamic)
            } catch (e: Exception) {
                Log.e(TAG, "导入动态失败: ${dynamic.id}, ${e.message}", e)
            }
        }
        
        Log.d(TAG, "导入 ${backupData.dynamicComments.size} 个动态评论")
        backupData.dynamicComments.forEach { comment ->
            try {
                database.dynamicDao().insertComment(comment)
            } catch (e: Exception) {
                Log.e(TAG, "导入动态评论失败: ${comment.id}, ${e.message}", e)
            }
        }
        
        Log.d(TAG, "导入 ${backupData.dynamicLikes.size} 个动态点赞")
        backupData.dynamicLikes.forEach { like ->
            try {
                database.dynamicDao().insertLike(like)
            } catch (e: Exception) {
                Log.e(TAG, "导入动态点赞失败: ${like.id}, ${e.message}", e)
            }
        }
        
        Log.d(TAG, "导入 ${backupData.votes.size} 个投票")
        backupData.votes.forEach { vote ->
            try {
                Log.d(TAG, "导入投票: ${vote.id}, createdAt=${vote.createdAt}, endTime=${vote.endTime}")
                database.voteDao().insertVote(vote)
            } catch (e: Exception) {
                Log.e(TAG, "导入投票失败: ${vote.id}, ${e.message}", e)
            }
        }
        
        Log.d(TAG, "导入 ${backupData.voteOptions.size} 个投票选项")
        backupData.voteOptions.forEach { option ->
            try {
                database.voteDao().insertVoteOption(option)
            } catch (e: Exception) {
                Log.e(TAG, "导入投票选项失败: ${option.id}, ${e.message}", e)
            }
        }
        
        Log.d(TAG, "导入 ${backupData.voteRecords.size} 个投票记录")
        backupData.voteRecords.forEach { record ->
            try {
                database.voteDao().insertVoteRecord(record)
            } catch (e: Exception) {
                Log.e(TAG, "导入投票记录失败: ${record.id}, ${e.message}", e)
            }
        }
        
        Log.d(TAG, "导入 ${backupData.systems.size} 个系统")
        backupData.systems.forEach { system ->
            try {
                database.systemDao().insertSystem(system)
            } catch (e: Exception) {
                Log.e(TAG, "导入系统失败: ${system.id}, ${e.message}", e)
            }
        }
        
        Log.d(TAG, "导入 ${backupData.onlineStatus.size} 个在线状态")
        backupData.onlineStatus.forEach { status ->
            try {
                database.onlineStatusDao().insertOnlineStatus(status)
            } catch (e: Exception) {
                Log.e(TAG, "导入在线状态失败: ${status.id}, ${e.message}", e)
            }
        }
        
        // 恢复用户偏好设置
        restorePreferences(backupData.preferences)
        
        // 同步所有用户信息，确保数据一致性
        Log.d(TAG, "开始同步用户信息，确保数据一致性...")
        syncUserInfoAfterRestore()
        
        Log.d(TAG, "数据导入完成")
    }
    
    /**
     * 恢复用户偏好设置
     */
    private suspend fun restorePreferences(preferences: PreferencesBackupData) {
        try {
            // 恢复当前成员ID
            preferences.currentMemberId?.let { memberId ->
                memberPreferences.saveCurrentMemberId(memberId)
                Log.d(TAG, "恢复当前成员ID: $memberId")
            } ?: run {
                memberPreferences.clearCurrentMemberId()
                Log.d(TAG, "清除当前成员ID")
            }
            
            // 恢复主题模式
            try {
                val themeMode = ThemeMode.valueOf(preferences.themeMode)
                memberPreferences.saveThemeMode(themeMode)
                Log.d(TAG, "恢复主题模式: ${preferences.themeMode}")
            } catch (e: IllegalArgumentException) {
                // 如果主题模式无效，设置为默认值
                memberPreferences.saveThemeMode(ThemeMode.SYSTEM)
                Log.w(TAG, "无效的主题模式: ${preferences.themeMode}，设置为默认值")
            }
            
            // 恢复快捷切换成员设置
            memberPreferences.saveQuickMemberSwitchEnabled(preferences.quickMemberSwitchEnabled)
            Log.d(TAG, "恢复快捷切换成员设置: ${preferences.quickMemberSwitchEnabled}")
            
        } catch (e: Exception) {
            Log.e(TAG, "恢复用户偏好设置失败: ${e.message}", e)
        }
    }

    /**
     * 恢复备份后同步用户信息，确保数据一致性
     */
    private suspend fun syncUserInfoAfterRestore() {
        try {
            // 获取所有成员
            val members = database.memberDao().getAllMembersSync()
            Log.d(TAG, "开始为 ${members.size} 个成员同步用户信息")
            
            var syncCount = 0
            members.forEach { member ->
                try {
                    // 同步动态表中的用户信息
                    database.dynamicDao().updateAuthorInfo(member.id, member.name, member.avatarUrl)
                    database.dynamicDao().updateCommentAuthorInfo(member.id, member.name, member.avatarUrl)
                    
                    // 同步投票表中的用户信息
                    database.voteDao().updateVoteAuthorInfo(member.id, member.name, member.avatarUrl)
                    database.voteDao().updateVoteRecordUserInfo(member.id, member.name, member.avatarUrl)
                    
                    syncCount++
                    Log.d(TAG, "已同步用户信息: ${member.id} - ${member.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "同步用户信息失败: ${member.id} - ${member.name}, ${e.message}", e)
                }
            }
            
            Log.d(TAG, "用户信息同步完成，成功同步 $syncCount/${members.size} 个用户")
        } catch (e: Exception) {
            Log.e(TAG, "同步用户信息时发生错误: ${e.message}", e)
        }
    }

    /**
     * 清空所有数据（谨慎使用）
     */
    private suspend fun clearAllData() {
        // 按照外键依赖关系的逆序删除
        database.onlineStatusDao().deleteAll()
        database.voteDao().deleteAllVoteRecords()
        database.voteDao().deleteAllVoteOptions()
        database.voteDao().deleteAllVotes()
        database.dynamicDao().deleteAllLikes()
        database.dynamicDao().deleteAllComments()
        database.dynamicDao().deleteAllDynamics()
        database.todoDao().deleteAll()
        database.messageReadStatusDao().deleteAll()
        database.messageDao().deleteAll()
        database.chatGroupDao().deleteAll()
        database.systemDao().deleteAll()
        database.memberDao().deleteAll()
    }
} 