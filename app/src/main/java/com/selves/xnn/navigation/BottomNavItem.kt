package com.selves.xnn.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.selves.xnn.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        titleResId = R.string.nav_home,
        icon = Icons.Default.Home
    )
    
    object Chat : BottomNavItem(
        route = "chat",
        titleResId = R.string.nav_chat,
        icon = Icons.AutoMirrored.Filled.Chat
    )
    
    object Settings : BottomNavItem(
        route = "system",
        titleResId = R.string.nav_system,
        icon = Icons.Default.AccountTree
    )
    
    companion object {
        val items = listOf(Home, Chat, Settings)
    }
} 