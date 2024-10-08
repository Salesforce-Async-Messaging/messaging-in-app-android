package com.salesforce.android.smi.messaging.features.ui.replacement.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessageContainer(isLocal: Boolean, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isLocal) 64.dp else 0.dp,
                end = if (isLocal) 0.dp else 64.dp,
            ),
        horizontalArrangement = if (isLocal) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
        content = content
    )
}
