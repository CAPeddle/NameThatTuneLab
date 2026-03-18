package com.capeddle.namethattunelab.di

import com.capeddle.namethattunelab.data.remote.MetadataProvider
import com.capeddle.namethattunelab.data.remote.musicbrainz.MusicBrainzApi
import com.capeddle.namethattunelab.data.remote.musicbrainz.MusicBrainzProvider
import com.capeddle.namethattunelab.data.repository.MetadataRepositoryImpl
import com.capeddle.namethattunelab.data.repository.NowPlayingRepositoryImpl
import com.capeddle.namethattunelab.domain.repository.MetadataRepository
import com.capeddle.namethattunelab.domain.repository.NowPlayingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

/**
 * Binds data-layer implementations to their domain interfaces.
 *
 * Split into a [Binds] abstract class + [Provides] companion object because
 * Dagger requires `@Binds` and `@Provides` to be in the same logical module
 * but `@Binds` must be abstract.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindNowPlayingRepository(impl: NowPlayingRepositoryImpl): NowPlayingRepository

    @Binds
    @Singleton
    abstract fun bindMetadataRepository(impl: MetadataRepositoryImpl): MetadataRepository

    @Binds
    @Singleton
    abstract fun bindMetadataProvider(impl: MusicBrainzProvider): MetadataProvider

    companion object {
        @Provides
        @Singleton
        fun provideMusicBrainzApi(httpClient: HttpClient): MusicBrainzApi = MusicBrainzApi(httpClient = httpClient)
    }
}
