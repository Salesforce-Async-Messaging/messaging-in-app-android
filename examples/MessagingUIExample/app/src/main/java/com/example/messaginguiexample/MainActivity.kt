package com.example.messaginguiexample

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
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
    private val viewModel: AppViewModel by viewModels()
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
        viewModel.resetMessagingConfig()

        binding.fab.setOnClickListener { view ->
            this.showMessagingUI()
        }
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