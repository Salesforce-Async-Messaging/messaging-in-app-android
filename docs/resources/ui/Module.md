# Module ui

The UI module provides a pre-built Jetpack Compose conversation interface for Messaging for In-App.
It handles the full messaging experience including pre-chat forms, the chat feed, file attachments,
and transcript downloads. Add it to your app with minimal code, then customize branding, components,
and behavior as needed.

## Getting Started

Create a `UIConfiguration` with your deployment values, then obtain a `UIClient` from the factory:

```kotlin
val config = UIConfiguration(
    serviceAPI = "https://myorg.my.salesforce-scrt.com",
    organizationId = "00D000000000001AAA",
    developerName = "My_Deployment",
    conversationId = UUID.randomUUID()
)
val uiClient = UIClient.Factory.create(config)
```

Alternatively, load configuration from the file downloaded from Salesforce setup:

```kotlin
val config = UIConfiguration(
    configuration = CoreConfiguration.fromFile(context),
    conversationId = UUID.randomUUID()
)
```

The factory maintains a singleton -- calling `create` with the same configuration returns the
existing instance. When using the same `CoreConfiguration` in both the `UIClientFactory` and
`CoreClientFactory`, the `UIClient` shares the same underlying `CoreClient` instance. This
allows the host app to perform operations on the active client even when the UI is not
displayed. Call `UIClient.Factory.destroy()` when messaging is no longer needed.

## Launching the Conversation UI

### Activity-based (simplest)

Let the SDK manage a full-screen activity:

```kotlin
uiClient.openConversationActivity(context)
```

### Composable-based (more control)

Embed the messaging UI directly in your Compose navigation graph:

```kotlin
uiClient.MessagingInAppUI(onExit = {
    // Navigate back or finish the activity
})
```

### Bottom sheet (modal)

Present the messaging UI in a `ModalBottomSheet`:

```kotlin
ModalBottomSheet(
    onDismissRequest = { updateOpenBottomSheet(false) },
    sheetState = sheetState
) {
    Box(Modifier.fillMaxHeight(0.9f)) {
        uiClient.MessagingInAppUI { dismissSheet() }
    }
}
```

When the bottom sheet is closed, `MessagingInAppUI` is removed from the composition and stops
managing the event stream. To continue receiving events (e.g., push-triggered unread counts),
call `coreClient.start(lifecycleScope)` while the sheet is closed and `coreClient.stop()` when
re-opening. Do **not** call `coreClient.start()` while `MessagingInAppUI` is active — the
composable manages the lifecycle automatically.

## Customizing the UI

### ViewComponents

Override composable slots by assigning a custom `ViewComponents` implementation.
Each slot receives a `content` lambda you can call to render the default, wrap, or ignore entirely.
Available slots: `ChatFeedEntry`, `ChatTopAppBar`, `ConversationClosed`, and `MarkdownContent`.

**Replace the top app bar with a custom header and unread message counter:**

```kotlin
uiClient.viewComponents = object : ViewComponents {
    @Composable
    override fun ChatTopAppBar(content: @Composable () -> Unit) {
        val conversation by uiClient.conversationClient(context)
            .conversation.map { it.data }.collectAsStateWithLifecycle(null)

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Support Chat", style = MaterialTheme.typography.titleLarge)
            conversation?.unreadMessageCount?.takeIf { it > 0 }?.let { count ->
                Badge { Text("$count") }
            }
        }
    }
}
```

**Start a new conversation when the current one is closed:**

```kotlin
uiClient.viewComponents = object : ViewComponents {
    @Composable
    override fun ConversationClosed(content: @Composable () -> Unit) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("This conversation has ended.")
            Button(onClick = {
                UIClient.Factory.destroy()
                val newConfig = UIConfiguration(
                    configuration = coreConfig,
                    conversationId = UUID.randomUUID()
                )
                UIClient.Factory.create(newConfig).openConversationActivity(context)
            }) {
                Text("Start New Conversation")
            }
        }
    }
}
```

For more examples including voice UI, bottom sheet widgets, and conversation list components,
see the [sample app](https://github.com/Salesforce-Async-Messaging/messaging-in-app-android).

### Entry-type rendering

Control rendering per entry type using `when` expressions inside `ChatFeedEntry`:

```kotlin
uiClient.viewComponents = object : ViewComponents {
    @Composable
    override fun ChatFeedEntry(entry: ChatFeedEntry, content: @Composable () -> Unit) {
        when (entry) {
            is ChatFeedEntry.ConversationEntryModel -> {
                when (entry.entryType) {
                    ConversationEntryType.Message -> MyCustomMessage(entry)
                    ConversationEntryType.ParticipantChanged -> { /* hide */ }
                    else -> content()
                }
            }
            else -> content()
        }
    }
}
```

**Important:** The render decision applies to **all** entries of a given type. You cannot
selectively replace individual messages while keeping others as defaults within the same type.

For interactive messages (quick replies, list pickers), use `ConversationClient.sendReply()`
to submit the user's selection — not `sendMessage()`.

### Navigation within custom components

Access `LocalSMINavigation` inside a custom `ViewComponents` composable to conditionally
render based on the current screen or navigate programmatically:

```kotlin
@Composable
override fun ChatTopAppBar(content: @Composable () -> Unit) {
    val navigation = LocalSMINavigation.current
    if (navigation.currentRoute?.route?.contains("ChatFeed") == true) {
        // Show custom header only on the chat feed
        MyCustomHeader()
    } else {
        content()
    }
}
```

Available navigation actions: `navigateToChatFeed`, `navigateToPreChat`, `navigateToForm`,
`navigateToOptions`, `navigateToTranscriptViewer`, `navigateToAttachmentViewer`, and
`navigateBack`.

### Branding and theming

The SDK supports three levels of UI customization:

1. **Color token overrides** — Override named color resources in your app's `colors.xml`
   to change colors throughout the SDK UI without replacing components.
2. **String overrides** — Override localized string resources to change text labels.
3. **Full component replacement** — Use `ViewComponents` to replace entire composables.

There is no middle ground between color/string overrides and full replacement. You cannot
change individual properties (font size, padding, etc.) of the default components.

To override color tokens, define colors in your app's `res/values/colors.xml` (light mode)
and `res/values-night/colors.xml` (dark mode) with the SDK token names:

```xml
<!-- res/values/colors.xml -->
<resources>
    <color name="smi_sent_bubble_background">#FF0070D2</color>
    <color name="smi_received_bubble_background">#FFF3F2F2</color>
    <color name="smi_navigation_background">#FFFFFFFF</color>
    <color name="smi_chat_background">#FFF3F2F2</color>
    <color name="smi_cta_buttons">#FF0070D2</color>
</resources>
```

Run `./gradlew dokkaGenerate` to produce the full branding token reference at
`build/dokka/html/resources/ui/`.

### URL Display Mode

Control how tappable URLs are opened via `UIConfiguration.urlDisplayMode`:
- `UrlDisplayMode.InlineBrowser` (default) -- Chrome Custom Tabs within your app
- `UrlDisplayMode.ExternalBrowser` -- device default browser

## Pre-Chat Fields

Pre-populate visible pre-chat field values so end users see defaults they can edit:

```kotlin
uiClient.preChatFieldValueProvider = { fields ->
    fields.onEach { field ->
        when (field.name) {
            "FirstName" -> field.userInput = "Jane"
            "Email" -> field.userInput = "jane@example.com"
        }
    }
}
```

For hidden pre-chat values used in routing (not visible to the user), use
`CoreClient.registerHiddenPreChatValuesProvider` instead. Hidden pre-chat values are used
for routing decisions in Salesforce Flows and are not displayed to the agent directly.

## Accessing Core APIs

Retrieve the underlying `CoreClient` or `ConversationClient` for advanced operations
such as registering a user verification provider or sending messages programmatically:

```kotlin
val coreClient = uiClient.coreClient(context)
val conversationClient = uiClient.conversationClient(context)
```

### CoreClient lifecycle

When `MessagingInAppUI` is active, the SDK manages `CoreClient.start()` and `CoreClient.stop()`
automatically. When the UI is not active (e.g., bottom sheet closed), the host app must manage
the lifecycle manually to maintain the real-time event stream:

```kotlin
LifecycleResumeEffect(Unit) {
    coreClient.start(lifecycleScope)
    onPauseOrDispose { coreClient.stop() }
}
```

Do **not** call `coreClient.start()` while `MessagingInAppUI` is active — this causes
instability.

## Conversation Lifecycle

### Conversation IDs

Use `UUID.randomUUID()` to start a new conversation. To resume an existing conversation,
pass the same UUID that was used when the conversation was created.

After an app reinstall for **unverified users**, always generate a new conversation ID. The
previous conversation is no longer accessible because the local encrypted database was cleared.
Reusing the old ID will result in errors. For **verified users** (user verification enabled),
conversations can be safely resumed across reinstalls because the server can re-associate the
user.

### Switching deployments

When switching between different Salesforce deployments within the same app, clear the local
storage first to avoid data corruption:

```kotlin
UIClient.Factory.destroy()
CoreClient.clearStorage(context, clearAuthorization = true)
```

Call `UIClient.Factory.destroy()` before `clearStorage` to release the active client, then
create a new `UIClient` with the new configuration.

<h3>Colors</h3>

To customize the color scheme in your app, create a color resource file in your app using the SDK's token names. To learn more, see [Customize Colors for Android](https://developer.salesforce.com/docs/service/messaging-in-app/guide/android-colors.html).


<details>
<summary>Show Colors</summary>

|**Keyword**|**Default Light Color**|**Default Dark Color**|**Description**|
|-|-|-|-|
|smi_color_primary|#FFFAFAF9|#FF2B2826|Primary branding color|
|smi_color_primary_variant|#FF706E6B|#FF2B2826|Variant of the primary color|
|smi_color_on_primary|#FF2B2826|#FFECEBEA|Primary text color|
|smi_color_secondary|#FF0070D2|#FF007ADD|Secondary branding color|
|smi_color_on_secondary|#FFFFFFFF|#FFFFFFFF|Secondary text color|
|smi_color_background|#FFF3F2F2|#FF080707|Background color|
|smi_color_on_background|#FF706E6B|#FFB0ADAB|Color for elements on the background color|
|smi_color_surface|#FFFFFFFF|#FF1A1B1E|Primary color for surfaces|
|smi_color_on_surface|#FF2B2826|#FFC9C7C5|Text color on surfaces|
|smi_color_surface_highlight|#FFB3B3B3|#FF66676B|Highlight color for surfaces|
|smi_color_surface_variant|#FFDDDBDA|#FF30373d|Variant color for surfaces|
|smi_color_on_surface_variant|#FF0070D2|#FFFFFFFF|Variant color for surfaces|
|smi_color_surface_secondary|#FFCED0D2|#FF30373d|Secondary color for surfaces|
|smi_color_surface_secondary_variant|#FFEDF4FF|#FF3f3f3f|Secondary color variant for surfaces|
|smi_color_highlight|#FFFCC003|#FFFCC003|Highlight color|
|smi_color_error|#FFD72E2D|#FFD4504C|Error feedback color|
|smi_color_success|#FF04844B|#FF4BCA81|Success feedback color|
|smi_color_surface_transparent|#80FFFFFF|#801A1B1E|Transparent surface color|
|smi_color_on_background_semi_transparent|#80706E6B|#80B0ADAB|Semi-transparent color for elements on the background color|

<details>
<summary>Show Branding</summary>


### Header/General Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_navigation_background|@color/smi_color_primary|Navigation background color|
|smi_navigation_text|@color/smi_color_on_primary|Navigation text color|
|smi_navigation_icon|@color/smi_color_on_primary|Navigation icon color|
|smi_chat_background|@color/smi_color_background|Navigation icon color|
|smi_cta_buttons|@color/smi_color_secondary|Call-to-action button color|
|smi_cta_buttons_text|@color/smi_color_secondary|Call-to-action text color|
|smi_banner_text|@color/smi_color_background|Banner text color|
|smi_banner_background|@color/smi_color_on_background|Banner background color|
|smi_url_text|@color/smi_color_secondary|Hyperlink text color|
|smi_keyboard_focus|@color/smi_color_secondary|Keyboard focus indicator color|
|smi_footer_button_background|@color/smi_color_primary|Background for buttons in the footer|

### Bubble/Chat Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_sent_bubble_text|@color/smi_color_on_secondary|Text color for an incoming message bubble|
|smi_sent_bubble_background|@color/smi_color_secondary|Background color for an outgoing chat bubble|
|smi_received_bubble_text|@color/smi_color_on_surface|Text color for an incoming message bubble|
|smi_received_bubble_background|@color/smi_color_surface|Background color for an incoming chat bubble|
|smi_received_bubble_border|@color/smi_color_surface_variant|Border color for incoming chat bubble|
|smi_quick_reply_bubble_text|@color/smi_color_on_surface|Text color for an incoming message bubble|
|smi_quick_reply_bubble_background|@color/smi_color_surface|Background color for an incoming chat bubble|
|smi_typing_indicator_text|@color/smi_color_on_background|Typing indicator color|
|smi_timestamp|@color/smi_color_on_background|Timestamp text color|
|smi_avatar_background|@color/smi_color_secondary|Avatar background color|
|smi_avatar_foreground|@color/smi_color_on_secondary|Avatar foreground color|
|smi_chat_separator|@color/smi_color_on_background_semi_transparent|Chat separator color|
|smi_unknown_entry_error_icon_background|@color/smi_color_error|Error entry icon background color|
|smi_icon_error|@color/smi_color_on_secondary|Error icon color that's used in the unknown entry|

### Button Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_button_text|@color/smi_color_on_surface_variant|Button text color|
|smi_button_background|@color/smi_color_surface|Button background color|
|smi_button_border|@color/smi_color_surface_variant|Button border color|
|smi_button_focus_background|@color/smi_color_on_surface_variant|Focus text background color|
|smi_button_disabled_text|@color/smi_color_on_background|Disabled text color|
|smi_button_disabled_selected_text|@color/smi_color_background|Disabled selected text color|
|smi_button_disabled_selected_background|@color/smi_color_on_background|Disabled selected text background color|
|smi_button_disabled_background|@color/smi_color_surface|Disabled text background color|
|smi_list_picker_title_text|@color/smi_color_on_surface|List picker title text color|
|smi_list_picker_title_background|@color/smi_color_surface|List picker title background color|
|smi_list_picker_border|@color/smi_color_surface_variant|List picker border color|
|smi_quick_reply_border|@color/smi_color_surface_variant|Quick reply border color|
|smi_cancel_button_active|@color/smi_color_on_secondary|Cancel button active color|
|smi_cancel_button_bg_active|@color/smi_color_on_background|Cancel button active background color|
|smi_cancel_button_pressed|@color/smi_color_on_secondary|Cancel button pressed color|

### Card Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_card_background|@color/smi_color_primary|Background color for a card|
|smi_card_headline_text|@color/smi_color_on_primary|Headline text for a card|
|smi_card_subheader_text|@color/smi_color_primary_variant|Subheader text for a card|

### Carousels Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_carousel_title_text|@color/smi_color_on_primary|Carousel text title color|
|smi_carousel_sub_title_text|@color/smi_color_on_primary|Carousel text subtitle color|
|smi_carousel_button_text|@color/smi_color_secondary|Carousel button text color|
|smi_carousel_button_text_disabled|@color/smi_color_on_background|Carousel button disabled text color|
|smi_carousel_button_background|@color/smi_color_surface|Carousel button background color|
|smi_carousel_background|@color/smi_color_surface|Carousel background color|
|smi_carousel_progress_indicator_active|@color/smi_color_secondary|Carousel progress indicator active color|
|smi_carousel_progress_indicator_inactive|@color/smi_color_on_background|Carousel progress indicator inactive color|
|smi_carousel_selected_highlight|@color/smi_color_secondary|Carousel selected highlight color|
|smi_carousel_selected_icon_foreground|@color/smi_color_on_secondary|Carousel selected icon foreground color|
|smi_carousel_selected_icon_background|@color/smi_color_secondary|Carousel selected icon background color|
|smi_carousel_image_background|@color/smi_color_primary|Carousel image background color|

### Icon Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_icon_foreground|@color/smi_color_on_secondary|Icon foreground color|
|smi_icon_background|@color/smi_color_secondary|Icon background color|
|smi_icon_background_secondary|@color/smi_color_on_background|Icon secondary background color|
|smi_icon_disabled|@color/smi_color_on_background|Icon disabled color|

### Input Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_input_background|@color/smi_color_surface|Background color for text input field|
|smi_input_border|@color/smi_color_surface_variant|Border color for text input field|
|smi_input_text|@color/smi_color_on_surface|Text font color when typing a message|
|smi_input_placeholder|@color/smi_color_on_background|Placeholder text color for the input text field|
|smi_input_bar_background|@color/smi_color_surface|Background color for the input bar container|

### Loading Screen Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_loading_screen_text_placeholder|@color/smi_color_surface_secondary|Loading screen placeholder text color|
|smi_loading_screen_sent_bubble_background|@color/smi_color_surface_secondary|Loading screen sent bubble background color|
|smi_loading_screen_received_bubble_background|@color/smi_color_on_background|Loading screen received bubble background color|
|smi_loading_screen_avatar|@color/smi_color_on_background|Loading screen avatar color|
|smi_loading_screen_background|@color/smi_color_background|Loading screen background color|
|smi_loading_screen_input_text_placeholder|@color/smi_color_surface_secondary|Loading screen input text placeholder color|
|smi_loading_screen_shimmer|@color/smi_color_surface_transparent|Loading screen animation shimmer|

### Message Search Color ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_message_search_highlight_border_agent|@color/smi_color_highlight|Highlighted border color for agent|
|smi_message_search_highlight_border_user|@color/smi_color_highlight|Highlighted border color for user|
|smi_message_search_input_bar|@color/smi_color_on_surface|Search input bar color|
|smi_message_search_input_background|@color/smi_color_surface|Search input bar background color|
|smi_message_search_input_placeholder|@color/smi_color_on_background_semi_transparent|Search input bar placeholder color|
|smi_message_search_history_header|@color/smi_color_on_surface|Message search history header color|
|smi_message_search_history_text|@color/smi_color_on_surface|Message search history text color|
|smi_message_search_history_dismiss|@color/smi_color_on_surface|Message search history dismiss color|
|smi_message_search_result_text|@color/smi_color_on_surface|Message search result text color|
|smi_message_search_result_timestamp|@color/smi_color_on_background|Message search result timestamp color|
|smi_message_search_empty_header|@color/smi_color_on_surface|Message search header when the empty state is shown|
|smi_message_search_empty_text|@color/smi_color_on_surface|Message search text when the empty state is shown|
|smi_message_search_empty_icon|@color/smi_color_on_surface|Message search icon when the empty state is shown|
|smi_message_search_avatar_icon_background|@color/smi_color_secondary|Message search avatar icon background|
|smi_message_search_avatar_icon_foreground|@color/smi_color_on_secondary|Message search avatar icon foreground|
|smi_message_search_url_text|@color/smi_color_secondary|Hyperlink text color in message search screen|

### Participant Client Menu ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_participant_client_menu_background|@color/smi_color_background|Client menu background color|
|smi_participant_client_menu_item_text|@color/smi_color_on_surface|Client menu text item color|
|smi_participant_client_menu_item_background|@color/smi_color_surface|Client menu item background color|
|smi_participant_client_menu_item_icon|@color/smi_color_secondary|Client menu item icon color|
|smi_participant_client_menu_search_icon|@color/smi_color_secondary|Client menu search icon color|
|smi_participant_client_menu_item_focus_background|@color/smi_color_on_surface_variant|Client menu item focus background color|
|smi_participant_client_menu_button|@color/smi_color_surface|Client menu button color|
|smi_participant_client_menu_button_text|@color/smi_color_on_surface|Client menu button text color|
|smi_participant_client_menu_button_border|@color/smi_color_surface_variant|Client menu button border color|

### Pre-Chat Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_prechat_background|@color/smi_color_background|Pre-chat window background color|
|smi_prechat_border|@color/smi_color_on_primary|Border color for text input field in pre-chat|
|smi_prechat_text|@color/smi_color_on_primary|Text color on pre-chat form|
|smi_prechat_text_active|@color/smi_color_secondary|Color for the active pre-chat cell|
|smi_prechat_button|@color/smi_color_secondary|Pre-chat button color|
|smi_prechat_button_text|@color/smi_color_on_secondary|Pre-chat button text color|
|smi_prechat_input_background|@color/smi_color_surface|Pre-chat window background color|
|smi_prechat_error|@color/smi_color_error|Pre-chat error feedback color|
|smi_prechat_disabled|@color/smi_color_surface_secondary|Disabled pre-chat field|

### Secure Forms Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_form_submit_button|@color/smi_color_secondary|Secure form submit button color|
|smi_form_submit_button_text|@color/smi_color_on_secondary|Secure form submit button color text|
|smi_form_next_button|@color/smi_color_secondary|Secure form next button background color|
|smi_form_next_button_text|@color/smi_color_on_secondary|Secure form next button text color|
|smi_form_back_button|@color/smi_color_on_secondary|Secure form back button background color|
|smi_form_back_button_text|@color/smi_color_secondary|Secure form back button text color|
|smi_form_background|@color/smi_color_background|Secure form background color|
|smi_form_title_text|@color/smi_color_on_primary|Secure form title text color|
|smi_form_sub_title_text|@color/smi_color_on_primary|Secure form subtitle text color|
|smi_form_option_title|@color/smi_color_on_primary|Secure form selectable option title color|
|smi_form_option_button_background|@color/smi_color_primary|Secure form selectable option button background color|
|smi_form_option_button_text|@color/smi_color_on_primary|Secure form selectable option button text color|
|smi_form_option_button_border|@color/smi_color_on_primary|Secure form selectable option button border color|
|smi_form_option_button_border_selected|@color/smi_color_secondary|Secure form selectable option button border selected color|
|smi_form_option_text_error|@color/smi_color_error|Secure form selectable option text error color|
|smi_form_input_footer_label|@color/smi_color_on_primary|Secure form input footer label|
|smi_form_input_border|@color/smi_color_on_primary|Secure form input border color|
|smi_form_input_border_active|@color/smi_color_secondary|Secure form input border active color|
|smi_form_input_border_error|@color/smi_color_error|Secure form input border error color|
|smi_form_input_title_text|@color/smi_color_on_primary|Secure form input title text color|
|smi_form_input_text|@color/smi_color_on_primary|Secure form input text color|
|smi_form_input_background|@color/smi_color_background|Secure form input background color|
|smi_form_confirmation_button_leave|@color/smi_color_secondary|Secure form confirmation window leave button background color|
|smi_form_confirmation_button_leave_text|@color/smi_color_on_secondary|Secure form confirmation window leave button text color|
|smi_form_confirmation_button_cancel|@color/smi_color_surface|Secure form confirmation cancel button background color|
|smi_form_confirmation_button_cancel_text|@color/smi_color_on_surface|Secure form confirmation cancel button text color|
|smi_form_confirmation_button_cancel_border|@color/smi_color_on_surface|Secure form confirmation cancel button border text color|
|smi_form_proress_bar_foreground|@color/smi_color_secondary|Secure form progress bar foreground color|
|smi_form_cell_icon_background_disabled|@color/smi_color_surface_highlight|Secure form cell icon disabled background color|
|smi_form_cell_icon_disabled|@color/smi_color_primary|Secure form cell icon disabled color|
|smi_form_option_button_icon|@color/smi_color_secondary|Secure form option icon button color|
|smi_form_date_picker_icon|@color/smi_color_secondary|Secure form date picker icon color|

### Request Transcript Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_transcript_background|@color/smi_color_background|Request transcript screen background color|
|smi_transcript_text|@color/smi_color_on_surface|Request transcript screen text color|
|smi_transcript_button_leave|@color/smi_color_secondary|Request transcript leave button color|
|smi_transcript_button_leave_border|@color/smi_color_secondary|Request transcript leave button border color|
|smi_transcript_button_leave_text|@color/smi_color_on_secondary|Request transcript leave button text color|
|smi_transcript_button_cancel|@color/smi_color_surface|Request transcript cancel button color|
|smi_transcript_button_cancel_border|@color/smi_color_on_surface_variant|Request transcript cancel button border color|
|smi_transcript_button_cancel_text|@color/smi_color_on_surface|Request transcript cancel button text color|
|smi_transcript_progress_indicator|@color/smi_color_secondary|Request transcript progress indicator color|
|smi_transcript_progress_indicator_background|@color/smi_color_on_secondary|Request transcript progress indicator background color|

### Progress Indicator Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_progress_indicator_text|@color/smi_color_on_surface|Progress indicator label text color|
|smi_progress_indicator_icon_outer|@color/smi_color_on_surface|Progress indicator animation icon outer arch color|
|smi_progress_indicator_icon_inner|@color/smi_color_on_surface|Progress indicator animation icon inner arch color|
|smi_notification_badge_background|@color/smi_color_surface|Notification badge background color|
|smi_notification_badge_icon|@color/smi_color_on_surface|Notification badge icon color|
|smi_notification_badge_outline|@color/smi_color_surface_variant|Notification badge outline color|
|smi_notification_badge_counter_background|@color/smi_color_error|Notification badge unread message counter background|
|smi_notification_badge_counter_text|@color/smi_color_surface|Notification badge unread message counter text color|

### End Chat Session Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_end_chat_confirmation_background|@color/smi_color_background|End Chat confirmation screen background color|
|smi_end_chat_confirmation_text|@color/smi_color_on_surface|End Chat confirmation screen text color|
|smi_end_chat_confirmation_button_background|@color/smi_color_secondary|End Chat confirmation button color|
|smi_end_chat_confirmation_button_border|@color/smi_color_secondary|End Chat confirmation button border color|
|smi_end_chat_confirmation_button_text|@color/smi_color_on_secondary|End Chat confirmation button text color|
|smi_end_chat_confirmation_button_cancel_background|@color/smi_color_surface|End Chat confirmation cancel button color|
|smi_end_chat_confirmation_button_cancel_border|@color/smi_color_on_surface_variant|End Chat confirmation cancel button border color|
|smi_end_chat_confirmation_button_cancel_text|@color/smi_color_on_surface|End Chat confirmation cancel button text color|
|smi_end_chat_confirmation_progress_indicator|@color/smi_color_secondary|End session progress indicator color|

### Closed Conversation Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_closed_conversation_background|@color/smi_color_primary|Closed conversation message background color|
|smi_closed_conversation_text|@color/smi_color_secondary|Closed conversation message text color|

### Fallback Message Screen Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_fallback_message_background|@color/smi_color_background|Fallback Message confirmation screen background color|
|smi_fallback_message_confirmation_text|@color/smi_color_on_surface|Fallback Message confirmation screen text color|
|smi_fallback_message_confirmation_button|@color/smi_color_secondary|Fallback Message confirmation button color|
|smi_fallback_message_confirmation_button_text|@color/smi_color_on_secondary|Fallback Message confirmation button text color|

### File Type Placeholders ###
|**Keyword**|**Default Light Color**|**Default Dark Color**|**Description**|
|-|-|-|-|
|smi_color_file_icon_xml|#FFF38303|#FFF38303|Placeholder color for .xml files shown in the feed.|
|smi_color_file_icon_csv|#FF2E844A|#FF2E844A|Placeholder color for .csv files shown in the feed.|
|smi_color_file_icon_txt|#FFE4A201|#FFE4A201|Placeholder color for .txt files shown in the feed.|
|smi_color_file_icon_word|#FF107CAD|#FF107CAD|Placeholder color for .doc and .docx files shown in the feed.|
|smi_color_file_icon_excel|#FF2E844A|#FF2E844A|Placeholder color for .xls and .xlsx files shown in the feed.|
|smi_color_file_icon_audio|#FFDD7A01|#FFBF6B04|Placeholder color for audio files shown in the feed.|
|smi_color_file_icon_video|#FF06A59A|#FF088F86|Placeholder color for video files shown in the feed.|
|smi_color_file_icon_catch_all|#FF7F8CED|#FF6F7ACC|Placeholder color for catch all files shown in the feed.|

### Markdown Text Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_received_markdown_link_text|@color/smi_color_secondary|Color used for link text that renders in markdown|
|smi_received_markdown_code_background|@color/smi_color_background|Color used for the background of code in markdown|
|smi_received_markdown_code_text|@color/smi_color_on_surface|Color used for the text of code in markdown|
|smi_received_markdown_horizontal_rule|@color/smi_color_on_surface|Color used for the background color of a horizontal rule rendered in markdown|

### Citation Colors ###
|**Detail Token**|**Generic Token**|**Description**|
|-|-|-|
|smi_citations_source_button_background|@color/smi_color_surface_secondary_variant|Color used for the background color of a citation source button|
|smi_citations_source_button_text|@color/smi_color_on_surface|Color used for the text color of a citation source button|

</details>

</details>

<h3>Icons</h3>

To customize the icons and images used throughout the SDK, add a drawable asset to your project using the associated keyword. To learn more, see [Customize Icons for Android](https://developer.salesforce.com/docs/service/messaging-in-app/guide/android-icons.html).


<details>
<summary>Show Icons</summary>

| Token | Image | Description |
| - | - | - |
| smi_action_attach_file | ![smi_action_attach_file](assets/smi_action_attach_file.svg) | Icon for attaching a file. |
| smi_action_attach_photo | ![smi_action_attach_photo](assets/smi_action_attach_photo.svg) | Icon for attaching a photo. |
| smi_action_cancel | ![smi_action_cancel](assets/smi_action_cancel.svg) | Icon representing the cancel action. |
| smi_action_delete | ![smi_action_delete](assets/smi_action_delete.svg) | Icon representing the delete action. |
| smi_action_download | ![smi_action_download](assets/smi_action_download.svg) | Icon representing the download action. |
| smi_action_jump_to | ![smi_action_jump_to](assets/smi_action_jump_to.svg) | Icon representing the jump-to action. |
| smi_action_mute | ![smi_action_mute](assets/smi_action_mute.svg) | null |
| smi_action_scroll_to_bottom | ![smi_action_scroll_to_bottom](assets/smi_action_scroll_to_bottom.svg) | Icon representing the scroll down action on the overlay badge in the feed. |
| smi_action_search | ![smi_action_search](assets/smi_action_search.svg) | Icon representing the message search action. |
| smi_action_send | ![smi_action_send](assets/smi_action_send.svg) | null |
| smi_action_share | ![smi_action_share](assets/smi_action_share.svg) | Icon representing the share action. |
| smi_action_take_photo | ![smi_action_take_photo](assets/smi_action_take_photo.svg) | Icon for taking a photo. |
| smi_action_unmute | ![smi_action_unmute](assets/smi_action_unmute.svg) | null |
| smi_add | ![smi_add](assets/smi_add.svg) | Icon representing the menu button on the text input. |
| smi_avatar_agent | ![smi_avatar_agent](assets/smi_avatar_agent.svg) | Agent avatar shown in the chat feed. |
| smi_avatar_bot | ![smi_avatar_bot](assets/smi_avatar_bot.svg) | Default bot avatar shown in the chat feed. |
| smi_avatar_user | ![smi_avatar_user](assets/smi_avatar_user.svg) | User avatar shown in search results. |
| smi_chevron_right | ![smi_chevron_right](assets/smi_chevron_right.svg) | Icon representing a link to the preview that can be navigated. |
| smi_icon_check | ![smi_icon_check](assets/smi_icon_check.svg) | Icon representing a confirmed action. |
| smi_icon_clock | ![smi_icon_clock](assets/smi_icon_clock.svg) | Icon representing the image used for business hours banner. |
| smi_icon_error | ![smi_icon_error](assets/smi_icon_error.svg) | Icon representing an error. |
| smi_icon_failed | ![smi_icon_failed](assets/smi_icon_failed.svg) | Icon representing an error that occured while sending a message. |
| smi_icon_file_audio | ![smi_icon_file_audio](assets/smi_icon_file_audio.svg) | Icon representing an audio file attachment. |
| smi_icon_file_csv | ![smi_icon_file_csv](assets/smi_icon_file_csv.svg) | Icon representing a CSV file with extension .csv. |
| smi_icon_file_excel | ![smi_icon_file_excel](assets/smi_icon_file_excel.svg) | Icon representing an Excel file with extension .xls or .xlsx. |
| smi_icon_file_other | ![smi_icon_file_other](assets/smi_icon_file_other.svg) | Icon representing a generic file attachment. |
| smi_icon_file_txt | ![smi_icon_file_txt](assets/smi_icon_file_txt.svg) | Icon representing a text file with extension .txt. |
| smi_icon_file_video | ![smi_icon_file_video](assets/smi_icon_file_video.svg) | Icon representing a video file attachment. |
| smi_icon_file_word | ![smi_icon_file_word](assets/smi_icon_file_word.svg) | Icon representing a word file with extension .doc or .docx. |
| smi_icon_file_xml | ![smi_icon_file_xml](assets/smi_icon_file_xml.svg) | Icon representing a XML file with extension .xml. |
| smi_icon_form | ![smi_icon_form](assets/smi_icon_form.svg) | Icon representing the form message shown in the feed. |
| smi_icon_offline | ![smi_icon_offline](assets/smi_icon_offline.svg) | Icon representing an offline state. |
| smi_icon_pre_chat_receipt | ![smi_icon_pre_chat_receipt](assets/smi_icon_pre_chat_receipt.svg) | Icon representing a checklist in the pre-chat receipt. |
| smi_icon_read | ![smi_icon_read](assets/smi_icon_read.svg) | Icon representing a read state. |
| smi_icon_sending | ![smi_icon_sending](assets/smi_icon_sending.svg) | Icon representing a message in transit. |
| smi_icon_sent | ![smi_icon_sent](assets/smi_icon_sent.svg) | Icon representing a successfully sent message. |
| smi_image_icon | ![smi_image_icon](assets/smi_image_icon.svg) | Icon representing an image placeholder in the feed. |
| smi_no_search_results | ![smi_no_search_results](assets/smi_no_search_results.svg) | Icon shown when there are no search results. |
| smi_options_menu_icon | ![smi_options_menu_icon](assets/smi_options_menu_icon.svg) | Icon representing the conversation options navigation action. |

</details>

<h3>Strings</h3>

To customize any UI text, create a strings.xml resource file in your app using the SDK's resource names. To learn more, see [Customize Strings for Android](https://developer.salesforce.com/docs/service/messaging-in-app/guide/android-strings.html).


<details>
<summary>Show Strings</summary>


###  Chat Feed Strings  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_chat_feed_title|Chat|Title for the chat feed view.|
|smi_chat_feed_accessibility|Chat Feed|Accessibility label for the chat feed container view.|
|smi_chat_add_button|Open option menu.|Content description for the add button in the chat view to open the options menu.|
|smi_chat_start_conversation|Start Conversation|Content description for a button that is used to resubmit pre-chat fields mid-conversation.|
|smi_chat_start_conversation_feed_input_text_accessibility|Fill out a pre-chat form to continue the conversation|Accessibility announcement when the user needs to enter and resubmit pre-chat fields.|
|smi_chat_closed_conversation|This conversation has ended|Text displayed instead of the text input when a conversation has closed and can longer be interacted with.|
|smi_chat_badge_overlay_count|9+|The chat feed badge overlay unread message count for amounts equal to or over 10.|

###  Chat Feed Input Strings  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_feed_input_text|Type a message|Placeholder text inside chat input field.|
|smi_feed_input_text_accessibility|Message Input|Accessibility label for identifying the input control.|
|smi_feed_input_button|Send|Action text for the send message button.|
|smi_feed_new_messages|New Messages|A notification that alerts the user that there are new unread messages in the chat feed.|
|smi_feed_new_messages_accessibility|You have new messages.|An accessibility announcement that alerts the user that there are new unread messages in the chat feed.|
|smi_feed_new_messages_button_accessibility|Scroll to bottom of chat feed.|The description for the notification badge button that alerts a user they will be brought to the bottom of the feed.|

###  Action Menu Strings  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_feed_action_menu_photo_send|Send a Photo|Action Menu text for sending a photo.|
|smi_feed_action_menu_file_send|Send a File|Action Menu text for sending a file.|
|smi_feed_action_menu_photo_take|Take a Photo|Action Menu text for taking a photo.|
|smi_feed_action_menu_accessibility|Action Menu|The accessibility string for the Action Menu to the left of the text input|

###  Delivery Status  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_delivery_status_sending|Sending|The message auxiliary view that will display the sending message indicator.|
|smi_delivery_status_sent|Sent|The message auxiliary view that will display the sent message indicator.|
|smi_delivery_status_read|Read|The message auxiliary view that will display the read message indicator.|
|smi_delivery_status_delivered|Delivered|The message auxiliary view that will display the delivered message indicator.|
|smi_delivery_status_error|Send failed. Tap to retry.|The message auxiliary view that will display the error message.|
|smi_delivery_status_error_unsupportedMediaType|Message Failed.|The error message appears when a message failed due to an unsupported media type|
|smi_delivery_status_error_additional_information_required|Provide additional info to proceed.|The error message that appears when receiving a 417 error. This message corresponds to needing to resubmit pre-chat fields.|
|smi_delivery_status_error_accessibility|Failed|The concise failed status for accessibility|
|smi_delivery_status_error_hint_accessibility|Double tap to retry|Hint for the user to double tap the message to retry|
|smi_local_message_identifier_accessibility|Your Message %s|The accessibility text for identifying messages sent from the local user|
|smi_message_received_event_accessibility|Message received from %s|The accessibility announcement text that informs the user when a message has been received|
|smi_message_sending_event_accessibility|Message sent|The accessibility announcement text that informs the user that their message has been sent|

###  Network Connectivity Banner  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_network_status_reconnecting|Waiting to reconnect|The network connectivity banner text that will inform the user the network connection has been lost and is attempting to reconnect|
|smi_network_status_reconnecting_accessibility|Network connectivity is lost, attempting to reconnect|The accessibility announcement that informs the user when the network connection has been lost and is attempting to reconnect|
|smi_network_status_connected|Connected|The network connectivity banner that will inform the user the network has connected after the network has lost connectivity|
|smi_network_status_connected_accessibility|Network connectivity established|The accessibility announcement that informs the user when the network is connected after having lost connectivity|

###  Business Hours Banner  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_business_hours_outside|New chats aren\'t available outside of business hours|The message that is shown in the chat feed banner when outside of business hours|
|smi_business_hours_outside_accessibility|New chats aren\'t currently available. Try again during business hours.|The accessibility announcement that informs the user that they are out of the set business hours and the contact center is closed|

###  Typing  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_typing_indicator_start_accessibility|%s has started typing|The accessibility announcement that informs the user when a participant starts typing|
|smi_typing_indicator_stop_accessibility|%s has stopped typing|The accessibility announcement that informs the user when a participant stops typing|

###  Progress Indicators  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_progress_indicator_single_participant|%s is typing...|The default label for a single participant progress indicator.|
|smi_progress_indicator_bot|%s is responding...|The default label for a bot progress indicator.|
|smi_progress_indicator_two_participants|%1$s and %2$s are typing...|The default label for two participants typing.|
|smi_progress_indicator_multiple_participants|Multiple people are typing...|The default label for multiple participants typing.|
|smi_progress_indicator_message_accessibility|%1$s %2$s|The accessibility announcement for a bot progress indicator containing the bot display name and the progress message text.|
|smi_progress_indicator_queue_position|Your current queue position is %1$d|The current queue position label (e.g. Your current queue position is 10) displayed as a progress indicator.|

###  Misc  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_misc_separator|•|separator used when conjoining information together in a string|
|smi_misc_unknown|Unknown|This is used when a value or result cannot be determined|
|smi_action_cancel|Cancel|Cancel action to be used for cancel button and menus throughout the SDK.|
|smi_action_settings|Settings|Settings action for permission alerts|
|smi_file_size_megabyte|MB|Size of a file in megabytes|
|smi_unknown_entry_message|This message type isn\'t supported.|The error message that appears when the message type of the message in the chat feed isn't supported.|

###  Alerts  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_alert_title_failed|Message Failed|Alert title text for the retry message alert.|
|smi_alert_title_permission_camera|Allow Camera Access|Alert title text for requesting camera access.|
|smi_alert_message_failed|Message failed to send. Retry?|Confirmation text for the retry message alert.|
|smi_alert_message_permission_camera|To use your camera, go to Settings and enable Camera access for this app.|Explanation that the user must allow camera permission|
|smi_alert_action_retry|Retry|Retry action for the retry message alert.|
|smi_alert_attachment_limit_reached|Attachment limit reached|Alert when limit for number of attachments is reached.|

###  Participant Changed  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_participant_name_default|Participant|Default string for a participant with no displayName or subject configured.|
|smi_participant_changed_joined|%s has joined|Notice that a participant has joined the conversation|
|smi_participant_changed_joined_accessibility|%s has joined the conversation|The accessibility announcement that informs the user that a participant has joined the conversation|
|smi_participant_changed_left|%s has left|Notice that a participant has left the conversation|
|smi_participant_changed_left_accessibility|%s has left the conversation|The accessibility announcement that informs the user that a participant has left the conversation|

###  PreChat  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_pre_chat_required_accessibility|%s is required|The accessibility string for the PreChat field if the field is required|
|smi_pre_chat_required|%s *|The string for a PreChat field label if the field is required|
|smi_pre_chat_submit_button|Chat with an Agent|The title of the PreChat submit button|
|smi_pre_chat_title|Just a few things before we connect you with an expert…|The title of the PreChat forum|
|smi_pre_chat_choice_list_none|None|Label for the None option in a pre-chat choice list|
|smi_pre_chat_back_button|Back|The back button for the prechat form|
|smi_pre_chat_error_field_required|This field is required.|Error message shown when a required field fails PreChat validation|
|smi_pre_chat_error_valid_email_format|Use a valid email format.|Error message shown when an email PreChat field fails PreChat validation|
|smi_pre_chat_error_valid_number_format|Field can only contain numbers.|Error message shown when a number PreChat field fails PreChat validation|
|smi_pre_chat_error_valid_phone_format|Use a valid phone number format.|Error message shown when a phone PreChat field fails PreChat validation|
|smi_pre_chat_submission_review_title|Submitted Form Details|Header for prechat submitted form details|
|smi_pre_chat_submission_card_title|Form submitted Tap to review|Text for prechat cell that shows in chat after submitting prechat|
|smi_back_button_accessibility|Back|Accessibility text for hitting the back button|
|smi_exit_button_accessibility|Exit to chat feed|Accessibility text for hitting the exit button on pre chat submission screen|
|smi_checkbox_selected_accessibility|Checkbox selected|Accessibility text for when the checkbox is selected|
|smi_checkbox_not_selected_accessibility|Checkbox not selected|Accessibility text for when the checkbox is not selected|
|smi_terms_and_conditions_error|Accept the Terms and Conditions to continue.|Error message displayed when the Terms and Conditions are required but not accepted.|
|smi_terms_and_conditions_accept|Accept|Message displayed next to the checkbox to accept the Terms and Conditions.|
|smi_terms_and_conditions_accepted_accessibility|Terms and conditions is accepted|Accessibility label for terms and conditions box when it is accepted.|
|smi_terms_and_conditions_not_accepted_accessibility|Terms and conditions is not accepted|Accessibility label for terms and conditions box when it is accepted.|
|smi_pre_chat_error_accessibility|%1$s input. Error %2$s.|Accessibility readout for prechat errors. The first parameter is the field name, the second field is the error on the field.|

###  Attachments  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_image_accessibility|Image message.|Accessibility text for an image message in the chat feed or attachment preview.|
|smi_remove_image_button_accessibility|Remove image message.|Accessibility text for the button to remove an image message from the input field.|
|smi_multiple_attachment_counter|+%d|A counter that takes an integer representing the number of file attachments not shown.|
|smi_image_preview_accessibility|Image preview.|Accessibility text for the image preview dialog.|
|smi_image_preview_cancel_button_accessibility|Close image preview.|Accessibility text for the button to close the image preview dialog.|
|smi_camera_gallery_button_accessibility|Photo gallery.|Accessibility text for the button to open the photo gallery.|
|smi_camera_switch_lens_button_accessibility|Switch lens direction.|Accessibility text for the button to switch the camera lens direction.|
|smi_camera_capture_button_accessibility|Capture photo.|Accessibility text for the button to capture a photo.|
|smi_camera_view_capturing_accessibility|Camera capturing photo|Accessibility text for the view that is displaying the phones camera capturing a photo|
|smi_image_viewer_jump_to_photo_accessibility|Jump to photo|Accessibility text for the jump to photo button in image viewer|
|smi_image_viewer_share_photo_accessibility|Share photo|Accessibility text for the share button in image viewer|
|smi_image_viewer_download_photo_accessibility|Download photo|Accessibility text for the download button in image viewer|
|smi_image_saved_message|Attachment saved|Shown in popup when user taps the download button and the attachment successfully saves|
|smi_attachment_download_failed_message|Failed to save attachment|Shown in popup when user clicks on download button and the attachment fails to save|
|smi_image_download_denied_title|Allow Photo Access|Title shown in popup when user tries to save a photo with denied permission|
|smi_image_download_denied_body|To save photos, go to Settings and enable Photos access for this app.|Message shown in popup when user tries to save a photo with denied permission|
|smi_file_format_unsupported|Unsupported file format.|Message shown if user sends an unsupported file in chat|
|smi_file_format_unsupported_alternate|File type not supported, try: %s|Message show if the user sends an unsupported file in the chat and a list of alternate file types given as a suggestion.|
|smi_file_size_exceeded|Attachment is too large. Maximum size is %s.|Message shown if the user sends an attachment that exceeds the max size that can be sent|

###  Agent Transfer  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_transfer_requested_event_text|Transfer requested at %s|A system message informing the user that a transfer to a service agent has been requested, and a string template indicating the time of the request.|

###  Routing Failure  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_routing_failure_event_text|Agents are not available. Try again later.|A default system message informing the user that routing to an service console agent has failed.|

###  Routing Estimated Wait Time  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_routing_initial_agent|An agent will be with you shortly.|Shown when the routing type is initial. and EWT is available|
|smi_routing_wait_resolved_minutes|Your expected wait time is %s minutes.|Shown when the estimated wait time is greater than 60 seconds. Rounded up to the nearest minute|
|smi_routing_wait_resolved_short|Your expected wait time is less than one minute.|Shown when the estimated wait time is less than or equal to 60 seconds|

###  Loading Screen  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_loading_screen_accessibility|Active loading screen|A message that will be read out to the user when a loading screen is shown, and read out to the user when the loading screen is selected.|

###  Link Preview Strings  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_link_preview_accessibility|Link preview|The screen reader will read out link preview and then the name of the link|

###  Message Search  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_nothing_entered_in_search|Nothing here yet|Message displayed when there’s nothing in the search bar and there are no recent searches|
|smi_no_search_results|No search results|Message displayed when there are no search results|
|smi_no_matching_results|We couldn\'t find what you\'re looking for. Try searching for something else.|Message displayed when nothing matches the users search|
|smi_message_search_header|Message Search|Header for the message search page|
|smi_recent_searches_header|Recent Searches|Header for the recent search list|
|smi_search_placeholder_text|Search|Placeholder text for the search bar|
|smi_today_timestamp_message_search|Today|Timestamp text for when the message was sent today|
|smi_yesterday_timestamp_message_search|Yesterday|Timestamp text for when the message was sent yesterday|
|smi_users_name|You|The name shown for the user in the time stamp row|
|smi_no_recent_searches|You haven\'t performed a recent search. Enter keywords in the search field.|Message displayed when there are no search results and no text is entered in the search field|
|smi_message_search_back_button|Back|The back button for message search|
|smi_message_search_clear_search_text_accessibility|Clear search text|Accessibility text for clearing the search field in message search|
|smi_message_search_agent_avatar_accessibility|Agent avatar|Accessibility text for the agent avatar in message search|
|smi_message_search_user_avatar_accessibility|User avatar|Accessibility text for the user avatar in message search|
|smi_message_search_agent_bot_accessibility|Bot avatar|Accessibility text for the bot avatar in message search|
|smi_message_search_result_item_accessibility|%s, %s, %s|Accessibility text for the message search result item|

###  Copy Menu  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_action_copy|Copy|Text displayed in the context menu for the copy behavior|

###  Participant Client Menu  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_participant_client_menu_title|Conversation Options|Text displayed in the header of the participant client menu. The participant client menu will contain actions a user can take in a chat feed. The header will display the name of the participant followed by Options.|
|smi_participant_client_menu_title_accessibility|Conversation Options Menus|The accessibility message for the participant client menu|

###  Form Message  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_form_message_footer_submit|Submit|Text displayed in the footer menu of the Form Message for the submit button.|
|smi_form_message_footer_back|Back|Text displayed in the footer menu of the Form Message for the back button.|
|smi_form_message_footer_next|Next|Text displayed in the footer menu of the Form Message for the next button.|
|smi_form_message_back_button|Back|The back button for the secure form|
|smi_form_message_back_button_accessibility|Back to chat feed|Accessibility text for hitting the back button on form message submission screen|
|smi_form_message_exit_screen_title|Leave the form?|The main title for the secure form exit confirmation screen.|
|smi_form_message_exit_screen_subtitle|After leaving the form, you can open it again in the messaging window. However, your progress isn\'t saved.|The subtitle for the secure form exit confirmation screen.|
|smi_form_message_exit_screen_confirm|Leave Form|The leave form button for the secure form exit confirmation screen.|
|smi_form_message_exit_screen_cancel|Cancel|The cancel button text for the secure form exit confirmation screen|
|smi_form_message_result_success|Form Submitted|The message shown in the chat bubble when a form has been successfully submitted.|
|smi_form_message_result_submitting|Form Submitting|The message shown in the chat bubble when a form is submitting.|
|smi_form_message_result_error|Error Submitting Form|The message shown in the chat bubble when a form fails to submit.|
|smi_form_message_required_accessibility|%s is required|The accessibility string for the form message field if the field is required|
|smi_form_message_required|%s *|The string for a form message field label if the field is required|
|smi_form_message_section_required_acccessibility|Required: %s|The string for a form message field label if the field is required|
|smi_form_message_text_invalid_email|Not a valid email|The string that is displayed below a form entry that indicates an invalid email has been entered.|
|smi_form_message_text_invalid_url|Not a valid url|The string that is displayed below a form entry that indicates an invalid url has been entered.|
|smi_form_message_text_input_required_message|This field is required|The string that is displayed below a form entry that indicates this item is required.|
|smi_form_message_option_required_message|One or more options required|The string that is displayed below a form entry that indicates this item is required.|
|smi_form_message_divider|%1$d/%2$d|The divider for the form message text field character counter|
|smi_form_message_text_input_accessibility|%s double tap to edit|The accessibility message for the grouped form text input|
|smi_form_message_text_input_required_accessibility|%s is required. Double tap to edit|The accessibility message for the group form text required input|
|smi_form_message_screen_accessibility|%s Screen|The accessibility message is show when a new screen is displayed in the form input|
|smi_form_error_accessibility|%1$s input. Error %2$s.|Accessibility readout for form errors. The first parameter is the form field label. The second parameter is the error message on the field.|

###  Carousel  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_carousel_image_placeholder_accessibility|Empty Carousel image|A placeholder icon to indicate there is no image for a carousel option.|
|smi_carousel_selected_icon_accessibility|Option selected|An icon to indicate that an option has been selected for a carousel message.|
|smi_carousel_image_accessibility|Carousel image|The default image content description for a carousel option, if no description is given.|
|smi_carousel_indicator_accessibility|Page %1$d of %2$d|Accessibility label for the carousel indicator|
|smi_carousel_indicator_dot_accessibility|Go to page %1$d of %2$d|Accessibility label for individual carousel indicator dots|

###  Request Transcript  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_transcript_request_button|Request Chat Transcript|The title of the request transcript button in the participant client menu.|
|smi_transcript_title|Chat Transcript|The title of the request transcript screen.|
|smi_transcript_leave_button|Leave|The leave button on the leave transcript screen. This screen warns the user that if they leave without saving, the transcript is lost.|
|smi_transcript_cancel_button|Cancel|The cancel button on the leave transcript screen. This screen warns the user that if they leave without saving, the transcript is lost.|
|smi_transcript_leave_title|Leave Transcript|Title of the leave transcript screen. This screen warns the user that if they leave without saving, the transcript is lost.|
|smi_transcript_leave_subtitle|Be sure to save the transcript before leaving.|Subtitle of the leave transcript screen. This screen warns the user that if they leave without saving, the transcript is lost.|
|smi_transcript_save_button_accessibility|Save Transcript|The accessibility string for the save transcript button.|
|smi_transcript_share_button_accessibility|Share Transcript|The accessibility string for the share transcript button.|
|smi_transcript_error_message|Something went wrong. Please try again later.|Error message to display on the error screen when the transcript fails to download from the server.|
|smi_transcript_alert_save_failed|Save failed|Message to display when the request transcript fails to save on the device.|
|smi_transcript_alert_save_succeed|Save was successful|Message to display when a request transcript successfully saves on the device.|

###  End Chat Session  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_end_chat_menu_button|End Chat|The title of the end chat button in the participant client menu.|
|smi_end_chat_title|End Chat?|The title label on the end chat confirmation page.|
|smi_end_chat_subtitle|Are you sure you want to end the chat session? You will be disconnected from the agent.|The subtitle label on the end chat confirmation page.|
|smi_end_chat_confirmation_button|End Chat|The end chat button label on the confirmation page.|
|smi_end_chat_cancel|Cancel|The cancel button label on the confirmation page.|
|smi_end_chat_error_message|Something went wrong. Please try again later.|Error message to display on the end chat confirmation page when there is an issue ending the chat|
|smi_fallback_message_close_button|Close|The close button label on the fallback message page.|

###  Streaming Tokens  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_streaming_text_cursor|▏|The cursor to display while text is streaming in.|
|smi_streaming_message_accessibility|The bot is responding|Accessibility readout for when a message is being streamed in.|
|smi_streaming_invalid|The previous response couldn’t be verified. The agent is generating a new response.|Displayed when a streaming token was invalidated|
|smi_streaming_revised|Revised|Displayed when a message was revised|
|smi_citation_inline_title|\\\[%d\\]|A string template for inline citations. Takes an integer representing the citation number.|
|smi_citation_source_button_title|\[%1$d] %2$s|A string template for citation source button. Takes an integer representing the citation number and a string representing the url title|
|smi_session_status_changed_ended|Chat Ended • %s|Displayed in the chat feed when the chat session ends. Ex: Chat Ended • 12:34 PM|

</details>