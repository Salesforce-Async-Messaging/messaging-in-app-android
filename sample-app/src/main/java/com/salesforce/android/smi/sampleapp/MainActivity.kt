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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
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
import com.salesforce.android.smi.messaging.features.components.LifecycleResumeMessagingStreamEffect
import com.salesforce.android.smi.messaging.features.components.MessagingBottomSheet
import com.salesforce.android.smi.messaging.features.components.MessagingButton
import com.salesforce.android.smi.messaging.features.components.MessagingWidget
import com.salesforce.android.smi.sampleapp.common.LabeledCheckbox
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
    var fullScreen: Boolean by rememberSaveable { mutableStateOf(true) }
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val openConversation = {
        when (fullScreen) {
            true -> salesforceMessaging.uiClient.openConversationActivity(context)
            false -> openBottomSheet = true
        }
    }

    // Fullscreen event stream can be handled here where the chat UI is launched in a separate activity.
    if (fullScreen) LifecycleResumeMessagingStreamEffect(salesforceMessaging)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Sample App") },
                actions = {
                    MessagingButton(salesforceMessaging, onOpen = openConversation)
                    ClearButton()
                }
            )
        },
        floatingActionButton = {
            MessagingWidget(salesforceMessaging, enabled = !openBottomSheet, onOpen = openConversation)
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
                LabeledCheckbox("Fullscreen", fullScreen) {
                    fullScreen = it
                }
            }
        }

        MessagingBottomSheet(salesforceMessaging, openBottomSheet = openBottomSheet) {
            openBottomSheet = it
        }
    }
}

@Composable
private fun ClearButton() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    IconButton(
        onClick = {
            coroutineScope.launch {
                CoreClient.clearStorage(context)
            }
        },
        content = { Icon(Icons.Default.Delete, Icons.Default.Delete.name) }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SampleAppTheme {
        MainScreen()
    }
}
