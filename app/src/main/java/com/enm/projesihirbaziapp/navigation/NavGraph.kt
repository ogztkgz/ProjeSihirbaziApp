@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.enm.projesihirbaziapp.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.enm.projesihirbaziapp.Screens.BeforeLoginUI
import com.enm.projesihirbaziapp.Screens.LoginUI
import com.enm.projesihirbaziapp.Screens.MainMenuUI
import com.enm.projesihirbaziapp.Screens.ProfileUI
import com.enm.projesihirbaziapp.Screens.ProjectsUI
import com.enm.projesihirbaziapp.Screens.AcademicsUI
import com.enm.projesihirbaziapp.Screens.ProjeSihirbaziAIUI

object Routes {
    const val BEFORE_LOGIN = "before_login"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROFILE = "profile"

    const val PROJECTS = "projects/{type}"
    fun projects(type: String) = "projects/$type"

    const val ACADEMICS = "academics"

    const val AI = "ai/{projectId}"
    fun ai(projectId: Int) = "ai/$projectId"
}

@Composable
fun NavGraph() {
    val nav = rememberNavController()
    val context = LocalContext.current

    val startDestination = remember {
        if (isLoggedIn(context)) Routes.HOME else Routes.BEFORE_LOGIN
    }

    NavHost(navController = nav, startDestination = startDestination) {

        composable(Routes.BEFORE_LOGIN) {
            BeforeLoginUI(
                onLoginClick = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.BEFORE_LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenPrivacy = { /* TODO: gizlilik / koşullar */ }
            )
        }

        composable(Routes.LOGIN) {
            LoginUI(
                onNavigateHome = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.HOME) {
            MainMenuUI(
                onOpenGrants = { nav.navigate(Routes.projects("Hibe")) },
                onOpenAcademics = { nav.navigate(Routes.ACADEMICS) },
                onOpenTenders = { nav.navigate(Routes.projects("İhale")) },
                onOpenProfile = { nav.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileUI(
                onLoggedOut = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Routes.PROJECTS,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type").orEmpty()
            ProjectsUI(
                projectsType = type,
                onOpenAIWizard = { projectId ->
                    nav.navigate(Routes.ai(projectId))
                }
            )
        }

        composable(Routes.ACADEMICS) {
            AcademicsUI()
        }

        // --- GÜNCEL AI ROTASI ---
        composable(
            route = Routes.AI,
            arguments = listOf(navArgument("projectId") { type = NavType.IntType })
        ) { entry ->
            val projectId = entry.arguments?.getInt("projectId") ?: 0
            ProjeSihirbaziAIUI(
                projectId = projectId,
                onBack = { nav.popBackStack() }
            )
        }
    }
}

private fun isLoggedIn(context: Context): Boolean {
    val sp = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val token = sp.getString("accessToken", "") ?: ""
    val flag = sp.getBoolean("isLoggedIn", false)
    return token.isNotBlank() || flag
}
