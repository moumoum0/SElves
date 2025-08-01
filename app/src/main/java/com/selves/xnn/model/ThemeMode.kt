package com.selves.xnn.model

/**
 * 主题模式枚举
 */
enum class ThemeMode {
    SYSTEM,  // 跟随系统
    LIGHT,   // 浅色模式
    DARK     // 深色模式
}

/**
 * 获取主题模式的显示名称
 */
fun ThemeMode.getDisplayName(): String {
    return when (this) {
        ThemeMode.SYSTEM -> "跟随系统"
        ThemeMode.LIGHT -> "浅色模式"
        ThemeMode.DARK -> "深色模式"
    }
} 