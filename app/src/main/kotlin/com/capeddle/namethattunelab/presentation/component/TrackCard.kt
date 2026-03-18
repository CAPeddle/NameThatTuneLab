package com.capeddle.namethattunelab.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.presentation.theme.NtlTheme

/**
 * Displays a single resolved [TrackMetadata] in a Material 3 card.
 *
 * Shows title, artist, album (if present) and year (if available).
 * A confidence chip is shown in the top-right corner.
 */
@Composable
fun TrackCard(metadata: TrackMetadata, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = metadata.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                ConfidenceChip(confidence = metadata.confidence)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = metadata.artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            metadata.album?.let { album ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = album,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            metadata.year?.let { year ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private sub-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConfidenceChip(confidence: MetadataConfidence, modifier: Modifier = Modifier) {
    val (label, color) = when (confidence) {
        MetadataConfidence.HIGH -> "High" to MaterialTheme.colorScheme.tertiary
        MetadataConfidence.MEDIUM -> "Med" to MaterialTheme.colorScheme.secondary
        MetadataConfidence.LOW -> "Low" to MaterialTheme.colorScheme.outline
        MetadataConfidence.NONE -> "—" to MaterialTheme.colorScheme.outline
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier.padding(start = 8.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
@Suppress("UnusedPrivateMember", "MagicNumber")
private fun PreviewTrackCardDefault() {
    NtlTheme {
        TrackCard(
            metadata = TrackMetadata(
                title = "Bohemian Rhapsody",
                artist = "Queen",
                album = "A Night at the Opera",
                year = 1975,
                confidence = MetadataConfidence.HIGH
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
