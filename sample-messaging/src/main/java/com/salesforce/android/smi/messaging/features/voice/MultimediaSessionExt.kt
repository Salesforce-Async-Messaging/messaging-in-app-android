package com.salesforce.android.smi.messaging.features.voice

import com.salesforce.android.smi.multimedia.common.api.audio.AudioStream
import com.salesforce.android.smi.multimedia.common.api.participant.MultimediaParticipantOrigin
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Get the maximum amplitudes from all participants of a specific origin.
 * This is used for audio visualization.
 */
internal fun MultimediaSession.getMaxAmplitudes(
    origin: MultimediaParticipantOrigin,
    barCount: Int
): Flow<FloatArray> = getAllAmplitudes(origin, barCount).getMaxAmplitudes(barCount)

/**
 * Get amplitudes from all participants, normalized to a list of FloatArray.
 */
private fun MultimediaSession.getAllAmplitudes(
    origin: MultimediaParticipantOrigin,
    barCount: Int
): Flow<List<FloatArray>> = when (origin) {
    MultimediaParticipantOrigin.Local -> localParticipant.flatMapLatest { participant ->
        participant.audioTracks.firstTrackAmplitudes(barCount).map { listOf(it) }
    }

    MultimediaParticipantOrigin.Remote -> remoteParticipants.flatMapLatest { participantList ->
        if (participantList.isEmpty()) {
            emptyFlow()
        } else {
            combine(participantList.map { it.audioTracks.firstTrackAmplitudes(barCount) }) {
                it.toList()
            }
        }
    }
}

/**
 * Select the loudest participant's amplitudes from a list.
 */
private fun Flow<List<FloatArray>>.getMaxAmplitudes(barCount: Int): Flow<FloatArray> = map { amplitudesList ->
    amplitudesList.maxByOrNull { amplitudes -> amplitudes.sum() } ?: FloatArray(barCount)
}

/**
 * Get amplitudes from the first audio track in the stream.
 */
private fun AudioStream.firstTrackAmplitudes(barCount: Int): Flow<FloatArray> = tracks.flatMapLatest { trackList ->
    trackList.firstOrNull()?.amplitudes(barCount) ?: emptyFlow()
}
