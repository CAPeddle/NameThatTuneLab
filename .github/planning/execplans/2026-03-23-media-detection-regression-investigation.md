# Media Detection Regression Investigation (Post-Settings Change)

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** 2026-03-23  
**Status:** 🚧 In Progress  
**Owner:** Overlord (GPT-5.3-Codex)  
**Refs:** User report: media detection stopped after latest APK on same device  
**Revision:** v1

---

## Purpose / Big Picture

The app previously detected song changes but now does not detect from either YouTube Music or Spotify. The user also reports Settings app instability while trying to inspect notification access. This work investigates root cause(s), creates a reproducible test case for the failure mode, and delivers a minimal safe fix validated through tests and quality gates.

Because the failure appears after recent configuration-related changes, this plan emphasizes regression analysis against recent commits before implementation. The plan also verifies device/runtime behavior with fresh log capture to avoid relying on stale assumptions.

**Observable outcome:** With NameThatTuneLab installed and notification access granted, playing tracks in Spotify and YouTube Music produces `NowPlayingEvent` emissions and updates the identified songs list; automated tests reproduce the prior failing condition and pass after the fix.

**Term definitions:**
- *Media detection:* The app receiving track-change signals from Android media sessions via `NotificationListenerService` + `MediaController` callbacks.
- *NowPlayingEvent emission:* An item emitted from `MediaSessionMonitor.events` that drives metadata resolution and announcement.
- *Regression:* Behavior that worked in an earlier commit/build but fails in newer commits without intended behavior change.
- *RED/GREEN/REFACTOR:* TDD sequence where test fails first, implementation makes it pass, then code is cleaned without behavior change.

---

## Progress

- [x] Create ExecPlan and define acceptance criteria
- [x] Investigation report completed (git + runtime logs)
- [x] RED: failing tests that reproduce the regression added
- [x] GREEN: implementation fixes regression and tests pass
- [x] REFACTOR: polish and guardrails complete
- [ ] Validation gate run (`testDebugUnitTest`, `ktlintCheck`, `detekt`, `assembleDebug`)
- [ ] Code review completed by `@code-reviewer`

---

## Surprises & Discoveries`r`n`r`n- User reports Settings app crashes when navigating to notification access listing for NameThatTuneLab, complicating manual permission verification.`r`n- Runtime check confirmed listener was initially not enabled in `enabled_notification_listeners`; this fully blocks all detections while UI can still show listening state.`r`n- Regression test confirmed lifecycle bug: `MediaSessionMonitor.detachAll()` canceled singleton scope, causing post-reconnect emission starvation.

---

## Decision Log

- **Decision:** Investigate runtime and git regression evidence before implementation.  
  Rationale: Avoid fixing wrong layer; both Spotify and YouTube failures suggest broader pipeline issue than app-specific metadata mapping.  
  Date: 2026-03-23

- **Decision:** Require explicit failing unit tests for the identified regression before code changes.  
  Rationale: Prevent future silent reintroduction and align with TDD mandate.  
  Date: 2026-03-23

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

Relevant runtime path:
- `app/src/main/AndroidManifest.xml`
- `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/NowPlayingListenerService.kt`
- `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt`
- `app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/TrackChangeDebouncer.kt`
- `app/src/main/kotlin/com/capeddle/namethattunelab/data/repository/NowPlayingRepositoryImpl.kt`
- `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModel.kt`

Potential regression window:
- Last known working baseline: `79de730`
- Suspected change series: `bf05921` onward

### Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Build | Gradle (Kotlin DSL) |
| Testing | JUnit 5 + MockK + Turbine + Compose UI Test |

---

## Plan of Work

> **TDD Order Mandate:** For every code-producing step, the sequence is always:
> 1. `@testing` writes failing tests (RED)
> 2. `@developer` writes implementation (GREEN)
> 3. `@testing` verifies all pass (REFACTOR)
> 4. `@code-reviewer` reviews
>
> Do NOT write implementation before tests exist.

> **Investigation-First Rule:** Use `@debugger` to produce an investigation report before implementation.

### Milestone 1 — Regression Investigation
Collect evidence from git history and fresh device logs to isolate failure cause(s).

1. Compare detection pipeline and manifest between `79de730` and `HEAD`.
2. Capture `adb logcat` during playback app switches.
3. Confirm whether listener connects and events emit.

### Milestone 2 — Reproducible Tests
Create failing tests that capture the identified regression path.

1. Add/extend tests in now-playing pipeline.
2. Ensure failure occurs before fix.

### Milestone 3 — Minimal Fix + Validation
Implement root-cause fix and validate with project quality gates.

1. Apply minimal changes in affected layer.
2. Run focused then broader validation tasks.

### Milestone 4 — Mandatory Review
Run `@code-reviewer`, address findings, and close plan.

---

## Concrete Steps

### Step 1 — Investigation Report
- **Agent:** debugger
- **Files:** pipeline files + runtime logs
- **Action:** Analyze commit history and runtime logs; produce ranked root-cause hypotheses with confidence and recommended test targets.
- **Depends on:** None

### Step 2 — RED Tests
- **Agent:** testing
- **Files:** `app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/*`
- **Action:** Add failing tests reproducing Step 1 root cause(s), including listener reconnect/lifecycle edge where applicable.
- **Depends on:** Step 1

### Step 3 — GREEN Implementation
- **Agent:** developer
- **Files:** minimal subset in now-playing/DI/presentation as required
- **Action:** Implement smallest safe fix to satisfy failing tests and preserve architecture boundaries.
- **Depends on:** Step 2

### Step 4 — REFACTOR + Validation
- **Agent:** testing
- **Files:** changed files + tests
- **Action:** Run focused tests, then `testDebugUnitTest`, `ktlintCheck`, `detekt`, `assembleDebug`; confirm no regressions.
- **Depends on:** Step 3

### Step 5 — Code Review Gate
- **Agent:** code-reviewer
- **Files:** all changed files
- **Action:** Perform mandatory review, provide learnings, and confirm no blockers.
- **Depends on:** Step 4

---

## Validation and Acceptance

| Criterion | Command / Evidence | Status |
|-----------|-------------------|--------|
| Listener service connects at runtime | `adb logcat` contains service connected signal and no fatal listener errors | - |
| Track-change event path active | Runtime logs show monitor event emission for Spotify/YouTube track changes | - |
| Regression test added and initially fails | Test output before fix shows expected failure on target scenario | - |
| Regression test passes after fix | Focused unit tests pass post-fix | - |
| All unit tests pass | `./gradlew testDebugUnitTest` — zero failures | - |
| No ktlint violations | `./gradlew ktlintCheck` — zero warnings | - |
| No detekt violations | `./gradlew detekt` — zero findings | - |
| Build succeeds | `./gradlew assembleDebug` — `BUILD SUCCESSFUL` | - |
| Code review completed | `@code-reviewer` — no blockers | - |
| TDD cycle completed | RED → GREEN → REFACTOR for all new code | - |

---

## Idempotence and Recovery

- Log capture and git diff steps are read-only and safe to repeat.
- Test additions are deterministic and can be re-run without side effects.
- If runtime capture fails due to ADB state, recover using `adb kill-server`, `adb start-server`, reconnect device, and retry capture.
- If fix introduces regressions, revert changed files in the affected milestone and re-run Step 2 baseline tests.

---

## Artifacts and Notes

- Expected investigation output includes: commit-level suspect list, runtime evidence excerpt, and final root-cause ranking.
- Device-specific Settings app crash should be treated as an environmental constraint; app-side permission guidance may still be needed.

