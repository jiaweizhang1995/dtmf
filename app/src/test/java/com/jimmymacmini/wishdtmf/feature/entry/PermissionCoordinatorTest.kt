package com.jimmymacmini.wishdtmf.feature.entry

import android.Manifest
import android.os.Build
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionCoordinatorTest {

    @Test
    fun `android 13 and above uses read media images`() {
        val coordinator = PermissionCoordinator(sdkInt = Build.VERSION_CODES.TIRAMISU)

        assertEquals(
            Manifest.permission.READ_MEDIA_IMAGES,
            coordinator.requiredPermission,
        )
    }

    @Test
    fun `android 12 and below uses read external storage`() {
        val coordinator = PermissionCoordinator(sdkInt = Build.VERSION_CODES.S_V2)

        assertEquals(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            coordinator.requiredPermission,
        )
    }

    @Test
    fun `needs permission state transitions to granted when permission already exists`() {
        val coordinator = PermissionCoordinator()

        assertEquals(
            PermissionEvent.Granted,
            coordinator.onEntryStateChanged(
                hasPermission = true,
                currentState = LaunchUiState.NeedsPermission(),
            ),
        )
    }

    @Test
    fun `denied permission result stays denied`() {
        val coordinator = PermissionCoordinator()

        assertEquals(
            PermissionEvent.Denied,
            coordinator.onPermissionResult(granted = false),
        )
    }

    @Test
    fun `granted permission result reports granted`() {
        val coordinator = PermissionCoordinator()

        assertEquals(
            PermissionEvent.Granted,
            coordinator.onPermissionResult(granted = true),
        )
    }

    @Test
    fun `entry state change does nothing once the launch flow is already past permission`() {
        val coordinator = PermissionCoordinator()

        assertEquals(
            PermissionEvent.None,
            coordinator.onEntryStateChanged(
                hasPermission = true,
                currentState = LaunchUiState.Ready(
                    session = LaunchSession(
                        photos = List(12) { index ->
                            LocalPhoto(
                                id = index.toLong(),
                                contentUri = "content://photo/$index",
                            )
                        },
                    ),
                ),
            ),
        )
    }
}
