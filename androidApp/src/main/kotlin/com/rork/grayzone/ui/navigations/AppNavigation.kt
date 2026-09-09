package com.rork.grayzone.ui.navigations

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rork.grayzone.ui.GrayzoneViewModel
import com.rork.grayzone.ui.screens.AppsScreen
import com.rork.grayzone.ui.screens.HomeScreen
import com.rork.grayzone.ui.screens.InsightsScreen
import com.rork.grayzone.ui.screens.OnboardingScreen
import com.rork.grayzone.ui.screens.SessionScreen
import com.rork.grayzone.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val vm: GrayzoneViewModel = viewModel(context as ComponentActivity)

    val goTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(
                vm = vm,
                onDone = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(vm = vm, openApp = { navController.navigate("session/$it") }, navigate = goTab)
        }
        composable("apps") { AppsScreen(vm = vm, navigate = goTab) }
        composable("insights") { InsightsScreen(vm = vm, navigate = goTab) }
        composable("settings") { SettingsScreen(vm = vm, navigate = goTab) }
        composable("session/{appId}") { entry ->
            val appId = entry.arguments?.getString("appId") ?: ""
            SessionScreen(vm = vm, appId = appId, onExit = { navController.popBackStack() })
        }
    }
}
