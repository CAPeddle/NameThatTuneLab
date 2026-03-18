package com.capeddle.namethattunelab.speech

import android.media.AudioFocusRequest
import android.media.AudioManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AudioFocusManagerTest {

    private val audioManager: AudioManager = mockk()
    private val focusRequest: AudioFocusRequest = mockk()

    private val manager = AudioFocusManager.createForTest(audioManager, focusRequest)

    @Test
    fun `request returns true when audio focus granted`() {
        every { audioManager.requestAudioFocus(focusRequest) } returns AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        val granted = manager.request()

        assertTrue(granted)
    }

    @Test
    fun `request returns false when audio focus denied`() {
        every { audioManager.requestAudioFocus(focusRequest) } returns AudioManager.AUDIOFOCUS_REQUEST_FAILED

        val granted = manager.request()

        assertFalse(granted)
    }

    @Test
    fun `abandon forwards to audio manager`() {
        every { audioManager.abandonAudioFocusRequest(focusRequest) } returns AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        manager.abandon()

        verify(exactly = 1) { audioManager.abandonAudioFocusRequest(focusRequest) }
    }
}
