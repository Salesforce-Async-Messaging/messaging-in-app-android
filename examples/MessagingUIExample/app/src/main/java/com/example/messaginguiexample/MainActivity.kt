package com.example.messaginguiexample

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.messaginguiexample.databinding.ActivityMainBinding
import com.salesforce.android.smi.core.CoreConfiguration
import com.salesforce.android.smi.ui.UIClient
import com.salesforce.android.smi.ui.UIConfiguration
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var uiConfig: UIConfiguration? = null
    var uiClient: UIClient? = null
    private val logger = Logger.getLogger(TAG)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Initialize Messaging for In-App configuration object
        this.resetMessagingConfig()

        binding.fab.setOnClickListener { view ->
            this.showMessagingUI()
        }
    }

    /**
     * Initializes the configuration object for Messaging for In-App
     */
    private fun resetMessagingConfig() {

        logger.log(Level.INFO, "Initializing config file.")

        // TO DO Set this value to true if using a userVerificationProvider, otherwise false
        val isUserVerificationEnabled = false

        // TO DO: Replace the config file in this app (assets/configFile.json)
        //        with the config file you downloaded from your Salesforce org.
        //        To learn more, see https://help.salesforce.com/s/articleView?id=sf.miaw_deployment_mobile.htm
        val coreConfig = CoreConfiguration
            .fromFile(this, "configFile.json", isUserVerificationEnabled)

        // Create a new conversation
        // This code uses a random UUID for the conversation ID, but
        // be sure to use the same ID to persist the same conversation.
        val conversationID = UUID.randomUUID()

        uiConfig = UIConfiguration(coreConfig, conversationID).also {
            // Optionally log events
            (applicationContext as MessagingApp).viewModel.setupMessaging(it)
        }

        logger.log(Level.INFO, "Config created using conversation ID $conversationID")
    }

    /**
     * Shows the UI for Messaging for In-App
     */
    private fun showMessagingUI() {
        // Create a UI client
        if (uiConfig != null) {
            uiClient = UIClient.Factory.create(uiConfig!!)
        }

        // Show the UI
        if (uiClient != null) {
            uiClient!!.openConversationActivity(this)
        } else {
            logger.log(Level.INFO, "Unable to create a conversation for messaging.")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    companion object {
        val TAG: String = MainActivity::class.java.name
    }
}