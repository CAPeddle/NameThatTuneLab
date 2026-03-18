package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.domain.repository.MetadataRepository
import javax.inject.Inject

/**
 * Resolves enriched [TrackMetadata] (including release year) for a given [NowPlayingEvent].
 *
 * Uses [MetadataRepository] which applies a cache-first strategy before hitting
 * external providers.
 */
class ResolveMetadataUseCase @Inject constructor(
    private val metadataRepository: MetadataRepository
) {
    suspend operator fun invoke(event: NowPlayingEvent): Result<TrackMetadata> =
        metadataRepository.resolveMetadata(event)
}
