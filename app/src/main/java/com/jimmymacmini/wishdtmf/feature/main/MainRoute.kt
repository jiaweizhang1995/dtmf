package com.jimmymacmini.wishdtmf.feature.main

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.jimmymacmini.wishdtmf.app.navigation.DELETED_PHOTO_IDS_KEY
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MainRoute(
    session: LaunchSession,
    onOpenReview: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    backStackEntry: NavBackStackEntry? = null,
) {
    val sessionKey = session.photos.joinToString(separator = "-") { it.id.toString() }
    val viewModel: MainViewModel = viewModel(
        key = "main-route-$sessionKey",
        factory = MainViewModel.factory(session),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    // Consume confirmed-deleted IDs relayed from the review result via SavedStateHandle.
    // Clears the now-stale swipe session so the app does not resume with deleted media refs.
    LaunchedEffect(backStackEntry) {
        val deletedIds = backStackEntry
            ?.savedStateHandle
            ?.remove<LongArray>(DELETED_PHOTO_IDS_KEY)
            ?.toSet()
            .orEmpty()
        if (deletedIds.isNotEmpty()) {
            viewModel.onDeleteConfirmed(deletedIds)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is MainNavigationEvent.OpenReview -> onOpenReview(event.stagedPhotoIds)
            }
        }
    }

    var showExitHint by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    BackHandler(enabled = true) {
        if (showExitHint) {
            (context as? ComponentActivity)?.finishAffinity()
        } else {
            showExitHint = true
            scope.launch {
                delay(2000L)
                showExitHint = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MainScreen(
            uiState = uiState.value,
            onStageCurrentPhoto = viewModel::stageCurrentPhoto,
            onSkipCurrentPhoto = viewModel::skipCurrentPhoto,
            onUndoLastDecision = viewModel::undoLastDecision,
            onProceed = viewModel::onProceedToReview,
        )

        AnimatedVisibility(
            visible = showExitHint,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Text(
                text = "再次返回回到桌面",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(
                        color = Color(0xCC000000),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}
