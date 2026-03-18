package com.capeddle.namethattunelab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Room DAO for the metadata cache table.
 *
 * Keys are lower-cased + trimmed (handled in the mapper) so all lookups
 * are case-insensitive.
 */
@Dao
interface MetadataCacheDao {

    /**
     * Returns the cached entry for the given [artistKey] / [titleKey] pair,
     * or `null` if there is no matching row.
     */
    @Query(
        """
        SELECT * FROM metadata_cache
        WHERE artist_key = :artistKey
          AND title_key  = :titleKey
        LIMIT 1
        """
    )
    suspend fun get(artistKey: String, titleKey: String): MetadataCacheEntity?

    /**
     * Inserts or replaces a cache entry.
     * REPLACE strategy updates [MetadataCacheEntity.cachedAt] so stale rows are refreshed.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MetadataCacheEntity)

    /**
     * Deletes all cache entries older than [thresholdMs] (epoch-millis).
     * Call periodically to prevent unbounded growth.
     */
    @Query("DELETE FROM metadata_cache WHERE cached_at < :thresholdMs")
    suspend fun deleteOlderThan(thresholdMs: Long)
}
