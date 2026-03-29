package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.repository.NowPlayingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Observes whether notification-listener access is granted for this app.
 */
class ObserveNotificationAccessUseCase @Inject constructor(
    private val nowPlayingRepository: NowPlayingRepository
) {
    operator fun invoke(): Flow<Boolean> = nowPlayingRepository.observeNotificationAccess()
}
