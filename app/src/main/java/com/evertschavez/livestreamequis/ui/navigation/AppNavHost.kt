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
    const val PLAYER = "player/{encodedUrl}?adTag={encodedAdTag}&title={encodedTitle}&subtitle={encodedSubtitle}"

    fun getPlayerRoute(url: String, adTag: String?, title: String, subtitle: String): String {
        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
        val encodedAdTag = if (adTag != null) java.net.URLEncoder.encode(adTag, "UTF-8") else ""
        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
        val encodedSubtitle = java.net.URLEncoder.encode(subtitle, "UTF-8")

        return "player/$encodedUrl?adTag=$encodedAdTag&title=$encodedTitle&subtitle=$encodedSubtitle"
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
                            title = video.title,
                            subtitle = video.subtitle,
                        )
                    )
                }
            )
        }

        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("encodedUrl") { type = NavType.StringType },
                navArgument("encodedAdTag") { type = NavType.StringType; nullable = true },
                navArgument("encodedTitle") { type = NavType.StringType },
                navArgument("encodedSubtitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("encodedUrl") ?: ""
            val adTagRaw = backStackEntry.arguments?.getString("encodedAdTag")
            val adTag = if (adTagRaw.isNullOrEmpty()) null else adTagRaw
            val title = backStackEntry.arguments?.getString("encodedTitle").orEmpty()
            val subTitle = backStackEntry.arguments?.getString("encodedSubtitle").orEmpty()

            PlayerScreen(url = url, adTag = adTag, title = title, subtitle = subTitle, onBack = { navController.popBackStack() })
        }
    }
}