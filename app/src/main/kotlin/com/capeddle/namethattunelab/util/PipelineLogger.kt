package com.capeddle.namethattunelab.util

import android.util.Log
import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Structured Logcat logger for the NowPlaying detection pipeline.
 *
 * Every stage of the pipeline emits a tagged log entry so the full pipeline
 * can be traced in Logcat using the filter: `tag:NTL`
 *
 * Tags:
 *  - `NTL:NowPlaying`  — media session detection events
 *  - `NTL:Metadata`    — metadata lookup requests and results
 *  - `NTL:Cache`       — Room cache hit/miss events
 *  - `NTL:Speech`      — TTS announcement triggers
 */
@Singleton
class PipelineLogger @Inject constructor() {

    fun logDetection(event: NowPlayingEvent) {
        Log.d(TAG_NOW_PLAYING, "Detected: \"${event.title}\" by ${event.artist} (source: ${event.sourceApp})")
    }

    fun logLookup(artist: String, title: String) {
        Log.d(TAG_METADATA, "Looking up: \"$title\" by $artist")
    }

    fun logLookupResult(artist: String, title: String, result: Result<TrackMetadata>) {
        if (result.isSuccess) {
            val meta = result.getOrNull()
            Log.d(
                TAG_METADATA,
                "Lookup OK: \"${meta?.title}\" by ${meta?.artist}" +
                    " — year=${meta?.year} confidence=${meta?.confidence}"
            )
        } else {
            Log.w(TAG_METADATA, "Lookup FAILED for \"$title\" by $artist: ${result.exceptionOrNull()?.message}")
        }
    }

    fun logCacheHit(artist: String, title: String) {
        Log.d(TAG_CACHE, "Cache HIT: \"$title\" by $artist")
    }

    fun logCacheMiss(artist: String, title: String) {
        Log.d(TAG_CACHE, "Cache MISS: \"$title\" by $artist — fetching from provider")
    }

    fun logSpeech(text: String) {
        Log.d(TAG_SPEECH, "Speaking: \"$text\"")
    }

    fun logSpeechSkipped(artist: String, title: String) {
        Log.d(TAG_SPEECH, "Skipped (cooldown): \"$title\" by $artist")
    }

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }

    companion object {
        const val TAG_NOW_PLAYING = "NTL:NowPlaying"
        const val TAG_METADATA = "NTL:Metadata"
        const val TAG_CACHE = "NTL:Cache"
        const val TAG_SPEECH = "NTL:Speech"
    }
}
