package com.salesforce.android.smi.messaging.features.ui.replacement.components

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
internal fun DateBreakHeaderReplacementEntry(
    timestamp: Long
) {
    val shape = RoundedCornerShape(16)
    Row(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(shape = shape, width = 1.dp, color = Color.Green),
            shape = shape
        ) {
            Text(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                text = DateUtils.formatElapsedTime(timestamp), color = Color.Green
            )
        }
    }
}

@Preview
@Composable
private fun DateBreakHeaderReplacementEntryPreview_Outbound() {
    DateBreakHeaderReplacementEntry(
        timestamp = 1568205860792
    )
}
