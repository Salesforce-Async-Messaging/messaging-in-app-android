# Module multimedia-common

Public interfaces and types for voice capabilities, including `MultimediaClient`, session management, participant state, and audio routing.

## Setup

Add the `multimedia:core` dependency and pass `MultimediaExtension` when creating your `CoreConfiguration`:

```kotlin
val config = CoreConfiguration.fromFile(
    context,
    multimediaExtension = MultimediaExtension.apply {
        configure { displayName = "Customer" }
    }
)
```

Retrieve the client from `CoreClient`:

```kotlin
val multimediaClient = coreClient.multimediaClient()
```

## Starting a Voice Call

Voice calls are initiated from the core module. Request the modality change on a `ConversationClient`:

```kotlin
conversationClient.changeModality(Modality.Voice)
```

The SDK handles the backend handshake and automatically creates a `MultimediaSession` observable through `MultimediaClient.currentSessionFlow`.

## Observing Session State

Use `currentSessionFlow` to reactively detect when a session becomes active or ends:

```kotlin
scope.launch {
    multimediaClient.currentSessionFlow
        .filterNotNull()
        .collect { session ->
            // Session became active; show call UI
        }
}
```

For a one-shot check, use `currentSession`:

```kotlin
val activeSession: MultimediaSession? = multimediaClient.currentSession
```

## Session Lifecycle

A session progresses through these statuses:

1. `Created` -- session exists but audio is not yet connected.
2. Call `join()` -- transitions to `Connecting`.
3. `Connected` -- audio link established.
4. `Answered` -- a remote participant has joined.
5. Call `end()` -- transitions to `Ended`.

## Session Operations

`MultimediaSession` exposes methods for in-call control:

```kotlin
session.join()             // Connect audio
session.muteMicrophone()   // Mute mic
session.unmuteMicrophone() // Unmute mic
session.hold(true)         // Place on hold
session.activate()         // Resume from hold
session.end()              // End the call
```

## Observing Participants

Monitor remote participants and their audio tracks for visualization:

```kotlin
scope.launch {
    session.remoteParticipants.collect { participants ->
        participants.forEach { participant ->
            // Use participant.audioTracks for audio visualization
        }
    }
}
```

## Handling Events

Collect `MultimediaSessionEvent` from the session's `event` flow for connection state changes and errors:

```kotlin
scope.launch {
    session.event.collect { event ->
        when (event) {
            is MultimediaSessionEvent.Connection.Connected -> { /* Call connected */ }
            is MultimediaSessionEvent.Connection.Reconnecting -> { /* Network interruption, attempting to reconnect */ }
            is MultimediaSessionEvent.Connection.Disconnected -> { /* Call ended */ }
            is MultimediaSessionEvent.ParticipantChanged -> { /* Agent joined/left */ }
            is MultimediaSessionEvent.Error -> { /* Handle error code */ }
            else -> {}
        }
    }
}
```

## Required Permissions

Request these permissions before calling `changeModality`:

| Permission | Min API | Purpose |
|---|---|---|
| `RECORD_AUDIO` | All | Microphone access for voice |
| `BLUETOOTH_CONNECT` | 31 (Android 12) | Bluetooth audio routing |
| `POST_NOTIFICATIONS` | 33 (Android 13) | Foreground service notification |