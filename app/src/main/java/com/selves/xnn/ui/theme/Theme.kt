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
import com.selves.xnn.model.ColorScheme

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// ============================================
// 云野配色 (Cloud Field) - 自然清新绿色主题
// ============================================
private val CloudFieldLightColorScheme = lightColorScheme(
    primary = CloudFieldPrimaryLight,
    onPrimary = CloudFieldOnPrimaryLight,
    primaryContainer = CloudFieldPrimaryContainerLight,
    onPrimaryContainer = CloudFieldOnPrimaryContainerLight,
    secondary = CloudFieldSecondaryLight,
    onSecondary = CloudFieldOnSecondaryLight,
    secondaryContainer = CloudFieldSecondaryContainerLight,
    onSecondaryContainer = CloudFieldOnSecondaryContainerLight,
    tertiary = CloudFieldTertiaryLight,
    onTertiary = CloudFieldOnTertiaryLight,
    tertiaryContainer = CloudFieldTertiaryContainerLight,
    onTertiaryContainer = CloudFieldOnTertiaryContainerLight,
    error = CloudFieldErrorLight,
    onError = CloudFieldOnErrorLight,
    errorContainer = CloudFieldErrorContainerLight,
    onErrorContainer = CloudFieldOnErrorContainerLight,
    background = CloudFieldBackgroundLight,
    onBackground = CloudFieldOnBackgroundLight,
    surface = CloudFieldSurfaceLight,
    onSurface = CloudFieldOnSurfaceLight,
    surfaceVariant = CloudFieldSurfaceVariantLight,
    onSurfaceVariant = CloudFieldOnSurfaceVariantLight,
    outline = CloudFieldOutlineLight,
    outlineVariant = CloudFieldOutlineVariantLight,
    inverseSurface = Color(0xFF2D322B),
    inverseOnSurface = Color(0xFFEFF2E9),
    inversePrimary = Color(0xFFA2D399),
    surfaceDim = Color(0xFFD8DBD2),
    surfaceBright = Color(0xFFF7FBF1),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5EB),
    surfaceContainer = Color(0xFFECEFE6),
    surfaceContainerHigh = Color(0xFFE6E9E0),
    surfaceContainerHighest = Color(0xFFE0E4DA)
)

private val CloudFieldDarkColorScheme = darkColorScheme(
    primary = CloudFieldPrimaryDark,
    onPrimary = CloudFieldOnPrimaryDark,
    primaryContainer = CloudFieldPrimaryContainerDark,
    onPrimaryContainer = CloudFieldOnPrimaryContainerDark,
    secondary = CloudFieldSecondaryDark,
    onSecondary = CloudFieldOnSecondaryDark,
    secondaryContainer = CloudFieldSecondaryContainerDark,
    onSecondaryContainer = CloudFieldOnSecondaryContainerDark,
    tertiary = CloudFieldTertiaryDark,
    onTertiary = CloudFieldOnTertiaryDark,
    tertiaryContainer = CloudFieldTertiaryContainerDark,
    onTertiaryContainer = CloudFieldOnTertiaryContainerDark,
    error = CloudFieldErrorDark,
    onError = CloudFieldOnErrorDark,
    errorContainer = CloudFieldErrorContainerDark,
    onErrorContainer = CloudFieldOnErrorContainerDark,
    background = CloudFieldBackgroundDark,
    onBackground = CloudFieldOnBackgroundDark,
    surface = CloudFieldSurfaceDark,
    onSurface = CloudFieldOnSurfaceDark,
    surfaceVariant = CloudFieldSurfaceVariantDark,
    onSurfaceVariant = CloudFieldOnSurfaceVariantDark,
    outline = CloudFieldOutlineDark,
    outlineVariant = CloudFieldOutlineVariantDark,
    inverseSurface = Color(0xFFE0E4DA),
    inverseOnSurface = Color(0xFF2D322B),
    inversePrimary = Color(0xFF3C6839),
    surfaceDim = Color(0xFF10140F),
    surfaceBright = Color(0xFF363A34),
    surfaceContainerLowest = Color(0xFF0B0F0A),
    surfaceContainerLow = Color(0xFF191D17),
    surfaceContainer = Color(0xFF1D211B),
    surfaceContainerHigh = Color(0xFF272B25),
    surfaceContainerHighest = Color(0xFF323630)
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
    colorScheme: ColorScheme = ColorScheme.APP_DEFAULT,
    content: @Composable () -> Unit
) {
    val darkTheme = shouldUseDarkTheme(themeMode)
    val materialColorScheme = when {
        colorScheme == ColorScheme.WALLPAPER && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        colorScheme == ColorScheme.CLOUD_FIELD -> {
            if (darkTheme) CloudFieldDarkColorScheme else CloudFieldLightColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> when (colorScheme) {
            ColorScheme.APP_DEFAULT -> LightColorScheme
            ColorScheme.WALLPAPER -> LightColorScheme
            ColorScheme.CLOUD_FIELD -> CloudFieldLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = materialColorScheme,
        typography = Typography,
        content = content
    )
}
