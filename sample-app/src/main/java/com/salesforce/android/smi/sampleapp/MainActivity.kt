package com.salesforce.android.smi.sampleapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
import com.salesforce.android.smi.sampleapp.ui.theme.SampleappTheme
import kotlinx.coroutines.launch

/**
 * Interface for handling WebView format messages
 */
interface WebViewFormatHandler {
    fun handleWebViewFormatMessage(webViewFormat: StaticContentFormat.WebViewFormat)
}

class MainActivity : ComponentActivity(), WebViewFormatHandler {
    lateinit var salesforceMessaging: SalesforceMessaging
    private lateinit var conversationClient: ConversationClient
    private lateinit var webViewInterceptor: WebViewMessageInterceptor

    private val TAG = "MainActivity"

    override fun onDestroy() {
        super.onDestroy()
        // Clear the handler reference to prevent memory leaks
        webViewInterceptor.clearHandler()
        Log.d(TAG, "Activity destroyed, cleared WebViewFormatHandler reference")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create the interceptor first
        webViewInterceptor = WebViewMessageInterceptor()
        // Set the handler using the proper method to avoid memory leaks
        webViewInterceptor.setHandler(this)

        // Initialize SalesforceMessaging with custom UI components
        salesforceMessaging = SalesforceMessaging(
            context = applicationContext,
            overridableUI = webViewInterceptor
        )

        conversationClient = salesforceMessaging.conversationClient

        setContent {
            SampleappTheme {
                MainScreen()
            }
        }
    }

    /**
     * Handle WebViewFormat messages
     */

    private fun openUrlInExternalBrowser(context: Context, url: String) {
        // Ensure the URL has a scheme (http or https)
        val webpageUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "http://$url"
        } else {
            url
        }

        val webpage: Uri = webpageUrl.toUri()
        val intent = Intent(Intent.ACTION_VIEW, webpage)

        // Verify that there is an application available to handle this intent
        // This is important because if no browser is installed, the app will crash.
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where no browser is installed or no app can handle the URL
            Toast.makeText(context, "No application can handle this request. Please install a web browser.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    override fun handleWebViewFormatMessage(webViewFormat: StaticContentFormat.WebViewFormat) {
        Log.d(TAG, "WebViewFormat message received: ${webViewFormat.url}")
        openUrlInExternalBrowser(this, webViewFormat.url)
    }

    // WebViewMessageInterceptor moved to its own file
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as? MainActivity

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Sample App with WebView Interceptor") },
                actions = {
                    IconButton(onClick = {
                        // Use the activity's existing SalesforceMessaging instance if available
                        if (activity != null) {
                            activity.salesforceMessaging.uiClient.openConversationActivity(context)
                        } else {
                            // Fallback to creating a new instance if needed
                            SalesforceMessaging(context.applicationContext)
                                .uiClient.openConversationActivity(context)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Default.Send, Icons.AutoMirrored.Default.Send.name)
                    }
                    IconButton(onClick = { scope.launch { CoreClient.clearStorage(context) } }) {
                        Icon(Icons.Default.Delete, Icons.Default.Delete.name)
                    }
                })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(4.dp)
        ) {
            Greeting(name = "WebView messages will be intercepted")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SampleappTheme {
        MainScreen()
    }
}
