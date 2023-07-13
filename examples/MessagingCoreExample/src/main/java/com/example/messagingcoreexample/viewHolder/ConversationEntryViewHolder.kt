package com.example.messagingcoreexample.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.messagingcoreexample.databinding.ConversationEntryItemBinding
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat

/**
 * View holder for a Messaging Conversation Entry.
 */
class ConversationEntryViewHolder(private val binding: ConversationEntryItemBinding) :
    ViewHolder(binding.root) {

    fun bind(item: ConversationEntry?) {
        binding.name = item?.senderDisplayName
        binding.text = getText(item)
        binding.executePendingBindings()
    }

    /**
     * Extracts the text from a conversation entry.
     * This sample code focuses on text-based messages, and simply
     * converts other conversation entries into string values.
     */
    private fun getText(item: ConversationEntry?): String =
        when (val payload = item?.payload) {
        is EntryPayload.MessagePayload ->
            when (val content = payload.abstractMessage.content) {

                // Get the contents for a text-based message
                is StaticContentFormat.TextFormat -> content.text

                // Handle other message formats (QuickRepliesFormat, RichLinkFormat, etc.)
                // using toString()
                else -> content.formatType.toString()
            }

            // Handle other entry types (ParticipantChangedPayload, AcknowledgeReadPayload, etc.)
            // using toString()
            else -> payload?.entryType.toString()
    }

    companion object {
        fun from(parent: ViewGroup): ConversationEntryViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationEntryItemBinding.inflate(layoutInflater, parent, false)
            return ConversationEntryViewHolder(binding)
        }
    }
}