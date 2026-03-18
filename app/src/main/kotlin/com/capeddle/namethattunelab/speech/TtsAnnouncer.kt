package com.capeddle.namethattunelab.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.capeddle.namethattunelab.domain.SpeechAnnouncer
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.util.PipelineLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * [SpeechAnnouncer] implementation backed by Android [TextToSpeech].
 *
 * Responsibilities:
 * - Initialises TTS engine lazily on first use.
 * - Checks [AnnouncementGuard] before every utterance.
 * - Requests audio focus via [AudioFocusManager] (TRANSIENT_MAY_DUCK) and
 *   releases it after the utterance completes.
 * - Suspends until the utterance finishes (or fails) using [suspendCancellableCoroutine].
 */
@Singleton
class TtsAnnouncer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioFocusManager: AudioFocusManager,
    private val announcementGuard: AnnouncementGuard,
    private val logger: PipelineLogger
) : SpeechAnnouncer {

    @Volatile private var tts: TextToSpeech? = null

    @Volatile private var ttsReady: Boolean = false

    // ─────────────────────────────────────────────────────────────────────────
    // SpeechAnnouncer
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun announce(metadata: TrackMetadata): Result<Unit> {
        if (!announcementGuard.shouldAnnounce(metadata)) {
            logger.logSpeechSkipped(metadata.artist, metadata.title)
            return Result.success(Unit)
        }

        val engine = getOrInitTts()
            ?: return Result.failure(IllegalStateException("TTS engine failed to initialise"))

        if (!audioFocusManager.request()) {
            logger.logError("TTS", "Audio focus request denied — skipping announcement", null)
            return Result.failure(IllegalStateException("Audio focus denied"))
        }

        return try {
            speak(engine, buildUtterance(metadata))
        } finally {
            audioFocusManager.abandon()
        }
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TTS initialisation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the ready [TextToSpeech] instance, initialising it if necessary.
     * Returns `null` if initialisation failed.
     */
    private suspend fun getOrInitTts(): TextToSpeech? {
        tts?.takeIf { ttsReady }?.let { return it }

        return suspendCancellableCoroutine { cont ->
            var engine: TextToSpeech? = null
            engine = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    engine?.language = Locale.getDefault()
                    tts = engine
                    ttsReady = true
                    cont.resume(engine)
                } else {
                    ttsReady = false
                    cont.resume(null)
                }
            }
            cont.invokeOnCancellation { engine?.shutdown() }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utterance
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Speak [text] and suspend until the utterance completes or errors.
     */
    private suspend fun speak(engine: TextToSpeech, text: String): Result<Unit> = suspendCancellableCoroutine { cont ->
        val utteranceId = UUID.randomUUID().toString()

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String) = Unit

            override fun onDone(id: String) {
                if (id == utteranceId) {
                    logger.logSpeech(text)
                    cont.resume(Result.success(Unit))
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String) {
                if (id == utteranceId) {
                    val ex = RuntimeException("TTS utterance failed for id=$id")
                    logger.logError("TTS", "Utterance error id=$id", ex)
                    cont.resume(Result.failure(ex))
                }
            }
        })

        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            val ex = RuntimeException("TTS.speak returned ERROR for '$text'")
            logger.logError("TTS", "speak() returned ERROR", ex)
            cont.resume(Result.failure(ex))
        }

        cont.invokeOnCancellation { engine.stop() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Formatting
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the announcement string: "Title — Artist — Year" (year omitted if null).
     */
    private fun buildUtterance(metadata: TrackMetadata): String {
        val parts = buildList {
            add(metadata.title)
            add(metadata.artist)
            metadata.year?.let { add(it.toString()) }
        }
        return parts.joinToString(separator = " \u2014 ") // em dash with surrounding spaces
    }
}
