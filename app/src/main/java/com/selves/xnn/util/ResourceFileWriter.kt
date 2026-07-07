package com.selves.xnn.util

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

/**
 * 资源文件写入器
 *
 * 将备份数据保存到缓存目录，使用图片文件后缀。
 */
object ResourceFileWriter {

    /** 图片文件后缀 */
    private const val FILE_EXTENSION = ".png"

    /** 文件名日期格式 */
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * 生成类似图片的文件名。
     * 格式：IMG_yyyyMMdd_HHmmss_XXXX.png
     */
    fun generateResourceName(): String {
        val timestamp = fileNameDateFormat.format(Date())
        val randomSuffix = String.format(Locale.US, "%04X", Random().nextInt(0x10000))
        return "IMG_${timestamp}_${randomSuffix}${FILE_EXTENSION}"
    }

    /**
     * 将资源数据保存为文件。
     *
     * @param sourceData 原始资源数据
     * @return 写入的文件，失败返回 null
     */
    fun saveResource(sourceData: ByteArray): File? {
        val cacheDir = ImageCacheDirManager.getCacheLocation() ?: return null
        val fileName = generateResourceName()
        val targetFile = File(cacheDir, fileName)

        return try {
            FileOutputStream(targetFile).use { out ->
                out.write(sourceData)
            }
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            if (targetFile.exists()) {
                targetFile.delete()
            }
            null
        }
    }

    /**
     * 从文件中读取资源数据。
     *
     * @param file 资源文件
     * @return 资源数据，失败返回 null
     */
    fun extractResource(file: File): ByteArray? {
        if (!file.exists() || !file.isFile) return null
        return try {
            file.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从输入流中读取资源数据。
     */
    fun extractResource(input: InputStream): ByteArray? {
        return try {
            input.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 扫描缓存目录中的所有资源文件。
     *
     * @return 资源文件列表，按修改时间倒序排列
     */
    fun listResources(): List<File> {
        val cacheDir = ImageCacheDirManager.getCacheLocation() ?: return emptyList()
        return cacheDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(FILE_EXTENSION) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }
}