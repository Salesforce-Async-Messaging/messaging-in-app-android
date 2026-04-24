package com.salesforce.android.smi.messaging.features.voice

import android.util.Log
import com.salesforce.android.smi.multimedia.common.api.audio.AudioStream
import com.salesforce.android.smi.multimedia.common.api.participant.MultimediaParticipantOrigin
import com.salesforce.android.smi.multimedia.common.api.session.MultimediaSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val TAG = "MultimediaSessionExt"

/**
 * Get the maximum amplitudes from all participants of a specific origin.
 * This is used for audio visualization.
 */
fun MultimediaSession.getMaxAmplitudes(
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
    MultimediaParticipantOrigin.Local -> localParticipant
        .onEach { Log.d(TAG, "Local participant updated: $it") }
        .flatMapLatest { participant ->
            participant.audioTracks.firstTrackAmplitudes(barCount)
                .onEach { Log.d(TAG, "Local audio track amplitudes: ${it.take(3).joinToString()}") }
                .map { listOf(it) }
        }

    MultimediaParticipantOrigin.Remote -> remoteParticipants
        .onEach { Log.d(TAG, "Remote participants updated: count=${it.size}") }
        .flatMapLatest { participantList ->
            if (participantList.isEmpty()) {
                Log.d(TAG, "No remote participants - emptyFlow")
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
