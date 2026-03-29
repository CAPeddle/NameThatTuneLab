# Media-Only Filtering Hardening with Allowlist and Ignore Telemetry

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** 2026-03-29  
**Status:** ✅ Complete  
**Owner:** Overlord (GPT-5.3-Codex)  
**Refs:** User request: "Implement stricter app-level filter now (active playback + package allowlist + telemetry to confirm non-media events are ignored)"; `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt`; `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/NowPlayingListenerService.kt`; `app/src/main/kotlin/com/capeddle/namethattunelab/util/PipelineLogger.kt`  
**Revision:** v1

---

## Purpose / Big Picture

Notification listener access is platform-wide, so privacy and behavior guarantees must be enforced by app logic. The current pipeline already prefers media sessions and checks playback state before emission, but there is no explicit source-package allowlist and no telemetry for ignored candidates. This makes it hard to prove that non-media or disallowed sources are intentionally dropped.

This plan introduces a strict, explicit media-only policy at the app layer by combining active playback gating, configurable package allowlisting, and structured "ignored" telemetry. The implementation was test-first and included both positive-path detection tests and negative-path ignore verification tests.

**Observable outcome:** The app emits `NowPlayingEvent` only when playback is active and source package is allowlisted, while non-allowlisted or otherwise invalid candidates are not emitted and are logged with an ignore reason that can be validated in unit tests.

**Term definitions:**
- *Active playback gate:* A rule requiring `PlaybackState.STATE_PLAYING` before metadata can produce an emitted now-playing event.
- *Package allowlist:* Explicit set of package names permitted to produce now-playing events.
- *Ignored candidate:* Any controller/metadata combination evaluated by the monitor but intentionally excluded from emission.
- *Ignore telemetry:* Structured logging that records why a candidate was dropped (for example: not allowlisted, missing title, missing artist, not actively playing).
- *Media-only policy:* Combined rules that ensure only intended media playback events become pipeline outputs.

---

## Progress

- [x] ExecPlan created before implementation
- [x] Initial investigation completed and candidate touchpoints identified
- [x] RED: add failing tests for allowlist and ignore telemetry behavior
- [x] GREEN: implement allowlist + stricter gating + ignore telemetry
- [x] REFACTOR: simplify policy wiring and remove duplication
- [x] REVIEW: code-reviewer gate passed with no blockers
- [x] Completion gate: commit and push completed

---

## Surprises & Discoveries

- `NowPlayingListenerService` is already minimal in `onNotificationPosted` and relies on active `MediaSessionManager` sessions, which is a good baseline for media-only behavior.
- `MediaSessionMonitor` already gates on `STATE_PLAYING`, but emits from any package and only logs successful detections.
- `PipelineLogger` initially had no explicit API for ignored-event reasons; telemetry was added during implementation.
- Code review caught a blocker where ignored candidates were logged with error severity; switching to info-level telemetry aligned implementation with policy intent.

---

## Decision Log

- **Decision:** Implement policy enforcement in `MediaSessionMonitor` rather than in UI or repository layers.  
  Rationale: Session-level eligibility and metadata validity are monitor concerns and should remain close to the event source.  
  Date: 2026-03-29

- **Decision:** Add dedicated ignore-telemetry logging API rather than overloading error logging.  
  Rationale: Ignored candidates are expected behavior, not errors; they need structured, low-noise observability.  
  Date: 2026-03-29

- **Decision:** Make allowlist configurable through DI-provided policy object/set.  
  Rationale: Keeps policy testable and adjustable without hard-coding logic in callbacks.  
  Date: 2026-03-29

- **Decision:** Treat ignored candidates as expected observability signals and log them at info level.  
  Rationale: Filter drops are normal control flow and should not pollute error monitoring.  
  Date: 2026-03-29

---

## Outcomes & Retrospective

**What was achieved:**
- Added strict package allowlist policy for now-playing source packages.
- Hardened candidate evaluation in `MediaSessionMonitor` to enforce allowlist, active playback, and required metadata gates before emission.
- Added structured ignore telemetry reasons and details in `PipelineLogger`.
- Added RED tests for non-allowlisted/non-playing/missing-metadata paths and ignore telemetry assertions, then drove GREEN implementation.
- Added explicit metadata-null ignore-path coverage and resolved the review blocker.
- Passed full quality gates (`testDebugUnitTest`, `ktlintCheck`, `detekt`, `lintDebug`, `assembleDebug`) and mandatory code review.

**What remains (if anything):**
- None for this scoped implementation.

**Patterns to promote:**
- Encode source-policy decisions in injectable policy objects instead of scattering package checks.
- Add one test per filter gate branch, including null-input paths.

**Reusable findings:**
- Ignore telemetry should be info-level structured logs, not error logs.
- Static allowlist defaults should be DI-provided so policy changes stay localized.

**New anti-patterns:**
- Logging expected filter drops as errors.

---

## Context and Orientation

### Current Baseline

- `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/NowPlayingListenerService.kt`
- `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt`
- `app/src/main/kotlin/com/capeddle/namethattunelab/util/PipelineLogger.kt`
- `app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitorTest.kt`
- `app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/NowPlayingListenerServiceTest.kt`

### Constraints

- Preserve Clean Architecture boundaries and existing DI approach.
- Avoid broad notification parsing; continue to prefer media-session-driven detection.
- Keep telemetry useful without leaking sensitive payload details.
- Follow repository gates: ktlint, detekt, lint, unit tests, assemble, and mandatory code review.

### Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin (JVM 17) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Build | Gradle (Kotlin DSL) |
| Testing | JUnit 5 + MockK + Turbine + Robolectric |

---

## Plan of Work

> **TDD Order Mandate:** For every code-producing step, sequence is RED -> GREEN -> REFACTOR -> code review.

### Milestone 1 — Policy Design and RED Tests

1. Define allowlist policy shape (set/provider) and ignore-reason enum/strings.
2. Add failing tests for:
   - non-allowlisted package does not emit
   - non-playing state does not emit
   - missing required metadata does not emit
   - ignored paths log reason telemetry

### Milestone 2 — GREEN Implementation

1. Implement policy checks in `MediaSessionMonitor` before event emission.
2. Wire allowlist via DI or constructor dependency.
3. Extend `PipelineLogger` with ignore telemetry API and integrate calls.

### Milestone 3 — REFACTOR and Broader Validation

1. Refactor callback flow for readability and single-responsibility helper functions.
2. Ensure no behavior regressions in existing monitor/listener tests.
3. Execute full quality gate suite.

### Milestone 4 — Review and Completion Gate

1. Mandatory `@code-reviewer` pass.
2. Overlord check-in: commit + push after approvals.

---

## Concrete Steps

### Step 1 — Investigation Report and Policy Proposal
- **Agent:** debugger
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/NowPlayingListenerService.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/util/PipelineLogger.kt`
- **Action:** Produce concrete policy proposal (allowlist source, ignore reasons, telemetry format).
- **Depends on:** None

### Step 2 — RED Tests
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitorTest.kt`
  - `app/src/test/kotlin/com/capeddle/namethattunelab/util/PipelineLoggerTest.kt` (new if needed)
- **Action:** Add failing tests for allowlist gating and ignore telemetry.
- **Depends on:** Step 1

### Step 3 — GREEN Implementation
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/util/PipelineLogger.kt`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/di/DataModule.kt` (or appropriate DI module)
  - `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/*` (if policy object extracted)
- **Action:** Implement policy and logging to satisfy RED tests.
- **Depends on:** Step 2

### Step 4 — REFACTOR + Validation
- **Agent:** testing
- **Files:** all touched implementation and test files
- **Action:** Run and record:
  - `./gradlew :app:testDebugUnitTest`
  - `./gradlew :app:ktlintCheck`
  - `./gradlew :app:detekt`
  - `./gradlew :app:lintDebug`
  - `./gradlew :app:assembleDebug`
- **Depends on:** Step 3

### Step 5 — Mandatory Code Review
- **Agent:** code-reviewer
- **Files:** all touched files
- **Action:** Review for blockers, regressions, and architecture/test adequacy.
- **Depends on:** Step 4

### Step 6 — Completion Gate
- **Agent:** Overlord
- **Files:** git state
- **Action:** `git status` -> commit -> push.
- **Depends on:** Step 5

---

## Validation and Acceptance

| Criterion | Command / Evidence | Status |
|-----------|-------------------|--------|
| Non-allowlisted sources never emit now-playing events | `MediaSessionMonitorTest` assertions on no event emission | ✅ |
| Active playback remains required for emission | `MediaSessionMonitorTest` with non-playing state | ✅ |
| Missing required metadata is ignored | `MediaSessionMonitorTest` metadata-null/blank assertions | ✅ |
| Ignore telemetry records reason for dropped candidates | logger verification in unit tests | ✅ |
| Existing valid media path still emits events | existing + updated monitor tests | ✅ |
| Unit tests pass | `./gradlew :app:testDebugUnitTest` | ✅ |
| No ktlint violations | `./gradlew :app:ktlintCheck` | ✅ |
| No detekt violations | `./gradlew :app:detekt` | ✅ |
| No Android Lint errors | `./gradlew :app:lintDebug` | ✅ |
| Build succeeds | `./gradlew :app:assembleDebug` | ✅ |
| Code review completed | `@code-reviewer` no blockers | ✅ |
| TDD cycle completed | RED -> GREEN -> REFACTOR recorded in Progress | ✅ |

---

## Idempotence and Recovery

- Test additions are repeatable and side-effect free.
- Policy checks are deterministic; re-running tests should produce identical outcomes.
- If allowlist policy causes over-filtering, recovery is a config update plus test adjustment; no schema migration required.
- If telemetry noise is high, reduce verbosity via logger-level controls without changing emission policy.
- No destructive git operations are permitted.

---

## Artifacts and Notes

- Existing related plan: `.github/planning/execplans/2026-03-29-unrelated-work-triage-permission-state-and-nowplaying.md`
- Existing related plan: `.github/planning/execplans/2026-03-29-notification-access-settings-intent-regression.md`
- Suggested candidate allowlist seed (to validate with product owner): Spotify, YouTube Music, Apple Music, Tidal, Pandora, Deezer, SoundCloud.
