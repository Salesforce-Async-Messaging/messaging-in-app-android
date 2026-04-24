package com.salesforce.android.smi.messaging.features.voice

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.salesforce.android.smi.messaging.features.voice.components.VoiceBottomSheetExpanded
import com.salesforce.android.smi.messaging.features.voice.components.VoiceBottomSheetMinimized
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSession
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSessionStatus
import kotlinx.coroutines.launch

/**
 * A modal bottom sheet that displays the voice call UI.
 * Automatically shows/hides based on session status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VoiceBottomSheet(
    session: MultimediaSession?,
    conversationClient: com.salesforce.android.smi.core.ConversationClient,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Manage sheet visibility based on session status
    LaunchedEffect(session?.status) {
        when (session?.status) {
            MultimediaSessionStatus.Created,
            MultimediaSessionStatus.Connecting,
            MultimediaSessionStatus.Connected,
            MultimediaSessionStatus.Answered -> {
                showSheet = true
                isExpanded = false // Start minimized
            }
            MultimediaSessionStatus.Disconnected,
            MultimediaSessionStatus.Ended,
            null -> {
                showSheet = false
                isExpanded = false
            }
            MultimediaSessionStatus.Reconnecting -> {
                // Keep existing state, don't hide
            }
        }
    }

    // Handle sheet state changes - if user swipes down, minimize instead of dismiss
    LaunchedEffect(sheetState.currentValue) {
        if (sheetState.currentValue == SheetValue.Hidden && isExpanded) {
            // User swiped down on expanded sheet - minimize instead
            isExpanded = false
            scope.launch {
                sheetState.show()
            }
        }
    }

    if (showSheet && session != null) {
        ModalBottomSheet(
            onDismissRequest = {
                if (isExpanded) {
                    // Don't dismiss, just minimize
                    isExpanded = false
                    scope.launch {
                        sheetState.show()
                    }
                } else {
                    // Minimized sheet dismissed - end call
                    onEndCall()
                }
            },
            sheetState = sheetState,
            modifier = modifier
        ) {
            if (isExpanded) {
                VoiceBottomSheetExpanded(
                    session = session,
                    conversationClient = conversationClient,
                    onEndCall = {
                        onEndCall()
                    }
                )
            } else {
                VoiceBottomSheetMinimized(
                    session = session,
                    onExpand = {
                        isExpanded = true
                    },
                    onEndCall = {
                        onEndCall()
                    }
                )
            }
        }
    }
}
