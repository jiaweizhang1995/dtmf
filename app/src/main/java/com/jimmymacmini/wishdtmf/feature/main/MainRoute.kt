package com.jimmymacmini.wishdtmf.feature.main

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
import com.jimmymacmini.wishdtmf.domain.LaunchSession

@Composable
fun MainRoute(
    session: LaunchSession,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Main flow placeholder",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Batch ${session.currentIndex + 1} of ${session.photoCount}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Current photo: ${session.photos[session.currentIndex].contentUri}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onAdvance,
            enabled = session.currentIndex < session.photoCount - 1,
        ) {
            Text("Next photo")
        }
    }
}
