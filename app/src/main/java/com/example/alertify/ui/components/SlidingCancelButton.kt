package com.example.alertify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.math.max
import kotlin.math.min

@Composable
fun SlidingCancelButton(
    onCancel: () -> Unit,

    modifier: Modifier = Modifier
) {
    val trackWidth = 280.dp
    val thumbSize = 56.dp
    val density = LocalDensity.current

    val maxOffsetPx = with(density) { (trackWidth - thumbSize).toPx() }

    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(thumbSize + 8.dp)
            .clip(CircleShape)
            .background(Color(0x33FFFFFF))
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        // Track text
        Text(
            text = "Slide to Cancel",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Sliding thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color(0xFF333333))
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetX = min(
                            max(0f, offsetX + delta),
                            maxOffsetPx
                        )
                    },
                    onDragStopped = {
                        if (offsetX > maxOffsetPx * 0.85f) {
                            onCancel()
                        }
                        offsetX = 0f // reset
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                tint = Color.White
            )
        }
    }
}