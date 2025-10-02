package com.salesforce.android.smi.messaging.features.ui.replacement

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.messaging.R
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.features.ui.replacement.components.AttachmentMessageReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.CarouselMessageReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.DateBreakHeaderReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.DisplayableOptionsReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.PreChatSubmissionReceiptReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.QuickRepliesReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.RichLinkMessageReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.TextMessageReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.TypingStartedReplacementEntry
import com.salesforce.android.smi.network.data.domain.conversation.Conversation
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.OptionItem.TypedOptionItem.TitleOptionItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.titleItem.TitleItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.richLink.LinkItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesResponseFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.FormFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.FormResponseFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.MessageFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
import com.salesforce.android.smi.network.data.domain.participant.ParticipantRoleType
import com.salesforce.android.smi.ui.ChatFeedEntry
import com.salesforce.android.smi.ui.UIClient
import com.salesforce.android.smi.ui.ViewComponents
import com.salesforce.android.smi.ui.internal.common.domain.extensions.messageContent
import com.salesforce.android.smi.ui.navigation.LocalSMINavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

open class OverridableUI {
    inner class CustomViewComponents(private val salesforceMessaging: SalesforceMessaging) : ViewComponents {
        @Composable
        override fun ChatFeedEntry(
            entry: ChatFeedEntry,
            content: @Composable () -> Unit
        ) {
            when (entryRenderMode(entry)) {
                ViewComponentRenderMode.None -> Unit
                ViewComponentRenderMode.Existing -> content()
                ViewComponentRenderMode.Replace -> CustomEntryContainer(entry, salesforceMessaging.conversationClient)
            }
        }

        @Composable
        override fun ConversationClosed(content: @Composable () -> Unit) {
            when (renderMode(OverridableViewComponent.CloseConversation)) {
                ViewComponentRenderMode.None -> Unit
                ViewComponentRenderMode.Existing -> content()
                ViewComponentRenderMode.Replace -> CustomConversationClosedView(salesforceMessaging.uiClient)
            }
        }

        @Composable
        override fun ChatTopAppBar(content: @Composable () -> Unit) {
            when (renderMode(OverridableViewComponent.ChatTopAppBar)) {
                ViewComponentRenderMode.None -> Unit
                ViewComponentRenderMode.Existing -> content()
                ViewComponentRenderMode.Replace -> CustomChatTopAppBar(salesforceMessaging.conversationClient, content)
            }
        }
    }

    open fun renderMode(component: OverridableViewComponent): ViewComponentRenderMode =
        when (component) {
            OverridableViewComponent.CloseConversation,
            OverridableViewComponent.ChatTopAppBar -> ViewComponentRenderMode.Existing
        }

    open fun entryRenderMode(entry: ChatFeedEntry): ViewComponentRenderMode =
        when (entry) {
            is ChatFeedEntry.ConversationEntryModel -> when (entry.payload) {
                is EntryPayload.MessagePayload -> messageRenderMode(entry.messageContent)
                is EntryPayload.ParticipantChangedPayload,
                is EntryPayload.ProgressIndicatorPayload,
                is EntryPayload.RoutingResultPayload,
                is EntryPayload.RoutingWorkResultPayload,
                is EntryPayload.StreamingTokenPayload,
                is EntryPayload.TypingStartedIndicatorPayload,
                is EntryPayload.UnknownEntryPayload -> ViewComponentRenderMode.Existing
                else -> ViewComponentRenderMode.Existing
            }
            is ChatFeedEntry.DateBreakModel,
            is ChatFeedEntry.PreChatReceiptModel,
            is ChatFeedEntry.TypingIndicatorModel -> ViewComponentRenderMode.Existing
        }

    open fun messageRenderMode(message: MessageFormat?): ViewComponentRenderMode =
        when (message) {
            is ChoicesFormat.CarouselFormat,
            is ChoicesFormat.DisplayableOptionsFormat,
            is ChoicesFormat.QuickRepliesFormat,
            is ChoicesResponseFormat.ChoicesResponseSelectionsFormat,
            is FormFormat.InputsFormat,
            is FormResponseFormat.InputsFormResponseFormat,
            is FormResponseFormat.ResultFormResponseFormat,
            is StaticContentFormat.AttachmentsFormat,
            is StaticContentFormat.RichLinkFormat,
            is StaticContentFormat.TextFormat,
            is StaticContentFormat.WebViewFormat -> ViewComponentRenderMode.Existing
            else -> ViewComponentRenderMode.Existing
        }

    @Composable
    open fun CustomConversationClosedView(uiClient: UIClient) {
        val context = LocalContext.current
        val activity: Activity? = context as? ComponentActivity

        LifecycleStartEffect(Unit) {
            onStopOrDispose {
                UIClient.Factory.create(
                    uiClient.configuration.copy(conversationId = UUID.randomUUID())
                ).openConversationActivity(context)
            }
        }
        Box(modifier = Modifier.background(color = colorResource(R.color.smi_closed_conversation_background))) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                onClick = { activity?.finish() }
            ) {
                Text("Start a new conversation", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    open fun CustomChatTopAppBar(
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
                IconButton(
                    onClick = { navigation.navigateToOptions() },
                    content = { Icons.Filled.Menu.run { Icon(this, this.name) } }
                )
            }
        )
    }

    @Composable
    open fun CustomEntryContainer(
        entry: ChatFeedEntry,
        conversationClient: ConversationClient?
    ) = when (entry) {
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

                    is ChoicesFormat.DisplayableOptionsFormat -> DisplayableOptionsReplacementEntry(
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

                    is ChoicesResponseFormat.ChoicesResponseSelectionsFormat -> TextMessageReplacementEntry(
                        isLocal = entry.isOutboundEntry,
                        text = "todo"
                    )

                    is FormFormat.InputsFormat,
                    is FormResponseFormat.InputsFormResponseFormat,
                    is FormResponseFormat.ResultFormResponseFormat,
                    is ChoicesFormat.ExperienceTypeFormat,
                    is ChoicesResponseFormat.ExperienceTypeFormat,
                    is FormFormat.ExperienceTypeFormat,
                    is FormResponseFormat.ExperienceTypeResponseFormat,
                    is StaticContentFormat.CancelActionFormat,
                    is StaticContentFormat.ErrorMessageFormat,
                    is StaticContentFormat.ExperienceTypeFormat,
                    null -> Text(entry.contentType)
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
}
