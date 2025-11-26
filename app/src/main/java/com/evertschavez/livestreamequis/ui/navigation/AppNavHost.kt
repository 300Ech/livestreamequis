package com.evertschavez.livestreamequis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.evertschavez.livestreamequis.ui.player.PlayerScreen

object Routes {
    const val PLAYER = "player"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.PLAYER,
    ) {
        composable(Routes.PLAYER) {
            PlayerScreen()
        }
    }
}