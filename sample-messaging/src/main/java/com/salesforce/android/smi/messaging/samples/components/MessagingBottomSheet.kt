package com.salesforce.android.smi.messaging.samples.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.messaging.samples.state.LifecycleResumeMessagingStreamEffect
import com.salesforce.android.smi.ui.UIClient
import kotlinx.coroutines.launch

/**
 * Bottom sheet style presentation of the chat UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingBottomSheet(
    salesforceMessaging: SalesforceMessaging,
    modifier: Modifier = Modifier,
    maxHeight: Float = 0.9f,
    openBottomSheet: Boolean = false,
    updateOpenBottomSheet: (isOpen: Boolean) -> Unit
) {
    MessagingBottomSheet(
        salesforceMessaging.coreClient,
        salesforceMessaging.uiClient,
        modifier,
        maxHeight,
        openBottomSheet,
        updateOpenBottomSheet
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingBottomSheet(
    coreClient: CoreClient,
    uiClient: UIClient,
    modifier: Modifier = Modifier,
    maxHeight: Float = 0.9f,
    openBottomSheet: Boolean = false,
    updateOpenBottomSheet: (isOpen: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val sheetState: SheetState = rememberModalBottomSheetState(true)
    val dismissSheet: () -> Unit = {
        coroutineScope
            .launch { sheetState.hide() }
            .invokeOnCompletion {
                if (!sheetState.isVisible) {
                    updateOpenBottomSheet(false)
                }
            }
    }
    when (openBottomSheet) {
        true -> {
            ModalBottomSheet(
                modifier = modifier,
                sheetGesturesEnabled = false,
                onDismissRequest = { updateOpenBottomSheet(false) },
                sheetState = sheetState
            ) {
                Box(modifier.fillMaxHeight(maxHeight)) {
                    uiClient.MessagingInAppUI {
                        dismissSheet()
                    }
                }
            }
        }

        false -> {
            // Reconnect to the event stream when the chat modal leaves the composition, which stops the event stream within the chat UI.
            LifecycleResumeMessagingStreamEffect(coreClient)
        }
    }
}
