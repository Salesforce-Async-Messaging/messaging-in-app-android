package com.salesforce.android.smi.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.samples.components.MessagingBottomSheet
import com.salesforce.android.smi.messaging.samples.components.MessagingButton
import com.salesforce.android.smi.messaging.samples.components.MessagingConversationList
import com.salesforce.android.smi.messaging.samples.components.MessagingWidget
import com.salesforce.android.smi.messaging.samples.state.MessagingStore
import com.salesforce.android.smi.sampleapp.common.ReadOnlyTextField
import com.salesforce.android.smi.sampleapp.common.rememberSampleOptions
import com.salesforce.android.smi.sampleapp.ui.theme.SampleAppTheme
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleAppTheme {
                var showMessaging by rememberSaveable { mutableStateOf(false) }
                if (showMessaging) {
                    MainScreen(onBack = { showMessaging = false })
                } else {
                    HomeScreen(onOpenMessaging = { showMessaging = true })
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onOpenMessaging: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.home_message),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = onOpenMessaging,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.home_open_messaging_button))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current

    // Messaging configuration
    var conversationId: UUID by rememberSaveable { mutableStateOf(UUID.randomUUID()) }
    val salesforceMessaging = remember(conversationId) {
        SalesforceMessaging(context, conversationId = conversationId)
    }

    // Single data entrypoint for this screen. Keyed on the app's CoreClient (a stable singleton), so
    // switching the active conversation does not tear it down — it keeps one shared flow per
    // conversation and dedups all observers.
    val storeScope = rememberCoroutineScope()
    val store = remember(salesforceMessaging.coreClient) {
        MessagingStore(salesforceMessaging.coreClient, storeScope)
    }

    // Resolve the active conversation from the inbox exactly once, and only after the conversation
    // list has finished loading. Until then the field shows a loading state and we observe nothing,
    // so no per-conversation entries request is made for a not-yet-decided id. Resolution:
    //   - inbox has a loadable (non-ended) conversation -> use the most recently active one
    //   - inbox is empty                                -> mint a fresh random id for a new
    //                                                       conversation (no entries request)
    val listState by store.listState.collectAsStateWithLifecycle()
    val latestConversationId by store.latestConversationId.collectAsStateWithLifecycle()
    var didResolveConversationId by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(listState.isLoading, latestConversationId) {
        if (!didResolveConversationId && !listState.isLoading) {
            conversationId = latestConversationId ?: UUID.randomUUID()
            didResolveConversationId = true
        }
    }

    // Sample options. Persisted across navigation / restarts by the sample-app scaffolding in
    // SampleOptions.kt (not part of the core messaging sample).
    val sampleOptions = rememberSampleOptions(context)
    val fullScreen = sampleOptions.options.fullScreen
    val openOnSelection = sampleOptions.options.openOnSelection
    val showWidget = sampleOptions.options.showWidget
    val showConversationList = sampleOptions.options.showConversationList
    val showIcon = sampleOptions.options.showIcon
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    // A conversation selected from the list that should be opened once [salesforceMessaging] (and its
    // uiClient) have been rebuilt for the new id. Opening in the same click would use the stale
    // uiClient still bound to the previous conversation, leaving the chat UI stuck loading.
    var pendingOpenConversationId: UUID? by remember { mutableStateOf(null) }
    val openConversation = {
        when (fullScreen) {
            true -> salesforceMessaging.uiClient.openConversationActivity(context)
            false -> openBottomSheet = true
        }
    }

    // Open only after the composition has caught up with the selected id, so uiClient matches.
    LaunchedEffect(conversationId, pendingOpenConversationId) {
        if (pendingOpenConversationId == conversationId) {
            pendingOpenConversationId = null
            openConversation()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Sample App") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (showIcon) {
                        MessagingButton(store, conversationId, onOpen = openConversation)
                    }
                    OptionsMenu(
                        fullScreen,
                        openOnSelection,
                        showWidget,
                        showConversationList,
                        showIcon,
                        { sampleOptions.setFullScreen(!fullScreen) },
                        { sampleOptions.setOpenOnSelection(!openOnSelection) },
                        { sampleOptions.setShowWidget(!showWidget) },
                        { sampleOptions.setShowConversationList(!showConversationList) },
                        { sampleOptions.setShowIcon(!showIcon) }
                    )
                }
            )
        },
        floatingActionButton = {
            if (showWidget) {
                MessagingWidget(store, conversationId, onOpen = openConversation)
            }
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
                    if (didResolveConversationId) "$conversationId" else "Loading conversations…",
                    leadingIcon = Icons.Default.Replay
                ) {
                    conversationId = UUID.randomUUID()
                }

                if (showConversationList) {
                    MessagingConversationList(store, selectedConversationId = conversationId) {
                        conversationId = it.identifier
                        if (openOnSelection) pendingOpenConversationId = it.identifier
                    }
                }
            }
        }

        MessagingBottomSheet(salesforceMessaging.uiClient, openBottomSheet = openBottomSheet) {
            openBottomSheet = it
        }
    }
}

@Composable
private fun OptionsMenu(
    fullscreen: Boolean,
    openOnSelection: Boolean,
    showWidget: Boolean,
    showConversationList: Boolean,
    showIcon: Boolean,
    onUpdateFullscreen: () -> Unit,
    onUpdateSelection: () -> Unit,
    onUpdateShowWidget: () -> Unit,
    onUpdateShowConversationList: () -> Unit,
    onUpdateShowIcon: () -> Unit
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
                    text = { Text("Show conversation list") },
                    leadingIcon = {
                        val icon = if (showConversationList) Icons.Default.Check else Icons.Default.Close
                        Icon(icon, contentDescription = icon.name)
                    },
                    onClick = {
                        onUpdateShowConversationList()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Show chat icon") },
                    leadingIcon = {
                        val icon = if (showIcon) Icons.Default.Check else Icons.Default.Close
                        Icon(icon, contentDescription = icon.name)
                    },
                    onClick = {
                        onUpdateShowIcon()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Show chat widget") },
                    leadingIcon = {
                        val icon = if (showWidget) Icons.Default.Check else Icons.Default.Close
                        Icon(icon, contentDescription = icon.name)
                    },
                    onClick = {
                        onUpdateShowWidget()
                    }
                )
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
fun HomeScreenPreview() {
    SampleAppTheme {
        HomeScreen(onOpenMessaging = {})
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SampleAppTheme {
        MainScreen()
    }
}
