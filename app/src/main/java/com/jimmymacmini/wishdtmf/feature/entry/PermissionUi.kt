package com.jimmymacmini.wishdtmf.feature.entry

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

data class PermissionRequestController(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit,
)

@Composable
fun rememberPermissionRequestController(
    permissionCoordinator: PermissionCoordinator,
    onPermissionResolved: (Boolean) -> Unit,
): PermissionRequestController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember(permissionCoordinator, context) {
        mutableStateOf(context.hasPermission(permissionCoordinator))
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val resolvedPermission = granted || context.hasPermission(permissionCoordinator)
        hasPermission = resolvedPermission
        when (permissionCoordinator.onPermissionResult(resolvedPermission)) {
            PermissionEvent.Granted -> onPermissionResolved(true)
            PermissionEvent.Denied -> onPermissionResolved(false)
            PermissionEvent.None -> Unit
        }
    }

    DisposableEffect(lifecycleOwner, context, permissionCoordinator) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = context.hasPermission(permissionCoordinator)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return remember(hasPermission, launcher, permissionCoordinator) {
        PermissionRequestController(
            hasPermission = hasPermission,
            requestPermission = {
                launcher.launch(permissionCoordinator.requiredPermission)
            },
        )
    }
}

@Composable
internal fun EntryContent(
    modifier: Modifier = Modifier,
    uiState: LaunchUiState,
    onGrantAccess: () -> Unit,
    onRetry: () -> Unit,
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
                        "Gallery access is still required to start a batch."
                    } else {
                        "Allow gallery access to start a new random review batch."
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
                    text = "Session ready with ${uiState.photoCount} photos.",
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

private fun Context.hasPermission(
    permissionCoordinator: PermissionCoordinator,
): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permissionCoordinator.requiredPermission,
    ) == PackageManager.PERMISSION_GRANTED
}
