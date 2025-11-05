package com.salesforce.android.smi.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.lifecycleScope
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.features.components.MessagingSessionWidget
import com.salesforce.android.smi.sampleapp.ui.theme.SampleAppTheme
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleAppTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var conversationId: UUID by remember { mutableStateOf(UUID.randomUUID()) }
    val salesforceMessaging = remember(conversationId) {
        SalesforceMessaging(context, conversationId = conversationId)
    }

    var maintainEventStream: Boolean by remember { mutableStateOf(false) }

    LifecycleStartEffect(Unit) {
        maintainEventStream = false
        salesforceMessaging.coreClient.start(this.lifecycleScope)
        onStopOrDispose {
            if (!maintainEventStream) salesforceMessaging.coreClient.stop()
        }
    }

    val openConversation: () -> Unit = {
        maintainEventStream = true
        salesforceMessaging.uiClient.openConversationActivity(context)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Sample App") },
                actions = {
                    IconButton(
                        onClick = { openConversation() },
                        content = { Icon(Icons.AutoMirrored.Default.Chat, Icons.AutoMirrored.Default.Chat.name) }
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                CoreClient.clearStorage(context)
                                conversationId = UUID.randomUUID()
                            }
                        },
                        content = { Icon(Icons.Default.Delete, Icons.Default.Delete.name) }
                    )
                }
            )
        },
        floatingActionButton = {
            MessagingSessionWidget(salesforceMessaging.conversationClient, openConversation)
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
    SampleAppTheme {
        MainScreen()
    }
}
