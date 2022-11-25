package com.example.messagingbasicexample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.events.CoreEvent
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.ConversationEntryType
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
import com.salesforce.android.smi.ui.UIConfiguration
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.logging.Level
import java.util.logging.Logger

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val logger = Logger.getLogger(TAG)

    fun logEvents(config: UIConfiguration) {
        val coreClient = CoreClient.Factory.create(getApplication(), config)
        val conversationClient = coreClient.conversationClient(config.conversationId)

        viewModelScope.launch {
            conversationClient.events
                .filterIsInstance<CoreEvent.ConversationEvent.Entry>()
                .map { it.conversationEntry.payload }
                .filterIsInstance<EntryPayload.MessagePayload>()
                .onEach {
                    when (val content = it.content) {
                        is StaticContentFormat.TextFormat ->
                            logEntry(it.entryType, content.text)
                        is ChoicesFormat.DisplayableOptionsFormat ->
                            logEntry(it.entryType, content.optionItems.toString())
                        else ->
                            logEntry(it.entryType)
                    }
                }.collect()
        }
    }

    private fun logEntry(type: ConversationEntryType, msg: String? = null) {
        logger.log(Level.INFO, "Entry | $type | $msg")
    }

    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            modelClass.getConstructor(Application::class.java).newInstance(application)
    }

    companion object {
        val TAG: String = AppViewModel::class.java.name
    }
}
