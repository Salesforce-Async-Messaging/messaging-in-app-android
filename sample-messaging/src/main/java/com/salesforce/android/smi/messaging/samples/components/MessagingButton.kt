package com.salesforce.android.smi.messaging.samples.components

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.samples.state.rememberMessagingSessionState

/**
 * Button to launch the chat UI with a badge for displaying the current unread message count.
 *
 * Must be connected to the MessagingEventStream to receive updates.
 * @see [com.salesforce.android.smi.messaging.samples.state.LifecycleResumeMessagingStreamEffect]
 */
@Composable
fun MessagingButton(salesforceMessaging: SalesforceMessaging) {
    val context = LocalContext.current

    MessagingButton(
        salesforceMessaging
    ) { salesforceMessaging.uiClient.openConversationActivity(context) }
}

@Composable
fun MessagingButton(
    salesforceMessaging: SalesforceMessaging,
    onOpen: () -> Unit
) {
    MessagingButton(salesforceMessaging.conversationClient, onOpen)
}

@Composable
fun MessagingButton(
    conversationClient: ConversationClient,
    onOpen: () -> Unit
) {
    val messagingSessionState = rememberMessagingSessionState(conversationClient)

    IconButton(
        onClick = onOpen,
        content = { MessagingIcon(unreadMessageCount = messagingSessionState.unreadMessageCount) }
    )
}
