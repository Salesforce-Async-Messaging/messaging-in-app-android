package com.salesforce.android.smi.messaging.features.ui.replacement

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.messaging.features.ui.replacement.components.AttachmentMessageReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.CarouselMessageReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.DateBreakHeaderReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.DisplayableOptionsReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.PreChatSubmissionReceiptReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.QuickRepliesReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.RichLinkMessageReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.TextMessageReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.TypingStartedReplacementEntry
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.OptionItem.TypedOptionItem.TitleOptionItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.titleItem.TitleItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.richLink.LinkItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesResponseFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.FormFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.FormResponseFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
import com.salesforce.android.smi.ui.ChatFeedEntry
import com.salesforce.android.smi.ui.ViewComponents
import com.salesforce.android.smi.ui.internal.common.domain.extensions.messageContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class OverridableUI {
    open fun renderMode(entry: ChatFeedEntry): EntryRenderMode = EntryRenderMode.Existing

    inner class CustomViewComponents(private val conversationClient: ConversationClient) : ViewComponents {
        @Composable
        override fun ChatFeedEntry(entry: ChatFeedEntry, content: @Composable () -> Unit) {
            when (renderMode(entry)) {
                EntryRenderMode.None -> Unit
                EntryRenderMode.Existing -> content()
                EntryRenderMode.Replace -> CustomEntryContainer(entry, conversationClient)
            }
        }
    }
}

@Composable
private fun CustomEntryContainer(entry: ChatFeedEntry, conversationClient: ConversationClient?) = when (entry) {
    is ChatFeedEntry.ConversationEntryModel -> when (entry.payload) {
        is EntryPayload.MessagePayload -> {
            when (val messageContent = entry.messageContent) {
                is StaticContentFormat.TextFormat -> TextMessageReplacementEntry(
                    isLocal = entry.isOutboundEntry,
                    text = messageContent.text
                )

                is StaticContentFormat.AttachmentsFormat -> messageContent.attachments.first().file?.let {
                    AttachmentMessageReplacementEntry(
                        isLocal = entry.isOutboundEntry,
                        file = it
                    )
                }

                is StaticContentFormat.RichLinkFormat -> RichLinkMessageReplacementEntry(
                    isLocal = entry.isOutboundEntry,
                    linkItem = messageContent.linkItem,
                    image = messageContent.image
                )

                is StaticContentFormat.WebViewFormat -> RichLinkMessageReplacementEntry(
                    isLocal = entry.isOutboundEntry,
                    linkItem =
                    LinkItem(
                            messageContent.url,
                            TitleItem.TitleLinkItem(messageContent.title.title, messageContent.title.subTitle)
                        ),
                    image = null
                )

                is ChoicesFormat.CarouselFormat -> CarouselMessageReplacementEntry(
                    isLocal = entry.isOutboundEntry,
                    list = messageContent.images
                )

                is ChoicesFormat.DisplayableOptionsFormat ->
                    DisplayableOptionsReplacementEntry(
                        messageContent.text,
                        messageContent.optionItems.filterIsInstance<TitleOptionItem>()
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            conversationClient?.sendReply(it)
                        }
                    }

                is ChoicesFormat.QuickRepliesFormat -> QuickRepliesReplacementEntry(
                    messageContent.text,
                    messageContent.optionItems.filterIsInstance<TitleOptionItem>()
                ) {
                    CoroutineScope(Dispatchers.Main).launch {
                        conversationClient?.sendReply(it)
                    }
                }

                is ChoicesResponseFormat.ChoicesResponseSelectionsFormat -> {
                    TextMessageReplacementEntry(isLocal = entry.isOutboundEntry, text = "todo")
                }

                is ChoicesResponseFormat.ExperienceTypeChoicesResponseFormat -> TextMessageReplacementEntry(isLocal = entry.isOutboundEntry, text = "todo")

                is ChoicesFormat.ExperienceTypeFormat -> TextMessageReplacementEntry(isLocal = entry.isOutboundEntry, text = "todo")


                is FormFormat.InputsFormat,
                is FormResponseFormat.InputsFormResponseFormat,
                is FormResponseFormat.ResultFormResponseFormat,
                is StaticContentFormat.ErrorMessageFormat,
                is StaticContentFormat.ExperienceTypeFormat,
                null -> {
                    Text(entry.contentType)
                }
            }
        }

        else -> {}
    }

    is ChatFeedEntry.DateBreakModel -> DateBreakHeaderReplacementEntry(timestamp = entry.timestamp)
    is ChatFeedEntry.PreChatReceiptModel -> PreChatSubmissionReceiptReplacementEntry {
        // navigation
    }

    is ChatFeedEntry.ProgressIndicatorModel -> {
        if (entry.isActive) {
            TypingStartedReplacementEntry(participant = entry.participants.first())
        } else {
            // inactive typing indicator
        }
    }

    is ChatFeedEntry.TypingIndicatorModel -> Unit
}
