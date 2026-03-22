package com.capeddle.namethattunelab.presentation.screen

import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.domain.usecase.AnnounceTrackUseCase
import com.capeddle.namethattunelab.domain.usecase.ObserveAppSettingsUseCase
import com.capeddle.namethattunelab.domain.usecase.ObserveNowPlayingUseCase
import com.capeddle.namethattunelab.domain.usecase.ResolveMetadataUseCase
import com.capeddle.namethattunelab.domain.usecase.UpdateAppSettingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeNowPlaying: ObserveNowPlayingUseCase = mockk()
    private val resolveMetadata: ResolveMetadataUseCase = mockk()
    private val announceTrack: AnnounceTrackUseCase = mockk()
    private val observeAppSettings: ObserveAppSettingsUseCase = mockk()
    private val updateAppSettings: UpdateAppSettingsUseCase = mockk()

    private val event = NowPlayingEvent(TRACK_TITLE, TRACK_ARTIST, null, "com.spotify.music")
    private val metadata = TrackMetadata(TRACK_TITLE, TRACK_ARTIST, TRACK_TITLE, 1971, MetadataConfidence.HIGH)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeAppSettings() } returns flowOf(AppSettings())
        coEvery { updateAppSettings(any()) } returns Unit
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MainViewModel = MainViewModel(
        observeNowPlaying,
        resolveMetadata,
        announceTrack,
        observeAppSettings,
        updateAppSettings
    )

    @Nested
    @Suppress("FunctionNaming")
    inner class InitialState {
        @Test
        fun `initial state is empty`() = runTest(testDispatcher) {
            every { observeNowPlaying() } returns flowOf()
            val vm = createViewModel()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertNull(state.currentTrack)
            assertTrue(state.recentTracks.isEmpty())
            assertNull(state.errorMessage)
        }
    }

    @Nested
    @Suppress("FunctionNaming")
    inner class HappyPath {
        @Test
        fun `uiState reflects resolved metadata after NowPlaying event`() = runTest(testDispatcher) {
            every { observeNowPlaying() } returns flowOf(event)
            coEvery { resolveMetadata(event) } returns Result.success(metadata)
            coEvery { announceTrack(metadata) } returns Result.success(Unit)

            val vm = createViewModel()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertEquals(metadata, state.currentTrack)
            assertTrue(state.recentTracks.contains(metadata))
        }

        @Test
        fun `recentTracks deduplicates same artist plus title`() = runTest(testDispatcher) {
            val event2 = event.copy(album = "Imagine (Remaster)")
            every { observeNowPlaying() } returns flowOf(event, event2)
            coEvery { resolveMetadata(any()) } returns Result.success(metadata)
            coEvery { announceTrack(any()) } returns Result.success(Unit)

            val vm = createViewModel()
            advanceUntilIdle()

            val recentCount = vm.uiState.value.recentTracks
                .count { it.title == metadata.title && it.artist == metadata.artist }
            assertEquals(1, recentCount)
        }
    }

    @Nested
    @Suppress("FunctionNaming")
    inner class ErrorHandling {
        @Test
        fun `errorMessage set when metadata resolution fails`() = runTest(testDispatcher) {
            every { observeNowPlaying() } returns flowOf(event)
            coEvery { resolveMetadata(event) } returns Result.failure(RuntimeException(ERROR_MESSAGE))
            coEvery { announceTrack(any()) } returns Result.success(Unit)

            val vm = createViewModel()
            advanceUntilIdle()

            assertNotNull(vm.uiState.value.errorMessage)
        }

        @Test
        fun `placeholder added to recentTracks when metadata resolution fails`() = runTest(testDispatcher) {
            every { observeNowPlaying() } returns flowOf(event)
            coEvery { resolveMetadata(event) } returns Result.failure(RuntimeException(ERROR_MESSAGE))
            coEvery { announceTrack(any()) } returns Result.success(Unit)

            val vm = createViewModel()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertEquals(1, state.recentTracks.size)
            assertEquals(TRACK_TITLE, state.recentTracks.first().title)
            assertEquals(TRACK_ARTIST, state.recentTracks.first().artist)
            assertEquals(MetadataConfidence.NONE, state.recentTracks.first().confidence)
            assertNotNull(state.errorMessage)
        }

        @Test
        fun `announceTrack called with placeholder when metadata resolution fails`() = runTest(testDispatcher) {
            every { observeNowPlaying() } returns flowOf(event)
            coEvery { resolveMetadata(event) } returns Result.failure(RuntimeException(ERROR_MESSAGE))
            coEvery { announceTrack(any()) } returns Result.success(Unit)

            val vm = createViewModel()
            advanceUntilIdle()

            coVerify {
                announceTrack(match { it.title == TRACK_TITLE && it.confidence == MetadataConfidence.NONE })
            }
        }

        @Test
        fun `dismissError clears errorMessage`() = runTest(testDispatcher) {
            every { observeNowPlaying() } returns flowOf(event)
            coEvery { resolveMetadata(event) } returns Result.failure(RuntimeException(ERROR_MESSAGE))
            coEvery { announceTrack(any()) } returns Result.success(Unit)

            val vm = createViewModel()
            advanceUntilIdle()
            vm.dismissError()

            assertNull(vm.uiState.value.errorMessage)
        }
    }

    @Nested
    @Suppress("FunctionNaming")
    inner class SettingsIntegration {
        @Test
        fun `settings inputs reflect observed settings`() = runTest(testDispatcher) {
            val settings = AppSettings(
                musicBrainzUserAgent = "NameThatTuneLab/2.0 (settings@example.com)",
                voiceOverDelayMs = EXPECTED_DELAY_MS
            )
            every { observeNowPlaying() } returns flowOf()
            every { observeAppSettings() } returns flowOf(settings)

            val vm = createViewModel()
            advanceUntilIdle()

            assertEquals(settings.musicBrainzUserAgent, vm.uiState.value.musicBrainzUserAgentInput)
            assertEquals(EXPECTED_DELAY_MS.toString(), vm.uiState.value.voiceOverDelayMsInput)
        }

        @Test
        fun `saveSettings sends updated values through use case`() = runTest(testDispatcher) {
            every { observeNowPlaying() } returns flowOf()
            val vm = createViewModel()
            advanceUntilIdle()

            vm.onMusicBrainzUserAgentChanged("NameThatTuneLab/3.0 (save@example.com)")
            vm.onVoiceOverDelayChanged(SAVE_DELAY_MS.toString())
            vm.saveSettings()
            advanceUntilIdle()

            coVerify {
                updateAppSettings(
                    match {
                        it.musicBrainzUserAgent == "NameThatTuneLab/3.0 (save@example.com)" &&
                            it.voiceOverDelayMs == SAVE_DELAY_MS
                    }
                )
            }
        }

        @Test
        fun `saveSettings sets errorMessage when repository throws`() = runTest(testDispatcher) {
            every { observeNowPlaying() } returns flowOf()
            coEvery { updateAppSettings(any()) } throws IOException("disk full")

            val vm = createViewModel()
            advanceUntilIdle()

            val agentInput = "NameThatTuneLab/1.0 (retry@example.com)"
            vm.onMusicBrainzUserAgentChanged(agentInput)
            vm.onVoiceOverDelayChanged(SAVE_DELAY_MS.toString())
            vm.saveSettings()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertNotNull(state.errorMessage)
            // Pending inputs retained so the user can retry (Q1 = retain)
            assertEquals(agentInput, state.musicBrainzUserAgentInput)
            assertEquals(SAVE_DELAY_MS.toString(), state.voiceOverDelayMsInput)
        }
    }

    companion object {
        private const val TRACK_TITLE = "Imagine"
        private const val TRACK_ARTIST = "John Lennon"
        private const val ERROR_MESSAGE = "API down"
        private const val EXPECTED_DELAY_MS = 2000L
        private const val SAVE_DELAY_MS = 1750L
    }
}
