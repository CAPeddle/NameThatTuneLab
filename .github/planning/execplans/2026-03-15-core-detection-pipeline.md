# NameThatTuneLab — Core Detection Pipeline

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** 2026-03-15  
**Status:** ✅ Implementation Complete — awaiting build validation  
**Owner:** Overlord Agent  
**Refs:** User brief (NameThatTuneLab initial instructions)  
**Revision:** v4

---

## Purpose / Big Picture

Build the first runnable version of **NameThatTuneLab** — an Android application that detects the currently playing song on any music player, enriches the track with its release year via MusicBrainz, and announces the result through text-to-speech.

**Observable outcome:** A user installs the debug APK, grants notification listener permission, plays a song in Spotify/YouTube Music, and hears:

> "Smells Like Teen Spirit — Nirvana — 1991"

The app shows a minimal UI with the current track, artist, year, last announcement timestamp, and permission status.

**Term definitions:**

- *MediaSession:* Android framework API (`android.media.session`) that exposes metadata from media-playing apps.
- *NotificationListenerService:* Android system service that allows an app to read all notifications — used here to observe `MediaSession` tokens published by music players.
- *MusicBrainz:* An open music encyclopedia and metadata database with a free REST API. Primary source for release year resolution.
- *TTS:* Android `TextToSpeech` engine — converts text to spoken audio.
- *Audio ducking:* Temporarily lowering the volume of other audio (music) while speaking, using `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`.
- *Debounce:* Delaying execution until a burst of rapid events settles (1–2 seconds window).
- *Announcement guard:* Preventing repeated announcements of the same track within a cooldown period (30 seconds).
- *NowPlayingEvent:* Domain entity emitted when active media track changes — contains title, artist, album, sourceApp.
- *TrackMetadata:* Domain entity with enriched data — title, artist, year, confidence.
- *MetadataProvider:* Interface for external metadata resolution (MusicBrainz, Discogs, Last.fm).
- *Pipeline:* The sequential flow: detect → enrich → announce.

---

## Progress

- [x] Milestone 1 — Project Bootstrap (2026-03-15 — awaiting build validation)
- [x] Milestone 2 — Domain Layer (complete)
- [x] Milestone 3 — NowPlaying Detection (complete)
- [x] Milestone 4 — Metadata Enrichment (MusicBrainz + Room cache) (complete)
- [x] Milestone 5 — Speech Output (TTS + debounce + audio focus) (complete)
- [x] Milestone 6 — Presentation (Minimal UI + permissions) (complete)
- [x] Milestone 7 — Pipeline Integration & Wiring (complete)
- [x] Milestone 8 — Testing Suite (complete — 10 test files, ~60 tests)
- [x] Milestone 9 — Code Review & Quality Gate (build validated ✅ — `app-debug.apk` 27.5 MB produced)

---

## Surprises & Discoveries

- AGP 8.7.3 requires Gradle 8.9 minimum (not 8.7 as initially assumed). Wrapper updated to `gradle-8.9-bin.zip`. (2026-03-16)
- `local.properties` reverted to username `Chris` on session reload — corrected to `cpeddle`. (2026-03-16)
- Build-tools 34.0.0 was a Linux-format installation (missing `.exe` files). Switched to `buildToolsVersion = "36.1.0"` which has proper Windows executables. (2026-03-16)
- `compileSdk`/`targetSdk` bumped from 34 → 36 because `androidx.core:1.15.0` and `activity:1.10.1` require compileSdk ≥ 35, and only android-34 and android-36 are installed locally. (2026-03-16)
- `PipelineLogger.logLookup` was defined with `result: Result<TrackMetadata>` parameter but called with only `(artist, title)` before the lookup executes — signature split into `logLookup(artist, title)` and `logLookupResult(artist, title, result)`. (2026-03-16)
- `PipelineLogger.logSpeechSkipped` was defined as `(reason: String)` but called with `(artist, title)` — updated to `(artist: String, title: String)`. (2026-03-16)
- `NowPlayingListenerService.componentName` was unresolved — replaced with explicit `ComponentName(this, NowPlayingListenerService::class.java)`. (2026-03-16)
- `MusicBrainzProvider.lookup` used `return` inside `withLock {}` (non-inline lambda) which is a compile error — refactored to use `withLock` only for rate limiting and `return` from the function body. (2026-03-16)

---

## Decision Log

- **Decision:** Package-level separation in single `:app` module (not multi-module).  
  Rationale: Faster MVP delivery; modules can be extracted later.  
  Date: 2026-03-15

- **Decision:** Root package = `com.capeddle.namethattunelab`.  
  Rationale: Avoids generic names, matches developer identity.  
  Date: 2026-03-15

- **Decision:** MusicBrainz only for MVP; Discogs/Last.fm as future stubs.  
  Rationale: MusicBrainz is free with no API key. Sufficient for prototype.  
  Date: 2026-03-15

- **Decision:** `foregroundServiceType="mediaPlayback"`.  
  Rationale: Lower Play Store rejection risk than `specialUse`; app interacts with media sessions.  
  Date: 2026-03-15

- **Decision:** `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` for TTS.  
  Rationale: Music dips briefly rather than pausing; least disruptive UX.  
  Date: 2026-03-15

- **Decision:** On metadata failure, announce without year (option C).  
  Rationale: Still useful; doesn't show misleading "Unknown".  
  Date: 2026-03-15

- **Decision:** Room for local metadata cache.  
  Rationale: Structured DB supports future listening statistics; aligns with governance stack.  
  Date: 2026-03-15

- **Decision:** `minSdk = 29`, `compileSdk = 34`, `targetSdk = 34`.  
  Rationale: Broader device support; MediaSession APIs stable from API 29.  
  Date: 2026-03-15

- **Decision:** Debug builds only; no release signing for MVP.  
  Rationale: Prototype phase; release signing added in a future plan.  
  Date: 2026-03-15

- **Decision:** MusicBrainz User-Agent contact email = `chrisapeddle@gmail.com`.  
  Rationale: Required by MusicBrainz API policy; identifies the app owner.  
  Date: 2026-03-15

- **Decision:** 1–2 second debounce window for track change events.  
  Rationale: Media sessions fire multiple metadata updates per track change.  
  Date: 2026-03-15

- **Decision:** 30-second cooldown before re-announcing the same track.  
  Rationale: Prevents repetitive announcements from looping/scrubbing.  
  Date: 2026-03-15

---

## Outcomes & Retrospective

*(Complete after plan closes.)*

**What was achieved:**

**What remains (if anything):**

**Patterns to promote:**

**Reusable findings:**

**New anti-patterns:**

---

## Context and Orientation

### Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin (latest stable) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture (package-level) |
| DI | Hilt |
| Min SDK | API 29 (Android 10) |
| Compile/Target SDK | 34 |
| Build | Gradle (Kotlin DSL) + Version Catalog |
| Networking | Ktor Client |
| Local DB | Room |
| Formatting | ktlint (via `.editorconfig`) |
| Static Analysis | detekt (`detekt.yml`) + Android Lint |
| Unit Testing | JUnit 5 + MockK + Turbine |
| UI Testing | Compose UI Test |
| CI | GitHub Actions (`android-ci.yml` — already exists) |

### Key Android APIs

| API | Purpose |
|-----|---------|
| `MediaSessionManager` | Enumerate active media sessions |
| `NotificationListenerService` | Obtain `MediaSession.Token` from notifications |
| `MediaController` / `MediaMetadata` | Read track title, artist, album |
| `TextToSpeech` | Speak announcements |
| `AudioManager` / `AudioFocusRequest` | Manage audio ducking |
| `Room` | Cache metadata lookups |

### External APIs

| Provider | Base URL | Auth | Rate Limit |
|----------|----------|------|------------|
| MusicBrainz | `https://musicbrainz.org/ws/2/` | None (User-Agent header required) | 1 req/sec |

### Package Structure

```
app/src/main/kotlin/com/capeddle/namethattunelab/
├── domain/
│   ├── model/
│   │   ├── NowPlayingEvent.kt
│   │   ├── TrackMetadata.kt
│   │   └── MetadataConfidence.kt
│   ├── repository/
│   │   ├── MetadataRepository.kt
│   │   └── NowPlayingRepository.kt
│   └── usecase/
│       ├── ObserveNowPlayingUseCase.kt
│       ├── ResolveMetadataUseCase.kt
│       └── AnnounceTrackUseCase.kt
├── data/
│   ├── local/
│   │   ├── MetadataCacheDao.kt
│   │   ├── MetadataCacheEntity.kt
│   │   └── AppDatabase.kt
│   ├── remote/
│   │   ├── musicbrainz/
│   │   │   ├── MusicBrainzApi.kt
│   │   │   ├── MusicBrainzResponse.kt
│   │   │   └── MusicBrainzProvider.kt
│   │   └── MetadataProvider.kt        ← interface
│   ├── repository/
│   │   ├── MetadataRepositoryImpl.kt
│   │   └── NowPlayingRepositoryImpl.kt
│   └── mapper/
│       ├── MetadataCacheMapper.kt
│       └── MusicBrainzMapper.kt
├── nowplaying/
│   ├── MediaSessionMonitor.kt
│   ├── NowPlayingListenerService.kt   ← NotificationListenerService
│   └── TrackChangeDebouncer.kt
├── speech/
│   ├── TtsAnnouncer.kt
│   ├── AnnouncementGuard.kt
│   └── AudioFocusManager.kt
├── presentation/
│   ├── screen/
│   │   ├── MainScreen.kt
│   │   └── MainViewModel.kt
│   ├── component/
│   │   ├── TrackCard.kt
│   │   ├── PermissionStatusBar.kt
│   │   └── RecentTracksList.kt
│   ├── navigation/
│   │   └── AppNavigation.kt
│   └── theme/
│       ├── Theme.kt
│       ├── Color.kt
│       ├── Type.kt
│       └── Shape.kt
├── di/
│   ├── AppModule.kt
│   ├── DataModule.kt
│   ├── DatabaseModule.kt
│   └── SpeechModule.kt
├── util/
│   └── PipelineLogger.kt
├── NtlApplication.kt                  ← @HiltAndroidApp
└── MainActivity.kt                    ← @AndroidEntryPoint

app/src/main/res/
├── values/
│   ├── strings.xml
│   └── themes.xml
└── xml/
    └── notification_listener_config.xml

app/src/test/kotlin/com/capeddle/namethattunelab/
├── domain/usecase/
│   ├── ObserveNowPlayingUseCaseTest.kt
│   ├── ResolveMetadataUseCaseTest.kt
│   └── AnnounceTrackUseCaseTest.kt
├── data/repository/
│   ├── MetadataRepositoryImplTest.kt
│   └── NowPlayingRepositoryImplTest.kt
├── data/remote/musicbrainz/
│   └── MusicBrainzProviderTest.kt
├── data/mapper/
│   ├── MetadataCacheMapperTest.kt
│   └── MusicBrainzMapperTest.kt
├── nowplaying/
│   ├── MediaSessionMonitorTest.kt
│   └── TrackChangeDebouncerTest.kt
├── speech/
│   ├── TtsAnnouncerTest.kt
│   └── AnnouncementGuardTest.kt
└── presentation/screen/
    └── MainViewModelTest.kt
```

### Dependency List (Version Catalog)

| Dependency | Purpose |
|-----------|---------|
| `androidx.core:core-ktx` | Kotlin extensions for Android |
| `androidx.lifecycle:lifecycle-runtime-compose` | `collectAsStateWithLifecycle` |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `hiltViewModel()` |
| `androidx.activity:activity-compose` | Compose Activity integration |
| `androidx.compose:compose-bom` | Compose version alignment |
| `androidx.compose.material3:material3` | Material 3 UI |
| `androidx.compose.ui:ui-tooling-preview` | Preview support |
| `androidx.navigation:navigation-compose` | Compose navigation |
| `com.google.dagger:hilt-android` | Hilt DI |
| `com.google.dagger:hilt-compiler` (KSP) | Hilt annotation processing |
| `androidx.hilt:hilt-navigation-compose` | Hilt + Compose Nav |
| `androidx.room:room-runtime` | Room DB |
| `androidx.room:room-ktx` | Room coroutines |
| `androidx.room:room-compiler` (KSP) | Room annotation processing |
| `io.ktor:ktor-client-android` | HTTP client for MusicBrainz |
| `io.ktor:ktor-client-content-negotiation` | JSON parsing |
| `io.ktor:ktor-serialization-kotlinx-json` | Kotlinx serialization |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | JSON model parsing |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | Coroutines |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Android dispatchers |
| JUnit 5 | Unit testing |
| MockK | Mocking |
| Turbine | Flow testing |
| `kotlinx-coroutines-test` | Coroutine test support |

---

## Plan of Work

### Milestone 1 — Project Bootstrap
Set up the Gradle project skeleton: root `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`, app `build.gradle.kts`, `AndroidManifest.xml`, Gradle wrapper, and the `@HiltAndroidApp` application class + `MainActivity`.

**Files:** ~10  
**Deliverable:** Project compiles with `./gradlew assembleDebug`

### Milestone 2 — Domain Layer
Create all domain entities, repository interfaces, and use cases. Pure Kotlin — no Android imports.

**Files:** ~8  
**Deliverable:** Domain classes compile, use cases have operator invoke signatures

### Milestone 3 — NowPlaying Detection
Implement `NotificationListenerService` to capture `MediaSession` tokens, `MediaSessionMonitor` to extract metadata, `TrackChangeDebouncer` to debounce rapid events, and `NowPlayingRepositoryImpl` to expose the stream.

**Files:** ~5  
**Deliverable:** Track changes from Spotify/YouTube Music emit `NowPlayingEvent` to Logcat

### Milestone 4 — Metadata Enrichment
Implement `MetadataProvider` interface, `MusicBrainzProvider` (HTTP via Ktor), `MusicBrainzMapper`, Room database for caching, `MetadataCacheMapper`, and `MetadataRepositoryImpl` with cache-first resolution.

**Files:** ~10  
**Deliverable:** Given artist + title, returns `TrackMetadata` with year; cache hit on second lookup

### Milestone 5 — Speech Output
Implement `TtsAnnouncer` wrapping Android `TextToSpeech`, `AudioFocusManager` for ducking, `AnnouncementGuard` for dedup/cooldown, and the `AnnounceTrackUseCase` pipeline.

**Files:** ~5  
**Deliverable:** Calling `AnnounceTrackUseCase` with a `TrackMetadata` speaks the result

### Milestone 6 — Presentation Layer
Build the minimal Compose UI: `MainScreen` showing current track/artist/year/timestamp, permission status indicators, recent tracks list. `MainViewModel` wiring to use cases. Material 3 theme. Permission onboarding for NotificationListenerService.

**Files:** ~12  
**Deliverable:** App launches, shows UI, permission flow works

### Milestone 7 — Pipeline Integration & Wiring
Wire the complete pipeline: `NowPlayingListenerService` → `ObserveNowPlayingUseCase` → `ResolveMetadataUseCase` → `AnnounceTrackUseCase`. Hilt modules bind all interfaces. `PipelineLogger` logs every stage. Foreground service with persistent notification.

**Files:** ~6  
**Deliverable:** End-to-end: play song → hear announcement

### Milestone 8 — Testing Suite
Write unit tests for all public interfaces: use cases, repository impls, mappers, debouncer, announcement guard, ViewModel. Follow TDD cycle (RED → GREEN → REFACTOR).

**Files:** ~12  
**Deliverable:** `./gradlew test` passes with full coverage of public API

### Milestone 9 — Code Review & Quality Gate
Run full quality gate: `ktlintCheck`, `detekt`, `lintDebug`, `test`. Code review all files. Address findings. Final validation.

**Files:** 0 new (fixes only)  
**Deliverable:** All quality gates pass; code review approved

---

## Concrete Steps

### Step 1 — Gradle Wrapper & Properties
- **Agent:** developer
- **Files:**
  - `gradle/wrapper/gradle-wrapper.properties`
  - `gradle/wrapper/gradle-wrapper.jar`
  - `gradlew` (unix)
  - `gradlew.bat` (windows)
  - `gradle.properties`
- **Action:** Initialize Gradle wrapper (version 8.7+). Set `gradle.properties` with configuration cache, build cache, parallel execution, JVM args.
- **Depends on:** None

### Step 2 — Version Catalog
- **Agent:** developer
- **Files:**
  - `gradle/libs.versions.toml`
- **Action:** Define all dependency versions, libraries, and plugins per the dependency list above. Include `minSdk = "29"`, `compileSdk = "34"`, `targetSdk = "34"` as version entries.
- **Depends on:** Step 1

### Step 3 — Root Build Script
- **Agent:** developer
- **Files:**
  - `build.gradle.kts` (root)
  - `settings.gradle.kts`
- **Action:** Configure root build script with plugin declarations (Kotlin Android, Hilt, KSP, Room, Kotlin Serialization — all via version catalog aliases). Settings file declares project name `NameThatTuneLab`, includes `:app`, configures `pluginManagement` and `dependencyResolutionManagement`.
- **Depends on:** Step 2

### Step 4 — App Build Script
- **Agent:** developer
- **Files:**
  - `app/build.gradle.kts`
- **Action:** Apply plugins (`kotlin-android`, `hilt`, `ksp`, `kotlin-serialization`, `room`). Configure `android {}` block with namespace `com.capeddle.namethattunelab`, SDK versions from catalog, Compose enabled, JVM target 17. Declare all dependencies from catalog. Add JUnit 5 test configuration. Add ktlint and detekt plugin application.
- **Depends on:** Step 3

### Step 5 — Android Manifest
- **Agent:** developer
- **Files:**
  - `app/src/main/AndroidManifest.xml`
- **Action:** Declare `<application>` with `NtlApplication` name, `MainActivity` (singleTask), `NowPlayingListenerService` (NotificationListenerService with intent filter + meta-data), INTERNET permission, FOREGROUND_SERVICE permission, FOREGROUND_SERVICE_MEDIA_PLAYBACK permission, BIND_NOTIFICATION_LISTENER_SERVICE permission.
- **Depends on:** Step 4

### Step 6 — Application & Activity Shell
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/NtlApplication.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/MainActivity.kt`
- **Action:** Create `@HiltAndroidApp` Application class. Create `@AndroidEntryPoint` `MainActivity` extending `ComponentActivity` with `setContent { NtlTheme { MainScreen() } }`. Verify build compiles.
- **Depends on:** Step 5

### Step 7 — Resource Files
- **Agent:** developer
- **Files:**
  - `app/src/main/res/values/strings.xml`
  - `app/src/main/res/values/themes.xml`
  - `app/src/main/res/xml/notification_listener_config.xml`
- **Action:** Define app name string, basic theme reference, and notification listener service configuration XML.
- **Depends on:** Step 5

### Step 8 — Validate Bootstrap
- **Agent:** developer
- **Files:** None (build validation)
- **Action:** Run `./gradlew assembleDebug`. Fix any compilation errors. Confirm APK is produced.
- **Depends on:** Steps 6, 7

---

### Step 9 — Domain Entities
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/model/NowPlayingEvent.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/model/TrackMetadata.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/model/MetadataConfidence.kt`
- **Action:** Create data classes. `NowPlayingEvent(title, artist, album, sourceApp)`. `TrackMetadata(title, artist, year: Int?, confidence: MetadataConfidence)`. `MetadataConfidence` enum: `HIGH, MEDIUM, LOW, NONE`. Pure Kotlin only.
- **Depends on:** Step 8

### Step 10 — Domain Repository Interfaces
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/repository/NowPlayingRepository.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/repository/MetadataRepository.kt`
- **Action:** `NowPlayingRepository` exposes `fun observeNowPlaying(): Flow<NowPlayingEvent>`. `MetadataRepository` exposes `suspend fun resolveMetadata(event: NowPlayingEvent): Result<TrackMetadata>`. Pure Kotlin, no Android imports.
- **Depends on:** Step 9

### Step 11 — Domain Use Cases
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/usecase/ObserveNowPlayingUseCase.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/usecase/ResolveMetadataUseCase.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/usecase/AnnounceTrackUseCase.kt`
- **Action:** Each use case follows the `@Inject constructor` + `operator fun invoke()` pattern. `ObserveNowPlayingUseCase` returns `Flow<NowPlayingEvent>`. `ResolveMetadataUseCase` accepts `NowPlayingEvent`, returns `Result<TrackMetadata>`. `AnnounceTrackUseCase` accepts `TrackMetadata`, returns `Result<Unit>` (delegates to a `SpeechAnnouncer` interface in domain).
- **Depends on:** Step 10

### Step 12 — SpeechAnnouncer Domain Interface
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/SpeechAnnouncer.kt`
- **Action:** Interface with `suspend fun announce(metadata: TrackMetadata): Result<Unit>` and `fun shutdown()`. Lives in domain — no Android imports.
- **Depends on:** Step 9

---

### Step 13 — NowPlayingListenerService
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/NowPlayingListenerService.kt`
- **Action:** Extend `NotificationListenerService`. Override `onNotificationPosted` / `onListenerConnected`. Extract `MediaSession.Token` from active notifications. Use `MediaController` to read `MediaMetadata` (title, artist, album). Emit events to a shared `MutableSharedFlow`. Log detections via `PipelineLogger`.
- **Depends on:** Step 9

### Step 14 — MediaSessionMonitor
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt`
- **Action:** Register `MediaController.Callback` for active sessions. Listen for `onMetadataChanged` and `onPlaybackStateChanged`. Extract structured metadata. Emit `NowPlayingEvent` via `SharedFlow`. Include `sourceApp` from the session's package name.
- **Depends on:** Step 13

### Step 15 — TrackChangeDebouncer
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/TrackChangeDebouncer.kt`
- **Action:** Accept `Flow<NowPlayingEvent>`, emit debounced flow using `kotlinx.coroutines.flow.debounce(1500)`. Also filter duplicate consecutive events (same title + artist). Inject `CoroutineDispatcher` for testability.
- **Depends on:** Step 9

### Step 16 — NowPlayingRepositoryImpl
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/repository/NowPlayingRepositoryImpl.kt`
- **Action:** Implement `NowPlayingRepository`. Subscribe to `MediaSessionMonitor` events, pipe through `TrackChangeDebouncer`, expose as `Flow<NowPlayingEvent>`. Annotate with `@Inject constructor`.
- **Depends on:** Steps 14, 15

### Step 17 — PipelineLogger Utility
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/util/PipelineLogger.kt`
- **Action:** Structured Logcat wrapper with tags: `NTL:NowPlaying`, `NTL:Metadata`, `NTL:Cache`, `NTL:Speech`. Methods: `logDetection(event)`, `logLookup(query, result)`, `logCacheHit(key)`, `logCacheMiss(key)`, `logSpeech(text)`. Injectable via Hilt.
- **Depends on:** Step 8

---

### Step 18 — MetadataProvider Interface
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/remote/MetadataProvider.kt`
- **Action:** Interface: `suspend fun lookup(artist: String, title: String, album: String?): Result<TrackMetadata>`. Lives in data layer (not domain — it's an implementation detail of the data layer's resolution strategy).
- **Depends on:** Step 9

### Step 19 — MusicBrainz API Models
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/remote/musicbrainz/MusicBrainzApi.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/remote/musicbrainz/MusicBrainzResponse.kt`
- **Action:** Define Ktor HTTP client interface / function for `GET /ws/2/recording?query=artist:{artist}+recording:{title}&fmt=json&limit=5`. Define `@Serializable` response models mapping the MusicBrainz JSON structure (recordings list, each with title, artist-credit, releases with date). Set `User-Agent: NameThatTuneLab/1.0 (chrisapeddle@gmail.com)`.
- **Depends on:** Step 2

### Step 20 — MusicBrainzMapper
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/mapper/MusicBrainzMapper.kt`
- **Action:** Map `MusicBrainzResponse` → `TrackMetadata`. Extract year from first release date. Set confidence based on match quality (exact title+artist match = HIGH, fuzzy = MEDIUM, no release date = NONE).
- **Depends on:** Steps 9, 19

### Step 21 — MusicBrainzProvider
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/remote/musicbrainz/MusicBrainzProvider.kt`
- **Action:** Implement `MetadataProvider`. Use Ktor client to call MusicBrainz API. Respect 1 req/sec rate limit (use a `Mutex` or `Semaphore` with delay). Map response via `MusicBrainzMapper`. Handle network errors gracefully → return `Result.failure`.
- **Depends on:** Steps 18, 19, 20

### Step 22 — Room Database
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/local/MetadataCacheEntity.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/local/MetadataCacheDao.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/local/AppDatabase.kt`
- **Action:** `MetadataCacheEntity` — `@Entity` with `artist + title` as composite primary key, `year`, `confidence`, `cachedAt` timestamp. `MetadataCacheDao` — `@Query` for lookup by artist+title, `@Insert(onConflict = REPLACE)` for upsert. `AppDatabase` — `@Database` with entity list.
- **Depends on:** Step 8

### Step 23 — MetadataCacheMapper
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/mapper/MetadataCacheMapper.kt`
- **Action:** Map between `MetadataCacheEntity` ↔ `TrackMetadata`. Explicit mapping — no `copy()` across layers.
- **Depends on:** Steps 9, 22

### Step 24 — MetadataRepositoryImpl
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/repository/MetadataRepositoryImpl.kt`
- **Action:** Implement `MetadataRepository`. Strategy: check Room cache first → if hit, return cached result → if miss, call `MusicBrainzProvider` → cache result → return. Log cache hit/miss via `PipelineLogger`. Inject `CoroutineDispatcher` for IO operations.
- **Depends on:** Steps 10, 21, 22, 23, 17

---

### Step 25 — TtsAnnouncer
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/speech/TtsAnnouncer.kt`
- **Action:** Implement `SpeechAnnouncer` interface. Wrap Android `TextToSpeech`. Initialize TTS engine in `init`. Format speech: `"${title} — ${artist} — ${year}"` (omit year if null). Use `suspendCancellableCoroutine` to await TTS completion. Implement `shutdown()` to release TTS resources. Use `AudioFocusManager` for ducking.
- **Depends on:** Steps 12, 26

### Step 26 — AudioFocusManager
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/speech/AudioFocusManager.kt`
- **Action:** Wrap `AudioManager.requestAudioFocus` / `abandonAudioFocusRequest`. Use `AudioFocusRequest.Builder` with `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` and `AudioAttributes.USAGE_ASSISTANT`. Provide `requestFocus(): Boolean` and `abandonFocus()`. Injectable via `@Inject constructor`.
- **Depends on:** Step 8

### Step 27 — AnnouncementGuard
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/speech/AnnouncementGuard.kt`
- **Action:** Track `lastAnnouncedTrackKey` (artist+title hash) and `lastAnnouncementTimestamp`. Method `shouldAnnounce(metadata: TrackMetadata): Boolean` returns false if same track within 30 seconds. Thread-safe (use `AtomicReference` or `Mutex`). Injectable, with configurable cooldown for testing.
- **Depends on:** Step 9

---

### Step 28 — Material 3 Theme
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/theme/Color.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/theme/Type.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/theme/Shape.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/theme/Theme.kt`
- **Action:** Define `NtlTheme` composable with dynamic color support (API 31+), light/dark schemes. Typography using Material 3 defaults with minor customization. Standard shapes.
- **Depends on:** Step 6

### Step 29 — UI Components
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/component/TrackCard.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/component/PermissionStatusBar.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/component/RecentTracksList.kt`
- **Action:** `TrackCard` — displays title, artist, year, last announcement timestamp. `PermissionStatusBar` — shows notification listener permission status with a button to open settings. `RecentTracksList` — lazy column of recent tracks. All stateless, accept `Modifier`, include `@Preview`. Use Material 3 tokens only.
- **Depends on:** Step 28

### Step 30 — MainViewModel
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModel.kt`
- **Action:** `@HiltViewModel`. Inject all three use cases. Expose `StateFlow<MainUiState>`. `MainUiState` data class: `currentTrack: TrackMetadata?`, `isListening: Boolean`, `hasPermission: Boolean`, `recentTracks: List<TrackMetadata>`, `lastAnnouncementTime: String?`. Collect `ObserveNowPlayingUseCase` flow → resolve → announce → update state.
- **Depends on:** Step 11

### Step 31 — MainScreen
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt`
- **Action:** Compose screen collecting `MainViewModel.uiState` via `collectAsStateWithLifecycle`. Compose `TrackCard`, `PermissionStatusBar`, `RecentTracksList`. Handle permission navigation intent. Include `@Preview`.
- **Depends on:** Steps 29, 30

### Step 32 — Navigation
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/navigation/AppNavigation.kt`
- **Action:** Single-destination `NavHost` wrapping `MainScreen`. Minimal — supports future screens.
- **Depends on:** Step 31

---

### Step 33 — Hilt Modules
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/di/AppModule.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/di/DataModule.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/di/DatabaseModule.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/di/SpeechModule.kt`
- **Action:**
  - `AppModule`: Provide dispatchers (`@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher` qualifiers), `Ktor` HttpClient.
  - `DataModule`: `@Binds` `NowPlayingRepository` → `NowPlayingRepositoryImpl`, `MetadataRepository` → `MetadataRepositoryImpl`, `MetadataProvider` → `MusicBrainzProvider`.
  - `DatabaseModule`: `@Provides` Room database instance, DAO.
  - `SpeechModule`: `@Binds` `SpeechAnnouncer` → `TtsAnnouncer`.
- **Depends on:** Steps 16, 24, 25

### Step 34 — Wire Pipeline in Service
- **Agent:** developer
- **Files:**
  - Update `NowPlayingListenerService.kt`
  - Update `MainActivity.kt`
- **Action:** Inject pipeline into the service (or use a `CoroutineScope` to collect the now-playing flow and drive the resolve → announce chain). Start a foreground notification (required for `mediaPlayback` service type). Wire `MainViewModel` to observe the same pipeline. Ensure service lifecycle is managed correctly.
- **Depends on:** Step 33

### Step 35 — PipelineLogger Integration
- **Agent:** developer
- **Files:**
  - Update multiple files to inject `PipelineLogger`
- **Action:** Add logging calls at every pipeline stage: detection, lookup, cache hit/miss, TTS trigger. Ensure all logs use structured tags.
- **Depends on:** Step 17

### Step 36 — End-to-End Validation Build
- **Agent:** developer
- **Files:** None (build + manual test)
- **Action:** Run `./gradlew assembleDebug`. Install on device/emulator. Grant notification listener permission. Play a song. Verify announcement. Check Logcat for pipeline logs.
- **Depends on:** Steps 34, 35

---

### Step 37 — Domain Use Case Tests (RED → GREEN → REFACTOR)
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/domain/usecase/ObserveNowPlayingUseCaseTest.kt`
  - `app/src/test/kotlin/com/capeddle/namethattunelab/domain/usecase/ResolveMetadataUseCaseTest.kt`
  - `app/src/test/kotlin/com/capeddle/namethattunelab/domain/usecase/AnnounceTrackUseCaseTest.kt`
- **Action:** Test each use case with MockK mocks. Cover success, failure, and edge cases. Use Turbine for flow tests. JUnit 5 + MockK only.
- **Depends on:** Step 11

### Step 38 — Repository Tests
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/data/repository/MetadataRepositoryImplTest.kt`
  - `app/src/test/kotlin/com/capeddle/namethattunelab/data/repository/NowPlayingRepositoryImplTest.kt`
- **Action:** Test cache-first strategy in `MetadataRepositoryImpl` (cache hit returns cached, cache miss calls provider then caches). Test debounced flow in `NowPlayingRepositoryImpl`.
- **Depends on:** Steps 16, 24

### Step 39 — MusicBrainz Provider Tests
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/data/remote/musicbrainz/MusicBrainzProviderTest.kt`
- **Action:** Mock Ktor client responses. Test successful lookup, no results, network error, malformed JSON.
- **Depends on:** Step 21

### Step 40 — Mapper Tests
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/data/mapper/MetadataCacheMapperTest.kt`
  - `app/src/test/kotlin/com/capeddle/namethattunelab/data/mapper/MusicBrainzMapperTest.kt`
- **Action:** Test all mapping paths. Edge cases: null year, empty artist, partial date strings ("1991", "1991-09", "1991-09-10").
- **Depends on:** Steps 20, 23

### Step 41 — NowPlaying Tests
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitorTest.kt`
  - `app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/TrackChangeDebouncerTest.kt`
- **Action:** Test debouncer with rapid events (verify only last event emits). Test duplicate filtering. Use `kotlinx-coroutines-test` for virtual time.
- **Depends on:** Steps 14, 15

### Step 42 — Speech Tests
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/speech/TtsAnnouncerTest.kt`
  - `app/src/test/kotlin/com/capeddle/namethattunelab/speech/AnnouncementGuardTest.kt`
- **Action:** Test `AnnouncementGuard` — same track within 30s → false, different track → true, same track after 30s → true. Test `TtsAnnouncer` speech format with and without year. Mock TTS engine.
- **Depends on:** Steps 25, 27

### Step 43 — ViewModel Tests
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModelTest.kt`
- **Action:** Test state transitions: initial → listening → track detected → metadata resolved. Test error states. Use Turbine for StateFlow assertions.
- **Depends on:** Step 30

### Step 44 — Run Full Test Suite
- **Agent:** testing
- **Files:** None
- **Action:** `./gradlew test`. All tests must pass. Report coverage.
- **Depends on:** Steps 37–43

---

### Step 45 — Code Review
- **Agent:** code-reviewer
- **Files:** All `.kt` files
- **Action:** Full review against governance checklist: Kotlin idioms, Compose patterns, architecture compliance, testing coverage, AI-specific checks. Generate review report. Delegate fixes to developer.
- **Depends on:** Step 44

### Step 46 — Fix Review Findings
- **Agent:** developer
- **Files:** Per review findings
- **Action:** Address all ERROR-level findings. Address WARNING-level findings or document justification.
- **Depends on:** Step 45

### Step 47 — Quality Gate
- **Agent:** developer
- **Files:** None (validation)
- **Action:** Run `./gradlew ktlintCheck`, `./gradlew detekt`, `./gradlew lintDebug`, `./gradlew test`, `./gradlew assembleDebug`. All must pass with zero violations.
- **Depends on:** Step 46

### Step 48 — Final Validation
- **Agent:** testing
- **Files:** None
- **Action:** Re-run all tests. Confirm end-to-end pipeline works. Update ExecPlan progress. Close plan.
- **Depends on:** Step 47

---

## Validation and Acceptance

- [ ] Project compiles: `./gradlew assembleDebug` succeeds
- [ ] Play a song in Spotify → app detects title + artist
- [ ] MusicBrainz lookup resolves release year
- [ ] Cache stores result; second lookup hits cache
- [ ] TTS announces: "Title — Artist — Year"
- [ ] TTS omits year when unavailable: "Title — Artist"
- [ ] Debouncer filters rapid duplicate events (1.5s window)
- [ ] Announcement guard prevents repeat within 30 seconds
- [ ] Audio ducking works (music dips during announcement)
- [ ] Minimal UI shows current track, artist, year, timestamp
- [ ] Permission status indicators display correctly
- [ ] Notification listener permission onboarding flow works
- [ ] Pipeline logger outputs to Logcat at every stage
- [ ] All unit tests pass: `./gradlew test`
- [ ] No ktlint violations: `./gradlew ktlintCheck`
- [ ] No detekt violations: `./gradlew detekt`
- [ ] No Android Lint errors: `./gradlew lintDebug`
- [ ] Build succeeds: `./gradlew build`
- [ ] Code review completed (via `code-reviewer` agent)
- [ ] TDD cycle completed for all code changes

---

## Idempotence and Recovery

- **All steps create new files** — the workspace is greenfield. Re-running any step is safe (overwrites).
- **Room database** — no migration needed (first version). If schema changes during development, increment version and add migration or use `fallbackToDestructiveMigration()` for debug.
- **Gradle wrapper** — can be re-initialized with `gradle wrapper --gradle-version 8.7`.
- **If a milestone fails**, roll back to the last compiling state and re-run from that milestone.
- **Tests are independent** — any test step can be re-run in isolation.

---

## Artifacts and Notes

- **MusicBrainz API docs:** https://musicbrainz.org/doc/MusicBrainz_API
- **MusicBrainz rate limit:** 1 request per second; include `User-Agent` header.
- **NotificationListenerService docs:** https://developer.android.com/reference/android/service/notification/NotificationListenerService
- **MediaSession docs:** https://developer.android.com/reference/android/media/session/MediaSession
- **TextToSpeech docs:** https://developer.android.com/reference/android/speech/tts/TextToSpeech
- **AudioFocus docs:** https://developer.android.com/guide/topics/media-apps/audio-focus
- **Hilt best practices:** https://developer.android.com/training/dependency-injection/hilt-android
- **Room docs:** https://developer.android.com/training/data-storage/room
