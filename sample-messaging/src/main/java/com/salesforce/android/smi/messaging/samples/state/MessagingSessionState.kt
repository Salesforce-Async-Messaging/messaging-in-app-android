package com.salesforce.android.smi.messaging.samples.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 *  Creates a MessagingSessionState that is remembered across compositions.
 */
@Composable
fun rememberMessagingSessionState(salesforceMessaging: SalesforceMessaging): MessagingSessionState =
    rememberMessagingSessionState(salesforceMessaging.conversationClient)

@Composable
fun rememberMessagingSessionState(conversationClients: List<ConversationClient>): List<MessagingSessionState> =
    conversationClients.map {
        rememberMessagingSessionState(it)
    }

@Composable
fun rememberMessagingSessionState(conversationClient: ConversationClient): MessagingSessionState {
    val state by remember(conversationClient.conversationId) {
        provideMessagingSessionStateFlow(conversationClient)
    }.collectAsStateWithLifecycle(MessagingSessionState())

    return state
}

@OptIn(FlowPreview::class)
private fun provideMessagingSessionStateFlow(conversationClient: ConversationClient): Flow<MessagingSessionState> =
    combine(
        conversationClient.conversation.map { it.data },
        conversationClient.conversationEntriesFlow().mapNotNull { it.data }
    ) { conversation, conversationEntries ->
        val sessionStatus: SessionStatus =
            conversationEntries.latestPayloadOrNull<EntryPayload.SessionStatusChangedPayload>()?.sessionStatus ?: SessionStatus.Inactive

        val queuePosition: Int = sessionStatus
            .takeIf { it == SessionStatus.Active }
            ?.let { conversationEntries.latestPayloadOrNull<EntryPayload.QueuePositionPayload>()?.position } ?: 0

        val unreadMessageCount: Int =
            sessionStatus
                .takeIf { it == SessionStatus.Active }
                ?.let { conversation?.unreadMessageCount } ?: 0

        val latestEntry = conversationEntries.latestEntry()
        val statusText = latestEntry?.text
        val agentName = latestEntry?.sender?.displayName ?: "Agent"

        MessagingSessionState(
            conversation,
            conversationEntries,
            sessionStatus,
            queuePosition,
            unreadMessageCount,
            statusText,
            agentName
        )
    }.debounce(500)

/**
 * A state object that can be hoisted to observe common messaging session state.
 */
data class MessagingSessionState(
    val conversation: Conversation? = null,
    val conversationEntries: List<ConversationEntry> = emptyList(),
    val sessionStatus: SessionStatus = SessionStatus.Inactive,
    val queuePosition: Int = 0,
    val unreadMessageCount: Int = 0,
    val statusText: String? = null,
    val agentName: String = "Agent"
)

/**
 * Helper to get a particular messaging payload from a [List] of [ConversationEntry].
 */
private inline fun <reified T : EntryPayload> List<ConversationEntry>.latestPayloadOrNull(): T? =
    this.firstNotNullOfOrNull { it.payload as? T }

/**
 * Helper to filter the latest entry we wish to render
 */
private fun List<ConversationEntry>.latestEntry(): ConversationEntry? =
    firstNotNullOfOrNull {
        val messagePayload = (it.payload as? EntryPayload.MessagePayload)
        when (messagePayload?.content) {
            is ChoicesFormat.DisplayableOptionsFormat,
            is ChoicesFormat.QuickRepliesFormat,
            is StaticContentFormat.AttachmentsFormat,
            is StaticContentFormat.RichLinkFormat,
            is StaticContentFormat.TextFormat,
            is StaticContentFormat.WebViewFormat -> it
            else -> null
        }
    }

/**
 * Gets the simple text body from various types of message payloads. Can be expanded to support more types.
 */
private val ConversationEntry.text: String?
    get() = when (val content = (payload as? EntryPayload.MessagePayload)?.content) {
        is ChoicesFormat.DisplayableOptionsFormat -> content.text
        is ChoicesFormat.QuickRepliesFormat -> content.text
        is StaticContentFormat.AttachmentsFormat -> content.text
        is StaticContentFormat.RichLinkFormat -> content.text
        is StaticContentFormat.TextFormat -> content.text
        is StaticContentFormat.WebViewFormat -> content.title.title
        else -> null
    }
