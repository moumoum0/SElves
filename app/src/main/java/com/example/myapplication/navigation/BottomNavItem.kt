package com.example.myapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Chat
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
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
        val items = listOf(Chat, Settings)
    }
} 