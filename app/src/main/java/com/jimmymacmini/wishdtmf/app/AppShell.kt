package com.jimmymacmini.wishdtmf.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jimmymacmini.wishdtmf.app.navigation.AppNavGraph
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.data.media.PhotoRepository
import com.jimmymacmini.wishdtmf.feature.entry.LaunchUiState
import com.jimmymacmini.wishdtmf.feature.entry.LaunchViewModel

@Composable
fun WishDtmfApp(
    photoRepository: PhotoRepository = PreviewPhotoRepository(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val launchViewModel: LaunchViewModel = viewModel(
        factory = LaunchViewModel.factory(photoRepository = photoRepository),
    )
    val uiState by launchViewModel.uiState.collectAsStateWithLifecycle()
    var hasMediaPermission by remember {
        mutableStateOf(context.hasMediaPermission())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasMediaPermission = granted || context.hasMediaPermission()
        launchViewModel.onPermissionResult(granted = hasMediaPermission)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasMediaPermission = context.hasMediaPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hasMediaPermission, uiState) {
        if (uiState is LaunchUiState.NeedsPermission && hasMediaPermission) {
            launchViewModel.onPermissionResult(granted = true)
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold { innerPadding ->
                AppNavGraph(
                    modifier = Modifier.padding(innerPadding),
                    uiState = uiState,
                    onGrantAccess = {
                        permissionLauncher.launch(currentMediaPermission())
                    },
                    onRetry = launchViewModel::retry,
                )
            }
        }
    }
}

private class PreviewPhotoRepository : PhotoRepository {
    override suspend fun loadEligiblePhotos(limitHint: Int?): List<LocalPhoto> = emptyList()
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

private fun currentMediaPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private fun Context.hasMediaPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        currentMediaPermission(),
    ) == PackageManager.PERMISSION_GRANTED
}
