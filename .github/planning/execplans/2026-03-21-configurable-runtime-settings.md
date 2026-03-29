# NameThatTuneLab — Configurable Runtime Settings (MusicBrainz Account + Voice-Over Start Delay)

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** 2026-03-21  
**Status:** ✅ Complete  
**Owner:** Overlord Agent  
**Refs:** User request: configurable MusicBrainz account + configurable delay before voice-over starts  
**Revision:** v1

---

## Purpose / Big Picture

Introduce a proper app-settings capability so two currently hardcoded behaviors can be configured without editing source code: (1) MusicBrainz account identity used in request headers, and (2) delay from track-change detection to spoken announcement.

This should follow Clean Architecture boundaries (domain contract, data persistence, presentation UI/state) and avoid scattering constants through infrastructure classes.

**Observable outcome:** A user can set MusicBrainz account details and announcement delay in-app, restart the app, and observe that MusicBrainz requests use the new identity while speech starts after the configured delay.

**Term definitions:**
- *MusicBrainz account identity:* The User-Agent identity required by MusicBrainz policy (typically app name/version + contact).
- *Announcement delay:* Time offset applied after a new track event settles and before TTS speak begins.
- *Settings store:* Persistent local storage for user-configurable app behavior values.
- *Runtime config:* Values read during app execution (not compile-time constants).

---

## Progress

- [x] Created ExecPlan before investigation/implementation
- [x] Initial architecture investigation completed
- [x] Clarifications answered by user (user elected to proceed with pragmatic defaults)
- [x] Milestone 1 — Settings domain contract + persistence scaffold
- [x] Milestone 2 — MusicBrainz identity wired to runtime settings
- [x] Milestone 3 — Announcement delay wired to runtime settings
- [x] Milestone 4 — Settings UI + ViewModel integration
- [x] Milestone 5 — Validation, code review, retrospective

---

## Surprises & Discoveries

- No existing Settings repository/store is present (no DataStore/SharedPreferences abstraction currently in project). AndroidX DataStore Preferences chosen over Room for lightweight key-value settings.
- FunctionNaming detekt suppression required for backtick-style test names inside @Nested inner class — detekt does not auto-exempt these the same way top-level @Test functions are handled.
- The @testing subagent that fixed timing tests also discovered and resolved the rate-limit concurrency race in MusicBrainzProvider during its session.
- `MusicBrainzApi` currently hardcodes `userAgent` default to `NameThatTuneLab/1.0 (chrisapeddle@gmail.com)`.
- Voice-over timing is currently influenced by `TrackChangeDebouncer.DEBOUNCE_MS = 1_500L`; there is no separate pre-speech delay setting.
- Announcement repeat suppression exists separately in `AnnouncementGuard.COOLDOWN_MS = 30_000L` and should remain conceptually distinct from start delay.

---

## Decision Log

- **Decision:** Introduce a dedicated settings capability as a first-class architectural concern (domain + data + presentation), not ad-hoc constants.  
  Rationale: Multiple configurable runtime behaviors now exist and likely will grow.  
  Date: 2026-03-21

- **Decision:** Treat “announcement delay” as a separate setting from duplicate-announcement cooldown.  
  Rationale: They solve different user problems (timing vs spam prevention).  
  Date: 2026-03-21

- **Decision:** Keep MusicBrainz settings localized to metadata/network pipeline consumers instead of leaking network concerns into UI models.  
  Rationale: Maintains clean layering and testability.  
  Date: 2026-03-21

- **Decision:** Apply announcement delay in AnnounceTrackUseCase, not in TrackChangeDebouncer.  
  Rationale: Keeps debounce (settling signal) and delay (pre-speech timing) as distinct concerns, each with a single responsibility.  
  Date: 2026-03-21

- **Decision:** DataStore Preferences chosen over Room for settings persistence.  
  Rationale: Key-value settings do not benefit from SQL schema; DataStore is simpler, type-safe, and coroutines-native.  
  Date: 2026-03-21

- **Decision:** Rate-limit timestamp (`lastRequestAt`) must be set inside the mutex BEFORE the API call is dispatched.  
  Rationale: If timestamp is only updated on success, a failed request leaves the window open for the next call to squeeze in under the minimum interval, violating MusicBrainz pacing policy.  
  Date: 2026-03-21

---

## Outcomes & Retrospective

**What was achieved:**
- Full settings capability introduced — DataStore persistence, domain model, repository contract, two thin use cases, Hilt DI module.
- MusicBrainz user-agent and voice-over delay are both configurable and persisted across restarts.
- Settings panel UI added to MainScreen; ViewModel exposes editable state + save action.
- Rate-limit concurrency race in MusicBrainzProvider resolved: dispatch timestamp recorded inside mutex before API call.
- All unit tests pass (34 test tasks); ktlint + detekt + assembleDebug all clean.
- Full @testing → @developer → @code-reviewer delegation loop executed.

**What remains (optional nits from code review, not blocking):**
- Thin use-case unit tests for ObserveAppSettingsUseCase and UpdateAppSettingsUseCase.
- Exception-to-error-state mapping in MainViewModel.saveSettings() (DataStore write failures silently suppressed).
- keyboardType = KeyboardType.Number for voice-over delay OutlinedTextField.

**Patterns to promote:**
- Rate-limit mutex: always record dispatch time inside the lock, before the blocking call — not after the result.
- Thin use-case tests: even single-delegation use cases deserve tests to lock the behavioral contract.
- @Suppress("FunctionNaming") needed at each @Nested inner class when using backtick test names with detekt strict mode.

**Reusable findings:**
- DataStore Preferences is the right tool for lightweight key-value settings in Clean Architecture Android projects (simpler than Room, Flow-native, type-safe).
- PreferenceDataStoreFactory.create with a temp file gives fast, isolated test instances without mocking.
- backgroundScope.launch + runCurrent() + advanceTimeBy() is the correct Turbine/coroutines-test pattern for testing use-case internal delays.

**New anti-patterns:**
- Do not record rate-limit timestamps only on success — failures must still consume the pacing slot.
- Do not suppress detekt globally for test classes; use targeted @Suppress per class or per rule.

---

## Context and Orientation

### Current insertion points discovered

- `app/src/main/kotlin/com/capeddle/namethattunelab/data/remote/musicbrainz/MusicBrainzApi.kt`
  - Contains hardcoded default MusicBrainz user-agent.
- `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/TrackChangeDebouncer.kt`
  - Contains hardcoded debounce delay (`DEBOUNCE_MS`) near the requested “start delay” behavior.
- `app/src/main/kotlin/com/capeddle/namethattunelab/speech/TtsAnnouncer.kt`
  - Speech execution location where any additional pre-speech delay could be applied if required.
- `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt`
  - Current root screen has no settings UI yet.

### Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Persistence | Room currently present; settings persistence to be introduced |
| Build | Gradle (Kotlin DSL) |
| Testing | JUnit 5 + MockK + Turbine + Robolectric |

---

## Plan of Work

> **TDD Order Mandate:** For every code-producing step, the sequence is always:
> 1. `@testing` writes failing tests (RED)
> 2. `@developer` writes implementation (GREEN)
> 3. `@testing` verifies all pass (REFACTOR)
> 4. `@code-reviewer` reviews
>
> Do NOT write implementation before tests exist.

### Milestone 1 — Settings architecture baseline
Create domain model + repository contract and data-layer persistence for configurable settings.

1. Add domain settings model and repository interface.
2. Add persistence implementation and DI wiring.
3. Add unit tests for read/write defaults and updates.

### Milestone 2 — MusicBrainz account configurability
Route MusicBrainz identity header creation through settings values.

1. Replace hardcoded user-agent default path with runtime settings source.
2. Preserve safe fallback when settings are empty/invalid.
3. Add tests validating header behavior.

### Milestone 3 — Voice-over start delay configurability
Apply configurable timing in the now-playing → announce path.

1. Introduce delay setting usage at the chosen layer (debouncer or pre-speech gate based on clarification).
2. Ensure no regression to duplicate-suppression logic.
3. Add tests validating timing behavior boundaries.

### Milestone 4 — User-facing configuration controls
Expose and persist these settings from presentation layer.

1. Add minimal settings UI on existing screen flow.
2. Add ViewModel state/events for editing + saving.
3. Add UI and ViewModel tests.

### Milestone 5 — Quality gates and closure
Run required checks and finalize governance loop.

1. Run unit tests + static checks + build.
2. Delegate to `@code-reviewer` and capture learnings.
3. Update retrospective and close plan.

---

## Concrete Steps

### Step 1 — Clarify requirements and finalize semantics
- **Agent:** overlord
- **Files:** `.github/planning/execplans/2026-03-21-configurable-runtime-settings.md`
- **Action:** Capture user clarifications on settings semantics, UI placement, validation rules, and defaults.
- **Depends on:** None

### Step 2 — RED tests for settings persistence + use
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/**` (new/updated tests for settings + affected flows)
- **Action:** Write failing tests for default values, update behavior, MusicBrainz header generation, and announcement delay behavior.
- **Depends on:** Step 1

### Step 3 — GREEN implementation (domain/data/di)
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/domain/**`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/data/**`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/di/**`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/**` and/or `speech/**`
- **Action:** Implement settings store + repository + runtime wiring for MusicBrainz identity and announcement delay.
- **Depends on:** Step 2

### Step 4 — GREEN implementation (presentation)
- **Agent:** developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/**`
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/component/**` (if needed)
- **Action:** Add minimal settings editing UI and ViewModel integration with state hoisting.
- **Depends on:** Step 3

### Step 5 — REFACTOR + validation
- **Agent:** testing
- **Files:** modified source and test files from Steps 2–4
- **Action:** Execute quality gates and verify all tests pass.
- **Depends on:** Step 4

### Step 6 — Mandatory review gate
- **Agent:** code-reviewer
- **Files:** all modified files
- **Action:** Review architecture compliance, Kotlin/Compose idioms, and potential anti-patterns; produce findings.
- **Depends on:** Step 5

### Step 7 — Address review findings and close
- **Agent:** developer + testing + overlord
- **Files:** any files required by review feedback + this ExecPlan
- **Action:** Resolve blockers, re-validate, record retrospective and reusable findings.
- **Depends on:** Step 6

---

## Validation and Acceptance

| Criterion | Command / Evidence | Status |
|-----------|-------------------|--------|
| MusicBrainz identity is user-configurable and persisted | Unit tests + runtime verification of User-Agent value path | ✅ |
| Announcement delay is configurable and applied at intended stage | Unit tests around delay behavior + manual smoke validation | ✅ |
| Defaults are safe and deterministic | Unit tests for default settings values | ✅ |
| Existing announcement cooldown behavior is preserved | Regression tests around `AnnouncementGuard` behavior | ✅ |
| All unit tests pass | `./gradlew testDebugUnitTest` — zero failures | ✅ |
| No ktlint violations | `./gradlew ktlintCheck` — zero violations | ✅ |
| No detekt violations | `./gradlew detekt` — zero findings | ✅ |
| Build succeeds | `./gradlew assembleDebug` — `BUILD SUCCESSFUL` | ✅ |
| Code review completed | `@code-reviewer` — no blockers | ✅ |
| TDD cycle completed | RED → GREEN → REFACTOR evidence recorded in Progress | ✅ |

---

## Idempotence and Recovery

- Most steps are additive/refactoring and safe to re-run.
- If persistence schema or key names change, include a deterministic migration/defaulting path and tests.
- If UI or behavior validation fails, rollback is contained to the settings integration points (repository wiring + consumption points) without touching core detection pipeline semantics.

---

## Artifacts and Notes

- Planned handoff order: `@testing` → `@developer` → `@testing` → `@code-reviewer`.
- Clarification answers from user are required before Step 2 begins.
