package com.salesforce.android.smi.messaging.features.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
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

    val sessionStatus: SessionStatus? by remember {
        derivedStateOf {
            conversationEntries.latestPayloadOrNull<EntryPayload.SessionStatusChangedPayload>()?.sessionStatus
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
private fun WidgetContainer(content: @Composable RowScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(.75f), contentAlignment = Alignment.CenterEnd) {
        Surface(
            modifier = Modifier.heightIn(max = 64.dp),
            shape = FloatingActionButtonDefaults.shape,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .animateContentSize(
                            animationSpec =
                                tween(
                                    durationMillis = 200,
                                    delayMillis = 0,
                                    easing = FastOutSlowInEasing
                                )
                        ),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

@Composable
private fun MessagingSessionWidget(
    sessionStatus: SessionStatus?,
    queuePosition: Int,
    conversation: Conversation?,
    openConversation: () -> Unit,
    endSession: () -> Unit
) {
    WidgetContainer {
        Box(contentAlignment = Alignment.Center) {
            when (sessionStatus) {
                SessionStatus.Active ->
                    IconButton(onClick = endSession) {
                        Icon(Icons.Default.Close, Icons.Default.Close.name)
                    }

                else -> Spacer(Modifier)
            }
        }
        Box(contentAlignment = Alignment.Center) {
            SessionStatusInfo(sessionStatus, queuePosition, conversation, openConversation)
        }
    }
}

@Composable
private fun SessionStatusInfo(
    sessionStatus: SessionStatus?,
    queuePosition: Int,
    conversation: Conversation?,
    openConversation: () -> Unit
) {
    TextButton(onClick = openConversation) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
            conversation?.let {
                when (sessionStatus) {
                    SessionStatus.Active ->
                        ActiveSessionText(
                            Modifier.weight(0.5f, false),
                            conversation,
                            queuePosition
                        )

                    else ->
                        Text(
                            text = sessionStatus?.name ?: "Loading...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                }
            } ?: Text(
                text = sessionStatus?.name ?: "Start a chat",
                style = MaterialTheme.typography.bodyLarge
            )

            val unreadMessageCount = if (sessionStatus == SessionStatus.Active) conversation?.unreadMessageCount ?: 0 else 0
            SessionBadgeIcon(Modifier, unreadMessageCount)
        }
    }
}

@Composable
private fun ActiveSessionText(
    modifier: Modifier,
    conversation: Conversation?,
    queuePosition: Int,
    showLastInboundMessage: Boolean = true
) {
    val activeSessionText = when {
        queuePosition > 0 -> "Your position in queue is $queuePosition."
        showLastInboundMessage -> conversation?.lastInboundMessage()
        else -> conversation?.agentParticipant()
    }
    activeSessionText?.let {
        Text(modifier = modifier, text = it, style = MaterialTheme.typography.bodyLarge, softWrap = false, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Conversation.agentParticipant(): String =
    activeParticipants
        .firstOrNull {
            it.roleType == ParticipantRoleType.Agent || it.roleType == ParticipantRoleType.Chatbot
        }?.displayName ?: "Agent"

@Composable
private fun Conversation.lastInboundMessage(): String {
    var lastInboundMessage: String by rememberSaveable { mutableStateOf("") }

    val messagePayload = (lastActivity?.payload as? EntryPayload.MessagePayload)
    when (val content = messagePayload?.content) {
        is ChoicesFormat.DisplayableOptionsFormat -> content.text
        is ChoicesFormat.QuickRepliesFormat -> content.text
        is StaticContentFormat.AttachmentsFormat -> content.text
        is StaticContentFormat.RichLinkFormat -> content.text
        is StaticContentFormat.TextFormat -> content.text
        is StaticContentFormat.WebViewFormat -> content.title.title
        else -> null
    }?.takeIf { it.isNotBlank() }?.let { lastInboundMessage = it }

    return lastInboundMessage
}

@Composable
private fun SessionBadgeIcon(
    modifier: Modifier,
    unreadMessageCount: Int
) {
    BadgedBox(
        modifier = modifier,
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
