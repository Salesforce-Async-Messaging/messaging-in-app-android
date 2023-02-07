package com.example.messagingbasicexample.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.messagingbasicexample.databinding.ConversationEntryItemBinding
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat

class ConversationEntryViewHolder(private val binding: ConversationEntryItemBinding) :
    ViewHolder(binding.root) {
    fun bind(item: ConversationEntry?) {
        binding.name = item?.senderDisplayName
        binding.text = getText(item)
        binding.executePendingBindings()
    }

    private fun getText(item: ConversationEntry?): String = when (val payload = item?.payload) {
        is EntryPayload.MessagePayload -> when (val content = payload.abstractMessage.content) {
            is StaticContentFormat.TextFormat -> content.text
            // Handle other message formats (QuickRepliesFormat, RichLinkFormat, etc.)
            else -> content.formatType.toString()
        }
        // Handle other entry types (ParticipantChangedPayload, AcknowledgeReadPayload, etc.)
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