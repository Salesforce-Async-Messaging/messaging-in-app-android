package com.salesforce.android.smi.messaging.samples.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.samples.state.MessagingSessionState
import com.salesforce.android.smi.messaging.samples.state.rememberMessagingSessionState
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.SessionStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

/**
 * Simple messaging widget that consumes the state contained in the [MessagingSessionState] object.
 * Features include:
 *  - Button to launch the chat UI
 *  - Button to End the current session
 *  - The queue position
 *  - The last message received
 *  - the unread message count
 *
 * Must be connected to the MessagingEventStream to receive updates.
 * @see [com.salesforce.android.smi.messaging.samples.state.LifecycleResumeMessagingStreamEffect]
 */
@OptIn(FlowPreview::class)
@Composable
fun MessagingWidget(
    salesforceMessaging: SalesforceMessaging,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    MessagingWidget(
        salesforceMessaging,
        modifier,
        { salesforceMessaging.uiClient.openConversationActivity(context) }
    )
}

@OptIn(FlowPreview::class)
@Composable
fun MessagingWidget(
    salesforceMessaging: SalesforceMessaging,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
    onEnd: () -> Unit = {}
) {
    MessagingWidget(salesforceMessaging.conversationClient, modifier, onOpen, onEnd)
}

@OptIn(FlowPreview::class)
@Composable
fun MessagingWidget(
    conversationClient: ConversationClient,
    modifier: Modifier = Modifier,
    onEnd: () -> Unit = {},
    onOpen: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val messagingSessionState = rememberMessagingSessionState(conversationClient)

    MessagingWidget(messagingSessionState, modifier, onOpen) {
        coroutineScope.launch {
            conversationClient.endSession()
            onEnd()
        }
    }
}

@Composable
fun MessagingWidget(
    messagingSessionState: MessagingSessionState,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
    onEnd: () -> Unit
) {
    val (_, _, sessionStatus: SessionStatus, queuePosition, unreadMessageCount, statusText) = messagingSessionState
    MessagingWidget(modifier, sessionStatus, queuePosition, unreadMessageCount, statusText, onOpen, onEnd)
}

@Composable
fun MessagingWidget(
    modifier: Modifier = Modifier,
    sessionStatus: SessionStatus,
    queuePosition: Int,
    unreadMessageCount: Int,
    statusText: String?,
    onClick: () -> Unit,
    endSession: () -> Unit
) {
    WidgetContainer(modifier) {
        Box(contentAlignment = Alignment.Center) {
            when (sessionStatus) {
                SessionStatus.Active -> {
                    IconButton(onClick = endSession) {
                        Icon(Icons.Default.Close, Icons.Default.Close.name)
                    }
                }

                else -> {
                    Spacer(Modifier)
                }
            }
        }

        SessionStatusInfo(sessionStatus, queuePosition, statusText, unreadMessageCount, onClick)
    }
}

@Composable
private fun WidgetContainer(
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    widthScale: Float = 0.75f,
    content: @Composable RowScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxWidth(widthScale).heightIn(max = height), contentAlignment = Alignment.CenterEnd) {
        Surface(shape = FloatingActionButtonDefaults.shape, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
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
private fun SessionStatusInfo(
    sessionStatus: SessionStatus,
    queuePosition: Int,
    text: String?,
    unreadMessageCount: Int,
    openConversation: () -> Unit
) {
    TextButton(onClick = openConversation, shape = FloatingActionButtonDefaults.shape, modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
            val sessionText = when (sessionStatus) {
                SessionStatus.Active -> when {
                    queuePosition > 0 -> "Your position in queue is $queuePosition."
                    else -> text
                }

                SessionStatus.Inactive -> "Start a Chat"

                else -> sessionStatus.name
            }

            Text(
                modifier = Modifier.weight(0.5f, false),
                text = sessionText ?: "",
                style = MaterialTheme.typography.bodyLarge,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            MessagingIcon(Modifier, unreadMessageCount)
        }
    }
}

@Composable
@Preview
private fun InactiveSessionWidgetPreview() {
    MessagingWidget(Modifier.fillMaxWidth(), SessionStatus.Inactive, 0, 0, "A message", {}) {}
}

@Composable
@Preview
private fun ActiveSessionWidgetPreview() {
    MessagingWidget(Modifier.fillMaxWidth(), SessionStatus.Active, 0, 10, "A message", {}) {}
}

@Composable
@Preview
private fun QueuedSessionWidgetPreview() {
    MessagingWidget(Modifier.fillMaxWidth(), SessionStatus.Active, 10, 0, "A message", {}) {}
}
