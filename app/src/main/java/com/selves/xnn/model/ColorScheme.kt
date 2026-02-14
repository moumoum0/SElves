package com.selves.xnn.model

enum class ColorScheme {
    APP_DEFAULT,
    WALLPAPER,
    CLOUD_FIELD
}

fun ColorScheme.getDisplayName(): String {
    return when (this) {
        ColorScheme.APP_DEFAULT -> "应用默认"
        ColorScheme.WALLPAPER -> "壁纸取色"
        ColorScheme.CLOUD_FIELD -> "云野"
    }
}

fun ColorScheme.getDescription(): String {
    return when (this) {
        ColorScheme.APP_DEFAULT -> "使用应用默认配色方案"
        ColorScheme.WALLPAPER -> "根据系统壁纸自动调整配色 (Android 12+)"
        ColorScheme.CLOUD_FIELD -> "云野 - 自然清新的绿色主题"
    }
}
