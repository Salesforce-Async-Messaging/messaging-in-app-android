package com.salesforce.android.smi.messaging.features.voice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.salesforce.android.smi.messaging.features.voice.components.VoiceBottomSheetExpanded
import com.salesforce.android.smi.messaging.features.voice.components.VoiceBottomSheetMinimized
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VoiceBottomSheetContent(
    sheetState: SheetState,
    session: MultimediaSession,
    onExpand: () -> Unit,
    onEndCall: () -> Unit
) {
    when (sheetState.currentValue) {
        SheetValue.PartiallyExpanded -> {
            VoiceBottomSheetMinimized(
                session = session,
                onExpand = onExpand,
                onEndCall = onEndCall
            )
        }
        SheetValue.Expanded -> {
            VoiceBottomSheetExpanded(
                session = session,
                onEndCall = onEndCall
            )
        }
        SheetValue.Hidden -> {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}
