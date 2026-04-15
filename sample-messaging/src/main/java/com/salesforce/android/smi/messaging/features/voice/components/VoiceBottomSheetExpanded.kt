package com.salesforce.android.smi.messaging.features.voice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import com.salesforce.android.smi.multimedia.common.api.participant.MultimediaParticipantOrigin
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSession
import com.salesforce.android.smi.ui.R
import com.salesforce.android.smi.messaging.features.voice.VoiceColors
import com.salesforce.android.smi.messaging.features.voice.VoiceDimens
import com.salesforce.android.smi.messaging.features.voice.VoiceIcons

@Composable
internal fun VoiceBottomSheetExpanded(
    session: MultimediaSession,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = VoiceColors.sheetExpandedBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = VoiceDimens.padding48,
                    start = VoiceDimens.padding12,
                    end = VoiceDimens.padding12,
                    bottom = VoiceDimens.padding12
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExpandedMiddleContainer(session = session)
            Spacer(Modifier.weight(1f))
            ExpandedStartContainer(session = session)
            Spacer(Modifier.height(VoiceDimens.padding32))
            ExpandedTranscriptPlaceholder()
            Spacer(Modifier.weight(1f))
            ExpandedEndContainer(session = session, onEndCall = onEndCall)
            Spacer(Modifier.height(VoiceDimens.padding32))
        }
    }
}

@Composable
private fun ExpandedMiddleContainer(session: MultimediaSession) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = session.displayName.ifEmpty { stringResource(R.string.smi_voice_call_default_name) },
            style = MaterialTheme.typography.headlineMedium,
            color = VoiceColors.textPrimary
        )
        ElapsedTime(
            startTime = session.createdAt,
            style = MaterialTheme.typography.bodyLarge,
            color = VoiceColors.textSecondary
        )
    }
}

@Composable
private fun ExpandedStartContainer(session: MultimediaSession) {
    Box(
        modifier = Modifier
            .size(VoiceDimens.size128)
            .clip(CircleShape)
            .background(VoiceColors.visualizerBackground),
        contentAlignment = Alignment.Center
    ) {
        VoiceVisualizer(
            session = session,
            origin = MultimediaParticipantOrigin.Remote,
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(VoiceDimens.size72),
            barCount = 3,
            barWidth = VoiceDimens.padding8,
            barSpacing = VoiceDimens.padding8,
            cornerRadius = VoiceDimens.radius8,
            minBarHeightFraction = 0.24f,
            isMirroredHorizontally = false,
            barColor = VoiceColors.visualizerBar
        )
    }
}

@Composable
private fun ExpandedTranscriptPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VoiceDimens.padding24)
    ) {
        Text(
            text = "Lorem ipsum dolor sit amet, " +
                "consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                " ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco laboris.",
            style = MaterialTheme.typography.titleLarge,
            color = VoiceColors.textSecondary
        )
    }
}

@Composable
private fun ExpandedEndContainer(
    session: MultimediaSession,
    onEndCall: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LocalVisualizerContainer(session = session)

        VoiceFloatingActionButton(
            onClick = { session.audioInput(!session.isMicrophoneMuted) },
            icon = if (session.isMicrophoneMuted) VoiceIcons.mute else VoiceIcons.unmute,
            contentDescription = stringResource(
                if (session.isMicrophoneMuted) {
                    R.string.smi_voice_unmute_microphone
                } else {
                    R.string.smi_voice_mute_microphone
                }
            )
        )

        VoiceFloatingActionButton(
            onClick = onEndCall,
            icon = VoiceIcons.endVoice,
            contentDescription = stringResource(R.string.smi_voice_end_call)
        )
    }
}

@Composable
private fun LocalVisualizerContainer(session: MultimediaSession) {
    Box(
        modifier = Modifier
            .size(VoiceDimens.size64)
            .clip(CircleShape)
            .background(VoiceColors.visualizerLocalBackground),
        contentAlignment = Alignment.Center
    ) {
        VoiceVisualizer(
            session = session,
            origin = MultimediaParticipantOrigin.Local,
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(VoiceDimens.size32),
            barCount = 3,
            barWidth = VoiceDimens.padding4,
            barSpacing = VoiceDimens.padding2,
            cornerRadius = VoiceDimens.radius8,
            minBarHeightFraction = 0.24f,
            isMirroredHorizontally = false,
            barColor = VoiceColors.visualizerBar
        )
    }
}

@Composable
private fun VoiceFloatingActionButton(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(VoiceDimens.padding2, VoiceColors.buttonBorder, CircleShape)
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = VoiceColors.buttonBackground,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = VoiceDimens.zero,
                pressedElevation = VoiceDimens.zero
            )
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = VoiceColors.buttonIcon
            )
        }
    }
}
