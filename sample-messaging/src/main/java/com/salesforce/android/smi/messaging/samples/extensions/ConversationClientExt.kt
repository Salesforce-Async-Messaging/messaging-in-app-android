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

/**
 * Default number of most-recent cached entries inspected when determining session activity.
 */
const val DEFAULT_SESSION_ENTRY_LIMIT = 100

/**
 * Sample: emits the current session-activity state of this conversation, derived entirely from
 * locally cached data (no network request is made).
 *
 * This is useful for building an efficient, cache-first inbox: the emitted flag lets you decide
 * whether a conversation still needs network activity, or whether the local cache is authoritative.
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
 * The session state is driven by the server: this reflects the latest [SessionStatus] received and
 * does not infer session changes from local actions such as sending a message.
 *
 * The returned [Flow] updates reactively as new entries are persisted to the local cache.
 *
 * @param limit The maximum number of most-recent cached entries to inspect. Must be large enough to
 * include the latest session-status entry for accurate results. Defaults to [DEFAULT_SESSION_ENTRY_LIMIT].
 */
fun ConversationClient.isActiveFlow(limit: Int = DEFAULT_SESSION_ENTRY_LIMIT): Flow<Pair<Boolean, Long>> =
    conversationEntriesFlow(limit = limit, forceRefresh = false)
        .filterIsInstance<Result.Success<List<ConversationEntry>>>()
        .map { result -> result.data.toSessionActivity() }

/**
 * Sample: reads the current session-activity state of this conversation once, from locally cached
 * data (no network request is made). See [isActiveFlow] for a description of the returned [Pair].
 *
 * @param limit The maximum number of most-recent cached entries to inspect. Defaults to
 * [DEFAULT_SESSION_ENTRY_LIMIT].
 */
suspend fun ConversationClient.isActiveNow(limit: Int = DEFAULT_SESSION_ENTRY_LIMIT): Pair<Boolean, Long> =
    isActiveFlow(limit).first()

/**
 * Sample: force-refreshes this conversation's entries over the network **only if** its cached session
 * is still active. Ended sessions can't receive new remote messages, so their local cache is
 * authoritative and no network call is made.
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
