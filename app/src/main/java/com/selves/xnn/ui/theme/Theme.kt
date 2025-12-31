package com.selves.xnn.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.selves.xnn.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    // 从0.9.3版本提取的完整动态颜色方案
    primary = Color(0xFF475D92),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001945),
    secondary = Color(0xFF575E71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE2F9),
    onSecondaryContainer = Color(0xFF151B2C),
    tertiary = Color(0xFF725572),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFDD7FA),
    onTertiaryContainer = Color(0xFF2A122C),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFEFBFF),
    onBackground = Color(0xFF1A1B20),
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF1A1B20),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44464F),
    outline = Color(0xFF757780),
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2F3036),
    inverseOnSurface = Color(0xFFF1F0F7),
    inversePrimary = Color(0xFFB0C6FF),
    // surfaceContainer系列颜色(根据Material3规范从surface推算)
    surfaceDim = Color(0xFFDAD9E0),
    surfaceBright = Color(0xFFFEFBFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F7FE),
    surfaceContainer = Color(0xFFF2F1F8),
    surfaceContainerHigh = Color(0xFFECEBF2),
    surfaceContainerHighest = Color(0xFFE6E5ED)
)

/**
 * 根据主题模式确定是否使用深色主题
 */
@Composable
fun shouldUseDarkTheme(themeMode: ThemeMode): Boolean {
    return when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

@Composable
fun SelvesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = shouldUseDarkTheme(themeMode)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}