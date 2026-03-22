package com.capeddle.namethattunelab.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capeddle.namethattunelab.R
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.presentation.component.PermissionStatusBar
import com.capeddle.namethattunelab.presentation.component.RecentTracksList
import com.capeddle.namethattunelab.presentation.component.TrackCard
import com.capeddle.namethattunelab.presentation.theme.NtlTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier, viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainScreenContent(
        uiState = uiState,
        onDismissError = viewModel::dismissError,
        onMusicBrainzUserAgentChanged = viewModel::onMusicBrainzUserAgentChanged,
        onVoiceOverDelayChanged = viewModel::onVoiceOverDelayChanged,
        onSaveSettings = viewModel::saveSettings,
        modifier = modifier
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
@Suppress("UnusedPrivateMember", "MagicNumber")
private fun PreviewMainScreenDefault() {
    NtlTheme {
        MainScreenContent(
            uiState = MainUiState(
                currentTrack = TrackMetadata(
                    title = "Bohemian Rhapsody",
                    artist = "Queen",
                    album = "A Night at the Opera",
                    year = 1975,
                    confidence = MetadataConfidence.HIGH
                ),
                recentTracks = listOf(
                    TrackMetadata("Hotel California", "Eagles", null, 1977, MetadataConfidence.HIGH),
                    TrackMetadata("Imagine", "John Lennon", "Imagine", 1971, MetadataConfidence.MEDIUM)
                ),
                isListening = true,
                musicBrainzUserAgentInput = "NameThatTuneLab/1.0 (you@example.com)",
                voiceOverDelayMsInput = "1000"
            ),
            onDismissError = {},
            onMusicBrainzUserAgentChanged = {},
            onVoiceOverDelayChanged = {},
            onSaveSettings = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreenContent(
    uiState: MainUiState,
    onDismissError: () -> Unit,
    onMusicBrainzUserAgentChanged: (String) -> Unit,
    onVoiceOverDelayChanged: (String) -> Unit,
    onSaveSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            onDismissError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PermissionStatusBar(isGranted = uiState.isListening)

            Spacer(modifier = Modifier.height(16.dp))

            CurrentTrackSection(uiState = uiState)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.recent_tracks),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            RecentTracksList(
                tracks = uiState.recentTracks,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            SettingsPanel(
                uiState = uiState,
                onMusicBrainzUserAgentChanged = onMusicBrainzUserAgentChanged,
                onVoiceOverDelayChanged = onVoiceOverDelayChanged,
                onSaveSettings = onSaveSettings
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CurrentTrackSection(uiState: MainUiState, modifier: Modifier = Modifier) {
    uiState.currentTrack?.let { track ->
        TrackCard(
            metadata = track,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    } ?: Text(
        text = stringResource(R.string.no_track_playing),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun SettingsPanel(
    uiState: MainUiState,
    onMusicBrainzUserAgentChanged: (String) -> Unit,
    onVoiceOverDelayChanged: (String) -> Unit,
    onSaveSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.musicBrainzUserAgentInput,
            onValueChange = onMusicBrainzUserAgentChanged,
            label = { Text(text = stringResource(R.string.settings_musicbrainz_user_agent_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.voiceOverDelayMsInput,
            onValueChange = onVoiceOverDelayChanged,
            label = { Text(text = stringResource(R.string.settings_voice_over_delay_ms_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSaveSettings,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = stringResource(R.string.settings_save_button))
        }
    }
}
