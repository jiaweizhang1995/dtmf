package com.jimmymacmini.wishdtmf.feature.entry

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    EntryScreen(
        modifier = modifier,
        uiState = uiState,
        onGrantAccess = onGrantAccess,
        onRetry = onRetry,
    )
}

private fun Context.hasPermission(
    permissionCoordinator: PermissionCoordinator,
): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permissionCoordinator.requiredPermission,
    ) == PackageManager.PERMISSION_GRANTED
}
