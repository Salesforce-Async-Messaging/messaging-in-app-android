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
import com.salesforce.android.smi.messaging.R
import com.salesforce.android.smi.messaging.features.voice.VoiceIcons
import com.salesforce.android.smi.messaging.theme.SMIDimens
import com.salesforce.android.smi.multimedia.common.api.participant.MultimediaParticipantOrigin
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSession

@Composable
fun VoiceBottomSheetMinimized(
    session: MultimediaSession,
    onExpand: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand),
        shape = RoundedCornerShape(topStart = SMIDimens.Radius.dp20, topEnd = SMIDimens.Radius.dp20),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SMIDimens.Size.dp24, vertical = SMIDimens.Padding.dp8),
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
        Spacer(Modifier.width(SMIDimens.Padding.dp16))
        MinimizedTextContainer(session = session)
    }
}

@Composable
private fun MinimizedVisualizerContainer(session: MultimediaSession) {
    Box(
        modifier = Modifier
            .size(SMIDimens.Size.dp48)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(SMIDimens.Padding.dp2, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        VoiceVisualizer(
            session = session,
            origin = MultimediaParticipantOrigin.Remote,
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(SMIDimens.Size.dp32),
            barCount = 3,
            barWidth = SMIDimens.Padding.dp4,
            barSpacing = SMIDimens.Padding.dp2,
            cornerRadius = SMIDimens.Radius.dp8,
            minBarHeightFraction = 0.24f,
            isMirroredHorizontally = true,
            barColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MinimizedTextContainer(session: MultimediaSession) {
    Column(verticalArrangement = Arrangement.spacedBy(SMIDimens.Padding.dp2)) {
        Text(
            text = session.displayName,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
        ElapsedTime(
            startTime = session.createdAt,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        horizontalArrangement = Arrangement.spacedBy(SMIDimens.Padding.dp8)
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
            .size(SMIDimens.Size.dp48)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(SMIDimens.Padding.dp2, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(SMIDimens.Size.dp48)
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(SMIDimens.Size.dp24)
            )
        }
    }
}
