package com.capeddle.namethattunelab.di

import android.content.Context
import androidx.room.Room
import com.capeddle.namethattunelab.data.local.AppDatabase
import com.capeddle.namethattunelab.data.local.MetadataCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Room [AppDatabase] and all derived DAOs.
 *
 * The database is a singleton — a single connection is shared across the
 * entire process lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "ntl_metadata.db"
    ).build()

    @Provides
    @Singleton
    fun provideMetadataCacheDao(db: AppDatabase): MetadataCacheDao = db.metadataCacheDao()
}
