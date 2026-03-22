package com.capeddle.namethattunelab.data.remote.musicbrainz

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MusicBrainzApiTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `searchRecordings returns parsed recordings`() = runTest {
        val userAgent = "NameThatTuneLab/2.0 (tests@example.com)"
        val mockEngine = MockEngine { request ->
            assertEquals(userAgent, request.headers[HttpHeaders.UserAgent])
            assertTrue(request.url.parameters["query"]?.contains(ARTIST) == true)
            respond(
                content = """
                    {
                      "recordings": [
                        {
                          "id": "test-id",
                          "title": "Bohemian Rhapsody",
                          "score": $PERFECT_SCORE,
                          "artist-credit": [
                            { "name": "$ARTIST", "artist": { "id": "a-id", "name": "$ARTIST", "sort-name": "$ARTIST" } }
                          ],
                          "releases": [
                            {
                              "id": "r-id",
                              "title": "A Night at the Opera",
                              "date": "1975-11-21",
                              "status": "Official",
                              "release-group": { "id": "rg-id", "primary-type": "Album" }
                            }
                          ]
                        }
                      ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val api = MusicBrainzApi(httpClient = client)
        val response = api.searchRecordings(artist = ARTIST, title = "Bohemian Rhapsody", userAgent = userAgent)

        assertEquals(1, response.recordings.size)
        val recording = response.recordings.first()
        assertEquals("Bohemian Rhapsody", recording.title)
        assertEquals(PERFECT_SCORE, recording.score)
        assertEquals(ARTIST, recording.artistCredit.firstOrNull()?.name)
        assertEquals(1, recording.releases.size)
        assertEquals("1975-11-21", recording.releases.first().date)
    }

    @Test
    fun `searchRecordings returns empty list on empty recordings response`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{ "recordings": [] }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val api = MusicBrainzApi(httpClient = client)
        val response = api.searchRecordings(
            artist = "Unknown",
            title = "Unknown",
            userAgent = "NameThatTuneLab/1.0 (default@example.com)"
        )

        assertTrue(response.recordings.isEmpty())
    }

    companion object {
        private const val ARTIST = "Queen"
        private const val PERFECT_SCORE = 100
    }
}
