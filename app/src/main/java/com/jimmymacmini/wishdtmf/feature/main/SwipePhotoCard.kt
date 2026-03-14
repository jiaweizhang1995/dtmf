package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.launch

private const val SWIPE_THRESHOLD_RATIO = 0.25f
private const val DISMISS_DISTANCE_RATIO = 1.25f
private const val MAX_ROTATION_DEGREES = 10f

@Composable
fun SwipePhotoCard(
    photo: MainPhotoUiModel,
    heroAspectRatio: Float,
    onStagePhoto: () -> Unit,
    onSkipPhoto: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val offsetX = remember { Animatable(0f) }
    var cardWidthPx by remember { mutableFloatStateOf(0f) }
    var isAnimatingDismiss by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(photo.id) {
        offsetX.snapTo(0f)
        isAnimatingDismiss = false
    }

    CurrentPhotoCard(
        photo = photo,
        heroAspectRatio = heroAspectRatio,
        modifier = modifier
            .onSizeChanged { cardWidthPx = it.width.toFloat() }
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = ((offsetX.value / cardWidthPx.coerceAtLeast(1f)) * MAX_ROTATION_DEGREES)
                    .coerceIn(-MAX_ROTATION_DEGREES, MAX_ROTATION_DEGREES)
            }
            .pointerInput(enabled, photo.id, cardWidthPx) {
                if (!enabled) {
                    return@pointerInput
                }

                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (isAnimatingDismiss) {
                            return@detectDragGestures
                        }

                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                        }
                    },
                    onDragEnd = {
                        if (isAnimatingDismiss) {
                            return@detectDragGestures
                        }

                        val threshold = (cardWidthPx * SWIPE_THRESHOLD_RATIO).coerceAtLeast(1f)
                        val commitLeftSwipe = offsetX.value <= -threshold
                        val commitRightSwipe = offsetX.value >= threshold

                        when {
                            commitLeftSwipe -> {
                                isAnimatingDismiss = true
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = -cardWidthPx.coerceAtLeast(1f) * DISMISS_DISTANCE_RATIO,
                                        animationSpec = spring(),
                                    )
                                    onStagePhoto()
                                    offsetX.snapTo(0f)
                                    isAnimatingDismiss = false
                                }
                            }

                            commitRightSwipe -> {
                                isAnimatingDismiss = true
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = cardWidthPx.coerceAtLeast(1f) * DISMISS_DISTANCE_RATIO,
                                        animationSpec = spring(),
                                    )
                                    onSkipPhoto()
                                    offsetX.snapTo(0f)
                                    isAnimatingDismiss = false
                                }
                            }

                            else -> {
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(),
                                    )
                                }
                            }
                        }
                    },
                )
            },
    )
}
