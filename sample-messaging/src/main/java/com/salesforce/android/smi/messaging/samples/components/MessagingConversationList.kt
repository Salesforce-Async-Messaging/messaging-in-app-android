package com.salesforce.android.smi.messaging.samples.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.messaging.samples.state.MessagingSessionState
import com.salesforce.android.smi.messaging.samples.state.MessagingStore
import com.salesforce.android.smi.messaging.samples.state.rememberTotalUnreadCount
import com.salesforce.android.smi.network.data.domain.conversation.Conversation
import java.util.UUID
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingConversationList(
    store: MessagingStore,
    selectedConversationId: UUID,
    onSelection: (Conversation) -> Unit
) {
    // All list data comes from the single MessagingStore. Rows reuse the same per-conversation
    // session-state flows that standalone components observe, so nothing is fetched twice, and the
    // background refresh of active conversations is owned by the store (not re-triggered per emit).
    val listState by store.listState.collectAsStateWithLifecycle()
    val totalUnread = rememberTotalUnreadCount(store)

    var showUnreadOnly by rememberSaveable { mutableStateOf(false) }
    val visibleStates = remember(listState.states, showUnreadOnly) {
        if (showUnreadOnly) listState.states.filter { it.unreadMessageCount > 0 } else listState.states
    }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        InboxHeader(
            totalUnreadCount = totalUnread,
            unreadOnly = showUnreadOnly,
            onToggleUnreadOnly = { showUnreadOnly = !showUnreadOnly }
        )
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                // Delegates to the store, which enforces the throttle internally: a pull within the
                // throttle window performs no network call and simply returns the cached list, so the
                // gesture can never bypass the throttle. listState updates reactively on a real fetch.
                scope.launch {
                    isRefreshing = true
                    try {
                        store.refreshConversations()
                    } finally {
                        isRefreshing = false
                    }
                }
            }
        ) {
            MessagingConversationList(
                visibleStates,
                isLoading = listState.isLoading,
                selectedConversationId = selectedConversationId,
                onSelection = onSelection
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxHeader(
    totalUnreadCount: Int,
    unreadOnly: Boolean,
    onToggleUnreadOnly: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Inbox", style = MaterialTheme.typography.titleMedium)
        FilterChip(
            selected = unreadOnly,
            onClick = onToggleUnreadOnly,
            label = { Text("($totalUnreadCount) Unread") }
        )
    }
}

@Composable
fun MessagingConversationList(
    conversations: List<MessagingSessionState>,
    isLoading: Boolean = false,
    selectedConversationId: UUID? = null,
    onSelection: (Conversation) -> Unit
) {
    LoadingContainer(isLoading = isLoading) {
        ConversationsList(conversations, selectedConversationId, onSelection)
    }
}

@Composable
private fun LoadingContainer(
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedContent(isLoading, label = "ConversationListLoading") {
        when (it) {
            true -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            false -> content()
        }
    }
}

@Composable
private fun ConversationsList(
    conversations: List<MessagingSessionState>,
    selectedConversationId: UUID?,
    onSelection: (Conversation) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.Start) {
        if (conversations.isEmpty()) {
            // Keep an item present when empty so the inbox explains itself and pull-to-refresh still
            // has something to pull on.
            item { EmptyConversationsItem() }
        }
        items(conversations) { state ->
            val isSelected = state.conversation?.identifier == selectedConversationId
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                headlineContent = {
                    // The row renders as soon as the conversation exists. `statusText` may still be
                    // null while the latest entry loads, so show a placeholder until it fills in.
                    val statusText = state.statusText ?: "Loading…"
                    val agentName = state.agentName
                    val unreadMessageCount = state.unreadMessageCount

                    ConversationItem(statusText, agentName, unreadMessageCount, state.isActive)
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
private fun LazyItemScope.EmptyConversationsItem() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillParentMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Text(
            "No conversations yet",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "Conversations you start will show up here. Pull down to refresh.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConversationItem(
    statusText: String,
    agentName: String,
    unreadMessageCount: Int,
    isActive: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isActive) 1f else 0.5f)
    ) {
        MessagingIcon(unreadMessageCount = unreadMessageCount, icon = Icons.Default.Person)
        Column {
            Text(agentName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Clip)
            Text(statusText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!isActive) {
                Text(
                    "Session ended",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
@Preview
private fun MessagingConversationList() {
    ConversationItem("This is a message", "Agent", 5)
}
