package com.jimmymacmini.wishdtmf.feature.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EntryScreen(
    uiState: LaunchUiState,
    onGrantAccess: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Local cleanup utility",
            style = MaterialTheme.typography.headlineSmall,
        )
        when (uiState) {
            is LaunchUiState.NeedsPermission -> {
                Text(
                    text = if (uiState.showSettingsHint) {
                        "Gallery access is still required to build a fresh batch."
                    } else {
                        "Allow gallery access to build a new random review batch."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onGrantAccess,
                ) {
                    Text(
                        if (uiState.showSettingsHint) {
                            "Try permission again"
                        } else {
                            "Allow gallery access"
                        },
                    )
                }
            }

            LaunchUiState.LoadingBatch -> {
                Text(
                    text = "Preparing a fresh photo batch...",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            is LaunchUiState.Ready -> {
                Text(
                    text = "Session ready with ${uiState.session.photoCount} photos.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            LaunchUiState.Empty -> {
                Text(
                    text = "No eligible photos were found on this device.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetry,
                ) {
                    Text("Retry")
                }
            }

            is LaunchUiState.Error -> {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetry,
                ) {
                    Text("Retry")
                }
            }
        }
    }
}
