package com.salesforce.android.smi.messaging.samples.state

import com.salesforce.android.smi.common.api.data
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.samples.extensions.Throttle
import com.salesforce.android.smi.messaging.samples.extensions.first
import com.salesforce.android.smi.messaging.samples.extensions.refreshEntriesIfActive
import com.salesforce.android.smi.network.data.domain.conversation.Conversation
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * Immutable snapshot of the conversation list, exposed by [MessagingStore.listState].
 *
 * @property states one [MessagingSessionState] per conversation, in list order. Each entry is backed
 * by the same shared per-conversation flow used everywhere else in the app.
 * @property isLoading `true` until the underlying conversation list has emitted for the first time.
 * This distinguishes "still loading the list" from "loaded, but there are no conversations".
 */
data class ConversationListState(
    val states: List<MessagingSessionState> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * The single entrypoint for reading messaging data across the sample app.
 *
 * Owns the one [CoreClient] for the app and hands out a **single shared** [MessagingSessionState]
 * flow per conversation, so that any number of components observing the same conversation (e.g. the
 * launcher button, the widget, and a row in the conversation list) all collect from **one** upstream
 * subscription rather than each opening their own. This is what keeps network activity proportional
 * to the number of distinct conversations on screen instead of the number of components.
 *
 * Hoist a single instance high in the composition (e.g. at the screen root) and pass it down.
 *
 * @param coreClient the app's single CoreClient. See `SalesforceMessaging` for why this instance is
 * stable across active-conversation switches.
 * @param scope the scope the shared flows are started in. Use a scope tied to the store's owner
 * (e.g. `rememberCoroutineScope()` at the hoist site).
 * @param listLimit maximum number of conversations fetched for [listState].
 * @param refreshThrottle minimum time between network refreshes of a single active conversation's
 * entries. The first refresh runs immediately; subsequent refreshes within this window are skipped
 * and served from the SDK cache. Defaults to 5 minutes.
 */
class MessagingStore(
    val coreClient: CoreClient,
    private val scope: CoroutineScope,
    private val listLimit: Int = 10,
    private val refreshThrottle: Duration = 5.minutes
) {
    // The SDK's CoreClient.conversationClient(UUID) creates a NEW ConversationClient on every call,
    // so we memoize per id to guarantee one ConversationClient (and therefore one set of underlying
    // flows) per conversation. ConcurrentHashMap because these maps are touched both from the Compose
    // main thread (component reads) and from flow-collection/refresh coroutines off the main thread.
    private val conversationClients = ConcurrentHashMap<UUID, ConversationClient>()

    // One shared, hot session-state flow per conversation. computeIfAbsent is the dedup: repeated calls for
    // the same id return the same StateFlow, so all collectors share a single upstream. stateIn with
    // WhileSubscribed keeps that upstream alive only while something is observing it (plus a short
    // grace period across config changes / quick navigation), then stops the network flow.
    private val sessionStates = ConcurrentHashMap<UUID, StateFlow<MessagingSessionState>>()

    // Throttles network refreshes: keyed by conversation UUID for per-conversation entry refreshes,
    // and by LIST_REFRESH_KEY for the conversation list. First call per key is immediate; repeats
    // within the window are served from the SDK cache.
    private val throttle = Throttle()

    fun conversationClient(conversationId: UUID): ConversationClient =
        conversationClients.computeIfAbsent(conversationId) {
            coreClient.conversationClient(conversationId)
        }

    // The ids of conversations that exist in the inbox, taken straight from the conversation list
    // (not from any session flow, so there's no cycle with [sessionState]). `null` = list not loaded
    // yet; empty = loaded and empty. [sessionState] uses this to skip network for ids not in the
    // inbox (e.g. a new-conversation id).
    private val knownConversationIds: StateFlow<Set<UUID>?> =
        coreClient.conversationsFlow(limit = listLimit, forceRefresh = false)
            .map { result -> result.data.orEmpty().map { it.identifier }.toSet() }
            .distinctUntilChanged()
            .onEach { ids -> refreshActive(ids.toList()) }
            .stateIn(scope, SharingStarted.WhileSubscribed(SHARE_STOP_TIMEOUT_MS), null)

    /**
     * Shared session-state flow for [conversationId]; the same id returns the same [StateFlow], so
     * collectors share one upstream.
     *
     * Self-gating on the inbox: it only opens the network-backed flows once [conversationId] is in
     * [knownConversationIds]. For an unknown id (e.g. a not-yet-started conversation) it emits a
     * default state and makes no request, so components can observe it unconditionally.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun sessionState(conversationId: UUID): StateFlow<MessagingSessionState> =
        sessionStates.computeIfAbsent(conversationId) {
            knownConversationIds
                .flatMapLatest { ids ->
                    if (ids?.contains(conversationId) == true) {
                        provideMessagingSessionStateFlow(conversationClient(conversationId))
                    } else {
                        flowOf(MessagingSessionState())
                    }
                }
                .stateIn(
                    scope,
                    SharingStarted.WhileSubscribed(SHARE_STOP_TIMEOUT_MS),
                    MessagingSessionState()
                )
        }

    /**
     * Cache-first stream of the conversation list. Rows reuse the same per-conversation
     * [sessionState] flows, so a conversation shown both here and standalone is fetched once.
     *
     * The active conversations are refreshed in the background whenever the set of conversation ids
     * actually changes (not on every emission), keeping the list current without looping: the id set
     * only changes when conversations are added/removed, so the refresh — which itself writes entries
     * — cannot re-trigger itself.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val listState: StateFlow<ConversationListState> =
        conversationListStateFlow()
            .stateIn(
                scope,
                SharingStarted.WhileSubscribed(SHARE_STOP_TIMEOUT_MS),
                ConversationListState(isLoading = true)
            )

    /**
     * Cold flow backing [listState]: projects the known ids onto their shared [sessionState] flows.
     * The list fetch and background refresh are owned by [knownConversationIds], so this is a pure
     * projection. Split from [listState] so the flow is testable and the [stateIn] policy lives once.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun conversationListStateFlow(): Flow<ConversationListState> =
        knownConversationIds.flatMapLatest { ids ->
            when {
                ids == null -> flowOf(ConversationListState(isLoading = true))
                else -> combineSessionStates(ids.toList())
            }
        }

    private fun combineSessionStates(ids: List<UUID>): Flow<ConversationListState> {
        if (ids.isEmpty()) return flowOf(ConversationListState(isLoading = false))
        val flows = ids.map { sessionState(it) }
        return combine(flows) { states ->
            ConversationListState(states = states.toList(), isLoading = false)
        }
    }

    /**
     * Total unread message count across every conversation in [listState], derived from the same
     * shared per-conversation flows (no extra fetch). This mirrors the per-row unread counts shown in
     * the inbox: [MessagingSessionState.unreadMessageCount] is only non-zero for active sessions, so
     * this sums unread for active conversations. To count unread across all conversations regardless
     * of session status, sum `it.conversation?.unreadMessageCount ?: 0` instead.
     */
    val totalUnreadCount: StateFlow<Int> = listState
        .map { state -> state.states.sumOf { it.unreadMessageCount } }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.WhileSubscribed(SHARE_STOP_TIMEOUT_MS), 0)

    /**
     * The most recently active conversation id in [listState], or `null` while loading or when none
     * is loadable. Ended conversations are skipped (they 403 on load), so this only returns one you
     * can open. "Most recent" is by last activity, falling back to list order. Handy for seeding a
     * default conversation instead of a brand-new random id.
     */
    val latestConversationId: StateFlow<UUID?> = listState
        .map { state -> if (state.isLoading) null else state.states.latestConversationId() }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.WhileSubscribed(SHARE_STOP_TIMEOUT_MS), null)

    private fun List<MessagingSessionState>.latestConversationId(): UUID? {
        val eligible = filter { it.isActive }
        if (eligible.isEmpty()) return null
        val mostRecentByActivity = eligible.maxByOrNull { it.lastActivityTimestamp }
        return (mostRecentByActivity ?: eligible.first()).conversation?.identifier
    }

    /**
     * Explicitly refreshes the conversation list from the network, **throttled and idempotent**.
     *
     * This is not automatic — [listState] stays cache-first on its own. Call this (e.g. from a
     * pull-to-refresh) when you want to reconcile with the backend. The first call forces a network
     * fetch; any call within the throttle window skips the network and simply returns the same
     * locally cached list. On success, [listState] updates reactively from the refreshed cache.
     *
     * @return the current conversation list (freshly fetched, or the throttled cached copy).
     */
    suspend fun refreshConversations(): List<Conversation> =
        throttle.first(LIST_REFRESH_KEY, refreshThrottle) {
            coreClient.conversations(listLimit, true, null).data
        } ?: coreClient.conversations(listLimit, false, null).data.orEmpty()

    // Force-refresh entries for each still-active conversation, at most once per throttle window per
    // conversation. Inactive conversations never hit the network (their cache is authoritative).
    private suspend fun refreshActive(ids: List<UUID>) {
        coroutineScope {
            ids.map { id ->
                async {
                    // Return Unit (non-null) only when a refresh actually ran, so the window starts;
                    // an inactive conversation returns null and stays eligible.
                    throttle.first(id, refreshThrottle) {
                        Unit.takeIf { conversationClient(id).refreshEntriesIfActive() }
                    }
                }
            }.awaitAll()
        }
    }

    companion object {
        private const val SHARE_STOP_TIMEOUT_MS = 5_000L
        private const val LIST_REFRESH_KEY = "conversation-list"
    }
}
