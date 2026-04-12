package com.selves.xnn.model

import android.content.Context
import androidx.annotation.StringRes
import com.selves.xnn.R

enum class ColorScheme(
    @StringRes val displayNameResId: Int,
    @StringRes val descriptionResId: Int
) {
    APP_DEFAULT(R.string.settings_color_default, R.string.settings_color_default_desc),
    WALLPAPER(R.string.settings_color_wallpaper, R.string.settings_color_wallpaper_desc),
    CLOUD_FIELD(R.string.settings_color_cloud_field, R.string.settings_color_cloud_field_desc)
}

fun ColorScheme.getDisplayName(context: Context): String {
    return context.getString(displayNameResId)
}

fun ColorScheme.getDescription(context: Context): String {
    return context.getString(descriptionResId)
}
