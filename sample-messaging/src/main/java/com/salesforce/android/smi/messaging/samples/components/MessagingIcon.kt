package com.salesforce.android.smi.messaging.samples.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.samples.state.rememberMessagingSessionState

/**
 * Icon with a badge for displaying the current unread message count.
 *
 * Must be connected to the MessagingEventStream to receive updates.
 * @see [com.salesforce.android.smi.messaging.samples.state.LifecycleResumeMessagingStreamEffect]
 */
@Composable
fun MessagingIcon(
    modifier: Modifier = Modifier,
    salesforceMessaging: SalesforceMessaging,
    icon: ImageVector = Icons.AutoMirrored.Filled.Chat
) {
    MessagingIcon(modifier, salesforceMessaging.conversationClient, icon)
}

@Composable
fun MessagingIcon(
    modifier: Modifier = Modifier,
    conversationClient: ConversationClient,
    icon: ImageVector = Icons.AutoMirrored.Filled.Chat
) {
    val messagingSessionState = rememberMessagingSessionState(conversationClient)
    MessagingIcon(modifier, messagingSessionState.unreadMessageCount, icon)
}

@Composable
fun MessagingIcon(
    modifier: Modifier = Modifier,
    unreadMessageCount: Int,
    icon: ImageVector = Icons.AutoMirrored.Filled.Chat
) {
    MessagingIcon(modifier, unreadMessageCount) {
        Icon(
            icon,
            icon.name,
            Modifier.size(32.dp)
        )
    }
}

@Composable
fun MessagingIcon(
    modifier: Modifier = Modifier,
    unreadMessageCount: Int,
    icon: @Composable () -> Unit
) {
    BadgedBox(
        modifier = modifier.padding(top = 6.dp, end = 12.dp),
        badge = {
            AnimatedVisibility(
                unreadMessageCount > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Badge { Text(unreadMessageCount.toString()) }
            }
        },
        content = { icon() }
    )
}

@Preview
@Composable
private fun MessagingIconPreview() {
    MessagingIcon(unreadMessageCount = 99)
}
