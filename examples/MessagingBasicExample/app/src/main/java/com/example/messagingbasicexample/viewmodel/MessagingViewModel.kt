package com.example.messagingbasicexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import com.example.messagingbasicexample.MessagingBasicApplication
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.events.CoreEvent.ConversationEvent
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class MessagingViewModel(
    private val coreClient: CoreClient,
    conversationId: UUID = UUID.randomUUID()
) : ViewModel() {
    private val conversationClient = coreClient.conversationClient(conversationId)

    val conversationEntries =
        conversationClient.conversationEntriesPaged()
            .filterIsInstance<Result.Success<PagingData<ConversationEntry>>>().map {
                it.data
            }
    val typingEvents =
        conversationClient.events.filterIsInstance<ConversationEvent.TypingIndicator>()

    fun startStream() = coreClient.start(viewModelScope)

    fun sendTextMessage(message: String) = viewModelScope.launch {
        conversationClient.sendMessage(message)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val coreClient = (this[APPLICATION_KEY] as MessagingBasicApplication).coreClient()
                MessagingViewModel(coreClient)
            }
        }

    }
}