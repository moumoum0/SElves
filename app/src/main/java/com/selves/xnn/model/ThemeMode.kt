package com.selves.xnn.model

import android.content.Context
import androidx.annotation.StringRes
import com.selves.xnn.R

/**
 * 主题模式枚举
 */
enum class ThemeMode(@StringRes val displayNameResId: Int) {
    SYSTEM(R.string.settings_theme_system),  // 跟随系统
    LIGHT(R.string.settings_theme_light),    // 浅色模式
    DARK(R.string.settings_theme_dark)       // 深色模式
}

/**
 * 获取主题模式的显示名称
 */
fun ThemeMode.getDisplayName(context: Context): String {
    return context.getString(displayNameResId)
}