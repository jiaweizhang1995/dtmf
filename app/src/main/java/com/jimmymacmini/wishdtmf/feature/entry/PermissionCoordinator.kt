package com.jimmymacmini.wishdtmf.feature.entry

import android.Manifest
import android.os.Build

class PermissionCoordinator(
    private val sdkInt: Int = Build.VERSION.SDK_INT,
) {
    val requiredPermission: String =
        if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun onEntryStateChanged(
        hasPermission: Boolean,
        currentState: LaunchUiState,
    ): PermissionEvent {
        return if (hasPermission && currentState is LaunchUiState.NeedsPermission) {
            PermissionEvent.Granted
        } else {
            PermissionEvent.None
        }
    }

    fun onPermissionResult(granted: Boolean): PermissionEvent {
        return if (granted) {
            PermissionEvent.Granted
        } else {
            PermissionEvent.Denied
        }
    }
}

sealed interface PermissionEvent {
    data object None : PermissionEvent
    data object Granted : PermissionEvent
    data object Denied : PermissionEvent
}
