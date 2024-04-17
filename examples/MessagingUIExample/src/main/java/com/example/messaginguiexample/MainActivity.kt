package com.example.messaginguiexample

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.messaginguiexample.databinding.ActivityMainBinding
import com.example.messaginguiexample.viewmodel.AppViewModel
import com.salesforce.android.smi.ui.UIClient
import java.util.logging.Level
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel: AppViewModel by viewModels()
    var uiClient: UIClient? = null
    private val logger = Logger.getLogger(TAG)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.getString("conversation_id")?.let { conversationID ->
            logger.log(Level.INFO, "Push notification with conversationId: $conversationID")
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Initialize Messaging for In-App configuration object
        viewModel.resetMessagingConfig()

        binding.fab.setOnClickListener { _ ->
            this.showMessagingUI()
        }
    }

    /**
     * This ensures that onCreate is called when the activity is opened
     * through a push notification with intent.
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    /**
     * Shows the UI for Messaging for In-App
     */
    private fun showMessagingUI() {
        // Create a UI client
        if (viewModel.uiConfig != null) {
            uiClient = UIClient.Factory.create(viewModel.uiConfig!!)
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