package com.capeddle.namethattunelab.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capeddle.namethattunelab.R
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.presentation.theme.NtlTheme

/**
 * Scrollable list of the most recently detected / resolved tracks.
 *
 * An empty-state label is shown when [tracks] is empty.
 */
@Composable
fun RecentTracksList(tracks: List<TrackMetadata>, modifier: Modifier = Modifier) {
    if (tracks.isEmpty()) {
        Text(
            text = stringResource(R.string.no_recent_tracks),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = tracks,
            key = { track -> "${track.artist}|${track.title}" }
        ) { track ->
            TrackCard(
                metadata = track,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, heightDp = 400)
@Composable
@Suppress("UnusedPrivateMember", "MagicNumber")
private fun PreviewRecentTracksListDefault() {
    NtlTheme {
        RecentTracksList(
            tracks = listOf(
                TrackMetadata("Bohemian Rhapsody", "Queen", "A Night at the Opera", 1975, MetadataConfidence.HIGH),
                TrackMetadata("Hotel California", "Eagles", "Hotel California", 1977, MetadataConfidence.HIGH),
                TrackMetadata("Smells Like Teen Spirit", "Nirvana", "Nevermind", 1991, MetadataConfidence.MEDIUM)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("UnusedPrivateMember")
private fun PreviewRecentTracksListEmpty() {
    NtlTheme { RecentTracksList(tracks = emptyList()) }
}
