package com.salesforce.android.smi.messaging.features.voice.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.multimedia.common.api.participant.MultimediaParticipantOrigin
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSession
import com.salesforce.android.smi.messaging.features.voice.getMaxAmplitudes
import com.salesforce.android.smi.messaging.theme.SMIDimens
import kotlin.math.abs

private const val TAG = "VoiceVisualizer"

/**
 * A customizable audio visualizer that displays amplitude data as vertical bars.
 *
 * @param session The current multimedia session to visualize
 * @param origin Whether to visualize local or remote participant audio
 * @param modifier Modifier for the component
 * @param barCount Number of frequency bars to display
 * @param barColor Color of the waveform bars
 * @param barWidth Width of each bar
 * @param barSpacing Spacing between bars
 * @param minBarHeightFraction Minimum bar height as fraction of total height (0.0 to 1.0)
 * @param cornerRadius Corner radius for rounded bar ends
 * @param isCentered If true, bars grow from center; if false, from bottom
 * @param isMirroredVertically If true and centered, bars mirror above and below center
 * @param isMirroredHorizontally If true, bars mirror from center outward
 */
@Composable
fun VoiceVisualizer(
    session: MultimediaSession,
    origin: MultimediaParticipantOrigin,
    modifier: Modifier = Modifier,
    barCount: Int = 16,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barWidth: Dp = SMIDimens.Padding.dp4,
    barSpacing: Dp = SMIDimens.Padding.dp2,
    minBarHeightFraction: Float = 0.05f,
    cornerRadius: Dp = SMIDimens.Radius.dp4,
    isCentered: Boolean = true,
    isMirroredVertically: Boolean = true,
    isMirroredHorizontally: Boolean = true
) {
    val amplitudeFlow = remember(session, origin, barCount) {
        Log.d(TAG, "Creating amplitude flow for $origin with barCount=$barCount")
        session.getMaxAmplitudes(origin, barCount)
    }

    val amplitudes: FloatArray by amplitudeFlow.collectAsStateWithLifecycle(FloatArray(barCount))

    LaunchedEffect(amplitudes) {
        val sum = amplitudes.sum()
        val max = amplitudes.maxOrNull() ?: 0f
        Log.d(TAG, "Amplitudes updated for $origin - sum=$sum, max=$max, values=${amplitudes.take(3).joinToString()}")
    }

    VoiceVisualizerContent(
        amplitudes = amplitudes,
        modifier = modifier,
        barColor = barColor,
        barWidth = barWidth,
        barSpacing = barSpacing,
        minBarHeightFraction = minBarHeightFraction,
        cornerRadius = cornerRadius,
        isCentered = isCentered,
        isMirroredVertically = isMirroredVertically,
        isMirroredHorizontally = isMirroredHorizontally
    )
}

@Composable
private fun VoiceVisualizerContent(
    amplitudes: FloatArray,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barWidth: Dp = SMIDimens.Padding.dp4,
    barSpacing: Dp = SMIDimens.Padding.dp2,
    minBarHeightFraction: Float = 0.05f,
    cornerRadius: Dp = SMIDimens.Radius.dp4,
    isCentered: Boolean = true,
    isMirroredVertically: Boolean = true,
    isMirroredHorizontally: Boolean = true
) {
    Canvas(modifier = modifier) {
        val barWidthPx = barWidth.toPx()
        val barSpacingPx = barSpacing.toPx()
        val cornerRadiusPx = cornerRadius.toPx()
        val totalBarWidth = barWidthPx + barSpacingPx

        val centerY = size.height / 2
        val maxBarHeight = size.height / 2
        val minBarHeight = size.height * minBarHeightFraction

        when (isMirroredHorizontally) {
            true -> renderCenterOut(
                amplitudes = amplitudes,
                barColor = barColor,
                isCentered = isCentered,
                isMirroredVertically = isMirroredVertically,
                barWidthPx = barWidthPx,
                barSpacingPx = barSpacingPx,
                cornerRadiusPx = cornerRadiusPx,
                totalBarWidth = totalBarWidth,
                centerY = centerY,
                maxBarHeight = maxBarHeight,
                minBarHeight = minBarHeight
            )
            false -> renderLeftToRight(
                amplitudes = amplitudes,
                barColor = barColor,
                isCentered = isCentered,
                isMirroredVertically = isMirroredVertically,
                barWidthPx = barWidthPx,
                barSpacingPx = barSpacingPx,
                cornerRadiusPx = cornerRadiusPx,
                totalBarWidth = totalBarWidth,
                centerY = centerY,
                maxBarHeight = maxBarHeight,
                minBarHeight = minBarHeight
            )
        }
    }
}

private fun DrawScope.renderCenterOut(
    amplitudes: FloatArray,
    barColor: Color,
    isCentered: Boolean,
    isMirroredVertically: Boolean,
    barWidthPx: Float,
    barSpacingPx: Float,
    cornerRadiusPx: Float,
    totalBarWidth: Float,
    centerY: Float,
    maxBarHeight: Float,
    minBarHeight: Float
) {
    val halfWidth = size.width / 2
    val maxBarsPerSide = ((halfWidth + barSpacingPx) / totalBarWidth).toInt()

    val step = if (amplitudes.size > maxBarsPerSide) amplitudes.size.toFloat() / maxBarsPerSide else 1f
    val barsPerSide = minOf(amplitudes.size, maxBarsPerSide)

    val centerX = size.width / 2

    for (i in 0 until barsPerSide) {
        val amplitudeIndex = (i * step).toInt().coerceIn(0, amplitudes.lastIndex)
        val amplitude = abs(amplitudes[amplitudeIndex]).coerceIn(0f, 1f)

        val barHeight = (minBarHeight + (maxBarHeight - minBarHeight) * amplitude)
            .coerceAtLeast(minBarHeight)

        // Right side
        val xRight = centerX + (barSpacingPx / 2) + i * totalBarWidth

        // Left side
        val xLeft = centerX - (barSpacingPx / 2) - barWidthPx - i * totalBarWidth

        drawBar(
            x = xRight,
            barHeight = barHeight,
            barWidthPx = barWidthPx,
            centerY = centerY,
            totalHeight = size.height,
            barColor = barColor,
            cornerRadiusPx = cornerRadiusPx,
            isCentered = isCentered,
            isMirroredVertically = isMirroredVertically
        )

        drawBar(
            x = xLeft,
            barHeight = barHeight,
            barWidthPx = barWidthPx,
            centerY = centerY,
            totalHeight = size.height,
            barColor = barColor,
            cornerRadiusPx = cornerRadiusPx,
            isCentered = isCentered,
            isMirroredVertically = isMirroredVertically
        )
    }
}

private fun DrawScope.renderLeftToRight(
    amplitudes: FloatArray,
    barColor: Color,
    isCentered: Boolean,
    isMirroredVertically: Boolean,
    barWidthPx: Float,
    barSpacingPx: Float,
    cornerRadiusPx: Float,
    totalBarWidth: Float,
    centerY: Float,
    maxBarHeight: Float,
    minBarHeight: Float
) {
    val maxBars = ((size.width + barSpacingPx) / totalBarWidth).toInt()
    val step = if (amplitudes.size > maxBars) amplitudes.size.toFloat() / maxBars else 1f
    val barsToDisplay = minOf(amplitudes.size, maxBars)

    val totalWidth = barsToDisplay * barWidthPx + (barsToDisplay - 1) * barSpacingPx
    val startX = (size.width - totalWidth) / 2

    for (i in 0 until barsToDisplay) {
        val amplitudeIndex = (i * step).toInt().coerceIn(0, amplitudes.lastIndex)
        val amplitude = abs(amplitudes[amplitudeIndex]).coerceIn(0f, 1f)

        val barHeight = (minBarHeight + (maxBarHeight - minBarHeight) * amplitude)
            .coerceAtLeast(minBarHeight)

        val x = startX + i * totalBarWidth

        drawBar(
            x = x,
            barHeight = barHeight,
            barWidthPx = barWidthPx,
            centerY = centerY,
            totalHeight = size.height,
            barColor = barColor,
            cornerRadiusPx = cornerRadiusPx,
            isCentered = isCentered,
            isMirroredVertically = isMirroredVertically
        )
    }
}

private fun DrawScope.drawBar(
    x: Float,
    barHeight: Float,
    barWidthPx: Float,
    centerY: Float,
    totalHeight: Float,
    barColor: Color,
    cornerRadiusPx: Float,
    isCentered: Boolean,
    isMirroredVertically: Boolean
) {
    if (isCentered) {
        if (isMirroredVertically) {
            // Draw bars up and down from center
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, centerY - barHeight),
                size = Size(barWidthPx, barHeight * 2),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )
        } else {
            // Draw bars upward from center
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, centerY - barHeight),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )
        }
    } else {
        // Draw bars upward from bottom
        drawRoundRect(
            color = barColor,
            topLeft = Offset(x, totalHeight - barHeight * 2),
            size = Size(barWidthPx, barHeight * 2),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )
    }
}
