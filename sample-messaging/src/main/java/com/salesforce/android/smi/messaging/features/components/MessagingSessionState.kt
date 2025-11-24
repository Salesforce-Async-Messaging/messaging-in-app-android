package com.salesforce.android.smi.messaging.features.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.common.api.data
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.network.data.domain.conversation.Conversation
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.SessionStatus
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
import com.salesforce.android.smi.network.data.domain.participant.ParticipantRoleType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 *  Creates a MessagingSessionState that is remembered across compositions.
 */
@Composable
fun rememberMessagingSessionState(
    salesforceMessaging: SalesforceMessaging,
    showLastMessage: Boolean = true
): MessagingSessionState = rememberMessagingSessionState(salesforceMessaging.conversationClient, showLastMessage)

/**
 *  Creates a MessagingSessionState that is remembered across compositions.
 */
@OptIn(FlowPreview::class)
@Composable
fun rememberMessagingSessionState(
    conversationClient: ConversationClient,
    showLastMessage: Boolean = true
): MessagingSessionState {
    val conversation: Conversation? by remember(conversationClient) {
        conversationClient.conversation
            .map { it.data }
            .debounce(500)
    }.collectAsStateWithLifecycle(null)

    val conversationEntries: List<ConversationEntry> by remember(conversation?.identifier) {
        conversationClient.conversationEntriesFlow().mapNotNull { it.data }.debounce(500)
    }.collectAsStateWithLifecycle(emptyList())

    val sessionStatus: SessionStatus =
        conversationEntries.latestPayloadOrNull<EntryPayload.SessionStatusChangedPayload>()?.sessionStatus ?: SessionStatus.Inactive

    val queuePosition: Int by remember(conversation?.identifier, sessionStatus) {
        derivedStateOf {
            sessionStatus
                .takeIf { it == SessionStatus.Active }
                ?.let { conversationEntries.latestPayloadOrNull<EntryPayload.QueuePositionPayload>()?.position } ?: 0
        }
    }
    val unreadMessageCount: Int by remember(sessionStatus) {
        derivedStateOf {
            sessionStatus
                .takeIf { it == SessionStatus.Active }
                ?.let { conversation?.unreadMessageCount } ?: 0
        }
    }

    val statusText: String = when (showLastMessage) {
        true -> conversation?.lastInboundMessage()
        false -> conversation?.agentParticipant()
    } ?: "Loading..."

    val messagingSessionState by remember(sessionStatus, queuePosition, unreadMessageCount, statusText) {
        derivedStateOf {
            MessagingSessionState(conversation, conversationEntries, sessionStatus, queuePosition, unreadMessageCount, statusText)
        }
    }

    return messagingSessionState
}

/**
 * A state object that can be hoisted to observe common messaging session state.
 */
data class MessagingSessionState(
    val conversation: Conversation? = null,
    val conversationEntries: List<ConversationEntry> = emptyList(),
    val sessionStatus: SessionStatus = SessionStatus.Inactive,
    val queuePosition: Int = 0,
    val unreadMessageCount: Int = 0,
    val statusText: String = "Loading..."
)

/**
 * Helper to get a particular messaging payload from a [List] of [ConversationEntry].
 */
private inline fun <reified T : EntryPayload> List<ConversationEntry>.latestPayloadOrNull(): T? =
    this.firstNotNullOfOrNull { it.payload as? T }

/**
 * Gets a remote Agent or ChatBot Participant from the [Conversation].
 */
private fun Conversation.agentParticipant(): String =
    activeParticipants
        .firstOrNull {
            it.roleType == ParticipantRoleType.Agent || it.roleType == ParticipantRoleType.Chatbot
        }?.displayName ?: "Agent"

/**
 * Basic helper to get the simple text body from various types of message payloads. Can be expanded to support more types.
 */
@Composable
private fun Conversation.lastInboundMessage(): String {
    var lastInboundMessage: String by rememberSaveable { mutableStateOf("Loading") }

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
