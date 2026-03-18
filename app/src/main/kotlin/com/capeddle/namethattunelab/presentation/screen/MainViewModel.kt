package com.capeddle.namethattunelab.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.domain.usecase.AnnounceTrackUseCase
import com.capeddle.namethattunelab.domain.usecase.ObserveNowPlayingUseCase
import com.capeddle.namethattunelab.domain.usecase.ResolveMetadataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class MainUiState(
    val currentTrack: TrackMetadata? = null,
    val recentTracks: List<TrackMetadata> = emptyList(),
    val isListening: Boolean = false,
    val errorMessage: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class MainViewModel @Inject constructor(
    private val observeNowPlaying: ObserveNowPlayingUseCase,
    private val resolveMetadata: ResolveMetadataUseCase,
    private val announceTrack: AnnounceTrackUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        startListening()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pipeline
    // ─────────────────────────────────────────────────────────────────────────

    private fun startListening() {
        _uiState.update { it.copy(isListening = true) }

        observeNowPlaying()
            .onEach { event -> handleNowPlayingEvent(event) }
            .catch { throwable ->
                _uiState.update { it.copy(isListening = false, errorMessage = throwable.message) }
            }
            .launchIn(viewModelScope)
    }

    private fun handleNowPlayingEvent(event: NowPlayingEvent) {
        viewModelScope.launch {
            // Show placeholder while metadata resolves
            val placeholder = TrackMetadata(
                title = event.title,
                artist = event.artist,
                album = event.album,
                year = null,
                confidence = MetadataConfidence.NONE
            )
            _uiState.update { it.copy(currentTrack = placeholder, errorMessage = null) }

            // Resolve enriched metadata
            resolveMetadata(event)
                .onSuccess { metadata ->
                    _uiState.update { state ->
                        val updated = state.copy(
                            currentTrack = metadata,
                            recentTracks = buildRecentList(metadata, state.recentTracks)
                        )
                        updated
                    }
                    announceTrack(metadata)
                }
                .onFailure { err ->
                    // Fallback: keep the placeholder visible and add it to history
                    _uiState.update { state ->
                        state.copy(
                            recentTracks = buildRecentList(placeholder, state.recentTracks),
                            errorMessage = "Could not fetch metadata: ${err.message}"
                        )
                    }
                    announceTrack(placeholder)
                }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // User actions
    // ─────────────────────────────────────────────────────────────────────────

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildRecentList(latest: TrackMetadata, existing: List<TrackMetadata>): List<TrackMetadata> {
        val deduped = (listOf(latest) + existing)
            .distinctBy { "${it.artist.lowercase()}|${it.title.lowercase()}" }
        return deduped.take(MAX_RECENT_TRACKS)
    }

    private companion object {
        const val MAX_RECENT_TRACKS = 20
    }
}
