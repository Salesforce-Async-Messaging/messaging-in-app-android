package com.salesforce.android.smi.messaging.features.ui.replacement.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun TextMessageReplacementEntry(
    isLocal: Boolean,
    text: String?
) {
    val shape = RoundedCornerShape(16)

    MessageContainer(isLocal = isLocal) {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .border(shape = shape, width = 1.dp, color = Color.Green),
            shape = shape
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .wrapContentSize(),
            ) {
                Text(
                    text = text ?: "",
                    color = Color.Green,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun TextMessageReplacementEntryPreview_Outbound() {
    TextMessageReplacementEntry(isLocal = true, text = "Test local message")
}

@Preview
@Composable
private fun TextMessageReplacementEntryPreview_Inbound() {
    TextMessageReplacementEntry(isLocal = false, text = "Test remote message")
}
