package com.example.messagingcoreexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import com.example.messagingcoreexample.MessagingCoreApplication
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.PreChatValuesProvider
import com.salesforce.android.smi.core.TemplatedUrlValuesProvider
import com.salesforce.android.smi.core.data.domain.businessHours.BusinessHoursInfo
import com.salesforce.android.smi.core.events.CoreEvent
import com.salesforce.android.smi.core.events.CoreEvent.ConversationEvent
import com.salesforce.android.smi.network.api.auth.UserVerificationProvider
import com.salesforce.android.smi.network.api.auth.UserVerificationToken
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.ConversationEntryType
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
import com.salesforce.android.smi.network.data.domain.prechat.PreChatField
import com.salesforce.android.smi.network.data.domain.webview.TemplatedWebView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * View model for this Messaging for In-App Core SDK example.
 */
class MessagingViewModel(
    private val coreClient: CoreClient
    ) : ViewModel() {
    // Conversation client instance.
    // By default, this object uses a random UUID for the conversation ID, but
    // be sure to use the same ID to persist the same conversation.
    private val conversationClient = coreClient.conversationClient(conversationId)
    private val logger = Logger.getLogger(TAG)
    private val _businessHours: MutableStateFlow<BusinessHoursInfo?> = MutableStateFlow(null)
    val businessHours: StateFlow<BusinessHoursInfo?> = _businessHours

    // Paged instance of the messaging conversation entries
    val conversationEntries =
        conversationClient.conversationEntriesPaged()
            .filterIsInstance<Result.Success<PagingData<ConversationEntry>>>().map {
                it.data
            }

    // Filtered instance of all typing indicator events
    val typingEvents =
        conversationClient.events.filterIsInstance<ConversationEvent.TypingIndicator>()

    fun setupMessaging() {
        CoreClient.setLogLevel(Level.ALL)
        logEvents()
        registerHiddenPreChatValuesProvider()
        registerTemplatedUrlValuesProvider()

        // Note: this will only be used if you also set the isUserVerification flag to true in your CoreConfig object.
        registerUserVerificationProvider()
    }

    // Starts the Messaging core client stream
    fun startStream() = coreClient.start(viewModelScope)

    // Sends a text message to the agent
    fun sendTextMessage(message: String) = viewModelScope.launch {
        conversationClient.sendMessage(message)
    }

    // This is an example of how you would get the Pre Chat values from the config and submitting
    // those values. This example does not have a UI component built for it, you would need to
    // create one so the user can provider values for your Pre Chat fields.
     fun handlePreChat() = viewModelScope.launch {
        val remoteConfig = coreClient.retrieveRemoteConfiguration()

        (remoteConfig as? Result.Success)?.data?.forms?.get(0)?.let { form ->
            for (field in form.formFields) {
                if ( field.required) {
                    field.userInput = "value"
                }
            }

            val hiddenPreChatFields: MutableList<PreChatField> = mutableListOf()

            // Submitting the Pre Chat data after setting the values on the Pre Chat fields.
            conversationClient.submitPreChatData(form.formFields, hiddenPreChatFields, true)
        }
    }

    // This retrieves business hours from the service so they an be used to check if a time is within
    // the current business hours.
    suspend fun checkIfInBusinessHours(): Boolean? {
        val result = coreClient.retrieveBusinessHours()
        val businessHoursInfo: BusinessHoursInfo? = (result as? Result.Success)?.data
        return businessHoursInfo?.isWithinBusinessHours()
    }

    // Registers the hidden Pre Chat provider. For your implementation you would need to set the
    // expected hidden Pre Chat values from your org configuration to values from your application.
    private fun registerHiddenPreChatValuesProvider() {
        coreClient.registerHiddenPreChatValuesProvider(object : PreChatValuesProvider {

            // Invoked automatically when hidden pre-chat fields are being sent
            override suspend fun setValues(input: List<PreChatField>): List<PreChatField> {
                return input.onEach {
                    when (it.name) {
                        "<YOUR_HIDDEN_FIELD1>" -> it.userInput = "<YOUR_HIDDEN_VALUE1>"
                        "<YOUR_HIDDEN_FIELD2>" -> it.userInput = "<YOUR_HIDDEN_VALUE2>"
                    }
                }
            }
        })
    }

    // This registers the templated url parameters you have configured in your setup to values in
    // your application.
    private fun registerTemplatedUrlValuesProvider() {
        coreClient.registerTemplatedUrlValuesProvider(object : TemplatedUrlValuesProvider {

            // Invoked automatically when values are needed for a given TemplatedWebView
            override suspend fun setValues(input: TemplatedWebView): TemplatedWebView {
                return input.apply {
                    // Set 0 or more path/query parameter values
                    setPathParameterValue("<YOUR_PATH_KEY>", "<YOUR_PATH_VALUE>")
                    setQueryParameterValue("<YOUR_QUERY_KEY>", "<YOUR_QUERY_VALUE>")
                }
            }
        })
    }

    // This is used to set the verification provider information in order to support user
    // verification.
    private fun registerUserVerificationProvider() {
        coreClient.registerUserVerificationProvider(object : UserVerificationProvider {

            // Invoked automatically when credentials are required for authorizing a verified user
            override suspend fun userVerificationChallenge(reason: UserVerificationProvider.ChallengeReason): UserVerificationToken {
                logger.log(Level.INFO, "User Verification: $reason")

                val token = when (reason) {
                    UserVerificationProvider.ChallengeReason.INITIAL -> {
                        // Salesforce doesn't currently have your customer identity token.
                        // Please provide one now.
                        "<YOUR_CUSTOMER_IDENTITY_TOKEN>"
                    }
                    UserVerificationProvider.ChallengeReason.RENEW -> {
                        // Salesforce needs to renew this user's authorization token.
                        // Please provide a customer identity token.
                        // Note: If your current token is nearing expiry, it may make sense to issue a new token at this time.
                        "<YOUR_CUSTOMER_IDENTITY_TOKEN>"
                    }
                    UserVerificationProvider.ChallengeReason.EXPIRED -> {
                        // The current customer identity token you've provided has expired.
                        // Please provide a newly issued customer identity token.
                        "<YOUR_NEW_CUSTOMER_IDENTITY_TOKEN>"
                    }
                    UserVerificationProvider.ChallengeReason.MALFORMED -> {
                        // Something is wrong with the token you provided.
                        // Log an error and perhaps retry with a newly issued customer identity token.
                        "<YOUR_CORRECTED_CUSTOMER_IDENTITY_TOKEN>"
                    }
                }
                return UserVerificationToken(UserVerificationToken.UserVerificationType.JWT, token)
            }
        })
    }


    private fun logEvents() {
        viewModelScope.launch {
            conversationClient.events
                .filterIsInstance<CoreEvent.ConversationEvent.Entry>()
                .map { it.conversationEntry.payload }
                .onEach {
                    when (it) {
                        is EntryPayload.MessagePayload -> when (val content = it.content) {
                            is StaticContentFormat.TextFormat ->
                                logEntry(it.entryType, content.text)
                            is ChoicesFormat.DisplayableOptionsFormat ->
                                logEntry(it.entryType, content.optionItems.toString())
                            else ->
                                logEntry(it.entryType, content.formatType.toString())
                        }
                        else -> logEntry(it.entryType)
                    }
                }.collect()
        }
    }

    private fun logEntry(type: ConversationEntryType, msg: String? = null) {
        logger.log(Level.INFO, "Entry | $type | $msg")
    }

    companion object {
        val TAG: String = MessagingViewModel::class.java.name
        var conversationId: UUID = UUID.randomUUID()
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val coreClient = (this[APPLICATION_KEY] as MessagingCoreApplication).coreClient()
                MessagingViewModel(coreClient)
            }
        }
    }
}