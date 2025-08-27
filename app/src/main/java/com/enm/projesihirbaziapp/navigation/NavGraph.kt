@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.enm.projesihirbaziapp.navigation

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.enm.projesihirbaziapp.Screens.*

object Routes {
    const val BEFORE_LOGIN = "before_login"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROFILE = "profile"

    // arg'lı rotalar
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

    // Uygulama ilk açılış hedefi: login olmuşsa HOME, değilse BEFORE_LOGIN
    val startDestination = remember {
        if (isLoggedIn(context)) Routes.HOME else Routes.BEFORE_LOGIN
    }

    NavHost(navController = nav, startDestination = startDestination) {

        // Giriş öncesi açılış ekranı
        composable(Routes.BEFORE_LOGIN) {
            BeforeLoginUI(
                onLoginClick = {
                    // BEFORE_LOGIN'ı yığından temizleyerek Login'e geç
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.BEFORE_LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenPrivacy = { /* Gizlilik/Koşullar ekranı ya da URL */ }
            )
        }

        // Login
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

        // Ana Menü
        composable(Routes.HOME) {
            MainMenuUI(
                onOpenGrants = { nav.navigate(Routes.projects("Hibe")) },
                onOpenAcademics = { nav.navigate(Routes.ACADEMICS) },
                onOpenTenders = { nav.navigate(Routes.projects("İhale")) },
                onOpenProfile = { nav.navigate(Routes.PROFILE) }
            )
        }

        // Profil
        composable(Routes.PROFILE) {
            ProfileUI(
                onLoggedOut = {
                    // Çıkış sonrası back stack'i temizleyip Login'e dön
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Projeler (parametreli: type = "Hibe" | "İhale" ...)
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

        // Akademisyenler
        composable(Routes.ACADEMICS) {
            AcademicsUI()
        }

        // Proje Sihirbazı AI (parametreli)
        composable(
            route = Routes.AI,
            arguments = listOf(navArgument("projectId") { type = NavType.IntType })
        ) { entry ->
            val projectId = entry.arguments?.getInt("projectId") ?: 0
            // Eğer kendi AI ekranınız hazırsa bunu çağırın:
            // ProjeSihirbaziAIUI(projectId = projectId)
            Text(text = "AI Sihirbaz • Proje ID: $projectId") // Placeholder
        }
    }
}

/* ---- Yardımcı: login kontrolü ---- */
private fun isLoggedIn(context: Context): Boolean {
    // UserDataAccess'te kullandığımızla tutarlı bir SharedPreferences adı:
    val sp = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val token = sp.getString("accessToken", "") ?: ""
    val flag = sp.getBoolean("isLoggedIn", false)
    return token.isNotBlank() || flag
}
