package com.salesforce.android.smi.messaging.samples.extensions

import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.network.data.domain.conversationEntry.ConversationEntry
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.ConversationEntryType
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Default number of recent cached entries inspected for session activity. */
const val DEFAULT_SESSION_ENTRY_LIMIT = 100

/**
 * Emits cache-derived activity and latest-activity time for this conversation without a network
 * request. [MessagingStore] uses this cache-first signal to refresh only active conversations.
 *
 * The emitted [Pair] is:
 *  - `first` ([Boolean]): whether the messaging session is currently considered **active**,
 *    determined solely from the most recent [SessionStatus] observed for the conversation:
 *      - No session status received yet -> assumed **active** (`true`).
 *      - Most recent status is [SessionStatus.Ended] -> **not** active (`false`).
 *      - Any other status (e.g. [SessionStatus.Active], [SessionStatus.Waiting],
 *        [SessionStatus.New]) -> active (`true`).
 *  - `second` ([Long]): the timestamp of the most recent activity in the conversation, in
 *    milliseconds since the epoch. This is the newest cached [ConversationEntry.timestamp], or `0`
 *    if the conversation has no cached entries.
 *
 * Session state reflects the latest server-provided [SessionStatus], not local actions. The returned
 * [Flow] updates as entries reach the local cache.
 *
 * @param limit The maximum number of most-recent cached entries to inspect. Must be large enough to
 * include the latest session-status entry for accurate results. Defaults to [DEFAULT_SESSION_ENTRY_LIMIT].
 */
fun ConversationClient.isActiveFlow(limit: Int = DEFAULT_SESSION_ENTRY_LIMIT): Flow<Pair<Boolean, Long>> =
    conversationEntriesFlow(limit = limit, forceRefresh = false)
        .filterIsInstance<Result.Success<List<ConversationEntry>>>()
        .map { result -> result.data.toSessionActivity() }

/**
 * Reads cache-derived activity once without a network request. See [isActiveFlow] for [Pair] values.
 *
 * @param limit The maximum number of most-recent cached entries to inspect. Defaults to
 * [DEFAULT_SESSION_ENTRY_LIMIT].
 */
suspend fun ConversationClient.isActiveNow(limit: Int = DEFAULT_SESSION_ENTRY_LIMIT): Pair<Boolean, Long> =
    isActiveFlow(limit).first()

/**
 * Refreshes entries only when cached session activity permits it. Ended sessions cannot receive new
 * remote messages, so their cache remains authoritative and no network call occurs.
 *
 * @return `true` if a network refresh was performed, `false` if the conversation was inactive.
 */
suspend fun ConversationClient.refreshEntriesIfActive(limit: Int = DEFAULT_SESSION_ENTRY_LIMIT): Boolean {
    if (!isActiveNow(limit).first) return false
    conversationEntries(limit = limit, forceRefresh = true)
    return true
}

private fun List<ConversationEntry>.toSessionActivity(): Pair<Boolean, Long> {
    val latestSessionStatus = this
        .asSequence()
        .filter { it.entryType == ConversationEntryType.SessionStatusChanged }
        .maxByOrNull { it.timestamp }
        ?.let { (it.payload as? EntryPayload.SessionStatusChangedPayload)?.sessionStatus }

    // No session status yet -> assume active. Ended -> inactive. Anything else -> active.
    val isActive = latestSessionStatus != SessionStatus.Ended

    val lastActivityTimestamp = this.maxOfOrNull { it.timestamp } ?: 0L

    return isActive to lastActivityTimestamp
}
