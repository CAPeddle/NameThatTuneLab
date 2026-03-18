package com.capeddle.namethattunelab.nowplaying

import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

/**
 * Applies debouncing and deduplication to a raw stream of [NowPlayingEvent]s.
 *
 * Media sessions often emit several rapid metadata updates during a single track change.
 * This class:
 *  1. Debounces events by [DEBOUNCE_MS] (1.5 seconds) to wait for the stream to settle.
 *  2. Filters consecutive duplicate events (same title + artist) using [distinctUntilChanged].
 *
 * @param ioDispatcher Injected dispatcher for Flow operations (testable with [UnconfinedTestDispatcher]).
 */
@OptIn(FlowPreview::class)
class TrackChangeDebouncer @Inject constructor(
    @Named("io") private val ioDispatcher: CoroutineDispatcher
) {
    fun debounce(upstream: Flow<NowPlayingEvent>): Flow<NowPlayingEvent> = upstream
        .debounce(DEBOUNCE_MS)
        .distinctUntilChanged { old, new ->
            old.title == new.title && old.artist == new.artist
        }
        .flowOn(ioDispatcher)

    companion object {
        const val DEBOUNCE_MS = 1_500L
    }
}
