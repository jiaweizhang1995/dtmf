package com.jimmymacmini.wishdtmf.feature.entry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jimmymacmini.wishdtmf.R

@Composable
fun WelcomeScreen(
    uiState: LaunchUiState,
    onRequestPermission: () -> Unit,
    onNavigateToEntry: () -> Unit,
) {
    var permissionRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onRequestPermission()
        permissionRequested = true
    }

    // Navigate to entry once uiState reflects the permission result.
    // Initial state before request is NeedsPermission(showSettingsHint=false).
    // After grant: becomes LoadingBatch -> Ready/Empty.
    // After deny: becomes NeedsPermission(showSettingsHint=true).
    LaunchedEffect(permissionRequested, uiState) {
        if (permissionRequested) {
            val isDenied = uiState is LaunchUiState.NeedsPermission && uiState.showSettingsHint
            val isLoading = uiState is LaunchUiState.LoadingBatch
            val isReady = uiState is LaunchUiState.Ready
            val isEmpty = uiState is LaunchUiState.Empty
            val isError = uiState is LaunchUiState.Error
            if (isDenied || isLoading || isReady || isEmpty || isError) {
                onNavigateToEntry()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
        )
    }
}
