package com.salesforce.android.smi.messaging

import android.content.Context
import com.salesforce.android.smi.core.Configuration
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.CoreConfiguration
import com.salesforce.android.smi.core.PreChatValuesProvider
import com.salesforce.android.smi.core.TemplatedUrlValuesProvider
import com.salesforce.android.smi.core.UserVerificationProvider
import com.salesforce.android.smi.messaging.features.core.HiddenPreChat
import com.salesforce.android.smi.messaging.features.core.TemplatedUrlValues
import com.salesforce.android.smi.messaging.features.core.UserVerification
import com.salesforce.android.smi.messaging.features.ui.PopulatePreChat
import com.salesforce.android.smi.messaging.features.ui.replacement.OverridableUI
import com.salesforce.android.smi.ui.UIClient
import com.salesforce.android.smi.ui.UIConfiguration
import java.util.UUID

class SalesforceMessaging(
    context: Context,
    conversationId: UUID = UUID.randomUUID(),
    configuration: Configuration = CoreConfiguration.fromFile(
        context,
        isUserVerificationRequired = false,
        remoteLocaleMap = mapOf("en-CA" to "en", "fr-Fr" to "fr", "fr-CH" to "fr", "default" to "en")
    ),
    uiConfiguration: UIConfiguration = UIConfiguration(configuration, conversationId),
    populatePopulatePreChat: PopulatePreChat = PopulatePreChat(),
    hiddenPreChat: PreChatValuesProvider = HiddenPreChat(),
    templatedUrls: TemplatedUrlValuesProvider = TemplatedUrlValues(),
    userVerification: UserVerificationProvider = UserVerification(),
    overridableUI: OverridableUI = OverridableUI()
) : MessagingClients {
    override val uiClient = UIClient.Factory.create(uiConfiguration).apply {
        preChatFieldValueProvider = populatePopulatePreChat::populate
    }

    override val coreClient: CoreClient = uiClient.coreClient(context.applicationContext).apply {
        registerHiddenPreChatValuesProvider {
            hiddenPreChat.setValues(it)
        }
        registerTemplatedUrlValuesProvider {
            templatedUrls.setValues(it)
        }

        if (uiClient.configuration.isUserVerificationRequired) {
            registerUserVerificationProvider {
                userVerification.userVerificationChallenge(it)
            }
        }
    }

    override val conversationClient: ConversationClient = coreClient.conversationClient(conversationId).apply {
        uiClient.viewComponents = overridableUI.CustomViewComponents(this@SalesforceMessaging)
    }
}
