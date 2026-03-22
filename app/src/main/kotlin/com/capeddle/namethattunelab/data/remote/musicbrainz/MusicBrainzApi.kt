package com.capeddle.namethattunelab.data.remote.musicbrainz

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter

/**
 * Thin Ktor wrapper around the MusicBrainz recording search endpoint.
 *
 * **Must not** be called more than once per second (MusicBrainz rate-limit policy).
 * Rate-limiting is enforced by [MusicBrainzProvider].
 *
 * Base URL and User-Agent are injected so this class stays testable.
 */
class MusicBrainzApi(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://musicbrainz.org"
) {
    /**
     * Searches for recordings matching [artist] + [title].
     *
     * Lucene query: `artist:{artist} AND recording:{title}`
     * Returns a maximum of [limit] results.
     *
     * @throws io.ktor.client.plugins.ClientRequestException on 4xx responses.
     * @throws io.ktor.client.plugins.ServerResponseException on 5xx responses.
     * @throws kotlinx.io.IOException on network failures.
     */
    @Suppress("LongParameterList")
    suspend fun searchRecordings(
        artist: String,
        title: String,
        userAgent: String,
        limit: Int = 5
    ): MusicBrainzRecordingSearchResponse {
        val query = buildLucene(artist, title)
        return httpClient.get("$baseUrl/ws/2/recording") {
            headers { append("User-Agent", userAgent) }
            parameter("query", query)
            parameter("fmt", "json")
            parameter("limit", limit)
        }.body()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a Lucene search query string.
     * Special Lucene characters in [artist] and [title] are escaped to prevent
     * syntax errors on the server side.
     */
    private fun buildLucene(artist: String, title: String): String {
        val escapedArtist = escapeLucene(artist)
        val escapedTitle = escapeLucene(title)
        return "artist:$escapedArtist AND recording:$escapedTitle"
    }

    /**
     * Escapes Lucene special characters:
     * + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
     */
    private fun escapeLucene(input: String): String {
        val specialChars = """[\+\-\&\|\!\(\)\{\}\[\]\^\"\~\*\?\:\\\/]""".toRegex()
        val escaped = specialChars.replace(input) { "\\${it.value}" }
        // Wrap in quotes for exact-phrase matching if the input contains spaces
        return if (escaped.contains(' ')) "\"$escaped\"" else escaped
    }
}
