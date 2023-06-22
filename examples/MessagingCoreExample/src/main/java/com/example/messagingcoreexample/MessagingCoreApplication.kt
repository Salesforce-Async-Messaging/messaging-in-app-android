package com.example.messagingcoreexample

import android.app.Application
import com.salesforce.android.smi.core.Configuration
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.CoreConfiguration
import java.util.logging.Level

class MessagingCoreApplication : Application() {

    /**
     * Creates a core client instance.
     */
    fun coreClient(): CoreClient {
        // Build a configuration from the config file that you downloaded from Salesforce
        val coreConfig: Configuration = CoreConfiguration.fromFile(this, "configFile.json")

        // Adjust the log level
        CoreClient.Companion.setLogLevel(Level.INFO)

        // Return a new core client object from this config
        return CoreClient.Factory.create(this, coreConfig)
    }
}