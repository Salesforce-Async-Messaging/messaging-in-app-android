# Module core

The primary entry point for the Messaging for In-App SDK. Provides `CoreClient` and `ConversationClient` for managing conversations, sending messages, handling authentication, and coordinating pre-chat flows. Use this module for headless integrations or as the foundation for custom UI implementations.

## Getting Started

Create a `CoreConfiguration` with your deployment values, then instantiate a `CoreClient`:

```kotlin
val configuration = CoreConfiguration(
    serviceAPI = URL("https://myorg.my.salesforce-scrt.com"),
    organizationId = "00D000000000001",
    developerName = "My_Messaging_Deployment"
)
val coreClient = CoreClient.Factory.create(context, configuration)
```

Alternatively, create the configuration from the JSON config file downloaded from Salesforce setup (Embedded Service Deployments > Get Code File):

```kotlin
val configuration = CoreConfiguration.fromFile(context)
val coreClient = CoreClient.Factory.create(context, configuration)
```

## Managing Conversations

Obtain a `ConversationClient` to interact with a specific conversation. Check for existing conversations first, and generate a new UUID only if none exist:

```kotlin
val result = coreClient.conversations(limit = 10, forceRefresh = true)
val conversationId = if (result is Result.Success) {
    result.data.firstOrNull()?.identifier ?: UUID.randomUUID()
} else {
    UUID.randomUUID()
}
val conversationClient = coreClient.conversationClient(conversationId)
```

To explicitly create a conversation on the server:

```kotlin
val result = coreClient.createConversation(conversationId)
```

**Important:** Always use a randomly generated UUID for new conversation IDs. Never use deterministic values (customer IDs, account numbers). For unverified users, reusing IDs after a reinstall or database clear with `clearAuthorization = true` causes HTTP 403 errors because the previous authorization token is lost, a new user identity is created, and the previous conversation ID becomes inaccessible to the new identity.

## Sending Messages

Use the `ConversationClient` to send text messages and file attachments:

```kotlin
val result = conversationClient.sendMessage("Hello, I need help with my order")
```

```kotlin
conversationClient.sendFile(file, message = "See attached")
```

Entries appear in the local conversation feed immediately and are delivered to the server asynchronously.

## Observing Events

Collect the `events` flow on `CoreClient` to observe real-time activity across all conversations:

```kotlin
scope.launch {
    coreClient.events.collect { event ->
        when (event) {
            is CoreEvent.ConversationEvent.Entry -> { /* New message received */ }
            is CoreEvent.ConversationEvent.ProgressIndicator -> { /* Agent is typing */ }
            is CoreEvent.Connection -> { /* Connection state changed */ }
            is CoreEvent.Error -> { /* Handle error */ }
            else -> {}
        }
    }
}
```

For conversation-scoped events, collect from `ConversationClient.events` instead.

## Pre-Chat Configuration

If your deployment uses pre-chat fields, retrieve the remote configuration and submit it when creating a conversation:

```kotlin
val config = coreClient.retrieveRemoteConfiguration()
if (config is Result.Success) {
    conversationClient.submitRemoteConfiguration(config.data)
}
```

To supply hidden pre-chat values (not visible to the user but used for routing), register a provider:

```kotlin
coreClient.registerHiddenPreChatValuesProvider { fields ->
    fields.onEach { field ->
        field.userInput = when (field.name) {
            "CustomerId" -> "customer-123"
            else -> ""
        }
    }
}
```

You can also fetch the remote configuration, modify hidden pre-chat field values directly, and then submit:

```kotlin
val configResult = coreClient.retrieveRemoteConfiguration()
if (configResult is Result.Success) {
    val remoteConfig = configResult.data
    remoteConfig.forms.forEach { form ->
        form.hiddenFormFields.forEach { field ->
            when (field.name) {
                "CustomerId" -> field.userInput = "customer-123"
                "Priority" -> field.userInput = "high"
            }
        }
    }
    conversationClient.submitRemoteConfiguration(remoteConfig)
}
```

## User Verification

For deployments that require authenticated users, set `isUserVerificationRequired = true` in your configuration and register a JWT provider:

```kotlin
coreClient.registerUserVerificationProvider { reason ->
    when (reason) {
        ChallengeReason.INITIAL -> UserVerificationToken.externalToken(fetchJwt())
        ChallengeReason.RENEW, ChallengeReason.EXPIRED -> UserVerificationToken.externalToken(renewJwt())
        ChallengeReason.MALFORMED -> UserVerificationToken.externalToken(fetchJwt())
    }
}
```

The provider is called whenever the SDK needs a token -- at initial connection, on token expiry, or if the previous token was rejected.

## Push Notifications

Register the device for push notifications by providing your FCM token from `FirebaseMessagingService`:

```kotlin
class MyFirebaseService : FirebaseMessagingService() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            CoreClient.provideDeviceToken(applicationContext, token)
        }
    }
}
```

The static `provideDeviceToken` method stores the token locally and defers registration until the first network request. If you have an active `CoreClient` instance and want to register immediately, use `coreClient.registerDevice(context, token)` instead.

Push notifications are delivered when the real-time connection is not active (i.e., when `stop()` has been called or the app is in the background).

## Lifecycle Management

Call `start` to open the real-time event connection and `stop` to close it. In Compose, use `LifecycleResumeEffect` to tie the connection to the screen lifecycle:

```kotlin
LifecycleResumeEffect(Unit) {
    coreClient.start(lifecycleScope)
    onPauseOrDispose { coreClient.stop() }
}
```

While started, events flow through `CoreClient.events` and push notifications are suppressed. When stopped, push notifications resume.

## Clearing Data

To clear all local SDK data (useful on user logout):

```kotlin
CoreClient.clearStorage(context)
```

**Warning:** The `clearAuthorization` parameter behaves differently depending on your verification mode:

- **Verified users** (`isUserVerificationRequired = true`): `clearAuthorization = true` is safe — the user can re-verify and regain access to their conversations.
- **Unverified users** (`isUserVerificationRequired = false`): `clearAuthorization = true` is **destructive** — the user permanently loses access to all conversations and a new user identity is created.

For unverified users, prefer `clearAuthorization = false` to preserve conversation access:

```kotlin
CoreClient.clearStorage(context, clearAuthorization = false)
```

To revoke the current auth token and optionally deregister from push:

```kotlin
coreClient.revokeToken(deregisterDevice = true)
```