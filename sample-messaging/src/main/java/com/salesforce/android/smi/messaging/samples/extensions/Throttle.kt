package com.salesforce.android.smi.messaging.samples.extensions

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

/**
 * A small keyed rate-limiter. It only tracks per-key state; the throttling flavour (e.g. [first],
 * [debounce]) is added as an extension over the [run] and [runLatest] primitives.
 *
 * Thread-safe. State is in-memory and resets when the process is recreated.
 */
class Throttle {
    private val lastRunMillis = ConcurrentHashMap<Any, Long>()
    private val pendingJobs = ConcurrentHashMap<Any, Job>()

    /**
     * Runs [block] if [allow] permits, recording the run when [block] returns non-null.
     *
     * @param key the action to throttle; each key tracks its own last-run time.
     * @param allow given the time since [key] last ran (`null` if never), returns whether to run now.
     * @param block the work to run when allowed. A non-null result records the run; `null` leaves the
     * key eligible, so a no-op or failure isn't penalised.
     * @return [block]'s result if it ran and returned non-null, else `null` (not allowed or no-op).
     */
    suspend fun <T : Any> run(
        key: Any,
        allow: (elapsed: Duration?) -> Boolean,
        block: suspend () -> T?
    ): T? {
        val elapsed = lastRunMillis[key]?.let { (System.currentTimeMillis() - it).milliseconds }
        if (!allow(elapsed)) return null
        return block()?.also { lastRunMillis[key] = System.currentTimeMillis() }
    }

    /**
     * Schedules [block] to run after [delay], cancelling any pending run for [key]. Only the last call
     * in a burst for a key survives.
     *
     * @param key the action to debounce; each key has its own pending run.
     * @param scope the scope the scheduled work runs in.
     * @param delay quiet period to wait before running.
     * @param block the work to run.
     * @return a [Deferred] completing with [block]'s result, or cancelled if superseded.
     */
    fun <T> runLatest(
        key: Any,
        scope: CoroutineScope,
        delay: Duration,
        block: suspend () -> T
    ): Deferred<T> {
        pendingJobs.remove(key)?.cancel()
        val job = scope.async {
            delay(delay)
            block()
        }
        pendingJobs[key] = job
        job.invokeOnCompletion { pendingJobs.remove(key, job) }
        return job
    }
}

/**
 * Leading-edge throttle: the first call for [key] runs; further calls within [window] are skipped.
 * Keeps cache-first data fresh without spamming the backend.
 *
 * @param key the action to throttle.
 * @param window minimum time between runs for [key].
 * @param block the work to run. A non-null result starts the window; `null` keeps [key] eligible.
 * @return [block]'s result if it ran and returned non-null, else `null` (suppressed or no-op).
 *
 * ```
 * throttle.first(key, 5.minutes) {
 *     fetchOverNetwork().takeIf { it.succeeded }
 * }
 * ```
 */
suspend fun <T : Any> Throttle.first(
    key: Any,
    window: Duration,
    block: suspend () -> T?
): T? = run(key, allow = { elapsed -> elapsed == null || elapsed >= window }, block)

/**
 * Trailing-edge debounce: runs [block] after [delay] of quiet for [key]. Repeated calls reset the
 * timer, so only the final call in a burst runs.
 *
 * @param key the action to debounce.
 * @param delay quiet period to wait before running.
 * @param scope the scope the work runs in.
 * @param block the work to run.
 * @return a [Deferred] completing with [block]'s result, or cancelled if superseded.
 *
 * ```
 * throttle.debounce(key, 300.milliseconds, scope) { search(query) }
 * ```
 */
fun <T> Throttle.debounce(
    key: Any,
    delay: Duration,
    scope: CoroutineScope,
    block: suspend () -> T
): Deferred<T> = runLatest(key, scope, delay, block)
