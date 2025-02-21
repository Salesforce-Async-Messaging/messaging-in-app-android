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
import com.salesforce.android.smi.network.data.domain.participant.CoreParticipant
import com.salesforce.android.smi.network.data.domain.participant.Participant
import com.salesforce.android.smi.network.data.domain.participant.ParticipantRoleType

@Composable
internal fun TypingStartedReplacementEntry(
    participant: Participant
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
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally),
                text = "${participant.displayName} is typing...",
                color = Color.Green
            )
        }
    }
}

@Preview
@Composable
private fun TypingStartedReplacementEntryPreview() {
    TypingStartedReplacementEntry(
        participant = CoreParticipant(subject = "subject", isLocal = false, roleType = ParticipantRoleType.Agent)
    )
}
