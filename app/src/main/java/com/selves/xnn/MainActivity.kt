package com.selves.xnn

import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.selves.xnn.ui.screens.AppNavigationScreen
import com.selves.xnn.ui.theme.SelvesTheme
import com.selves.xnn.ui.theme.shouldUseDarkTheme
import com.selves.xnn.ui.viewmodels.MainViewModel
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.model.ThemeMode
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var memberPreferences: MemberPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 根据不同的 Android 版本处理状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 5.0 及以上版本
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.Transparent.toArgb()
        }
        
        // 设置窗口以支持边缘到边缘的显示
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            val themeMode by memberPreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            
            SelvesTheme(themeMode = themeMode) {
                val systemUiController = rememberSystemUiController()
                val isDarkTheme = shouldUseDarkTheme(themeMode)
                val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                val surfaceColor = MaterialTheme.colorScheme.surface

                SideEffect {
                    // 根据主题模式设置状态栏颜色
                    systemUiController.setStatusBarColor(
                        color = if (isDarkTheme) surfaceColor else Color.White,
                        darkIcons = !isDarkTheme
                    )
                    
                    // 导航栏使用surfaceVariant颜色
                    systemUiController.setNavigationBarColor(
                        color = surfaceVariantColor,
                        darkIcons = !isDarkTheme
                    )
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigationScreen(viewModel = mainViewModel)
                }
            }
        }
    }
}