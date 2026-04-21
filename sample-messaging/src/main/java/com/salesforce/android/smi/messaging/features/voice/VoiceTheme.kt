package com.salesforce.android.smi.messaging.features.voice

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.salesforce.android.smi.messaging.R

/**
 * Voice UI icons
 */
object VoiceIcons {
    val voice: Painter
        @Composable get() = painterResource(R.drawable.smi_action_voice)

    val endVoice: Painter
        @Composable get() = painterResource(R.drawable.smi_action_end_voice)

    val mute: Painter
        @Composable get() = painterResource(R.drawable.smi_action_mute)

    val unmute: Painter
        @Composable get() = painterResource(R.drawable.smi_action_unmute)
}
