package com.salesforce.android.smi.messaging.features.voice.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.network.data.domain.conversation.Conversation
import com.salesforce.android.smi.network.data.domain.participant.ParticipantRoleType
import com.salesforce.android.smi.ui.navigation.LocalSMINavigation
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTopAppBar(
    coreClient: CoreClient,
    conversationClient: ConversationClient,
    defaultTopAppBar: @Composable () -> Unit
) {
    val navigation = LocalSMINavigation.current

    // Show default UI for all routes other than the ChatFeed
    if (navigation.currentRoute?.route?.contains("SMIDestination.ChatFeed") == false) {
        return defaultTopAppBar()
    }

    val conversation by remember {
        conversationClient.conversation
            .filterIsInstance<Result.Success<Conversation>>()
            .map { it.data }
    }.collectAsStateWithLifecycle(null)

    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                BadgedBox(
                    badge = {
                        conversation?.unreadMessageCount
                            ?.takeIf { it > 0 }?.toString()
                            ?.let { count ->
                                Badge { Text(count) }
                            }
                    }
                ) {
                    conversation?.activeParticipants
                        ?.filter { it.roleType == ParticipantRoleType.Agent || it.roleType == ParticipantRoleType.Chatbot }
                        ?.map { it.displayName }
                        ?.let {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = it.joinToString(", "),
                                textAlign = TextAlign.Center
                            )
                        }
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = { navigation.navigateBack() },
                content = { Icons.AutoMirrored.Default.ExitToApp.run { Icon(this, this.name) } }
            )
        },
        actions = {
            VoiceButton(coreClient, conversationClient)
            IconButton(
                onClick = { navigation.navigateToOptions() },
                content = { Icons.Filled.Menu.run { Icon(this, this.name) } }
            )
        }
    )
}