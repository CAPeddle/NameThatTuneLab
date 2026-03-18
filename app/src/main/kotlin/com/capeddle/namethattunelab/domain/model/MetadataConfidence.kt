package com.capeddle.namethattunelab.domain.model

/**
 * Confidence level of a metadata lookup result.
 *
 * HIGH   — Exact title + artist match with a confirmed release date.
 * MEDIUM — Fuzzy match or partial date available.
 * LOW    — Weak match; year may be inaccurate.
 * NONE   — No match found; year unavailable.
 */
enum class MetadataConfidence {
    HIGH,
    MEDIUM,
    LOW,
    NONE
}
