package com.goldmonitor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "首页", Icons.Default.Home)
    data object Monitor : Screen("monitor", "监控", Icons.Default.Notifications)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Home, Monitor, Settings)
    }
}
