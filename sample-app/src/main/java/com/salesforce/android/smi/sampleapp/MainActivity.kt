package com.salesforce.android.smi.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.features.core.salesforceAuthentication.PassthroughUserVerification
import com.salesforce.android.smi.messaging.features.core.salesforceAuthentication.SampleSalesforceSDKManager
import com.salesforce.android.smi.sampleapp.ui.theme.SampleappTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleappTheme {
                MainScreen(true) // todo default false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(enableSalesforceAuth: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Sample App") },
                actions = {
                    IconButton(onClick = {
                        val salesforceMessaging = SalesforceMessaging(context)

                        if (enableSalesforceAuth && activity != null) {
                            val salesforceSDKManager = SampleSalesforceSDKManager.getInstance(salesforceMessaging.coreClient) { activity }
                            salesforceMessaging.coreClient.registerUserVerificationProvider(
                                PassthroughUserVerification(salesforceSDKManager)
                            )
                        }

                        salesforceMessaging.uiClient.openConversationActivity(context)
                    }) {
                        Icon(Icons.AutoMirrored.Default.Send, Icons.AutoMirrored.Default.Send.name)
                    }
                    IconButton(onClick = { scope.launch { CoreClient.clearStorage(context) } }) {
                        Icon(Icons.Default.Delete, Icons.Default.Delete.name)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(4.dp)
        ) {
            Greeting(name = "Android")
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier
) {
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
