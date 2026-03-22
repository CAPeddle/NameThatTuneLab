# NameThatTuneLab — Compose UI Test Infrastructure + SettingsPanel Keyboard Semantics

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** 2026-03-22
**Status:** 🆕 Not Started
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

- [ ] Milestone 1 — Investigation and testability design decisions
- [ ] Milestone 2 — AndroidTest baseline scaffolding (RED)
- [ ] Milestone 3 — Keyboard semantics test implementation (GREEN)
- [ ] Milestone 4 — Quality gates + review + closeout

---

## Surprises & Discoveries

*(Record unexpected findings as they occur.)*

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

---

## Outcomes & Retrospective

*(Complete after plan closes.)*

**What was achieved:**

**What remains (if anything):**

**Patterns to promote:**

**Reusable findings:**
[Governance updates identified during review — changes to instructions, agent guidance, or detekt rules that should be applied to improve future cycles.]

**New anti-patterns:**

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
| AndroidTest baseline exists and compiles | `./gradlew :app:connectedDebugAndroidTest` reaches execution phase | - |
| Delay field is uniquely and stably selectable in test | Test code uses resilient selector strategy | - |
| Keyboard semantics assertion implemented | Instrumented test asserts numeric/IME intent evidence | - |
| Done action path verified | Test verifies Done pathway behavior (focus/event semantics) | - |
| All unit tests pass | `./gradlew :app:testDebugUnitTest` — zero failures | - |
| No ktlint violations | `./gradlew :app:ktlintCheck` — zero violations | - |
| No detekt violations | `./gradlew :app:detekt` — zero findings | - |
| Build succeeds | `./gradlew :app:assembleDebug` — `BUILD SUCCESSFUL` | - |
| Code review completed | `@code-reviewer` — no blockers | - |
| TDD cycle completed | RED → GREEN → REFACTOR for new code | - |

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