package com.capeddle.namethattunelab.nowplaying

import app.cash.turbine.test
import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Uses [StandardTestDispatcher] tied to [runTest]'s [TestCoroutineScheduler] so that
 * [kotlinx.coroutines.flow.debounce]'s internal [kotlinx.coroutines.delay] calls
 * are controlled by virtual time instead of wall-clock time.
 *
 * Between each emission we advance virtual time by [advanceMs] (> 1 500 ms) so the
 * debounce window expires and the item is forwarded downstream.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrackChangeDebouncerTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private val sourceApp = "com.music"
    private val eventA = NowPlayingEvent("Song A", "Artist A", null, sourceApp)
    private val eventB = NowPlayingEvent("Song B", "Artist B", null, sourceApp)

    /** More than the 1 500 ms debounce window. */
    private val advanceMs = 2_000L

    // ─────────────────────────────────────────────────────────────────────────
    // Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `debounce emits distinct events`() = runTest {
        val debouncer = TrackChangeDebouncer(StandardTestDispatcher(testScheduler))

        val upstream = flow {
            emit(eventA)
            advanceTimeBy(advanceMs) // expire debounce → eventA emitted
            emit(eventB)
            advanceTimeBy(advanceMs) // expire debounce → eventB emitted
        }

        debouncer.debounce(upstream).test {
            assertEquals(eventA, awaitItem())
            assertEquals(eventB, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `debounce suppresses rapid consecutive duplicates`() = runTest {
        val debouncer = TrackChangeDebouncer(StandardTestDispatcher(testScheduler))

        // eventA emitted twice rapidly — debounce keeps only the last emission;
        // because both have identical title+artist, distinctUntilChanged also suppresses.
        val sameKeyEvent = eventA.copy(album = "different album")
        val upstream = flow {
            emit(eventA)
            // No time advance — second emission arrives within debounce window.
            // debounce will cancel the timer for eventA and restart for sameKeyEvent.
            emit(sameKeyEvent)
            advanceTimeBy(advanceMs) // expire debounce → sameKeyEvent wins, but same key
        }

        debouncer.debounce(upstream).test {
            // Only one item should emerge (the last rapid emission that survived debounce);
            // distinctUntilChanged then suppresses it if the key matches the previous.
            // Result: exactly 1 item with the same artist+title key.
            val item = awaitItem()
            assertEquals(eventA.title, item.title)
            assertEquals(eventA.artist, item.artist)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `debounce emits when title changes even if artist same`() = runTest {
        val debouncer = TrackChangeDebouncer(StandardTestDispatcher(testScheduler))
        val event1 = NowPlayingEvent("Song A", "Artist", null, sourceApp)
        val event2 = NowPlayingEvent("Song B", "Artist", null, sourceApp)

        val upstream = flow {
            emit(event1)
            advanceTimeBy(advanceMs)
            emit(event2)
            advanceTimeBy(advanceMs)
        }

        debouncer.debounce(upstream).test {
            assertEquals(event1, awaitItem())
            assertEquals(event2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `debounce emits when artist changes even if title same`() = runTest {
        val debouncer = TrackChangeDebouncer(StandardTestDispatcher(testScheduler))
        val event1 = NowPlayingEvent("Cover Song", "Original Artist", null, sourceApp)
        val event2 = NowPlayingEvent("Cover Song", "Cover Artist", null, sourceApp)

        val upstream = flow {
            emit(event1)
            advanceTimeBy(advanceMs)
            emit(event2)
            advanceTimeBy(advanceMs)
        }

        debouncer.debounce(upstream).test {
            assertEquals(event1, awaitItem())
            assertEquals(event2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `debounce does not emit on empty upstream`() = runTest {
        val debouncer = TrackChangeDebouncer(StandardTestDispatcher(testScheduler))

        debouncer.debounce(emptyFlow()).test {
            assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
    }
}
