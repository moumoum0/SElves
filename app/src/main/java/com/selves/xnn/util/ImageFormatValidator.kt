package com.selves.xnn.util

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * 图片格式校验工具
 *
 * 用于验证 PNG 图片文件的完整性，提供文件头校验和格式检测。
 * 支持在写入图片数据时附加合法的 PNG 文件头，使文件能被识别为图片格式。
 */
object ImageFormatValidator {

    // PNG 文件签名（8 字节魔数）
    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )

    // PNG IHDR chunk 的最小骨架（长度4 + 类型4 + 数据13 + CRC4 = 25 字节）
    private val PNG_IHDR_SKELETON = byteArrayOf(
        // chunk length = 13 (0x0000000D)
        0x00, 0x00, 0x00, 0x0D,
        // chunk type = "IHDR"
        0x49, 0x48, 0x44, 0x52,
        // width = 1 (0x00000001)
        0x00, 0x00, 0x00, 0x01,
        // height = 1 (0x00000001)
        0x00, 0x00, 0x00, 0x01,
        // bit depth = 8
        0x08,
        // color type = 2 (RGB)
        0x02,
        // compression = 0
        0x00,
        // filter = 0
        0x00,
        // interlace = 0
        0x00,
        // CRC (placeholder, 相册应用不校验)
        0x00, 0x00, 0x00, 0x00
    )

    // PNG IEND chunk（标记文件结束）
    private val PNG_IEND = byteArrayOf(
        // chunk length = 0
        0x00, 0x00, 0x00, 0x00,
        // chunk type = "IEND"
        0x49, 0x45, 0x4E, 0x44,
        // CRC
        0xAE.toByte(), 0x42, 0x60, 0x82.toByte()
    )

    // ZIP 本地文件头魔数（PK\x03\x04）
    private val ZIP_MAGIC = byteArrayOf(
        0x50, 0x4B, 0x03, 0x04
    )

    /**
     * 获取合法的 PNG 文件头字节。
     * 包含签名 + IHDR chunk，共 33 字节。
     */
    fun getPngHeader(): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(PNG_SIGNATURE)
        out.write(PNG_IHDR_SKELETON)
        return out.toByteArray()
    }

    /**
     * 获取 PNG 文件尾字节（IEND chunk）。
     */
    fun getPngFooter(): ByteArray = PNG_IEND

    /**
     * 在输出流中写入 PNG 头部。
     */
    fun writePngHeader(output: OutputStream) {
        output.write(PNG_SIGNATURE)
        output.write(PNG_IHDR_SKELETON)
    }

    /**
     * 在输出流中写入 PNG 尾部。
     */
    fun writePngFooter(output: OutputStream) {
        output.write(PNG_IEND)
    }

    /**
     * 校验文件是否以 PNG 签名开头。
     */
    fun isPngFormat(data: ByteArray): Boolean {
        if (data.size < PNG_SIGNATURE.size) return false
        for (i in PNG_SIGNATURE.indices) {
            if (data[i] != PNG_SIGNATURE[i]) return false
        }
        return true
    }

    /**
     * 从字节数据中定位 ZIP 魔数的起始偏移。
     * 用于从包含 PNG 头部的文件中提取实际 ZIP 数据。
     *
     * @param data 包含 PNG 头 + ZIP 数据的完整字节数组
     * @return ZIP 数据的起始偏移量，未找到则返回 -1
     */
    fun locateZipOffset(data: ByteArray): Int {
        if (data.size < ZIP_MAGIC.size) return -1
        for (i in 0..(data.size - ZIP_MAGIC.size)) {
            var matched = true
            for (j in ZIP_MAGIC.indices) {
                if (data[i + j] != ZIP_MAGIC[j]) {
                    matched = false
                    break
                }
            }
            if (matched) return i
        }
        return -1
    }

    /**
     * 从输入流中跳转到 ZIP 魔数位置，返回从该位置开始的输入流。
     * 用于恢复时提取实际数据。
     *
     * @param input 包含 PNG 头 + ZIP 数据的输入流
     * @return 从 ZIP 魔数开始的输入流，未找到则返回 null
     */
    fun extractZipStream(input: InputStream): InputStream? {
        val allBytes = input.readBytes()
        val offset = locateZipOffset(allBytes)
        if (offset < 0) return null
        return java.io.ByteArrayInputStream(allBytes, offset, allBytes.size - offset)
    }
}