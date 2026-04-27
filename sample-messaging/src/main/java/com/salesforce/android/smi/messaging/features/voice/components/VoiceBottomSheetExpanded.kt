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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.common.api.data
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.messaging.R
import com.salesforce.android.smi.messaging.features.voice.VoiceIcons
import com.salesforce.android.smi.messaging.theme.SMIDimens
import com.salesforce.android.smi.multimedia.common.api.participant.MultimediaParticipantOrigin
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSession
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.modality.Modality
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat

@Composable
fun VoiceBottomSheetExpanded(
    session: MultimediaSession,
    conversationClient: ConversationClient,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conversation by conversationClient.conversation
        .collectAsStateWithLifecycle(initialValue = Result.Loading)

    val conversationEntries by conversationClient.conversationEntriesFlow(limit = 10)
        .collectAsStateWithLifecycle(initialValue = Result.Loading)

    val latestVoiceMessage by remember {
        derivedStateOf {
            conversation.data?.let { conv ->
                val isVoiceActive = conv.activeModalities.contains(Modality.Voice)

                if (isVoiceActive) {
                    conversationEntries.data?.firstNotNullOfOrNull { entry ->
                        val messagePayload = entry.payload as? EntryPayload.MessagePayload
                        when (val content = messagePayload?.content) {
                            is ChoicesFormat.DisplayableOptionsFormat -> content.text
                            is ChoicesFormat.QuickRepliesFormat -> content.text
                            is StaticContentFormat.AttachmentsFormat -> content.text
                            is StaticContentFormat.RichLinkFormat -> content.text
                            is StaticContentFormat.TextFormat -> content.text
                            is StaticContentFormat.WebViewFormat -> content.title.title
                            else -> null
                        }
                    }
                } else {
                    null
                }
            }
        }
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = SMIDimens.Padding.dp48,
                    start = SMIDimens.Padding.dp12,
                    end = SMIDimens.Padding.dp12,
                    bottom = SMIDimens.Padding.dp12
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExpandedMiddleContainer(session = session)
            Spacer(Modifier.weight(1f))
            ExpandedStartContainer(session = session)
            Spacer(Modifier.height(SMIDimens.Padding.dp32))
            ExpandedTranscriptDisplay(latestVoiceMessage = latestVoiceMessage)
            Spacer(Modifier.weight(1f))
            ExpandedEndContainer(session = session, onEndCall = onEndCall)
            Spacer(Modifier.height(SMIDimens.Padding.dp32))
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
            text = session.displayName,
            style = MaterialTheme.typography.headlineMedium,
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
private fun ExpandedStartContainer(session: MultimediaSession) {
    Box(
        modifier = Modifier
            .size(SMIDimens.Size.dp128)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        VoiceVisualizer(
            session = session,
            origin = MultimediaParticipantOrigin.Remote,
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(SMIDimens.Size.dp72),
            barCount = 3,
            barWidth = SMIDimens.Padding.dp8,
            barSpacing = SMIDimens.Padding.dp8,
            cornerRadius = SMIDimens.Radius.dp8,
            minBarHeightFraction = 0.24f,
            isMirroredHorizontally = true,
            barColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun ExpandedTranscriptDisplay(latestVoiceMessage: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SMIDimens.Padding.dp24)
    ) {
        Text(
            text = latestVoiceMessage ?: "",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
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
            .size(SMIDimens.Size.dp64)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(SMIDimens.Padding.dp2, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        VoiceVisualizer(
            session = session,
            origin = MultimediaParticipantOrigin.Local,
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
private fun VoiceFloatingActionButton(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .border(SMIDimens.Padding.dp2, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = SMIDimens.zero,
            pressedElevation = SMIDimens.zero,
            focusedElevation = SMIDimens.zero,
            hoveredElevation = SMIDimens.zero
        )
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
