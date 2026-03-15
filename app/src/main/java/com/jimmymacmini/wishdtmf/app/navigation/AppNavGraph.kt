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
import com.jimmymacmini.wishdtmf.feature.review.ReviewRoute

const val ENTRY_ROUTE = "entry"
const val MAIN_ROUTE = "main"
const val REVIEW_ROUTE = "review"

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    uiState: LaunchUiState,
    onGrantAccess: () -> Unit,
    onRetry: () -> Unit,
    onRefreshAfterDelete: () -> Unit = {},
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
        composable(MAIN_ROUTE) { backStackEntry ->
            val readyState = uiState as? LaunchUiState.Ready
            if (readyState != null) {
                MainRoute(
                    session = readyState.session,
                    onOpenReview = { stagedPhotoIds ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set(REVIEW_STAGED_PHOTO_IDS_KEY, stagedPhotoIds.toLongArray())
                        navController.navigate(REVIEW_ROUTE)
                    },
                    backStackEntry = backStackEntry,
                )
            }
        }
        composable(REVIEW_ROUTE) {
            val stagedPhotoIds = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<LongArray>(REVIEW_STAGED_PHOTO_IDS_KEY)
                ?.toList()
                .orEmpty()
            ReviewRoute(
                stagedPhotoIds = stagedPhotoIds,
                onBack = navController::popBackStack,
                onDeleteConfirmed = { deletedIds ->
                    // Pop review off the back stack so the app returns to main/entry.
                    navController.popBackStack()
                    // Ask main to clear its stale session. The main back-stack entry SavedStateHandle
                    // is used to relay the deleted IDs; the main route reads this on recomposition.
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(DELETED_PHOTO_IDS_KEY, deletedIds.toLongArray())
                    // Kick off the post-delete refresh so LaunchViewModel rebuilds from remaining media.
                    onRefreshAfterDelete()
                },
            )
        }
    }
}

const val REVIEW_STAGED_PHOTO_IDS_KEY = "review_staged_photo_ids"

/**
 * Key used to relay confirmed-deleted IDs from the review result back to [MainRoute] via the
 * main back-stack entry SavedStateHandle. [MainRoute] consumes this to clear the stale session.
 */
const val DELETED_PHOTO_IDS_KEY = "deleted_photo_ids"
