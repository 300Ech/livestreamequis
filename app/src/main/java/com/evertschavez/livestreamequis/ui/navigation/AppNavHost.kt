package com.evertschavez.livestreamequis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.evertschavez.livestreamequis.ui.home.HomeScreen
import com.evertschavez.livestreamequis.ui.player.PlayerScreen

object Routes {
    const val HOME = "home"
    const val PLAYER = "player/{encodedUrl}?adTag={encodedAdTag}"

    fun getPlayerRoute(url: String, adTag: String?): String {
        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
        val encodedAdTag = if (adTag != null) java.net.URLEncoder.encode(adTag, "UTF-8") else ""
        return "player/$encodedUrl?adTag=$encodedAdTag"
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onVideoSelected = { video ->
                    navController.navigate(
                        Routes.getPlayerRoute(
                            url = video.streamUrl,
                            adTag = video.adTagUrl,
                        )
                    )
                }
            )
        }

        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("encodedUrl") { type = NavType.StringType },
                navArgument("encodedAdTag") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("encodedUrl") ?: ""
            val adTagRaw = backStackEntry.arguments?.getString("encodedAdTag")
            val adTag = if (adTagRaw.isNullOrEmpty()) null else adTagRaw

            PlayerScreen(url = url, adTag = adTag)
        }
    }
}