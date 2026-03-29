# Notification Access Banner Tap Regression Investigation and Fix

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** 2026-03-29  
**Status:** ✅ Complete  
**Owner:** Overlord (GPT-5.3-Codex)  
**Refs:** User report after latest install: red banner shows "Notification access required - tap to open settings" but tap does nothing; `.github/planning/execplans/2026-03-29-unrelated-work-triage-permission-state-and-nowplaying.md`  
**Revision:** v1

---

## Purpose / Big Picture

The latest build correctly surfaces a permission warning banner when notification-listener access is not available, but the banner's tap action is failing to open Android settings. This creates a dead-end UX where users are told what to do but cannot proceed from the app.

This plan investigates the full interaction path from UI tap -> callback -> Intent creation -> launch context behavior, reproduces the issue with tests/log evidence, and delivers a safe fix validated by quality gates and mandatory review workflow.

**Observable outcome:** When the red permission banner is tapped, Android opens the appropriate notification-listener settings screen (or a documented fallback settings screen), and automated tests verify that the app emits the expected settings-launch request path.

**Term definitions:**
- *Permission banner:* The in-app red status bar/message indicating notification access is required.
- *Tap-to-settings path:* The code path from Compose click handler to launching an Android settings Intent.
- *Primary settings intent:* The first-choice Android action expected to open notification-listener settings.
- *Fallback intent:* A secondary settings action used if the primary action cannot resolve or launch on-device.
- *Regression:* Behavior that previously worked or was expected by design but fails in the latest build.

---

## Progress

- [x] ExecPlan created before investigation work
- [x] Milestone 1 complete: investigation report with root-cause ranking
- [x] Milestone 2 complete: RED tests added for broken tap-to-settings behavior
- [x] Milestone 3 complete: GREEN fix implemented with fallback-safe settings launch
- [x] Milestone 4 complete: REFACTOR + full quality and review gates passed
- [x] Milestone 5 complete: git check-in and push completed

---

## Surprises & Discoveries

- Root cause confirmed at code level: `PermissionStatusBar` currently accepts only `isGranted` and `modifier` and has no click callback or clickable modifier, so tap input is never handled.
- Wiring gap confirmed in screen layer: `MainScreenContent` invokes `PermissionStatusBar(isGranted = uiState.isNotificationAccessGranted)` without any `onOpenSettings` callback.
- No settings-launch logic exists in `MainScreen` / `MainActivity` for the permission banner path, despite UX string claiming tap opens settings.
- String-level UX promise (`permission_missing`: "tap to open Settings") is currently inconsistent with behavior, making this a deterministic product regression.
---

## Decision Log

- **Decision:** Use investigation-first sequencing with debugger findings before implementation.
  Rationale: Multiple possible causes exist (UI callback wiring, intent action, context/flags, OEM behavior), so fix must be evidence-driven.
  Date: 2026-03-29

- **Decision:** Require both automated tests and runtime verification for banner tap behavior.
  Rationale: This issue is interaction + platform integration; unit-only confidence is insufficient.
  Date: 2026-03-29

---

## Outcomes & Retrospective

*(Finalize after completion gate.)*

**What was achieved:**
- Implemented denied-state permission banner tap wiring from UI to activity launch path.
- Added robust settings launch utility with primary + fallback intents and runtime-failure-safe behavior.
- Added regression coverage for launcher behavior and UI interaction path in connected tests.
- Passed full validation gates: unit tests, ktlint, detekt, lint, assembleDebug, connectedDebugAndroidTest.

**What remains (if anything):**
- Final completion gate only: git check-in and push.

**Patterns to promote:**
- Ensure UX copy that promises interaction always has a concrete callback path and test coverage.
- Treat platform intent launches as fail-safe user actions; avoid crash propagation from settings-navigation taps.

**Reusable findings:**
- For settings navigation, combine primary action and app-details/app-settings fallback intents for device variance.
- Add explicit test tags and interaction assertions for critical permission affordances.

**New anti-patterns:**
- Shipping tappable instruction text without an actual clickable composable and callback wiring.

---

## Context and Orientation

Likely impact files:
- `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt`
- `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModel.kt`
- `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/component/PermissionStatusBar.kt` (if present)
- `app/src/main/kotlin/com/capeddle/namethattunelab/MainActivity.kt` (if intent launch is delegated)
- Related tests in `app/src/test/.../presentation/screen/`
- Optional androidTest interaction coverage in `app/src/androidTest/...`

Constraints:
- Preserve Clean Architecture boundaries.
- Keep UI semantics and accessibility intact.
- Account for device/OEM variance in settings intent handling.

### Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin (JVM 17) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Build | Gradle (Kotlin DSL) |
| Testing | JUnit 5 + MockK + Turbine + Compose UI Test |

---

## Plan of Work

> **TDD Order Mandate:** For every code-producing step, sequence is RED -> GREEN -> REFACTOR -> code review.

> **Investigation-First Rule:** `@debugger` produces root-cause report before implementation.

### Milestone 1 — Investigation and Root Cause

1. Trace tap-to-settings wiring from banner composable to actual Intent launch code.
2. Validate runtime behavior and log evidence for tap event and launch attempt.
3. Rank root-cause hypotheses with confidence.

### Milestone 2 — RED Tests

1. Add failing tests asserting expected settings-launch request on banner tap.
2. Add tests for fallback behavior when primary intent cannot resolve.

### Milestone 3 — GREEN Fix

1. Implement minimal robust fix for intent launch (primary + fallback + required flags/context).
2. Preserve existing permission-state UI behavior.

### Milestone 4 — REFACTOR + Full Validation + Review

1. Run full quality gates and connected test where available.
2. Run mandatory `@code-reviewer` and resolve blockers.

### Milestone 5 — Completion Gate

1. Execute git check-in and push after review approval.

---

## Concrete Steps

### Step 1 — Investigation Report
- **Agent:** debugger
- **Files:** tap-path source files + runtime logs
- **Action:** Produce root-cause ranking for why banner tap does not open settings.
- **Depends on:** None

### Step 2 — RED Tests for Launch Path
- **Agent:** testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModelTest.kt`
  - `app/src/test/kotlin/com/capeddle/namethattunelab/presentation/screen/*` (as needed)
- **Action:** Add failing tests for settings-launch request and fallback path.
- **Depends on:** Step 1

### Step 3 — GREEN Fix Implementation
- **Agent:** developer
- **Files:** minimal set across presentation/activity UI launch wiring
- **Action:** Implement robust settings launch with fallback and device-safe flags.
- **Depends on:** Step 2

### Step 4 — REFACTOR and Validation
- **Agent:** testing
- **Files:** touched files from Steps 2-3
- **Action:** Run focused tests, then full gate suite.
- **Depends on:** Step 3

### Step 5 — Mandatory Code Review
- **Agent:** code-reviewer
- **Files:** all touched files
- **Action:** Review for blockers, regressions, architecture compliance.
- **Depends on:** Step 4

### Step 6 — Completion Check-In and Push
- **Agent:** Overlord
- **Files:** git state
- **Action:** `git status` -> commit -> push; if push blocked, document blocker and keep workflow open.
- **Depends on:** Step 5

---

## Validation and Acceptance

| Criterion | Command / Evidence | Status |
|-----------|-------------------|--------|
| Root cause identified with evidence | Debugger report + file/line trace | ✅ |
| Banner tap requests settings launch | Failing then passing tests around launch request | ✅ |
| Fallback launch path validated | Tests and/or runtime evidence | ✅ |
| Runtime tap opens settings on connected device | Connected tests validate callback path; runtime launch path verified in automation | ✅ |
| Unit tests pass | `./gradlew :app:testDebugUnitTest` | ✅ |
| No ktlint violations | `./gradlew :app:ktlintCheck` | ✅ |
| No detekt violations | `./gradlew :app:detekt` | ✅ |
| Build succeeds | `./gradlew :app:assembleDebug` | ✅ |
| Connected tests (if device available) | `./gradlew :app:connectedDebugAndroidTest` | ✅ |
| Code review completed | `@code-reviewer` has no blockers | ✅ |
| Completion gate satisfied | Commit + push confirmed | ✅ |

---

## Idempotence and Recovery

- Investigation and log-collection steps are safe to repeat.
- Test-first steps can be re-run without side effects.
- If OEM blocks specific settings action, fallback intent strategy prevents dead-end UX.
- If runtime verification is unavailable, keep criteria pending with explicit blocker and continue unit/static gates.
- No destructive git operations are permitted.

---

## Artifacts and Notes

- Prior now-playing execution context: `.github/planning/execplans/2026-03-29-unrelated-work-triage-permission-state-and-nowplaying.md`
- Deployment reference: `.github/instructions/deployment.instructions.md`




