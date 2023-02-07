package com.example.messagingbasicexample.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.messagingbasicexample.viewHolder.ConversationEntryViewHolder
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry

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
            // Id is unique.
            return oldItem.identifier == newItem.identifier
        }

        // Can expand on this to prevent unnecessary UI refreshes
        override fun areContentsTheSame(
            oldItem: ConversationEntry,
            newItem: ConversationEntry
        ): Boolean {
            return oldItem == newItem
        }
    }
}