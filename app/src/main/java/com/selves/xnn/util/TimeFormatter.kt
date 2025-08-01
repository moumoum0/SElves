package com.selves.xnn.util

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 统一的时间格式化工具类
 */
object TimeFormatter {
    
    /**
     * 格式化时间戳
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的时间字符串
     * 规则：
     * - 1小时内：显示"几分钟前"（刚刚）
     * - 当天内：显示时间（如"14:30"）
     * - 其他：显示日期+时间（如"12月25日 14:30"）
     */
    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            // 1小时内显示相对时间
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
            
            // 当天内显示时间
            isToday(timestamp) -> {
                val date = Date(timestamp)
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
            
            // 其他显示日期+时间
            else -> {
                val date = Date(timestamp)
                if (isThisYear(timestamp)) {
                    SimpleDateFormat("M月d日 HH:mm", Locale.getDefault()).format(date)
                } else {
                    SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.getDefault()).format(date)
                }
            }
        }
    }
    
    /**
     * 格式化LocalDateTime
     * @param dateTime LocalDateTime对象
     * @return 格式化后的时间字符串
     */
    fun formatDateTime(dateTime: LocalDateTime): String {
        val timestamp = dateTime.toEpochSecond(ZoneOffset.UTC) * 1000
        return formatTimestamp(timestamp)
    }
    
    /**
     * 简单的日期时间格式化（用于详情页面等）
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的时间字符串（如"2024年1月1日 14:30"）
     */
    fun formatDetailDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        return SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.getDefault()).format(date)
    }
    
    /**
     * 简单的日期时间格式化（用于详情页面等）
     * @param dateTime LocalDateTime对象
     * @return 格式化后的时间字符串
     */
    fun formatDetailDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm"))
    }
    
    /**
     * 判断时间戳是否是今天
     */
    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val tomorrow = today + 24 * 60 * 60 * 1000
        
        return timestamp >= today && timestamp < tomorrow
    }
    
    /**
     * 判断时间戳是否是今年
     */
    private fun isThisYear(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val thisYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = timestamp
        val timestampYear = calendar.get(Calendar.YEAR)
        
        return thisYear == timestampYear
    }
} 