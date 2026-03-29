package com.capeddle.namethattunelab.nowplaying

import javax.inject.Inject
import javax.inject.Named

/**
 * Explicit package-level policy used to gate which media sources can emit now-playing events.
 */
class MediaPackageAllowlistPolicy @Inject constructor(
    @Named(ALLOWLIST_QUALIFIER) private val allowedPackages: Set<String>
) {

    constructor(allowedPackages: Set<String>, normalize: Boolean = true) : this(
        if (normalize) {
            allowedPackages.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        } else {
            allowedPackages
        }
    )

    fun isAllowed(packageName: String): Boolean = packageName in allowedPackages

    companion object {
        const val ALLOWLIST_QUALIFIER = "media_source_allowlist"

        val DEFAULT_ALLOWED_PACKAGES: Set<String> = setOf(
            "com.spotify.music",
            "com.google.android.apps.youtube.music",
            "com.apple.android.music",
            "com.aspiro.tidal",
            "pandora.app",
            "deezer.android.app",
            "com.soundcloud.android"
        )
    }
}
