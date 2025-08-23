package com.example.stecu.data.navigation

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.stecu.data.ui.screen.AssistantScreen
import com.example.stecu.ui.screen.AmberPointsScreen
import com.example.stecu.ui.screen.CareerDetailScreen
import com.example.stecu.ui.screen.CareerReportScreen
import com.example.stecu.ui.screen.ChatScreen
import com.example.stecu.viewmodel.AssistantUiState
import com.example.stecu.viewmodel.ChatViewModel

sealed class Screen(val route: String) {
    data object Assistant : Screen("assistant")
    data object CareerReport : Screen("career_report")
    data object Profile : Screen("profile")
    data object AmberPoints : Screen("amber_points")
    object CareerDetail : Screen("career_detail/{careerId}") {
        fun createRoute(careerId: String) = "career_detail/$careerId"
    }
    object Chat : Screen("chat?conversationId={conversationId}") {
        val arguments = listOf(
            navArgument("conversationId") {
                type = NavType.LongType
                defaultValue = -1L // ID -1L berarti chat baru
            }
        )

        fun createRoute(conversationId: Long) = "chat?conversationId=$conversationId"
        val newChatRoute = "chat?conversationId=-1"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    assistantUiState: AssistantUiState,
    onMicClick: () -> Unit,
    onStopListeningClick: () -> Unit,
    onStopSpeakingClick: () -> Unit,
    paddingValues: PaddingValues
) {
    NavHost(navController = navController, startDestination = Screen.Chat.route) {
        composable(Screen.Assistant.route) {
            AssistantScreen(
                uiState = assistantUiState,
                onMicClick = onMicClick,
                onStopListeningClick = onStopListeningClick,
                onStopSpeakingClick = onStopSpeakingClick,
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.newChatRoute)
                }
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = Screen.Chat.arguments // Tambahkan ini untuk menerima argumen
        ) { backStackEntry ->

            // Ambil ID dari argumen navigasi
            val conversationId = backStackEntry.arguments?.getLong("conversationId")
            val application = LocalContext.current.applicationContext as Application

            // Buat instance ChatViewModel dengan factory yang benar
            val chatViewModel: ChatViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return ChatViewModel(application) as T
                    }
                }
            )
            // Panggil fungsi di ViewModel untuk memuat percakapan yang sesuai
            LaunchedEffect(key1 = conversationId) {
                chatViewModel.loadOrCreateConversation(conversationId)
            }

            ChatScreen(
                viewModel = chatViewModel,
                navController = navController,
                onNavigateBackToAssistant = {
                    navController.navigate(Screen.Assistant.route) {
                        popUpTo(Screen.Assistant.route) { inclusive = true }
                    }
                }
            )
        }
        // TODO: Tambahkan composable untuk CareerReport dan Profile
        composable(Screen.CareerReport.route) {
            CareerReportScreen(navController = navController)
        }

        composable(Screen.AmberPoints.route) {
            AmberPointsScreen(navController = navController)
        }

        composable(
            route = Screen.CareerDetail.route,
            arguments = listOf(navArgument("careerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val careerId = backStackEntry.arguments?.getString("careerId")
            CareerDetailScreen(
                careerId = careerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) { /* Screen untuk Akun Pengguna */ }
    }
}