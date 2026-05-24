package com.selves.xnn.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.selves.xnn.R
import java.util.*

object LanguageManager {
    
    /**
     * 应用语言设置到Context
     */
    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "en" -> Locale.ENGLISH
            "system" -> getSystemLocale()
            else -> getSystemLocale()
        }
        
        return updateContextLocale(context, locale)
    }
    
    /**
     * 获取系统默认语言
     */
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale
        }
    }
    
    /**
     * 更新Context的语言设置
     */
    private fun updateContextLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.setLocales(android.os.LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * 获取语言显示名称
     */
    fun getLanguageDisplayName(context: Context, languageCode: String): String {
        return when (languageCode) {
            "zh" -> context.getString(R.string.language_chinese_simplified)
            "en" -> "English"
            "system" -> {
                // 根据当前系统语言返回对应的"跟随系统"文本
                val currentLocale = context.resources.configuration.locales[0]
                if (currentLocale.language == "zh") {
                    context.getString(R.string.language_follow_system)
                } else {
                    "Follow System"
                }
            }
            else -> "Follow System"
        }
    }
}
