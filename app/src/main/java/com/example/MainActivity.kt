package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.PrompterScreen
import com.example.ui.screens.SavedScriptsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioBackground
import com.example.ui.viewmodel.TeleprompterViewModel

object NavigationDestinations {
    const val EDITOR = "editor"
    const val PROMPTER = "prompter"
    const val SAVED_SCRIPTS = "saved_scripts"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioBackground
                ) {
                    TeleprompterAppNav()
                }
            }
        }
    }
}

@Composable
fun TeleprompterAppNav(
    viewModel: TeleprompterViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationDestinations.EDITOR,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(
            route = NavigationDestinations.EDITOR,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            EditorScreen(
                viewModel = viewModel,
                onNavigateToPrompter = {
                    navController.navigate(NavigationDestinations.PROMPTER)
                },
                onNavigateToSavedScripts = {
                    navController.navigate(NavigationDestinations.SAVED_SCRIPTS)
                }
            )
        }

        composable(
            route = NavigationDestinations.PROMPTER,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            }
        ) {
            PrompterScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavigationDestinations.SAVED_SCRIPTS,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            SavedScriptsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPrompter = {
                    navController.navigate(NavigationDestinations.PROMPTER)
                }
            )
        }
    }
}
