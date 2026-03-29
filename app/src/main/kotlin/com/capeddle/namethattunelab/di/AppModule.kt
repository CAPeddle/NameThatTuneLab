package com.capeddle.namethattunelab.di

import com.capeddle.namethattunelab.nowplaying.MediaPackageAllowlistPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

/**
 * Application-level bindings:
 * - Named [CoroutineDispatcher]s (`io`, `default`, `main`)
 * - Ktor [HttpClient] (configured for MusicBrainz JSON responses)
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ─────────────────────────────────────────────────────────────────────────
    // Dispatchers
    // ─────────────────────────────────────────────────────────────────────────

    @Provides
    @Named("io")
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Named("default")
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Named("main")
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Named(MediaPackageAllowlistPolicy.ALLOWLIST_QUALIFIER)
    fun provideMediaSourceAllowlist(): Set<String> = MediaPackageAllowlistPolicy.DEFAULT_ALLOWED_PACKAGES

    // ─────────────────────────────────────────────────────────────────────────
    // Networking
    // ─────────────────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) { json(json) }
        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : Logger {
                override fun log(message: String) {
                    android.util.Log.d("NTL:Ktor", message)
                }
            }
        }
    }
}
