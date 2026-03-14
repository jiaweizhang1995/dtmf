package com.jimmymacmini.wishdtmf.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jimmymacmini.wishdtmf.app.navigation.AppNavGraph
import com.jimmymacmini.wishdtmf.data.media.PhotoRepository
import com.jimmymacmini.wishdtmf.feature.entry.LaunchViewModel
import com.jimmymacmini.wishdtmf.feature.entry.PermissionCoordinator
import com.jimmymacmini.wishdtmf.feature.entry.PermissionEvent
import com.jimmymacmini.wishdtmf.feature.entry.rememberPermissionRequestController

@Composable
fun WishDtmfApp(
    photoRepository: PhotoRepository = (LocalContext.current.applicationContext as App).photoRepository,
) {
    val launchViewModel: LaunchViewModel = viewModel(
        factory = LaunchViewModel.factory(photoRepository = photoRepository),
    )
    val uiState by launchViewModel.uiState.collectAsStateWithLifecycle()
    val permissionCoordinator = remember { PermissionCoordinator() }
    val permissionController = rememberPermissionRequestController(
        permissionCoordinator = permissionCoordinator,
        onPermissionResolved = launchViewModel::onPermissionResult,
    )

    LaunchedEffect(permissionController.hasPermission, uiState, permissionCoordinator) {
        when (
            permissionCoordinator.onEntryStateChanged(
                hasPermission = permissionController.hasPermission,
                currentState = uiState,
            )
        ) {
            PermissionEvent.Granted -> launchViewModel.onPermissionResult(granted = true)
            PermissionEvent.Denied,
            PermissionEvent.None -> Unit
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold { innerPadding ->
                AppNavGraph(
                    modifier = Modifier.padding(innerPadding),
                    uiState = uiState,
                    onGrantAccess = permissionController.requestPermission,
                    onRetry = launchViewModel::retry,
                    onProceedToReview = {},
                )
            }
        }
    }
}
