package com.salesforce.android.smi.messaging.features.ui.replacement.components

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
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.event.ParticipantChangedOperation
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.event.entries.ParticipantChangedEntry
import com.salesforce.android.smi.network.data.domain.participant.CoreParticipant
import com.salesforce.android.smi.network.data.domain.participant.ParticipantRoleType

@Composable
internal fun ParticipantChangedReplacementEntry(
    participantChangedEntry: ParticipantChangedEntry
) {
    val shape = RoundedCornerShape(16)
    Row(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(shape = shape, width = 1.dp, color = Color.Green),
            shape = shape
        ) {
            val text = when (participantChangedEntry.operation) {
                ParticipantChangedOperation.Add -> "${participantChangedEntry.displayName} has joined the conversation"
                ParticipantChangedOperation.Remove -> "${participantChangedEntry.displayName} has left the conversation"
                else -> ""
            }
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally), text = text, color = Color.Green
            )
        }
    }
}

@Preview
@Composable
private fun ParticipantChangedReplacementEntryPreview_Outbound() {
    ParticipantChangedReplacementEntry(
        participantChangedEntry = ParticipantChangedEntry(
            CoreParticipant(subject = "subject", isLocal = false, roleType = ParticipantRoleType.Agent),
            ParticipantChangedOperation.Add,
            "Sarah Y"
        )
    )
}
