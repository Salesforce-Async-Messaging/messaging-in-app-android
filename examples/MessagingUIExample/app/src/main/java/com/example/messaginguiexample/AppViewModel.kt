package com.example.messaginguiexample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.CoreConfiguration
import com.salesforce.android.smi.core.PreChatValuesProvider
import com.salesforce.android.smi.core.TemplatedUrlValuesProvider
import com.salesforce.android.smi.core.events.CoreEvent
import com.salesforce.android.smi.network.api.auth.UserVerificationProvider
import com.salesforce.android.smi.network.api.auth.UserVerificationToken
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.ConversationEntryType
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
import com.salesforce.android.smi.network.data.domain.prechat.PreChatField
import com.salesforce.android.smi.network.data.domain.webview.TemplatedWebView
import com.salesforce.android.smi.ui.UIConfiguration
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val logger = Logger.getLogger(TAG)
    var conversationId = UUID.randomUUID()
    var uiConfig: UIConfiguration? = null

    fun setupMessaging(config: UIConfiguration) {
        CoreClient.setLogLevel(Level.ALL)
        logEvents(config)
        registerHiddenPreChatValuesProvider(config)
        registerTemplatedUrlValuesProvider(config)

        // Note: this will only be used if you also set the isUserVerification flag to true in your CoreConfig object.
        registerUserVerificationProvider(config)
    }

    /**
     * Initializes the configuration object for Messaging for In-App
     */
     fun resetMessagingConfig() {

        logger.log(Level.INFO, "Initializing config file.")

        // TO DO Set this value to true if using a userVerificationProvider, otherwise false
        val isUserVerificationEnabled = false

        // TO DO: Replace the config file in this app (assets/configFile.json)
        //        with the config file you downloaded from your Salesforce org.
        //        To learn more, see https://help.salesforce.com/s/articleView?id=sf.miaw_deployment_mobile.htm
        val coreConfig = CoreConfiguration
            .fromFile(getApplication(), "configFile.json", isUserVerificationEnabled)

        // Create a new conversation
        // This code uses a random UUID for the conversation ID, but
        // be sure to use the same ID to persist the same conversation.

        uiConfig = UIConfiguration(coreConfig, conversationId).also {
            // Optionally log events
           setupMessaging(it)
        }

        logger.log(Level.INFO, "Config created using conversation ID $conversationId")
    }

    private fun registerHiddenPreChatValuesProvider(config: UIConfiguration) {
        coreClient(config).registerHiddenPreChatValuesProvider(object : PreChatValuesProvider {

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

    private fun registerTemplatedUrlValuesProvider(config: UIConfiguration) {
        coreClient(config).registerTemplatedUrlValuesProvider(object : TemplatedUrlValuesProvider {

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

    private fun registerUserVerificationProvider(config: UIConfiguration) {
        coreClient(config).registerUserVerificationProvider(object : UserVerificationProvider {

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

    private fun logEvents(config: UIConfiguration) {
        viewModelScope.launch {
            conversationClient(config).events
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

    private fun coreClient(config: UIConfiguration) =
        CoreClient.Factory.create(getApplication(), config)

    private fun conversationClient(config: UIConfiguration) =
        coreClient(config).conversationClient(config.conversationId)

    private fun logEntry(type: ConversationEntryType, msg: String? = null) {
        logger.log(Level.INFO, "Entry | $type | $msg")
    }

    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            modelClass.getConstructor(Application::class.java).newInstance(application)
    }

    companion object {
        val TAG: String = AppViewModel::class.java.name
    }
}
