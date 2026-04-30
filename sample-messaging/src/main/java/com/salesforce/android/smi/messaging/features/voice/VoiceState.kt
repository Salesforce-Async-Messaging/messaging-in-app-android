package com.salesforce.android.smi.messaging.features.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.multimedia.common.api.MultimediaClient
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSession
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.modality.Modality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Stable
data class VoiceState(
    val multimediaClient: MultimediaClient?,
    val activeSession: MultimediaSession?,
    val scope: CoroutineScope,
    val requestPermissionAndStartCall: () -> Unit
)

@Composable
fun rememberVoiceState(
    coreClient: CoreClient,
    conversationClient: ConversationClient
): VoiceState {
    val multimediaClient = coreClient.multimediaClient()

    val activeSession: MultimediaSession? by remember {
        multimediaClient?.currentSessionFlow ?: flowOf(null)
    }.collectAsStateWithLifecycle(null)

    val scope = rememberCoroutineScope()

    val requestPermissionAndStartCall = rememberRecordAudioPermission {
        scope.launch {
            conversationClient.changeModality(Modality.Voice)
        }
    }

    return remember(multimediaClient, activeSession, scope) {
        VoiceState(
            multimediaClient = multimediaClient,
            activeSession = activeSession,
            scope = scope,
            requestPermissionAndStartCall = requestPermissionAndStartCall
        )
    }
}
