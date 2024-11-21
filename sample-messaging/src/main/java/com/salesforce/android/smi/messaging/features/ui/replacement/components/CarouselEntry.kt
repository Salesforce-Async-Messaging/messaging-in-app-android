package com.salesforce.android.smi.messaging.features.ui.replacement.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.imageLoader
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.attachment.FileAsset

@Composable
internal fun CarouselMessageReplacementEntry(
    isLocal: Boolean,
    list: List<FileAsset.ImageAsset.CarouselImage>
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(16)

    MessageContainer(isLocal = isLocal) {
        Card(
            modifier = Modifier
                .wrapContentWidth()
                .border(shape = shape, width = 1.dp, color = Color.Green),
            shape = shape
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(100.dp)
                    .width(350.dp)
                    .wrapContentSize(),
            ) {
                Row(Modifier.fillMaxWidth()) {
                    list.forEach {
                        AsyncImage(
                            imageLoader = context.imageLoader,
                            modifier = Modifier
                                .height(75.dp)
                                .width(75.dp)
                                .padding(4.dp),
                            model = it.file,
                            contentDescription = ""
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CarouselMessageReplacementEntryPreview_Outbound() {
    CarouselMessageReplacementEntry(
        isLocal = true,
        list = listOf(
            FileAsset.ImageAsset.CarouselImage(
                "id",
                "id",
                "https://static.scientificamerican.com/sciam/cache/file/C37174A9-488C-49D4-B9C79B4E8EB39287_source.jpg?w=1200",
                "Test",
                "image/png"
            )
        )
    )
}
