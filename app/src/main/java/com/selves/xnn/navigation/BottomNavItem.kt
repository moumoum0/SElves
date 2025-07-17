package com.selves.xnn.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "首页",
        icon = Icons.Default.Home
    )
    
    object Chat : BottomNavItem(
        route = "chat",
        title = "聊天",
        icon = Icons.Default.Chat
    )
    
    object Settings : BottomNavItem(
        route = "settings",
        title = "系统",
        icon = Icons.Default.AccountTree
    )
    
    companion object {
        val items = listOf(Home, Chat, Settings)
    }
} 