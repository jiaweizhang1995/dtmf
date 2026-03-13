package com.jimmymacmini.wishdtmf.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jimmymacmini.wishdtmf.feature.entry.LaunchUiState
import com.jimmymacmini.wishdtmf.feature.entry.EntryContent

private const val ENTRY_ROUTE = "entry"
private const val MAIN_ROUTE = "main"

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    uiState: LaunchUiState,
    onGrantAccess: () -> Unit,
    onRetry: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    LaunchedEffect(uiState, navController) {
        if (uiState is LaunchUiState.Ready) {
            navController.navigate(MAIN_ROUTE) {
                popUpTo(ENTRY_ROUTE) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        } else if (navController.currentDestination?.route == MAIN_ROUTE) {
            navController.popBackStack(ENTRY_ROUTE, inclusive = false)
        }
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = ENTRY_ROUTE,
    ) {
        composable(ENTRY_ROUTE) {
            EntryContent(
                uiState = uiState,
                onGrantAccess = onGrantAccess,
                onRetry = onRetry,
            )
        }
        composable(MAIN_ROUTE) {
            MainPlaceholderScreen()
        }
    }
}

@Composable
private fun MainPlaceholderScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Main flow placeholder",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Phase 1 only establishes the app shell. The screenshot-faithful swipe UI lands in later plans.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
