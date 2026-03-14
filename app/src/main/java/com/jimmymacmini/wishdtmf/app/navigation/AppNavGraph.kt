package com.jimmymacmini.wishdtmf.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jimmymacmini.wishdtmf.feature.entry.EntryRoute
import com.jimmymacmini.wishdtmf.feature.entry.LaunchUiState
import com.jimmymacmini.wishdtmf.feature.main.MainRoute

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
            EntryRoute(
                uiState = uiState,
                onGrantAccess = onGrantAccess,
                onRetry = onRetry,
            )
        }
        composable(MAIN_ROUTE) {
            val readyState = uiState as? LaunchUiState.Ready
            if (readyState != null) {
                MainRoute(
                    session = readyState.session,
                    onOpenReview = { stagedPhotoIds ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set(REVIEW_STAGED_PHOTO_IDS_KEY, stagedPhotoIds.toLongArray())
                    },
                )
            }
        }
    }
}

const val REVIEW_STAGED_PHOTO_IDS_KEY = "review_staged_photo_ids"
