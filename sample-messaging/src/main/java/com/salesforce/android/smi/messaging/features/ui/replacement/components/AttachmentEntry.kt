package com.salesforce.android.smi.messaging.features.ui.replacement.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
internal fun AttachmentMessageReplacementEntry(
    isLocal: Boolean,
    file: File
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
                    .height(100.dp)
                    .width(150.dp)
                    .wrapContentSize(),
            ) {
                AsyncImage(model = file, contentDescription = "")
            }
        }
    }
}

@Preview
@Composable
private fun AttachmentMessageReplacementEntryPreview_Outbound() {
    AttachmentMessageReplacementEntry(
        isLocal = true,
        file = File("https://static.scientificamerican.com/sciam/cache/file/C37174A9-488C-49D4-B9C79B4E8EB39287_source.jpg?w=1200")
    )
}

@Preview
@Composable
private fun AttachmentMessageReplacementEntryPreview_Inbound() {
    AttachmentMessageReplacementEntry(
        isLocal = false,
        file = File("https://static.scientificamerican.com/sciam/cache/file/C37174A9-488C-49D4-B9C79B4E8EB39287_source.jpg?w=1200")
    )
}
