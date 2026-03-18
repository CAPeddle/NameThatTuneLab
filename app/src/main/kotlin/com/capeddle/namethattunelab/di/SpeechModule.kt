package com.capeddle.namethattunelab.di

import com.capeddle.namethattunelab.domain.SpeechAnnouncer
import com.capeddle.namethattunelab.speech.TtsAnnouncer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the [SpeechAnnouncer] domain interface to its [TtsAnnouncer] implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechModule {

    @Binds
    @Singleton
    abstract fun bindSpeechAnnouncer(impl: TtsAnnouncer): SpeechAnnouncer
}
