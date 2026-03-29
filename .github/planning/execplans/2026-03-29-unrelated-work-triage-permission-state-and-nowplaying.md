# Unrelated Work Triage + Permission-State Test Repair + Now-Playing Regression Continuation

This ExecPlan is a living document. Keep Progress, Surprises & Discoveries, Decision Log, and Outcomes & Retrospective up to date as work proceeds.

**Date:** 2026-03-29  
**Status:** ✅ Complete  
**Owner:** Overlord (GPT-5.3-Codex)  
**Refs:** User report (media detection stopped), [2026-03-23 investigation plan], current dirty worktree  
**Revision:** v1

---

## Purpose / Big Picture

The repository currently contains a mixed working tree: targeted now-playing regression work plus unrelated changes from prior sessions (UI-test infrastructure, governance/skills, and permission-state tests). The immediate objective is to prevent losing valuable unrelated work while also unblocking red tests and completing the now-playing regression recovery.

This plan intentionally starts with triage and classification of unrelated work. Valuable pieces will be incorporated and documented. Low-value or incomplete pieces will be isolated, fixed, or deferred with explicit rationale. After unrelated-work handling is complete, we will repair the unrelated permission-state test failures, then continue and finalize now-playing regression verification.

**Observable outcome:** Running the agreed validation set completes with no blockers, and both (a) unrelated work disposition and (b) now-playing regression status are documented with clear keep/defer decisions.

**Term definitions:**
- *Unrelated work:* Any modified/untracked files in the working tree not strictly required for the original now-playing bug fix.
- *Incorporate:* Keep in active scope and validate as part of this execution.
- *Defer:* Exclude from this execution and track separately, without deleting user work.
- *Permission-state test failures:* Failing tests introduced by new assertions/use-case stubs around notification-listener permission state.
- *Now-playing regression:* Failure mode where song changes are not detected/propagated after listener lifecycle transitions.

---

## Progress

- [x] Baseline triage data gathered (git status, diffs, runtime evidence)
- [x] Initial value assessment completed for unrelated work items
- [x] Milestone 1 complete: unrelated work disposition applied and documented
- [x] Milestone 2 complete: permission-state test failures repaired
- [x] Milestone 3 complete: now-playing regression continuation validated end-to-end
- [x] Milestone 4 complete: mandatory testing and code-review gates passed

---

## Surprises & Discoveries

- The device-level notification listener for this app was previously absent from enabled_notification_listeners, which can produce zero detections while UI still shows listening.
- A concrete lifecycle defect was reproduced in unit tests: MediaSessionMonitor.detachAll() canceled singleton scope and could starve emissions after reconnect.
- Unrelated work includes valuable Compose instrumented-test infrastructure that appears intentional and mostly complete.
- Unrelated permission-state tests are currently incomplete and can fail deterministically (missing production use case and mismatched UI-state expectations).

---

## Decision Log

- **Decision:** Preserve and evaluate unrelated changes rather than discard by default.  
  Rationale: User explicitly requested value assessment and selective incorporation.  
  Date: 2026-03-29

- **Decision:** Sequence work as triage -> permission-state repair -> now-playing continuation.  
  Rationale: Prevent noisy red tests from masking regression validation signal.  
  Date: 2026-03-29

- **Decision:** Treat governance/skills additions as low-risk but separate from runtime bug verification unless they block build/test.  
  Rationale: Minimize cross-concern coupling in a runtime regression lane.  
  Date: 2026-03-29

---

## Outcomes & Retrospective

**What was achieved:**
- Unrelated-work triage completed and high-value Compose instrumented-test infrastructure retained and validated.
- Permission-state lane repaired by adding explicit notification-access observation across domain/data/presentation and aligning tests with architecture.
- Now-playing reconnect regression fix preserved (MediaSessionMonitor.detachAll() no longer cancels singleton scope) and reconfirmed with focused tests.
- Mandatory gates passed: unit suite, ktlint, detekt, assembleDebug, and connectedDebugAndroidTest.
- Dedicated code review completed with no blockers (approved after one blocker fix cycle).

**What remains (if anything):**
- Optional hardening follow-up: in MainViewModel catch path for notification-access observation, set isListening = false together with permission false to avoid mixed-signal UI in exceptional error paths.

**Patterns to promote:**
- Keep permission state as explicit modeled UI state (isNotificationAccessGranted) instead of inferring from broader listening flags.
- Treat lifecycle reconnect behavior as first-class with targeted regression tests around service detach/reattach.
- Maintain strict Overlord gate order: developer -> testing -> code-reviewer; reject on any failed gate.

**Reusable findings:**
- Device-level listener enablement can drift and should be checked with adb shell settings get secure enabled_notification_listeners and adb shell dumpsys notification during runtime triage.
- Focused lane tests plus full suite/static/build gates provide fast iteration without sacrificing release confidence.

**New anti-patterns:**
- Avoid coupling isListening to previous state when permission streams can transition from denied to granted after startup.

---

## Context and Orientation

### Current analysis snapshot

### Resume pack (compaction-safe)

#### Repository and environment snapshot

- Repo: NameThatTuneLab
- Branch: main
- Date snapshot: 2026-03-29
- OS: Windows
- Device transport: ADB over Wi-Fi (192.168.2.28:5555 connected earlier in session)

#### Current working tree snapshot (from `git status --short`)

- Modified:
  - .github/planning/execplans/2026-03-22-compose-ui-test-infrastructure.md
  - app/build.gradle.kts
  - app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt
  - app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt
  - app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitorTest.kt
  - app/src/test/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModelTest.kt
  - gradle/libs.versions.toml
- Untracked (relevant):
  - app/src/androidTest/**
  - app/src/debug/**
  - app/src/test/kotlin/com/capeddle/namethattunelab/domain/usecase/ObserveNotificationAccessUseCaseTest.kt
  - .github/skills/adopt-template-updates/**
  - .github/skills/review-upstream-sources/**
  - .github/skills/validate-agent-tools/**
  - .github/planning/execplans/2026-03-23-media-detection-regression-investigation.md

#### Verified command evidence

1. Now-playing reconnect regression test status (GREEN currently):

```bash
./gradlew.bat :app:testDebugUnitTest --tests "com.capeddle.namethattunelab.nowplaying.MediaSessionMonitorTest" --no-daemon --console=plain
```

Result: BUILD SUCCESSFUL (test task FROM-CACHE in last run).

2. Permission-state lane status (RED currently):

```bash
./gradlew.bat :app:testDebugUnitTest \
  --tests "com.capeddle.namethattunelab.domain.usecase.ObserveNotificationAccessUseCaseTest" \
  --tests "com.capeddle.namethattunelab.presentation.screen.MainViewModelTest" \
  --no-daemon --console=plain
```

Result: BUILD FAILED with 3 failing tests.

Failing tests:
- ObserveNotificationAccessUseCaseTest > invoke delegates to repository observeNotificationAccess() FAILED
  - Cause: ClassNotFoundException / AssertionError at ObserveNotificationAccessUseCaseTest.kt:18
- MainViewModelTest > InitialState > notification access state reports not granted when access flow emits false() FAILED
  - Assertion failure at MainViewModelTest.kt:85
- MainViewModelTest > InitialState > listening indicator is false when notification listener permission is disabled() FAILED
  - Assertion failure at MainViewModelTest.kt:105

3. Runtime listener evidence captured earlier:
- `adb shell settings get secure enabled_notification_listeners` showed the app listener missing initially, then present after allow_listener.
- `adb shell dumpsys notification` later showed NowPlayingListenerService in both allowed and live listeners.

#### Known root-cause findings to preserve

- Lifecycle regression: MediaSessionMonitor.detachAll() canceled singleton coroutine scope, which can starve event emission after service reconnect.
- UI signal gap: current listening indicator can remain true even if notification listener is not effectively granted/enabled.
- Unrelated but valuable: Compose instrumented test harness additions appear intentional and useful.

#### Fast resume commands for next agent

```bash
# 1) confirm branch and working tree
 git status --short

# 2) confirm now-playing reconnect lane
 ./gradlew.bat :app:testDebugUnitTest --tests "com.capeddle.namethattunelab.nowplaying.MediaSessionMonitorTest"

# 3) reproduce permission-state red lane
 ./gradlew.bat :app:testDebugUnitTest --tests "com.capeddle.namethattunelab.domain.usecase.ObserveNotificationAccessUseCaseTest" --tests "com.capeddle.namethattunelab.presentation.screen.MainViewModelTest"

# 4) run broader safety gates once lane is fixed
 ./gradlew.bat :app:testDebugUnitTest
 ./gradlew.bat :app:ktlintCheck :app:detekt
 ./gradlew.bat :app:assembleDebug
```

### Initial unrelated-work disposition matrix

| Area | Files | Value | Risk | Initial decision |
|------|-------|-------|------|------------------|
| Compose UI test infrastructure | app/src/androidTest/**, app/src/debug/**, app/build.gradle.kts, gradle/libs.versions.toml, app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt | High | Medium (build/test wiring touch) | Incorporate and validate |
| Permission-state test spike | app/src/test/kotlin/com/capeddle/namethattunelab/domain/usecase/ObserveNotificationAccessUseCaseTest.kt, app/src/test/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModelTest.kt | Medium | High (currently red/incomplete) | Repair in Milestone 2 |
| Governance skill additions | .github/skills/adopt-template-updates/**, .github/skills/review-upstream-sources/**, .github/skills/validate-agent-tools/** | Medium | Low | Defer from runtime lane; keep as separate governance scope |
| Prior planning docs imported/untracked | .github/planning/execplans/2026-03-15-*.md, 2026-03-21-*.md, 2026-03-22-*.md | Medium | Low | Keep, no runtime impact |
| Runtime now-playing fix lane | app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt, app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitorTest.kt | High | Medium | Continue and complete validation |


1. **High-value unrelated work likely worth incorporating:**
   - Compose instrumented-test baseline files:
     - app/src/androidTest/kotlin/com/capeddle/namethattunelab/HiltTestRunner.kt
     - app/src/androidTest/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreenInstrumentedTest.kt
     - app/src/debug/kotlin/com/capeddle/namethattunelab/TestActivity.kt
     - app/src/debug/AndroidManifest.xml
   - Supporting build wiring:
     - app/build.gradle.kts (Hilt test runner + androidTest deps)
     - gradle/libs.versions.toml (espresso, hilt-android-testing aliases)
   - Stability hook:
     - app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt (delay field testTag)

2. **Unrelated work currently failing / incomplete:**
   - app/src/test/kotlin/com/capeddle/namethattunelab/domain/usecase/ObserveNotificationAccessUseCaseTest.kt
   - app/src/test/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModelTest.kt (new permission-state assertions ahead of production model)

3. **Now-playing regression evidence:**
   - RED test exists for reconnect emission starvation in app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitorTest.kt
   - Minimal production fix in app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt removes scope cancellation in detachAll

### Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin (JVM 17) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Build | Gradle (Kotlin DSL) |
| Testing | JUnit 5 + MockK + Turbine + Compose UI test |

---

## Plan of Work

> TDD Order Mandate: For every code-producing step, sequence is RED -> GREEN -> REFACTOR -> code review.

### Milestone 1 - Evaluate unrelated work and incorporate valuable parts

1. Classify each unrelated change as Incorporate, Defer, or Remove from current lane.
2. Keep high-value Compose test infrastructure and required Gradle wiring if validated.
3. Record final disposition table in this plan.

### Milestone 2 - Fix unrelated permission-state test failures

1. Decide target behavior and architecture for notification-access state.
2. Bring tests and production into alignment (either implement missing use case/state or scope tests down).
3. Ensure permission-state test lane is green.

### Milestone 3 - Continue now-playing regression

1. Re-verify reconnect lifecycle regression with focused tests.
2. Validate runtime behavior with device logs after stable listener access.
3. Confirm no regression from unrelated incorporation.

### Milestone 4 - Mandatory validation and review gates

1. Run required quality gates.
2. Run @code-reviewer and resolve blockers.
3. Close this plan with retrospective notes.

---

## Concrete Steps

### Step 1 - Unrelated work classification matrix
- **Agent:** Overlord
- **Files:** git working tree summary + changed files
- **Action:** Produce a table: file, value, risk, decision (Incorporate/Defer), rationale.
- **Depends on:** None

### Step 2 - RED for permission-state lane
- **Agent:** testing
- **Files:**
  - app/src/test/kotlin/com/capeddle/namethattunelab/domain/usecase/ObserveNotificationAccessUseCaseTest.kt
  - app/src/test/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModelTest.kt
- **Action:** Ensure tests represent agreed target behavior and fail for meaningful reasons only.
- **Depends on:** Step 1

### Step 3 - GREEN for permission-state lane
- **Agent:** developer
- **Files:** minimal required production and test files in domain/presentation/di
- **Action:** Implement or adjust behavior so permission-state tests pass without reflection hacks or false assumptions.
- **Depends on:** Step 2

### Step 4 - REFACTOR permission-state lane
- **Agent:** testing
- **Files:** touched lane files
- **Action:** Clean tests/code; run focused lane tests then broader unit tests.
- **Depends on:** Step 3

### Step 5 - Reconfirm now-playing regression fix
- **Agent:** developer + testing
- **Files:**
  - app/src/main/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitor.kt
  - app/src/test/kotlin/com/capeddle/namethattunelab/nowplaying/MediaSessionMonitorTest.kt
- **Action:** Keep reconnect-emission fix and verify focused nowplaying tests remain green.
- **Depends on:** Step 4

### Step 6 - Runtime verification on device
- **Agent:** debugger
- **Files:** runtime logs only
- **Action:** Capture logcat during playback app switching and confirm now-playing events flow through NTL pipeline.
- **Depends on:** Step 5

### Step 7 - Validation gate
- **Agent:** testing
- **Files:** all touched files
- **Action:** Run:
  - ./gradlew :app:testDebugUnitTest
  - ./gradlew :app:ktlintCheck :app:detekt
  - ./gradlew :app:assembleDebug
  - ./gradlew :app:connectedDebugAndroidTest (if device available)
- **Depends on:** Step 6

### Step 8 - Mandatory code review
- **Agent:** code-reviewer
- **Files:** all touched files
- **Action:** Review for blockers; feed findings back into fixes.
- **Depends on:** Step 7

---

## Validation and Acceptance

| Criterion | Command / Evidence | Status |
|-----------|-------------------|--------|
| Unrelated changes disposition documented | Decision matrix committed in this plan | ✅ |
| Valuable unrelated UI-test infra validated | connectedDebugAndroidTest passes target test | ✅ |
| Permission-state test failures resolved | Focused tests pass with no placeholder/missing-class failures | ✅ |
| Reconnect regression covered | MediaSessionMonitor reconnect test passes | ✅ |
| Nowplaying test lane green | :app:testDebugUnitTest --tests com.capeddle.namethattunelab.nowplaying.* | ✅ |
| Unit tests pass | ./gradlew :app:testDebugUnitTest | ✅ |
| No ktlint violations | ./gradlew :app:ktlintCheck | ✅ |
| No detekt violations | ./gradlew :app:detekt | ✅ |
| Build succeeds | ./gradlew :app:assembleDebug | ✅ |
| Code review completed | @code-reviewer has no blockers | ✅ |

---

## Idempotence and Recovery

- Classification and logging steps are read-only and safe to repeat.
- If permission-state lane grows too broad, split into a dedicated follow-up ExecPlan and keep this plan scoped to stabilization.
- If runtime verification is blocked by device state, proceed with unit/instrumented checks and mark runtime criterion pending with exact blocker.
- No destructive git operations are allowed; preserve unrelated work unless explicitly instructed otherwise.

---

## Artifacts and Notes

- Existing investigation plan retained: .github/planning/execplans/2026-03-23-media-detection-regression-investigation.md
- Runtime listener checks performed via ADB:
  - adb shell settings get secure enabled_notification_listeners
  - adb shell dumpsys notification





