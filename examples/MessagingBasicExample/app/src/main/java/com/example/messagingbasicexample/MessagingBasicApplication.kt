package com.example.messagingbasicexample

import android.app.Application
import com.salesforce.android.smi.core.Configuration
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.CoreConfiguration
import java.util.logging.Level

class MessagingBasicApplication : Application() {
    fun coreClient(): CoreClient {
        val coreConfig: Configuration = CoreConfiguration.fromFile(this, "configFile.json")
        CoreClient.Companion.setLogLevel(Level.INFO)
        return CoreClient.Factory.create(this, coreConfig)
    }
}