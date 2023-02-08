package com.example.messagingcoreexample.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.messagingcoreexample.viewHolder.ConversationEntryViewHolder
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry

/**
 * Paging adapter for a Messaging Conversation Entry.
 */
class ConversationEntryAdapter(
    diffCallback: DiffUtil.ItemCallback<ConversationEntry> = ConversationEntryComparator
) : PagingDataAdapter<ConversationEntry, ConversationEntryViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationEntryViewHolder {
        return ConversationEntryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ConversationEntryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    object ConversationEntryComparator : DiffUtil.ItemCallback<ConversationEntry>() {
        override fun areItemsTheSame(
            oldItem: ConversationEntry,
            newItem: ConversationEntry
        ): Boolean {
            // ID is unique
            return oldItem.identifier == newItem.identifier
        }

        override fun areContentsTheSame(
            oldItem: ConversationEntry,
            newItem: ConversationEntry
        ): Boolean {
            // Can expand on this to prevent unnecessary UI refreshes
            return oldItem == newItem
        }
    }
}