package com.salesforce.android.smi.messaging.samples.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.common.api.data
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.samples.state.MessagingSessionState
import com.salesforce.android.smi.messaging.samples.state.rememberMessagingSessionState
import com.salesforce.android.smi.network.data.domain.conversation.Conversation
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

@Composable
fun MessagingConversationList(
    salesforceMessaging: SalesforceMessaging,
    onSelection: (Conversation) -> Unit
) {
    MessagingConversationList(salesforceMessaging.coreClient, onSelection)
}

@Composable
fun MessagingConversationList(
    coreClient: CoreClient,
    onSelection: (Conversation) -> Unit
) {
    val conversationClients: List<ConversationClient> by remember(coreClient) {
        coreClient.conversationsFlow(limit = 10).mapNotNull { it.data }.map { list ->
            list.map { coreClient.conversationClient(it.identifier) }
        }
    }.collectAsStateWithLifecycle(emptyList())

    val states: List<MessagingSessionState> = conversationClients.map { rememberMessagingSessionState(it) }

    MessagingConversationList(states, onSelection = onSelection)
}

@Composable
fun MessagingConversationList(
    conversations: List<MessagingSessionState>,
    onSelection: (Conversation) -> Unit
) {
    LoadingContainer(conversations) {
        ConversationsList(conversations, onSelection)
    }
}

@Composable
private fun LoadingContainer(
    conversations: List<MessagingSessionState>,
    showLoadingState: Boolean = false,
    content: @Composable () -> Unit
) {
    val isLoading = conversations.any { it.conversation == null }

    AnimatedContent(showLoadingState && isLoading) {
        when (it) {
            true -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            false -> content()
        }
    }
}

@Composable
private fun ConversationsList(
    conversations: List<MessagingSessionState>,
    onSelection: (Conversation) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.Start) {
        items(
            conversations.filter {
                it.conversation != null
            }
        ) { state ->
            ListItem(
                headlineContent = {
                    val statusText = state.statusText ?: "Loading"
                    val agentName = state.agentName
                    val unreadMessageCount = state.unreadMessageCount

                    ConversationItem(statusText, agentName, unreadMessageCount)
                },
                modifier = Modifier
                    .clickable { state.conversation?.let { onSelection(it) } }
                    .animateItem(
                        placementSpec = null,
                        fadeInSpec = tween(500)
                    )
                    .fillParentMaxWidth()
            )
        }
    }
}

@Composable
private fun ConversationItem(
    statusText: String,
    agentName: String,
    unreadMessageCount: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        MessagingIcon(unreadMessageCount = unreadMessageCount, icon = Icons.Default.Person)
        Column {
            Text(agentName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Clip)
            Text(statusText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
@Preview
private fun MessagingConversationList() {
    ConversationItem("This is a message", "Agent", 5)
}
