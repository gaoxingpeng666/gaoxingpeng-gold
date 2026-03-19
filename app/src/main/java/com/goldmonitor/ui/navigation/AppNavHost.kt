package com.goldmonitor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.goldmonitor.ui.home.HomeScreen
import com.goldmonitor.ui.monitor.MonitorScreen
import com.goldmonitor.ui.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Monitor.route) {
            MonitorScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
