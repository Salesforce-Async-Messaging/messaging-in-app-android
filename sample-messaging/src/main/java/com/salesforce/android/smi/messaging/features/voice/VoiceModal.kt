package com.salesforce.android.smi.messaging.features.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSessionStatus
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.modality.Modality
import kotlinx.coroutines.launch

@Composable
fun VoiceModal(
    coreClient: CoreClient,
    conversationClient: ConversationClient
) {
    val voiceState = rememberVoiceState(coreClient, conversationClient)

    // Join the session when it's first created
    LaunchedEffect(voiceState.activeSession?.identifier) {
        voiceState.activeSession?.let { session ->
            if (session.status == MultimediaSessionStatus.Created) {
                session.join()
            }
        }
    }

    VoiceBottomSheet(
        session = voiceState.activeSession,
        conversationClient = conversationClient,
        onEndCall = {
            voiceState.scope.launch {
                voiceState.activeSession?.end()
                conversationClient.changeMode(Modality.Messaging)
            }
        }
    )
}