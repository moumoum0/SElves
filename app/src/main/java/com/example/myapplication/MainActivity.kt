package com.example.myapplication

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.example.myapplication.ui.screens.MainScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodels.MainViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    
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
            MyApplicationTheme {
                val systemUiController = rememberSystemUiController()
                val isDarkTheme = isSystemInDarkTheme()
                val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

                SideEffect {
                    // 设置状态栏为白色
                    systemUiController.setStatusBarColor(
                        color = Color.White,
                        darkIcons = true // 因为背景是白色，所以图标使用深色
                    )
                    
                    // 导航栏仍然使用surfaceVariant颜色
                    systemUiController.setNavigationBarColor(
                        color = surfaceVariantColor,
                        darkIcons = !isDarkTheme
                    )
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen(viewModel = mainViewModel)
                }
            }
        }
    }
}