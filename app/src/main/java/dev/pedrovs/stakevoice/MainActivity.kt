package dev.pedrovs.stakevoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import dev.pedrovs.stakevoice.screens.AuthScreen
import dev.pedrovs.stakevoice.screens.CompanyDetailsScreen
import dev.pedrovs.stakevoice.screens.FeedbackScreen
import dev.pedrovs.stakevoice.screens.HomeScreen
import dev.pedrovs.stakevoice.screens.UserFeedbackListScreen
import dev.pedrovs.stakevoice.ui.theme.StakeVoiceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        setContent {
            val navController = rememberNavController()

            StakeVoiceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    NavHost(
                        startDestination = if (user != null) "homeScreen" else "login",
                        navController = navController,
                    ) {
                        composable(route = "login") {
                            AuthScreen(navController)
                        }
                        composable(route = "homeScreen") {
                            HomeScreen(navController)
                        }
                        composable(
                            route = "companyDetails/{companyId}",
                            arguments = listOf(navArgument("companyId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val companyId = backStackEntry.arguments?.getString("companyId") ?: ""
                            CompanyDetailsScreen(navController, companyId)
                        }

                        composable(
                            route="createFeedback/{companyId}",
                            arguments = listOf(navArgument("companyId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val companyId = backStackEntry.arguments?.getString("companyId") ?: ""
                            FeedbackScreen(navController, companyId)
                        }

                        composable(route="myFeedbacks") {
                            UserFeedbackListScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
