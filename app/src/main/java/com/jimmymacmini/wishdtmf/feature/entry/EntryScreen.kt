package com.jimmymacmini.wishdtmf.feature.entry

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun EntryScreen(
    uiState: LaunchUiState,
    onGrantAccess: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
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
                if (uiState.showSettingsHint) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null),
                                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                    ) {
                        Text("Open app settings")
                    }
                }
            }

            LaunchUiState.LoadingBatch -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
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
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "No photos to clean up",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Only visible, non-trashed photos that are not currently uploading appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetry,
                ) {
                    Text("Scan again")
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
