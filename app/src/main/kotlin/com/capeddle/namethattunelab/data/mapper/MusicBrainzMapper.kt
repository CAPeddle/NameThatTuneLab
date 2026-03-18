package com.capeddle.namethattunelab.data.mapper

import com.capeddle.namethattunelab.data.remote.musicbrainz.MbRecording
import com.capeddle.namethattunelab.data.remote.musicbrainz.MbRelease
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata

/**
 * Maps a [MbRecording] (MusicBrainz API model) to a [TrackMetadata] domain model.
 *
 * **Confidence heuristic:**
 * - [MetadataConfidence.HIGH]   — best-scoring recording has a release with an Official
 *                                  status AND a parseable release date.
 * - [MetadataConfidence.MEDIUM] — year found but no Official status, or status absent.
 * - [MetadataConfidence.LOW]    — recording matched but no year could be extracted.
 * - [MetadataConfidence.NONE]   — result list was empty or score < threshold.
 */
object MusicBrainzMapper {

    private const val MIN_ACCEPTABLE_SCORE = 60
    private const val YEAR_DIGITS = 4
    private const val MIN_YEAR = 1860
    private const val MAX_YEAR = 2100

    /**
     * Converts the top-scoring [MbRecording] from a search result into [TrackMetadata].
     * [recordings] is the ordered list from the API (best score first).
     *
     * Returns `null` if the list is empty or the top score is below [MIN_ACCEPTABLE_SCORE].
     */
    fun map(recordings: List<MbRecording>, originalTitle: String, originalArtist: String): TrackMetadata? {
        val best = recordings.firstOrNull() ?: return null
        if (best.score < MIN_ACCEPTABLE_SCORE) return null

        val artistName = best.artistCredit
            .firstOrNull()
            ?.let { it.name ?: it.artist?.name }
            ?: originalArtist

        val resolvedTitle = best.title.ifBlank { originalTitle }

        val (year, confidence) = extractYearAndConfidence(best.releases)

        return TrackMetadata(
            title = resolvedTitle,
            artist = artistName,
            album = bestAlbumTitle(best.releases),
            year = year,
            confidence = confidence
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Picks the earliest Official release with a valid date, falling back to
     * any release with a valid date if no Official one exists.
     */
    private fun extractYearAndConfidence(releases: List<MbRelease>): Pair<Int?, MetadataConfidence> {
        if (releases.isEmpty()) return Pair(null, MetadataConfidence.LOW)

        val official = releases.filter { it.status.equals("Official", ignoreCase = true) }
        val withDate = releases.filter { it.date?.isNotBlank() == true }
        val officialWithDate = official.filter { it.date?.isNotBlank() == true }

        val bestRelease = officialWithDate.minByOrNull { it.date.orEmpty() }
            ?: withDate.minByOrNull { it.date.orEmpty() }

        val year = bestRelease?.date?.let { parseYear(it) }

        val confidence = when {
            year != null && officialWithDate.isNotEmpty() -> MetadataConfidence.HIGH
            year != null -> MetadataConfidence.MEDIUM
            else -> MetadataConfidence.LOW
        }

        return Pair(year, confidence)
    }

    /**
     * Extracts the four-digit year from a MusicBrainz date string.
     * Accepts formats: "YYYY", "YYYY-MM", "YYYY-MM-DD".
     * Returns `null` if the string is malformed.
     */
    private fun parseYear(date: String): Int? =
        date.trim().take(YEAR_DIGITS).toIntOrNull()?.takeIf { it in MIN_YEAR..MAX_YEAR }

    /**
     * Returns the title of the best (Official + dated, falling back to any) album release,
     * or `null` if no release has type "Album".
     */
    private fun bestAlbumTitle(releases: List<MbRelease>): String? = releases
        .filter { it.releaseGroup?.primaryType.equals("Album", ignoreCase = true) }
        .firstOrNull()
        ?.title
}
