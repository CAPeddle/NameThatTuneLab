package com.capeddle.namethattunelab.data.repository

import com.capeddle.namethattunelab.data.local.MetadataCacheDao
import com.capeddle.namethattunelab.data.mapper.MetadataCacheMapper
import com.capeddle.namethattunelab.data.mapper.MetadataCacheMapper.normalise
import com.capeddle.namethattunelab.data.remote.MetadataProvider
import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.domain.repository.MetadataRepository
import com.capeddle.namethattunelab.util.PipelineLogger
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Cache-first implementation of [MetadataRepository].
 *
 * Resolution order:
 * 1. Check [MetadataCacheDao] for a non-expired entry.
 * 2. On cache miss, delegate to [MetadataProvider] (MusicBrainz).
 * 3. On successful remote fetch, persist result to cache.
 * 4. Return the resolved [TrackMetadata], or propagate failure.
 *
 * All I/O is dispatched on the injected [ioDispatcher].
 */
@Singleton
class MetadataRepositoryImpl @Inject constructor(
    private val provider: MetadataProvider,
    private val cacheDao: MetadataCacheDao,
    private val logger: PipelineLogger,
    @Named("io") private val ioDispatcher: CoroutineDispatcher
) : MetadataRepository {

    override suspend fun resolveMetadata(event: NowPlayingEvent): Result<TrackMetadata> = withContext(ioDispatcher) {
        val artistKey = event.artist.normalise()
        val titleKey = event.title.normalise()

        // ── 1. Cache lookup ──────────────────────────────────────────────
        val cached = cacheDao.get(artistKey, titleKey)
        if (cached != null && !isCacheExpired(cached.cachedAt)) {
            logger.logCacheHit(event.artist, event.title)
            return@withContext Result.success(MetadataCacheMapper.toDomain(cached))
        }

        logger.logCacheMiss(event.artist, event.title)

        // ── 2. Remote fetch ──────────────────────────────────────────────
        val result = provider.lookup(
            artist = event.artist,
            title = event.title,
            album = event.album
        )

        // ── 3. Persist on success ────────────────────────────────────────
        result.onSuccess { metadata ->
            val entity = MetadataCacheMapper.toEntity(metadata)
            cacheDao.upsert(entity)
        }

        result
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun isCacheExpired(cachedAt: Long): Boolean = System.currentTimeMillis() - cachedAt > CACHE_TTL_MS

    private companion object {
        /** 7 days — release years do not change; long TTL reduces API traffic. */
        const val CACHE_TTL_MS = 7L * 24 * 60 * 60 * 1_000
    }
}
