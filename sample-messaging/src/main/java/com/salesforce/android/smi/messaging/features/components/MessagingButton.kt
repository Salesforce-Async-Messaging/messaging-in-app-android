package com.salesforce.android.smi.messaging.features.components

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.salesforce.android.smi.messaging.SalesforceMessaging

/**
 * Button to launch the chat UI with a badge for displaying the current unread message count.
 *
 * Must be connected to the MessagingEventStream to receive updates.
 * @see [LifecycleResumeMessagingStreamEffect]
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
    val messagingSessionState = rememberMessagingSessionState(salesforceMessaging)

    IconButton(
        onClick = onOpen,
        content = { MessagingIcon(unreadMessageCount = messagingSessionState.unreadMessageCount) }
    )
}
