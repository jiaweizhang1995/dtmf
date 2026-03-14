package com.jimmymacmini.wishdtmf.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object ReviewScreenTags {
    const val Root = "review_root"
    const val BackButton = "review_back_button"
    const val Subtitle = "review_subtitle"
    const val Count = "review_count"
    const val StagedList = "review_staged_list"
}

@Composable
fun ReviewScreen(
    uiState: ReviewUiState,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1F1F))
            .semantics {
                stateDescription = uiState.stagedPhotoIds.joinToString(prefix = "staged:", separator = ",")
            }
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag(ReviewScreenTags.Root),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(22.dp))
                    .clickable(onClick = onBack)
                    .testTag(ReviewScreenTags.BackButton),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "‹",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = uiState.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${uiState.stagedPhotoIds.size}",
                modifier = Modifier.testTag(ReviewScreenTags.Count),
                color = Color(0xFF61A8FF),
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = uiState.subtitle,
            modifier = Modifier.testTag(ReviewScreenTags.Subtitle),
            color = Color(0xFFB8B8B8),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ReviewScreenTags.StagedList),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.stagedPhotoIds, key = { it }) { photoId ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2B2B2B), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                ) {
                    Text(
                        text = "Selected photo $photoId",
                        color = Color.White,
                    )
                }
            }
        }
    }
}
