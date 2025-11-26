package com.salesforce.android.smi.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.samples.components.MessagingBottomSheet
import com.salesforce.android.smi.messaging.samples.components.MessagingButton
import com.salesforce.android.smi.messaging.samples.components.MessagingConversationList
import com.salesforce.android.smi.messaging.samples.components.MessagingWidget
import com.salesforce.android.smi.messaging.samples.state.LifecycleResumeMessagingStreamEffect
import com.salesforce.android.smi.sampleapp.common.ReadOnlyTextField
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

    // Messaging configuration
    var conversationId: UUID by rememberSaveable { mutableStateOf(UUID.randomUUID()) }
    val salesforceMessaging = remember(conversationId) {
        SalesforceMessaging(context, conversationId = conversationId)
    }

    // Sample options
    var fullScreen: Boolean by rememberSaveable { mutableStateOf(false) }
    var openOnSelection: Boolean by rememberSaveable { mutableStateOf(false) }
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val openConversation = {
        when (fullScreen) {
            true -> salesforceMessaging.uiClient.openConversationActivity(context)
            false -> openBottomSheet = true
        }
    }

    // Fullscreen event stream can be handled here where the chat UI is launched in a separate activity.
    if (fullScreen) LifecycleResumeMessagingStreamEffect(salesforceMessaging.coreClient)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Sample App") },
                actions = {
                    MessagingButton(salesforceMessaging.conversationClient, onOpen = openConversation)
                    OptionsMenu(
                        fullScreen,
                        openOnSelection,
                        { fullScreen = !fullScreen },
                        { openOnSelection = !openOnSelection }
                    )
                }
            )
        },
        floatingActionButton = {
            MessagingWidget(salesforceMessaging.conversationClient, onOpen = openConversation)
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadOnlyTextField(
                    "Conversation ID",
                    "$conversationId",
                    leadingIcon = Icons.Default.Replay
                ) {
                    conversationId = UUID.randomUUID()
                }

                MessagingConversationList(salesforceMessaging.coreClient) {
                    conversationId = it.identifier
                    if (openOnSelection) openConversation()
                }
            }
        }

        MessagingBottomSheet(salesforceMessaging.coreClient, salesforceMessaging.uiClient, openBottomSheet = openBottomSheet) {
            openBottomSheet = it
        }
    }
}

@Composable
private fun OptionsMenu(
    fullscreen: Boolean,
    openOnSelection: Boolean,
    onUpdateFullscreen: () -> Unit,
    onUpdateSelection: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { isExpanded = !isExpanded },
        content = {
            Icon(Icons.Default.Settings, Icons.Default.Settings.name)
            DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Fullscreen activity") },
                    leadingIcon = {
                        val icon = if (fullscreen) Icons.Default.Check else Icons.Default.Close
                        Icon(icon, contentDescription = icon.name)
                    },
                    onClick = {
                        onUpdateFullscreen()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Open on selection") },
                    leadingIcon = {
                        val icon = if (openOnSelection) Icons.Default.Check else Icons.Default.Close
                        Icon(icon, contentDescription = icon.name)
                    },
                    onClick = {
                        onUpdateSelection()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Clear storage") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = Icons.Default.Delete.name) },
                    onClick = {
                        coroutineScope.launch {
                            CoreClient.clearStorage(context, true)
                        }
                    }
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SampleAppTheme {
        MainScreen()
    }
}
