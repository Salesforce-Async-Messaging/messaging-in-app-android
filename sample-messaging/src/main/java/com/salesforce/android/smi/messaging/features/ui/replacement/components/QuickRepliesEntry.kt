package com.salesforce.android.smi.messaging.features.ui.replacement.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.OptionItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.titleItem.TitleItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuickRepliesReplacementEntry(
    title: String,
    optionItems: List<OptionItem.TypedOptionItem.TitleOptionItem>,
    onSelection: ((OptionItem) -> Unit)
) {
    val shape = RoundedCornerShape(16)
    Column {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .border(shape = shape, width = 1.dp, color = Color.Green),
            shape = shape
        ) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                text = title, color = Color.Green,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
            ) {
                repeat(optionItems.size) { index ->
                    val optionItem = optionItems[index]
                    TextButton(
                        modifier = Modifier
                            .wrapContentSize()
                            .border(1.dp, Color.Green, shape),
                        onClick = {
                            onSelection(OptionItem.SelectionsOptionItem(optionItem.optionId))
                        }
                    ) {
                        Text(text = (optionItem.titleItem.title), color = Color.Green)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun QuickRepliesReplacementEntryPreview() {
    QuickRepliesReplacementEntry(
        title = "Options",
        optionItems = listOf(
            OptionItem.TypedOptionItem.TitleOptionItem("1", "1", TitleItem.DefaultTitleItem("Option 1")),
            OptionItem.TypedOptionItem.TitleOptionItem("2", "1", TitleItem.DefaultTitleItem("Option 2"))
        )
    ) {
    }
}
