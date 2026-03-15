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
            text = "本地清理工具",
            style = MaterialTheme.typography.headlineSmall,
        )
        when (uiState) {
            is LaunchUiState.NeedsPermission -> {
                Text(
                    text = if (uiState.showSettingsHint) {
                        "仍需相册访问权限以生成新批次。"
                    } else {
                        "请允许访问相册以生成随机审核批次。"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onGrantAccess,
                ) {
                    Text(
                        if (uiState.showSettingsHint) {
                            "重试权限"
                        } else {
                            "允许访问相册"
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
                        Text("打开应用设置")
                    }
                }
            }

            LaunchUiState.LoadingBatch -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                Text(
                    text = "正在准备新照片批次...",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            is LaunchUiState.Ready -> {
                Text(
                    text = "已加载 ${uiState.session.photoCount} 张照片。",
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
                    text = "没有需要清理的照片",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "此处仅显示可见、未删除且未正在上传的照片。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetry,
                ) {
                    Text("重新扫描")
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
                    Text("重试")
                }
            }
        }
    }
}
