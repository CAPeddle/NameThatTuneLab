package com.capeddle.namethattunelab.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity that caches resolved [com.capeddle.namethattunelab.domain.model.TrackMetadata].
 *
 * Primary key is the composite (artist, title) pair, normalised to lower-case
 * trimmed strings so cache hits are case-insensitive.
 *
 * [cachedAt] stores the epoch-millisecond timestamp used to enforce TTL in
 * [MetadataRepositoryImpl].
 */
@Entity(
    tableName = "metadata_cache",
    primaryKeys = ["artist_key", "title_key"]
)
data class MetadataCacheEntity(
    @ColumnInfo(name = "artist_key") val artistKey: String,
    @ColumnInfo(name = "title_key") val titleKey: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist") val artist: String,
    @ColumnInfo(name = "album") val album: String?,
    @ColumnInfo(name = "year") val year: Int?,
    @ColumnInfo(name = "confidence") val confidence: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Long
)
