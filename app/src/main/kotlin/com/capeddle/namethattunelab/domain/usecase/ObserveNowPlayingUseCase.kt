package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.repository.NowPlayingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Observes actively playing tracks as a stream of [NowPlayingEvent]s.
 *
 * Wraps [NowPlayingRepository.observeNowPlaying] to provide clean call-site syntax.
 */
class ObserveNowPlayingUseCase @Inject constructor(
    private val nowPlayingRepository: NowPlayingRepository
) {
    operator fun invoke(): Flow<NowPlayingEvent> = nowPlayingRepository.observeNowPlaying()
}
