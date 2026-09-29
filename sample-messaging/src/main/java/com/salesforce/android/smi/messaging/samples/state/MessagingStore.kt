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
 * Conversation-list snapshot exposed by [MessagingStore.listState].
 *
 * @property states one shared [MessagingSessionState] per conversation, in list order.
 * @property isLoading `true` until the list first loads; `false` for an empty loaded list.
 */
data class ConversationListState(
    val states: List<MessagingSessionState> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * Shared cache-first source of messaging state for this sample.
 *
 * Hoist one store in Compose and pass it to consumers. It owns the [CoreClient] and memoizes one
 * [ConversationClient] and shared [MessagingSessionState] flow per conversation, so list rows and
 * standalone UI observe the same state without duplicate subscriptions.
 *
 * @param coreClient app's single [CoreClient], stable across active-conversation switches.
 * @param scope scope that owns shared flows, such as `rememberCoroutineScope()` at the hoist site.
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

    // One shared, hot session-state flow per conversation. Synchronized memoization is the dedup: repeated
    // calls for the same id return the same StateFlow, so all collectors share a single upstream. stateIn with
    // WhileSubscribed keeps that upstream alive only while something is observing it (plus a short
    // grace period across config changes / quick navigation), then stops the network flow.
    private val sessionStates = ConcurrentHashMap<UUID, StateFlow<MessagingSessionState>>()

    // Throttles network refreshes: keyed by conversation UUID for per-conversation entry refreshes,
    // and by LIST_REFRESH_KEY for the conversation list. First call per key is immediate; repeats
    // within the window are served from the SDK cache.
    private val throttle = Throttle()

    fun conversationClient(conversationId: UUID): ConversationClient =
        conversationClients.getOrPutSynchronized(conversationId) {
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
     * Shared UI-state flow for [conversationId]. Repeated calls for one id return the same flow.
     *
     * Known inbox conversations use cache-backed flows. Unknown ids emit a default state without a
     * request, allowing Compose callers to observe them unconditionally.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun sessionState(conversationId: UUID): StateFlow<MessagingSessionState> =
        sessionStates.getOrPutSynchronized(conversationId) {
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

    // ConcurrentHashMap.computeIfAbsent is only available on Android API 24+. Synchronize each map's
    // check-and-create operation so API 23 also creates and retains exactly one value per UUID.
    private fun <T> ConcurrentHashMap<UUID, T>.getOrPutSynchronized(
        key: UUID,
        create: () -> T
    ): T = synchronized(this) {
        get(key) ?: create().also { put(key, it) }
    }

    /**
     * Cache-first conversation-list state. Rows reuse [sessionState], so list and standalone UI share
     * each conversation's state. Active conversations refresh when the list membership changes.
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
     * Cold flow backing [listState], mapping known ids to their shared [sessionState] flows.
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
     * Total unread count for active conversations in [listState], derived from shared state without an
     * extra fetch. For every conversation regardless of status, sum
     * `it.conversation?.unreadMessageCount ?: 0` instead.
     */
    val totalUnreadCount: StateFlow<Int> = listState
        .map { state -> state.states.sumOf { it.unreadMessageCount } }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.WhileSubscribed(SHARE_STOP_TIMEOUT_MS), 0)

    /**
     * Most recently active conversation in [listState], or `null` while loading or when none is
     * loadable. Ended conversations are skipped; activity time determines recency, then list order.
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
     * Reconciles the cache-first list with the network, such as for pull-to-refresh.
     *
     * First call forces a fetch. Calls within the throttle window return the locally cached list, and
     * [listState] updates from refreshed cache data.
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
