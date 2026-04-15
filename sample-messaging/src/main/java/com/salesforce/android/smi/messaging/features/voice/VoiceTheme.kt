package com.salesforce.android.smi.messaging.features.voice

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salesforce.android.smi.messaging.R

/**
 * Voice UI dimensions
 */
internal object VoiceDimens {
    val padding2 = 2.dp
    val padding4 = 4.dp
    val padding8 = 8.dp
    val padding12 = 12.dp
    val padding16 = 16.dp
    val padding24 = 24.dp
    val padding32 = 32.dp
    val padding48 = 48.dp

    val size24 = 24.dp
    val size32 = 32.dp
    val size48 = 48.dp
    val size64 = 64.dp
    val size72 = 72.dp
    val size80 = 80.dp
    val size128 = 128.dp

    val radius8 = 8.dp
    val radius20 = 20.dp
    val zero = 0.dp
}

/**
 * Voice UI colors
 */
internal object VoiceColors {
    val sheetMinimizedBackground: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val sheetExpandedBackground: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    val textPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val textSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val visualizerBackground: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer

    val visualizerLocalBackground: Color
        @Composable get() = MaterialTheme.colorScheme.secondaryContainer

    val visualizerBar: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val buttonBackground: Color
        @Composable get() = MaterialTheme.colorScheme.secondaryContainer

    val buttonBorder: Color
        @Composable get() = MaterialTheme.colorScheme.outline

    val buttonIcon: Color
        @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer
}

/**
 * Voice UI icons
 */
internal object VoiceIcons {
    val voice: Painter
        @Composable get() = painterResource(R.drawable.smi_action_voice)

    val endVoice: Painter
        @Composable get() = painterResource(R.drawable.smi_action_end_voice)

    val mute: Painter
        @Composable get() = painterResource(R.drawable.smi_action_mute)

    val unmute: Painter
        @Composable get() = painterResource(R.drawable.smi_action_unmute)
}
