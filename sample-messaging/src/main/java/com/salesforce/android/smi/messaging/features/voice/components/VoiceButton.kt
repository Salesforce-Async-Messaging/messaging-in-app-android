package com.salesforce.android.smi.messaging.features.voice.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.features.voice.rememberVoiceState
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSessionStatus
import com.salesforce.android.smi.network.data.domain.conversation.Conversation
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.modality.Modality
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun VoiceButton(
    coreClient: CoreClient,
    conversationClient: ConversationClient
) {
    val voiceState = rememberVoiceState(coreClient, conversationClient)

    val conversation by remember {
        conversationClient.conversation
            .filterIsInstance<Result.Success<Conversation>>()
            .map { it.data }
    }.collectAsStateWithLifecycle(null)

    val isVoiceSupported = conversation?.supportedModalities?.contains(Modality.Voice) == true

    IconButton(
        onClick = {
            val session = voiceState.activeSession
            if (voiceState.multimediaClient != null) {
                // Check if there's an active session (not ended/disconnected)
                val isSessionActive = session != null &&
                    session.status != MultimediaSessionStatus.Ended &&
                    session.status != MultimediaSessionStatus.Disconnected

                if (isSessionActive) {
                    voiceState.scope.launch {
                        conversationClient.changeMode(Modality.Messaging)
                    }
                } else {
                    voiceState.requestPermissionAndStartCall()
                }
            }
        },
        enabled = voiceState.multimediaClient != null && isVoiceSupported,
        content = {
            val session = voiceState.activeSession
            val isSessionActive = session != null &&
                session.status != MultimediaSessionStatus.Ended &&
                session.status != MultimediaSessionStatus.Disconnected

            Icons.Filled.Phone.run {
                Icon(
                    this,
                    contentDescription = if (isSessionActive) "End Voice Call" else "Start Voice Call"
                )
            }
        }
    )
}