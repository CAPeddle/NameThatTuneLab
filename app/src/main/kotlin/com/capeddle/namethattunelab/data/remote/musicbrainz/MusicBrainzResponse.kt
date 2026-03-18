package com.capeddle.namethattunelab.data.remote.musicbrainz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Top-level response from /ws/2/recording?query=…&fmt=json&limit=5
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class MusicBrainzRecordingSearchResponse(
    @SerialName("recordings") val recordings: List<MbRecording> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────────
// Recording
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class MbRecording(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("score") val score: Int = 0,
    @SerialName("artist-credit") val artistCredit: List<MbArtistCredit> = emptyList(),
    @SerialName("releases") val releases: List<MbRelease> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────────
// Artist credit inside a recording
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class MbArtistCredit(
    @SerialName("name") val name: String? = null,
    @SerialName("artist") val artist: MbArtist? = null
)

@Serializable
data class MbArtist(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("sort-name") val sortName: String = ""
)

// ─────────────────────────────────────────────────────────────────────────────
// Release
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class MbRelease(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("date") val date: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("release-group") val releaseGroup: MbReleaseGroup? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Release group — carries primary-type ("Album", "Single", etc.)
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class MbReleaseGroup(
    @SerialName("id") val id: String = "",
    @SerialName("primary-type") val primaryType: String? = null
)
