package com.salesforce.android.smi.messaging

import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.ui.UIClient

interface MessagingClients {
    val uiClient: UIClient
    val coreClient: CoreClient
    val conversationClient: ConversationClient
}
