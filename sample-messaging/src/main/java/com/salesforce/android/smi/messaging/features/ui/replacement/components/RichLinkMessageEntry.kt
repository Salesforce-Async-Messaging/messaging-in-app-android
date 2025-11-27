package com.salesforce.android.smi.messaging.features.ui.replacement.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
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
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.titleItem.TitleItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.richLink.LinkItem

@Composable
internal fun RichLinkMessageEntry(
    isLocal: Boolean,
    linkItem: LinkItem,
    image: FileAsset.ImageAsset.RichLinkImage?
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
            Column(
                modifier = Modifier.wrapContentWidth()
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = linkItem.titleItem.title, color = Color.Green)
                    Text(text = linkItem.url, color = Color.Green)
                    linkItem.titleItem.subTitle?.let { Text(text = it, color = Color.Green) }
                }
                image?.let {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            imageLoader = context.imageLoader,
                            modifier = Modifier
                                .height(150.dp)
                                .width(250.dp),
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
private fun RichLinkMessageReplacementEntryPreview_Outbound() {
    RichLinkMessageEntry(
        isLocal = true,
        linkItem = LinkItem(
            "www.slack.com",
            TitleItem.TitleLinkItem("Title", "Subtitle")
        ),
        image = FileAsset.ImageAsset.RichLinkImage(
            "id",
            "id",
            "https://static.scientificamerican.com/sciam/cache/file/C37174A9-488C-49D4-B9C79B4E8EB39287_source.jpg?w=1200",
            mimeType = "image/png"
        )
    )
}
