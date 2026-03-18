package com.capeddle.namethattunelab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Single Room database instance for NameThatTuneLab.
 *
 * To perform a schema migration in the future, add a [androidx.room.migration.Migration]
 * to the `DatabaseModule` builder and increment [version].
 */
@Database(
    entities = [MetadataCacheEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metadataCacheDao(): MetadataCacheDao
}
