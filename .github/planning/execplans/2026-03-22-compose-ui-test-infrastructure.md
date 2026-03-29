# NameThatTuneLab — Compose UI Test Infrastructure + SettingsPanel Keyboard Semantics

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** 2026-03-22
**Status:** ✅ Done
**Owner:** Overlord Agent
**Refs:** Deferred scope from `2026-03-22-code-review-nits-settings.md` (Q3); commit `b09e57c`
**Revision:** v1

---

## Purpose / Big Picture

The previous settings nits session intentionally shipped `KeyboardType.Number` and
`ImeAction.Done` as a declarative UI change without Compose UI instrumented coverage.
This plan establishes a minimal, reliable Compose UI test baseline under `androidTest` and
adds one focused test that proves keyboard semantics for the delay input in `SettingsPanel`.

The scope is intentionally narrow: introduce only what is needed to run and maintain a single
high-value test, without expanding into broad UI coverage or new testing abstractions.

**Observable outcome:** Running `./gradlew :app:connectedDebugAndroidTest` on a connected
device/emulator executes a Compose test that verifies the delay field exposes numeric keyboard
semantics and a Done IME action pathway.

**Term definitions:**
- *Compose UI instrumented test:* Android test in `src/androidTest` that runs on a device/emulator
  and validates composable behavior through semantics and UI interactions.
- *Keyboard semantics:* The semantic properties of a text field tied to keyboard behavior,
  including IME action and input expectations exposed through Compose testing APIs.
- *Baseline infrastructure:* The minimum Gradle dependencies, runner config, and test package
  scaffolding required so `connectedDebugAndroidTest` can execute reliably.
- *MVP test harness:* Smallest practical setup that supports one meaningful assertion path and
  can be expanded later without redesign.

---

## Progress

- [x] Milestone 1 — Investigation and testability design decisions
- [x] Milestone 2 — AndroidTest baseline scaffolding (RED)
- [x] Milestone 3 — Keyboard semantics test implementation (GREEN)
- [x] Milestone 4 — Quality gates + review + closeout

---

## Surprises & Discoveries

- Compose semantics can reliably assert ImeAction.Done and Done-path behavior (performImeAction + focus clearing), but cannot directly prove KeyboardType.Number via a stable built-in matcher.
- app/src/androidTest/ is absent, so baseline infrastructure must be created as part of this plan.
- app/build.gradle.kts currently sets instrumentation runner args for de.mannodermaus.junit5.AndroidJUnit5Builder; no matching plugin/dependency is evident, so this is a likely connected-test startup risk to verify during RED setup.
- Compose instrumented tests fail with "No compose hierarchies found" when the device screen is off (mAwake=false) — the ComposeRootRegistry never sees the hierarchy even though the activity launches correctly. Fix: declare showWhenLocked/turnScreenOn on the test activity in the debug manifest.
- The test activity must live in app/src/debug/ (not androidTest/) because createAndroidComposeRule<T>() launches it in the app process, not the test process. Declaring it only in the androidTest manifest causes "Intent resolved to different process" errors.

---

## Decision Log

- **Decision:** Keep this plan limited to one end-to-end keyboard semantics test.
  Rationale: The deferred scope is specifically keyboard semantics validation; expanding beyond
  this would violate the original scope split and delay delivery.
  Date: 2026-03-22

- **Decision:** Prefer explicit semantics hooks (e.g., test tags / semantic markers) only when
  text-based node selection is brittle.
  Rationale: Avoid unnecessary production code changes for testability unless stability requires it.
  Date: 2026-03-22

- **Decision:** Use exactly one stable `testTag` on the delay input if RED tests show selector brittleness due to localization or node ordering.
  Rationale: One explicit hook keeps production impact minimal while eliminating fragile text/index-based selection.
  Date: 2026-03-22
- **Decision:** Implement one stable delay-field `testTag` and target the field by tag in the instrumented test.
  Rationale: This avoids localization and index-order brittleness while keeping production changes minimal.
  Date: 2026-03-22

---

## Outcomes & Retrospective

**What was achieved:**
- Full Compose UI instrumented test baseline established: HiltTestRunner, androidTest dependencies, TestActivity in debug source set, one passing keyboard-semantics test.
- Both quality gates pass: connectedDebugAndroidTest (1 test, PASSED on Pixel 8 API 16) and the combined ktlintCheck detekt testDebugUnitTest assembleDebug gate.
- ImeAction.Done semantics and focus-clearing behaviour verified end-to-end for the delay input field.

**What remains (if anything):**
None. All acceptance criteria met.

**Patterns to promote:**
- Place test-only activities in pp/src/debug/ and declare them with ndroid:showWhenLocked="true" and ndroid:turnScreenOn="true" in pp/src/debug/AndroidManifest.xml. This ensures reliable Compose test execution regardless of device screen state.
- Add composeTestRule.waitForIdle() immediately after setContent {} as a defensive synchronization barrier before the first node interaction.

**Reusable findings:**
- The HiltTestRunner pattern (extend AndroidJUnitRunner, substitute HiltTestApplication) is the correct minimal approach for any project using @HiltAndroidApp, even when individual tests don't use @HiltAndroidTest.
- KeyboardType.Number cannot be directly asserted via a stable Compose UI test API; assert ImeAction.Done and the Done-path focus-clearing behaviour as the closest reliable semantic evidence.
- Compose instrumented tests require the device screen to be on (mAwake=true). When the screen is off, ComposeRootRegistry never registers the hierarchy and tests fail with "No compose hierarchies found" even though the activity launches. Declare showWhenLocked/	urnScreenOn in the debug manifest to make tests self-sufficient.

**New anti-patterns:**
- Do not declare test activities in pp/src/androidTest/AndroidManifest.xml with an <activity> element — the test APK runs in a different process and cannot host activities launched by the test framework targeting the app process. Activities used in createAndroidComposeRule<T>() must live in the app's debug source set.

---

## Context and Orientation

### Known current state

- `app/src/androidTest/` does not exist yet in this repository.
- Compose UI test dependencies are present in version catalog and referenced in app module
  (`androidTestImplementation(platform(libs.compose.bom))` and
  `androidTestImplementation(libs.compose.ui.test.junit4)`).
- Prior settings change introduced `KeyboardType.Number`, `ImeAction.Done`, `singleLine = true`,
  and `KeyboardActions(onDone = { focusManager.clearFocus() })` in `SettingsPanel`.
- We must validate this behavior via instrumented test, not just static code inspection.

### Candidate files in scope

| File | Expected role |
|------|----------------|
| `app/src/androidTest/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreenInstrumentedTest.kt` | New Compose UI test(s) for settings panel semantics |
| `app/src/androidTest/AndroidManifest.xml` (if needed) | Instrumented test manifest overrides |
| `app/build.gradle.kts` | Validate/add androidTest config only if missing |
| `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt` | Optional minimal testability hooks if selector brittleness is found |

### Constraints

- Respect existing Material 3 Compose patterns and state hoisting.
- Avoid introducing additional features/pages/components.
- Do not add broad test utility layers unless required for this single target test.
- Keep all file writes UTF-8 without BOM to avoid static-analysis false positives.

### Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin (JVM 17) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Build | Gradle (Kotlin DSL) |
| Testing (unit) | JUnit 5 + MockK + Turbine + Robolectric |
| Testing (UI) | Compose UI Test (instrumented, AndroidJUnit4 runner) |

---

## Plan of Work

> **TDD Order Mandate:** For every code-producing step, the sequence is always:
> 1. `@testing` writes failing tests (RED)
> 2. `@developer` writes implementation (GREEN)
> 3. `@testing` verifies all pass (REFACTOR)
> 4. `@code-reviewer` reviews
>
> Do NOT write implementation before tests exist.

> **Investigation-First Rule:** This plan begins with explicit investigation to verify whether
> existing UI semantics are directly assertable or need minimal testability hooks.

### Milestone 1 — Investigation + assertion strategy
1. Verify current `MainScreen` / `SettingsPanel` node selectors available to Compose tests.
2. Determine whether keyboard semantics can be asserted without production code changes.
3. If selectors are brittle, define the smallest stable hook (e.g., one `testTag`).

### Milestone 2 — Baseline androidTest scaffolding
1. Create `androidTest` package and minimal Compose test class scaffold.
2. Ensure test runner/dependencies are sufficient for `connectedDebugAndroidTest`.

### Milestone 3 — Implement one keyboard semantics test
1. Add a focused test that navigates to the settings panel state and targets delay field.
2. Assert numeric keyboard semantics + Done IME action path (or closest supported semantics evidence).
3. Keep assertions robust to localization and theme variants.

### Milestone 4 — Validate + review + close
1. Run connected test task and standard quality gates applicable to touched files.
2. Perform mandatory `@code-reviewer` review.
3. Apply findings and close plan with retrospective.

---

## Concrete Steps

### Step 1 — Investigation report (pre-implementation)
- **Agent:** @debugger
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt`
  - `app/build.gradle.kts`
- **Action:** Produce a short report: current selector options, semantics visibility, and whether
  one test can validate keyboard intent without production modifications.
- **Depends on:** None

### Step 2 — RED: write failing instrumented test skeleton
- **Agent:** @testing
- **Files:**
  - `app/src/androidTest/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreenInstrumentedTest.kt` (new)
  - `app/src/androidTest/AndroidManifest.xml` (optional, only if runner setup needs it)
- **Action:** Add a failing Compose test for delay-field keyboard semantics and Done action path.
  Keep test isolated to one scenario.
- **Depends on:** Step 1

### Step 3 — GREEN: minimal implementation/hooks
- **Agent:** @developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt` (only if needed)
  - `app/build.gradle.kts` (only if missing config)
- **Action:** Make minimal code/config changes so the new test passes. Prefer no production code
  changes if assertions can be achieved with existing semantics.
- **Depends on:** Step 2

### Step 4 — REFACTOR: run validation suite
- **Agent:** @testing
- **Files:** all touched files
- **Action:** Run:
  - `./gradlew :app:connectedDebugAndroidTest`
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug`
- **Depends on:** Step 3

### Step 5 — Mandatory code review
- **Agent:** @code-reviewer
- **Files:** all touched files
- **Action:** Review test robustness, selector stability, Compose idioms, and scope discipline.
- **Depends on:** Step 4

### Step 6 — Address findings and close plan
- **Agent:** @developer + @testing + Overlord
- **Files:** as directed by review
- **Action:** Resolve any blockers, re-run relevant gates, update Progress and Retrospective.
- **Depends on:** Step 5

---

## Validation and Acceptance

| Criterion | Command / Evidence | Status |
|-----------|-------------------|--------|
| AndroidTest baseline exists and compiles | `./gradlew :app:connectedDebugAndroidTest` — 1 test PASSED on Pixel 8 API 16 | ✅ connectedDebugAndroidTest passes |
| Delay field is uniquely and stably selectable in test | Test code uses resilient selector strategy | ✅ Stable testTag selector implemented |
| Keyboard semantics assertion implemented | Instrumented test asserts numeric/IME intent evidence | ✅ IME Done semantics asserted on delay field |
| Done action path verified | Test verifies Done pathway behavior (focus/event semantics) | ✅ performImeAction clears focus |
| All unit tests pass | `./gradlew :app:testDebugUnitTest` — zero failures | ✅ via combined gate command |
| No ktlint violations | `./gradlew :app:ktlintCheck` — zero violations | ✅ via combined gate command |
| No detekt violations | `./gradlew :app:detekt` — zero findings | ✅ via combined gate command |
| Build succeeds | `./gradlew :app:assembleDebug` — `BUILD SUCCESSFUL` | ✅ via combined gate command |
| Code review completed | `@code-reviewer` — no blockers | ✅ Reviewed; R1 (waitForIdle after setContent) applied |
| TDD cycle completed | RED → GREEN → REFACTOR for new code | ✅ Complete |

---

## Idempotence and Recovery

- Re-running scaffolding and test generation steps is safe; they are additive.
- If selector strategy proves unstable, rollback optional production test hooks and prefer a
  single explicit `testTag` as the only non-functional UI change.
- If connected device/emulator is unavailable, keep plan in `🚧 In Progress` and document the
  blocked validation criteria with exact missing prerequisite (`adb devices` empty / emulator offline).
- Keep edits minimal and isolated so reverting to pre-plan state is a simple file-level rollback.

---

## Artifacts and Notes

- Parent/dependency plan: `.github/planning/execplans/2026-03-22-code-review-nits-settings.md`
- Trigger: Q3 deferred decision (instrumented keyboard semantics test split out)
- Scope guard: this plan validates existing behavior; it does not redesign settings UX.