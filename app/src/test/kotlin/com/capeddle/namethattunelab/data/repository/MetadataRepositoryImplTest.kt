package com.capeddle.namethattunelab.data.repository

import com.capeddle.namethattunelab.data.local.MetadataCacheDao
import com.capeddle.namethattunelab.data.mapper.MetadataCacheMapper
import com.capeddle.namethattunelab.data.remote.MetadataProvider
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.util.PipelineLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MetadataRepositoryImplTest {

    private val provider: MetadataProvider = mockk()
    private val cacheDao: MetadataCacheDao = mockk(relaxed = true)
    private val logger: PipelineLogger = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: MetadataRepositoryImpl

    private val event = NowPlayingEvent(TRACK_TITLE, "John Lennon", null, "com.spotify.music")
    private val metadata = TrackMetadata(TRACK_TITLE, "John Lennon", TRACK_TITLE, 1971, MetadataConfidence.HIGH)

    @BeforeEach
    fun setUp() {
        repository = MetadataRepositoryImpl(provider, cacheDao, logger, testDispatcher)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cache hit
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `returns cached metadata when cache hit and not expired`() = runTest(testDispatcher) {
        val entity = MetadataCacheMapper.toEntity(metadata, cachedAt = System.currentTimeMillis())
        coEvery { cacheDao.get(any(), any()) } returns entity

        val result = repository.resolveMetadata(event)

        assertTrue(result.isSuccess)
        assertEquals(metadata, result.getOrNull())
        coVerify(exactly = 0) { provider.lookup(any(), any(), any()) }
    }

    @Test
    fun `calls remote provider on cache miss`() = runTest(testDispatcher) {
        coEvery { cacheDao.get(any(), any()) } returns null
        coEvery { provider.lookup(any(), any(), any()) } returns Result.success(metadata)

        val result = repository.resolveMetadata(event)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { provider.lookup(event.artist, event.title, event.album) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cache expiry
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `calls remote provider when cached entry is expired`() = runTest(testDispatcher) {
        val expiredAt = System.currentTimeMillis() - EIGHT_DAYS_MS
        val entity = MetadataCacheMapper.toEntity(metadata, cachedAt = expiredAt)
        coEvery { cacheDao.get(any(), any()) } returns entity
        coEvery { provider.lookup(any(), any(), any()) } returns Result.success(metadata)

        repository.resolveMetadata(event)

        coVerify(exactly = 1) { provider.lookup(any(), any(), any()) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cache write-through
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `writes successful remote result to cache`() = runTest(testDispatcher) {
        coEvery { cacheDao.get(any(), any()) } returns null
        coEvery { provider.lookup(any(), any(), any()) } returns Result.success(metadata)

        repository.resolveMetadata(event)

        coVerify(exactly = 1) { cacheDao.upsert(any()) }
    }

    @Test
    fun `does not write to cache on remote failure`() = runTest(testDispatcher) {
        coEvery { cacheDao.get(any(), any()) } returns null
        coEvery { provider.lookup(any(), any(), any()) } returns Result.failure(RuntimeException("Network error"))

        repository.resolveMetadata(event)

        coVerify(exactly = 0) { cacheDao.upsert(any()) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Failure propagation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `propagates remote failure`() = runTest(testDispatcher) {
        val error = RuntimeException("Network error")
        coEvery { cacheDao.get(any(), any()) } returns null
        coEvery { provider.lookup(any(), any(), any()) } returns Result.failure(error)

        val result = repository.resolveMetadata(event)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    companion object {
        private const val TRACK_TITLE = "Imagine"
        private const val EIGHT_DAYS_MS = 8L * 24 * 60 * 60 * 1_000
    }
}
