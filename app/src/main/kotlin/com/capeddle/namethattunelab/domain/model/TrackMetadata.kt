package com.capeddle.namethattunelab.domain.model

/**
 * Enriched track metadata, including release year resolved from an external provider.
 *
 * @property title      Track title.
 * @property artist     Artist name.
 * @property album      Album name, if known.
 * @property year       Release year, or null if it could not be resolved.
 * @property confidence Confidence level of the metadata lookup.
 */
data class TrackMetadata(
    val title: String,
    val artist: String,
    val album: String?,
    val year: Int?,
    val confidence: MetadataConfidence
)
