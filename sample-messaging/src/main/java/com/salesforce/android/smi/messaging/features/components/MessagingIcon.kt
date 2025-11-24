package com.salesforce.android.smi.messaging.features.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salesforce.android.smi.messaging.SalesforceMessaging

/**
 * Icon with a badge for displaying the current unread message count.
 *
 * Must be connected to the MessagingEventStream to receive updates.
 * @see [LifecycleResumeMessagingStreamEffect]
 */
@Composable
fun MessagingIcon(
    modifier: Modifier = Modifier,
    salesforceMessaging: SalesforceMessaging,
    icon: @Composable () -> Unit = { MessagingIconDefaults.Icon }
) {
    val messagingSessionState = rememberMessagingSessionState(salesforceMessaging)
    MessagingIcon(modifier, messagingSessionState.unreadMessageCount, icon)
}

@Composable
fun MessagingIcon(
    modifier: Modifier = Modifier,
    unreadMessageCount: Int,
    icon: @Composable () -> Unit = { MessagingIconDefaults.Icon }
) {
    BadgedBox(
        modifier = modifier.padding(top = 6.dp, end = 12.dp),
        badge = {
            unreadMessageCount.takeIf { it > 0 }?.let {
                Badge { Text(it.toString()) }
            }
        },
        content = { icon() }
    )
}

object MessagingIconDefaults {
    val Icon @Composable get() = Icon(
        Icons.AutoMirrored.Filled.Chat,
        Icons.AutoMirrored.Filled.Chat.name,
        Modifier.size(32.dp)
    )
}

@Preview
@Composable
private fun MessagingIconPreview() {
    MessagingIcon(unreadMessageCount = 99)
}
