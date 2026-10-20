# Module data

Domain models and data types for the Messaging for In-App SDK. This module defines the type hierarchy
for conversations, entries, messages, participants, pre-chat fields, and errors. These types appear
throughout the `core` and `ui` module APIs as return values and callback parameters.

## Conversation

[Conversation][com.salesforce.android.smi.network.data.domain.conversation.Conversation] represents an ongoing or completed messaging session between an end user and one or more agents or bots.

Key properties:
- `identifier` -- unique conversation ID
- `status` -- derived [ConversationStatus][com.salesforce.android.smi.network.data.domain.conversation.ConversationStatus]: `Open`, `Closed`, or `Unknown`
- `participants` / `activeParticipants` -- all or currently active [Participant][com.salesforce.android.smi.network.data.domain.participant.Participant] list
- `activeModalities` -- communication channels in use (messaging, voice, video)
- `lastActivity` -- the most recent [ConversationEntry][com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry]
- `unreadMessageCount` -- messages not yet read by the local participant

## Conversation Entries

[ConversationEntry][com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry] is a single item in the conversation timeline -- a message, typing indicator,
routing event, or participant change. Each entry has a `sender`, a delivery `status`, and a typed `payload`.

```
ConversationEntry
 +-- sender: Participant
 +-- status: ConversationEntryStatus
 +-- payload: EntryPayload  (sealed interface)
```

[ConversationEntryStatus][com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntryStatus] tracks delivery progress:
`Sending` -> `Sent` -> `Delivered` -> `Read`, or `Error` on failure.

[EntryPayload][com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload] is a sealed interface with subtypes for each event kind:

- `MessagePayload` -- a participant message (see Messages below)
- `TypingIndicatorPayload` / `TypingStartedIndicatorPayload` / `TypingStoppedIndicatorPayload`
- `ParticipantChangedPayload` -- join/leave/role-change events
- `RoutingResultPayload` -- agent assignment or routing failure
- `QueuePositionPayload` -- end user position in agent queue
- `CloseConversationPayload` -- conversation ended
- `SessionStatusChangedPayload` -- session lifecycle transitions
- `ModalityConnectionPayload` -- voice/video connection state changes
- `StreamingTokenPayload` -- incremental tokens from a streamed response
- `UnknownEntryPayload` -- fallback for unrecognized entry types

```kotlin
when (val payload = entry.payload) {
    is EntryPayload.MessagePayload -> renderMessage(payload)
    is EntryPayload.ParticipantChangedPayload -> showJoinLeave(payload.entries)
    is EntryPayload.RoutingResultPayload -> showRoutingStatus(payload)
    else -> { /* handle or ignore */ }
}
```

## Messages

`EntryPayload.MessagePayload` implements the [Message][com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.Message] interface. A message's structure is
determined by its `messageType` enum and its `content: MessageFormat`.

```
Message
 +-- messageType: ConversationEntryMessageType
 +-- content: MessageFormat  (sealed interface)
```

[MessageFormat][com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.MessageFormat] subtypes:

| Format | Description |
|--------|-------------|
| `StaticContentFormat.TextFormat` | Plain or markdown text |
| `StaticContentFormat.AttachmentsFormat` | File attachments with optional text |
| `StaticContentFormat.RichLinkFormat` | Link preview with image |
| `StaticContentFormat.WebViewFormat` | Embedded web view (surveys) |
| `ChoicesFormat.QuickRepliesFormat` | Pill-shaped reply suggestions |
| `ChoicesFormat.DisplayableOptionsFormat` | Stacked buttons |
| `ChoicesFormat.CarouselFormat` | Scrollable rich cards |
| `FormFormat.InputsFormat` | Structured input form with titled sections |
| `FormFormat.ExperienceTypeFormat` | Experience-type form with inputs and optional confirmation |

```kotlin
when (val format = message.content) {
    is StaticContentFormat.TextFormat -> showText(format.text)
    is StaticContentFormat.AttachmentsFormat -> showAttachments(format.attachments)
    is ChoicesFormat.QuickRepliesFormat -> showQuickReplies(format.optionItems)
    is ChoicesFormat.CarouselFormat -> showCarousel(format.items)
    else -> showFallback()
}
```

## Participants

[Participant][com.salesforce.android.smi.network.data.domain.participant.Participant] identifies a conversation member. The `roleType` property indicates their role:

| [ParticipantRoleType][com.salesforce.android.smi.network.data.domain.participant.ParticipantRoleType] | Description |
|----------------------|-------------|
| `EndUser` | The customer on the device |
| `Agent` | A human support agent |
| `Chatbot` | An automated bot |
| `System` | System-generated events |
| `Supervisor` | A monitoring supervisor |
| `Router` | The routing engine |

Use `participant.isLocal` to distinguish the current device user from remote participants.

```kotlin
val agentName = conversation.activeParticipants
    .firstOrNull { it.roleType == ParticipantRoleType.Agent }
    ?.displayName
```

## Pre-Chat Fields

[PreChatField][com.salesforce.android.smi.network.data.domain.prechat.PreChatField] defines a form field the end user completes before starting a conversation.
Access the list from `conversation.preChatFields`.

To populate fields for submission, set `userInput` on each field and call `validate()`:

```kotlin
preChatFields?.forEach { field ->
    field.userInput = collectedValues[field.name] ?: ""
    val error = field.validate()
    if (error != PreChatErrorType.None) showError(field, error)
}
```

Key properties: `name`, `type`, `required`, `maxLength`, `isHidden`, `isEditable`, `labels`.

## Error Handling

[NetworkError][com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.NetworkError] is a sealed class hierarchy representing errors on failed conversation entries.
Access via `conversationEntry.error` when `status == ConversationEntryStatus.Error`.

| Subclass | HTTP Code | Meaning | Consumer Action |
|----------|-----------|---------|-----------------|
| `PreconditionFailedError` | 412 | The server rejected the request because a precondition was not met, typically an unsupported media type. | Display an error indicating the file type is not supported and allow the user to retry with a different file. |
| `UnsupportedFileTypeError` | 415 | The attached file type is not accepted by the server. | Show the list of supported file types from the deployment's attachment configuration and prompt the user to choose a compatible file. |
| `FileSizeLimitError` | 413 | The attached file exceeds the server's maximum size limit. Use `fileSizeLimit` to get the maximum allowed size in MB. | Display the maximum file size to the user (e.g., "File exceeds 5 MB limit") and ask them to attach a smaller file. |
| `ExpectationFailedError` | 417 | Additional information is required to complete the request. | Prompt the user to provide the missing information before retrying. |
| `GeneralError` | -- | An unrecognized or default error that does not match a specific category. | Display a generic delivery failure message and offer a retry option. |

```kotlin
when (val error = entry.error) {
    is NetworkError.FileSizeLimitError -> showSizeLimit(error.fileSizeLimit)
    is NetworkError.UnsupportedFileTypeError -> showFileTypeError()
    is NetworkError -> showGenericError(error.message)
    null -> { /* no error */ }
}
```