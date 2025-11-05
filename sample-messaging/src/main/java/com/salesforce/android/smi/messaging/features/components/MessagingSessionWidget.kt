package com.salesforce.android.smi.messaging.features.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.common.api.data
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.network.data.domain.conversation.Conversation
import com.salesforce.android.smi.network.data.domain.conversation.CoreConversation
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.SessionStatus
import com.salesforce.android.smi.network.data.domain.participant.ParticipantRoleType
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MessagingSessionWidget(
    conversationClient: ConversationClient,
    openConversation: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val endSession: () -> Unit = {
        coroutineScope.launch { conversationClient.endSession() }
    }

    val conversation: Conversation? by remember {
        conversationClient.conversation.map { it.data }
    }.collectAsStateWithLifecycle(null)

    val conversationEntries: Result<List<ConversationEntry>> by remember {
        conversationClient.conversationEntriesFlow()
    }.collectAsStateWithLifecycle(Result.Loading)

    val sessionStatus: SessionStatus by remember {
        derivedStateOf {
            conversationEntries.latestPayloadOrNull<EntryPayload.SessionStatusChangedPayload>()?.sessionStatus ?: SessionStatus.Inactive
        }
    }
    val queuePosition: Int by remember {
        derivedStateOf {
            conversationEntries.latestPayloadOrNull<EntryPayload.QueuePositionPayload>()?.position ?: 0
        }
    }

    MessagingSessionWidget(
        sessionStatus = sessionStatus,
        queuePosition = queuePosition,
        conversation = conversation,
        openConversation = openConversation,
        endSession = endSession
    )
}

@Composable
private fun MessagingSessionWidget(
    sessionStatus: SessionStatus,
    queuePosition: Int,
    conversation: Conversation?,
    openConversation: () -> Unit,
    endSession: () -> Unit
) {
    Surface(
        modifier = Modifier.heightIn(max = 64.dp),
        shape = FloatingActionButtonDefaults.shape,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxHeight().animateContentSize(
                animationSpec = tween(durationMillis = 200, delayMillis = 0, easing = FastOutSlowInEasing)
            ),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = endSession) {
                    Icon(Icons.Default.Close, Icons.Default.Close.name)
                }
            }
            Box(contentAlignment = Alignment.Center) {
                SessionStatusInfo(sessionStatus, queuePosition, conversation, openConversation)
            }
        }
    }
}

@Composable
private fun SessionStatusInfo(
    sessionStatus: SessionStatus,
    queuePosition: Int,
    conversation: Conversation?,
    openConversation: () -> Unit
) {
    AnimatedContent(sessionStatus) { sessionStatusState ->
        TextButton(onClick = openConversation) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                when (sessionStatusState) {
                    SessionStatus.Active -> ActiveSessionText(conversation, queuePosition)
                    else -> Text(text = sessionStatusState.name, style = MaterialTheme.typography.bodyLarge)
                }

                val unreadMessageCount = if (sessionStatusState == SessionStatus.Active) conversation?.unreadMessageCount ?: 0 else 0
                SessionBadgeIcon(unreadMessageCount)
            }
        }
    }
}

@Composable
private fun ActiveSessionText(
    conversation: Conversation?,
    queuePosition: Int
) {
    val activeSessionText = if (queuePosition > 0) {
        "Your position in queue is $queuePosition."
    } else {
        conversation?.activeParticipants?.firstOrNull {
            it.roleType == ParticipantRoleType.Agent || it.roleType == ParticipantRoleType.Chatbot
        }?.displayName ?: "Agent"
    }

    Text(text = activeSessionText, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun SessionBadgeIcon(unreadMessageCount: Int) {
    BadgedBox(
        badge = {
            unreadMessageCount.takeIf { it > 0 }?.let {
                Badge { Text(it.toString()) }
            }
        },
        content = {
            Icon(Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Filled.Chat.name, Modifier.size(32.dp))
        }
    )
}

private inline fun <reified T : EntryPayload> Result<List<ConversationEntry>>.latestPayloadOrNull(): T? =
    this.data?.firstNotNullOfOrNull { it.payload as? T }

@Composable
@Preview
private fun InactiveSessionWidgetPreview() {
    MessagingSessionWidget(SessionStatus.Inactive, 0, null, {}) {}
}

@Composable
@Preview
private fun ActiveSessionWidgetPreview() {
    val fakeConversation = CoreConversation(UUID.randomUUID(), "dev", emptyList(), unreadMessageCount = 10)
    MessagingSessionWidget(SessionStatus.Active, 0, fakeConversation, {}) {}
}

@Composable
@Preview
private fun QueuedSessionWidgetPreview() {
    MessagingSessionWidget(SessionStatus.Active, 10, null, {}) {}
}
