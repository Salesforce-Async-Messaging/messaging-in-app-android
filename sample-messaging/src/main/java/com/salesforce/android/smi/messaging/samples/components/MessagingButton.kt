package com.salesforce.android.smi.messaging.samples.components

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.samples.state.MessagingSessionState
import com.salesforce.android.smi.messaging.samples.state.MessagingStore
import com.salesforce.android.smi.messaging.samples.state.rememberMessagingSessionState
import java.util.UUID

/**
 * Button to launch the chat UI with a badge for displaying the current unread message count.
 *
 * Backed by the [MessagingStore], so its session state is shared/deduped and gated on the inbox.
 *
 * Must be connected to the MessagingEventStream to receive updates.
 * @see [com.salesforce.android.smi.messaging.samples.state.LifecycleResumeMessagingStreamEffect]
 */
@Composable
fun MessagingButton(
    store: MessagingStore,
    conversationId: UUID,
    onOpen: () -> Unit
) {
    MessagingButton(rememberMessagingSessionState(store, conversationId), onOpen)
}

/**
 * [CoreClient]-backed overload for standalone use without a [MessagingStore]. Ungated: observes the
 * conversation's session state directly, so pass an id that is backed by a real conversation. See
 * [rememberMessagingSessionState] for the trade-offs vs the store.
 */
@Composable
fun MessagingButton(
    coreClient: CoreClient,
    conversationId: UUID,
    onOpen: () -> Unit
) {
    MessagingButton(rememberMessagingSessionState(coreClient, conversationId), onOpen)
}

/**
 * [ConversationClient]-backed overload for standalone use without a [MessagingStore]. Ungated; opens
 * its own subscription (no cross-caller dedup).
 */
@Composable
fun MessagingButton(
    conversationClient: ConversationClient,
    onOpen: () -> Unit
) {
    MessagingButton(rememberMessagingSessionState(conversationClient), onOpen)
}

/**
 * Presentational core shared by every source overload: renders the unread badge from a resolved
 * [MessagingSessionState].
 */
@Composable
fun MessagingButton(
    messagingSessionState: MessagingSessionState,
    onOpen: () -> Unit
) {
    IconButton(
        onClick = onOpen,
        content = { MessagingIcon(unreadMessageCount = messagingSessionState.unreadMessageCount) }
    )
}
