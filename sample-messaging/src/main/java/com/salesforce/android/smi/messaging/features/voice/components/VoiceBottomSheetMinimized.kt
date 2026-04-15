package com.salesforce.android.smi.messaging.features.voice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
internal fun VoiceBottomSheetMinimized(
    session: MultimediaSession,
    onExpand: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand),
        shape = RoundedCornerShape(topStart = VoiceDimens.radius20, topEnd = VoiceDimens.radius20),
        color = VoiceColors.sheetMinimizedBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VoiceDimens.size24, vertical = VoiceDimens.padding8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MinimizedContentContainer(session = session)
            MinimizedEndContainer(session = session, onEndCall = onEndCall)
        }
    }
}

@Composable
private fun RowScope.MinimizedContentContainer(session: MultimediaSession) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
    ) {
        MinimizedVisualizerContainer(session = session)
        Spacer(Modifier.width(VoiceDimens.padding16))
        MinimizedTextContainer(session = session)
    }
}

@Composable
private fun MinimizedVisualizerContainer(session: MultimediaSession) {
    Box(
        modifier = Modifier
            .size(VoiceDimens.size48)
            .clip(CircleShape)
            .background(VoiceColors.visualizerBackground),
        contentAlignment = Alignment.Center
    ) {
        VoiceVisualizer(
            session = session,
            origin = MultimediaParticipantOrigin.Remote,
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
private fun MinimizedTextContainer(session: MultimediaSession) {
    Column(verticalArrangement = Arrangement.spacedBy(VoiceDimens.padding2)) {
        Text(
            text = session.displayName.ifEmpty { stringResource(R.string.smi_voice_call_default_name) },
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
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
private fun MinimizedEndContainer(
    session: MultimediaSession,
    onEndCall: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VoiceDimens.padding8)
    ) {
        VoiceIconButton(
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

        VoiceIconButton(
            onClick = onEndCall,
            icon = VoiceIcons.endVoice,
            contentDescription = stringResource(R.string.smi_voice_end_call)
        )
    }
}

@Composable
private fun VoiceIconButton(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(VoiceDimens.padding2, VoiceColors.buttonBorder, CircleShape)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(VoiceDimens.size48)
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = VoiceColors.buttonIcon,
                modifier = Modifier.size(VoiceDimens.size24)
            )
        }
    }
}
