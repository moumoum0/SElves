package com.selves.xnn.util

import com.github.promeg.pinyinhelper.Pinyin

/**
 * 拼音工具类，用于处理中文拼音转换和排序
 */
object PinyinUtils {
    
    /**
     * 获取字符串的首字母，支持中文和英文
     * @param text 输入文本
     * @return 首字母，中文返回拼音首字母，英文返回首字母，数字和特殊字符返回"#"
     */
    fun getFirstLetter(text: String): String {
        if (text.isEmpty()) return "#"
        
        val firstChar = text.first()
        
        return when {
            // 中文字符，转换为拼音首字母
            Pinyin.isChinese(firstChar) -> {
                val pinyin = Pinyin.toPinyin(firstChar)
                if (pinyin.isNotEmpty() && pinyin[0].isLetter()) {
                    pinyin[0].uppercaseChar().toString()
                } else {
                    "#"
                }
            }
            // 英文字母
            firstChar.isLetter() -> firstChar.uppercaseChar().toString()
            // 数字和其他字符
            else -> "#"
        }
    }
    
    /**
     * 获取字符串的完整拼音，用于排序
     * @param text 输入文本
     * @return 完整拼音字符串，中文转拼音，英文保持原样
     */
    fun getPinyin(text: String): String {
        if (text.isEmpty()) return text
        
        // 使用TinyPinyin的字符串转换API，不使用分隔符
        return Pinyin.toPinyin(text, "").lowercase()
    }
    
    /**
     * 按拼音排序字符串列表
     * @param list 待排序的字符串列表
     * @return 按拼音排序后的列表
     */
    fun sortByPinyin(list: List<String>): List<String> {
        return list.sortedWith { a, b ->
            val pinyinA = getPinyin(a)
            val pinyinB = getPinyin(b)
            pinyinA.compareTo(pinyinB)
        }
    }
    
    /**
     * 生成可用的索引字母列表
     * @param texts 文本列表
     * @return 排序后的首字母列表
     */
    fun getAvailableIndexLetters(texts: List<String>): List<String> {
        return texts
            .map { getFirstLetter(it) }
            .distinct()
            .sorted()
    }
    
    /**
     * 检查文本是否匹配搜索关键词（支持拼音搜索）
     * @param text 原文本
     * @param keyword 搜索关键词
     * @return 是否匹配
     */
    fun matchesKeyword(text: String, keyword: String): Boolean {
        if (keyword.isEmpty()) return true
        
        val lowerKeyword = keyword.lowercase()
        
        // 原文本匹配
        if (text.lowercase().contains(lowerKeyword)) {
            return true
        }
        
        // 拼音匹配
        val pinyin = getPinyin(text)
        if (pinyin.contains(lowerKeyword)) {
            return true
        }
        
        // 拼音首字母匹配
        val pinyinFirstLetters = buildString {
            for (char in text) {
                if (Pinyin.isChinese(char)) {
                    val charPinyin = Pinyin.toPinyin(char)
                    if (charPinyin.isNotEmpty() && charPinyin[0].isLetter()) {
                        append(charPinyin[0].lowercase())
                    }
                } else if (char.isLetter()) {
                    append(char.lowercase())
                } else if (char.isDigit()) {
                    append(char)
                }
            }
        }
        
        return pinyinFirstLetters.contains(lowerKeyword)
    }
}
