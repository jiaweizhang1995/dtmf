package com.jimmymacmini.wishdtmf.feature.entry

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EntryRoute(
    uiState: LaunchUiState,
    onGrantAccess: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EntryScreen(
        modifier = modifier,
        uiState = uiState,
        onGrantAccess = onGrantAccess,
        onRetry = onRetry,
    )
}
