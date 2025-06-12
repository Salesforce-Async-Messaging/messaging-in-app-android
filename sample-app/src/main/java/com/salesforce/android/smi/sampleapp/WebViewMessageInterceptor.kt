package com.salesforce.android.smi.sampleapp

import android.util.Log
import com.salesforce.android.smi.messaging.features.ui.replacement.EntryRenderMode
import com.salesforce.android.smi.messaging.features.ui.replacement.OverridableUI
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
import com.salesforce.android.smi.ui.ChatFeedEntry
import com.salesforce.android.smi.ui.internal.common.domain.extensions.messageContent
import java.lang.ref.WeakReference
import java.util.Date

/**
 * Custom UI components that intercept WebViewFormat messages
 */
class WebViewMessageInterceptor : OverridableUI() {
    // Keep track of the last entry ID we've processed
    private var lastProcessedEntryId: String? = null
    // Keep track of all entries to determine if this is the last active entry
    private val processedEntries = mutableListOf<ChatFeedEntry>()
    // Weak reference to the handler to avoid memory leaks
    private var handlerRef: WeakReference<WebViewFormatHandler>? = null

    private val TAG = "WebViewInterceptor"

    /**
     * Set the handler for WebView format messages
     */
    fun setHandler(handler: WebViewFormatHandler) {
        handlerRef = WeakReference(handler)
    }

    /**
     * Clear the handler reference
     */
    fun clearHandler() {
        handlerRef = null
    }

    override fun renderMode(entry: ChatFeedEntry): EntryRenderMode {
        // Add entry to our tracking list
        if (entry !is ChatFeedEntry.ProgressIndicatorModel &&
            entry !is ChatFeedEntry.TypingIndicatorModel) {
            processedEntries.add(entry)
        }

        // Check if this is a WebViewFormat message
        if (entry is ChatFeedEntry.ConversationEntryModel &&
            entry.payload is EntryPayload.MessagePayload) {

            val messageContent = entry.messageContent
            if (messageContent is StaticContentFormat.WebViewFormat) {
                // Check if we've already processed this entry
                if (entry.entryId == lastProcessedEntryId) {
                    Log.d(TAG, "Already processed this WebViewFormat message: ${entry.entryId}")
                    return EntryRenderMode.None
                }

                // Check if this is the last active entry or within 5 minutes
                val (shouldShow, isLastEntry, isRecent) = shouldShowSurvey(entry)
                if (shouldShow) {
                    Log.d(TAG, "Showing WebViewFormat survey: ${entry.entryId}")
                    // Remember that we've processed this entry
                    lastProcessedEntryId = entry.entryId

                    // Get the handler and call it if available
                    val handler = handlerRef?.get()
                    if (handler != null) {
                        handler.handleWebViewFormatMessage(messageContent)
                    } else {
                        Log.w(TAG, "WebViewFormatHandler is no longer available")
                    }
                } else {
                    Log.d(TAG, "Skipping WebViewFormat survey: ${entry.entryId} - not recent enough or not last entry")
                }

                // Return EntryRenderMode.None to prevent default rendering
                return EntryRenderMode.None
            }
        }

        // Use default rendering for other entry types
        return EntryRenderMode.Existing
    }

    /**
     * Determine if we should show the survey based on:
     * 1. If it's the last active entry in the conversation
     * 2. OR if it was received within the last 5 minutes
     *
     * @return Triple of (shouldShow, isLastEntry, isRecent)
     */
    private fun shouldShowSurvey(entry: ChatFeedEntry.ConversationEntryModel): Triple<Boolean, Boolean, Boolean> {
        // Check if this is the last entry in our tracked list (excluding typing indicators)
        val isLastEntry = processedEntries.lastOrNull {
            it !is ChatFeedEntry.ProgressIndicatorModel &&
                    it !is ChatFeedEntry.TypingIndicatorModel
        }?.let { it == entry } ?: false

        // Check if the entry was received within the last 5 minutes
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        val isRecent = entry.timestamp >= fiveMinutesAgo

        // Format timestamps for logging
        val entryTime = Date(entry.timestamp).toString()
        val cutoffTime = Date(fiveMinutesAgo).toString()

        Log.d(TAG, "Entry ${entry.entryId}: isLastEntry=$isLastEntry, isRecent=$isRecent")
        Log.d(TAG, "Entry time: $entryTime, Cutoff time: $cutoffTime")

        // Show survey if either condition is met
        val shouldShow = isLastEntry || isRecent
        return Triple(shouldShow, isLastEntry, isRecent)
    }
}