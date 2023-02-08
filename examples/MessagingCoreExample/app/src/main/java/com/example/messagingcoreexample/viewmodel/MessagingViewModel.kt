package com.example.messagingcoreexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import com.example.messagingcoreexample.MessagingCoreApplication
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.events.CoreEvent.ConversationEvent
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

/**
 * View model for this Messaging for In-App Core SDK example.
 */
class MessagingViewModel(
    private val coreClient: CoreClient,
    conversationId: UUID = UUID.randomUUID()
) : ViewModel() {
    // Conversation client instance.
    // By default, this object uses a random UUID for the conversation ID, but
    // be sure to use the same ID to persist the same conversation.
    private val conversationClient = coreClient.conversationClient(conversationId)

    // Paged instance of the messaging conversation entries
    val conversationEntries =
        conversationClient.conversationEntriesPaged()
            .filterIsInstance<Result.Success<PagingData<ConversationEntry>>>().map {
                it.data
            }

    // Filtered instance of all typing indicator events
    val typingEvents =
        conversationClient.events.filterIsInstance<ConversationEvent.TypingIndicator>()

    // Starts the Messaging core client stream
    fun startStream() = coreClient.start(viewModelScope)

    // Sends a text message to the agent
    fun sendTextMessage(message: String) = viewModelScope.launch {
        conversationClient.sendMessage(message)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val coreClient = (this[APPLICATION_KEY] as MessagingCoreApplication).coreClient()
                MessagingViewModel(coreClient)
            }
        }

    }
}