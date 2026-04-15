package com.salesforce.android.smi.messaging.features.voice.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

/**
 * Displays elapsed time since the given start time, updating every second.
 *
 * @param startTime The start time in milliseconds
 * @param style Text style for the elapsed time
 * @param color Text color
 * @param modifier Modifier for the component
 */
@Composable
internal fun ElapsedTime(
    startTime: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    var elapsedMillis by remember(startTime) {
        mutableLongStateOf(Date().time - startTime)
    }

    LaunchedEffect(startTime) {
        while (true) {
            val interval = 1000L
            delay(interval)
            elapsedMillis = Date().time - startTime
        }
    }

    val totalSeconds = (elapsedMillis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val timeString = if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    Text(
        text = timeString,
        style = style,
        color = color,
        modifier = modifier
    )
}
