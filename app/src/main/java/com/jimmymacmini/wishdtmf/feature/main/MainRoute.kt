package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.jimmymacmini.wishdtmf.domain.LaunchSession

@Composable
fun MainRoute(
    session: LaunchSession,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState = remember(session) { MainUiState.fromSession(session) }

    MainScreen(
        uiState = uiState,
        onAdvance = onAdvance,
        modifier = modifier,
    )
}
