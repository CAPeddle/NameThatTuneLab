# NameThatTuneLab — Address Code-Review Nits: Use-Case Tests, Save-Error Handling, Keyboard Type + IME

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** 2026-03-22
**Status:** ✅ Complete
**Owner:** Overlord Agent
**Refs:** Code-review nits from `2026-03-21-configurable-runtime-settings.md`; commit `bf05921`
**Revision:** v2

---

## Purpose / Big Picture

Three non-blocking findings from the previous code-review cycle were deferred after quality gates passed.
This plan addresses them in a single focused session:

1. **Thin use-case tests** — `ObserveAppSettingsUseCase` and `UpdateAppSettingsUseCase` have no dedicated
   unit tests. The classes are short delegates, but their behavioral contracts (what they call, with what
   arguments, and what they return) are unverified in isolation.

2. **Save-failure error mapping** — `MainViewModel.saveSettings()` calls `updateAppSettings(…)` inside a
   `viewModelScope.launch` with no exception handling. If the DataStore write throws (e.g., `IOException`),
   the coroutine silently crashes and the user sees nothing.

3. **Numeric keyboard type + IME action** — The voice-over delay `OutlinedTextField` in `SettingsPanel`
   has no `keyboardOptions`. This plan adds `KeyboardType.Number` and `ImeAction.Done` (with
   `LocalFocusManager.clearFocus()` on confirm). The Compose UI instrumented test that verifies the
   keyboard semantics is deferred to a dedicated follow-on plan (see Artifacts and Notes).

**Observable outcome:**
- `./gradlew testDebugUnitTest` passes with new tests covering `ObserveAppSettingsUseCase`,
  `UpdateAppSettingsUseCase`, and `MainViewModel.saveSettings()` exception path.
- A DataStore write failure in `saveSettings()` sets `uiState.errorMessage` to a non-null string
  (verified by unit test); pending inputs are retained so the user can retry.
- The delay `OutlinedTextField` in `SettingsPanel` carries `keyboardType = KeyboardType.Number` and
  `imeAction = ImeAction.Done`; pressing Done dismisses the keyboard via `LocalFocusManager.clearFocus()`.
  Instrumented verification is tracked separately.

**Term definitions:**
- *Thin use case:* A use case class whose entire `invoke` body is a single delegation to a repository
  method with no additional logic. Even so, the contract (type, delegation, argument threading) warrants
  a unit test.
- *Save-failure:* Any `Throwable` thrown by `UpdateAppSettingsUseCase.invoke()` /
  `AppSettingsRepository.updateSettings()` during the DataStore write. `CancellationException` must
  be re-thrown, not caught.
- *errorMessage:* `MainUiState.errorMessage: String?`; when non-null it surfaces as a
  `SnackbarDuration.Short` snackbar via `LaunchedEffect(uiState.errorMessage)` in `MainScreenContent`.
- *Pending input:* The current (unsaved) string values `musicBrainzUserAgentInput` and
  `voiceOverDelayMsInput` in `MainUiState` that the user has typed but not yet persisted.
- *IME action:* The action button shown on the soft keyboard (Done, Next, Search, etc.), provided via
  `KeyboardOptions.imeAction` and handled via `KeyboardActions`.

---

## Clarifying Questions — Resolved

| # | Question | Answer | Impact on plan |
|---|----------|--------|---------------|
| Q1 | On save failure, retain or revert pending inputs? | **(a) Retain** | `saveSettings()` only sets `errorMessage`; no input-state change on failure |
| Q2 | Two files or one combined file for use-case tests? | **(a) Two separate files** | `ObserveAppSettingsUseCaseTest.kt` + `UpdateAppSettingsUseCaseTest.kt` |
| Q3 | Compose UI instrumented test for keyboard type? | **(b) Yes — but separate plan** | Declarative change ships here; instrumented test deferred to follow-on ExecPlan |
| Q4 | Add `ImeAction.Done` + keyboard dismissal? | **(b) Yes** | Step 4 adds `ImeAction.Done` + `LocalFocusManager.clearFocus()`; new imports required |

---

## Progress

- [x] Clarifications answered (Q1–Q4)
- [x] Milestone 1 — Thin use-case unit tests
- [x] Milestone 2 — Save-failure error mapping + test
- [x] Milestone 3 — Numeric keyboard type + ImeAction.Done on delay field
- [x] Milestone 4 — Quality gates + code review

---

## Surprises & Discoveries

- `UpdateAppSettingsUseCase.invoke()` is `suspend` but not wrapped in any coroutines scope — correctly
  called from `viewModelScope.launch` in `MainViewModel.saveSettings()`. The `CancellationException`
  must be re-thrown (not swallowed) in the `onFailure` block of the save-error fix.
- No `KeyboardOptions`, `KeyboardType`, `ImeAction`, `KeyboardActions`, or `LocalFocusManager` imports
  exist in `MainScreen.kt` yet — all five will be added as part of Step 4.
- No existing `ObserveAppSettingsUseCaseTest.kt` or `UpdateAppSettingsUseCaseTest.kt` in test source set.
- Q3(b) revealed that this repo has no Compose UI instrumented test infrastructure yet; building that
  baseline is a non-trivial separate task and will be addressed in a dedicated plan.

---

## Decision Log

- **Decision:** Retain pending inputs on save failure (Q1 = a).
  Rationale: Simpler, no extra state required, consistent with standard Android form patterns. User can
  correct and retry immediately after the snackbar.
  Date: 2026-03-22

- **Decision:** Two separate test files for the thin use cases (Q2 = a).
  Rationale: Matches existing one-use-case-per-file convention established by `AnnounceTrackUseCaseTest.kt`.
  Date: 2026-03-22

- **Decision:** Compose UI keyboard-type instrumented test deferred to follow-on plan (Q3 = b, scoped out).
  Rationale: No Compose UI test infrastructure exists in the repo; standing it up is more than a nit fix.
  Declarative `keyboardType` change ships here; instrumented coverage ships separately.
  Date: 2026-03-22

- **Decision:** Add `ImeAction.Done` + `LocalFocusManager.clearFocus()` (Q4 = b).
  Rationale: Improves UX — keyboard closes cleanly when user confirms the numeric entry. Cost is low
  (~5 lines + imports) and fits naturally alongside `KeyboardType.Number`.
  Date: 2026-03-22

---

## Outcomes & Retrospective

**What was achieved:**
- All three deferred nits from the previous code-review cycle are resolved.
- `ObserveAppSettingsUseCaseTest` and `UpdateAppSettingsUseCaseTest` lock the delegation contracts
  for both thin use cases (4 new tests).
- `MainViewModel.saveSettings()` now handles DataStore write failures via `runCatching` with proper
  `CancellationException` re-throw, verified by a new unit test.
- The `onSuccess` path clears any stale `errorMessage` so snackbars don't persist after a successful retry.
- The delay `OutlinedTextField` carries `KeyboardType.Number`, `ImeAction.Done`, and `singleLine = true`
  with keyboard dismissal via `LocalFocusManager.clearFocus()`.
- All 4 code-review nits were addressed before the final commit.

**What remains (if anything):**
- Follow-on plan: Compose UI instrumented test infrastructure and keyboard-semantics test for `SettingsPanel`
  (Q3 deferred scope). Track in `2026-03-22-compose-ui-test-infrastructure.md`.

**Patterns to promote:**
- `runCatching { ... }.onSuccess { ... }.onFailure { throwable -> if (throwable is CancellationException) throw throwable; ... }`
  is the correct ViewModel exception-handling pattern for coroutine-launched operations.
- Thin use-case tests should always assert delegation (what method was called, with what args) rather
  than re-asserting the data model.
- Always use `UTF-8 without BOM` when writing Kotlin files via PowerShell — use
  `New-Object System.Text.UTF8Encoding($false)` to avoid `MissingPackageDeclaration` detekt false positives.

**Reusable findings:**
- detekt `MissingPackageDeclaration` at 1:1 is a reliable signal of a UTF-8 BOM in the file, not a
  missing package declaration.
- detekt configuration cache can serve stale PASS results; always re-run with `--rerun-tasks` when
  investigating a quality gate regression after file writes.
- `singleLine = true` is required alongside `ImeAction.Done` on `OutlinedTextField` for reliable
  IME Done action across all Android IME implementations.

**New anti-patterns:**
- Do NOT write Kotlin files using `[System.IO.File]::WriteAllText($path, $content, [System.Text.Encoding]::UTF8)` —
  this encoding variant writes a BOM. Always use `New-Object System.Text.UTF8Encoding($false)` explicitly.

---

## Context and Orientation

### Relevant source files

| File | Role in this plan |
|------|------------------|
| `domain/usecase/ObserveAppSettingsUseCase.kt` | Subject of new test — single-line Flow delegation |
| `domain/usecase/UpdateAppSettingsUseCase.kt` | Subject of new test — single-line suspend delegation |
| `domain/repository/AppSettingsRepository.kt` | Interface mocked in both new test files |
| `domain/model/AppSettings.kt` | Data used in test assertions |
| `presentation/screen/MainViewModel.kt` | `saveSettings()` needs `runCatching` around `updateAppSettings(…)` |
| `presentation/screen/MainScreen.kt` | `SettingsPanel` delay field needs `keyboardOptions` + `keyboardActions` |
| `presentation/screen/MainViewModelTest.kt` | Add save-failure test inside `SettingsIntegration` nested class |
| `domain/usecase/AnnounceTrackUseCaseTest.kt` | Convention reference for new test file structure |

### Current `saveSettings()` — no exception handling

```kotlin
fun saveSettings() {
    viewModelScope.launch {
        val delayMs = uiState.value.voiceOverDelayMsInput.toLongOrNull()
        if (delayMs == null) {
            _uiState.update { it.copy(errorMessage = "Delay must be a whole number in milliseconds") }
            return@launch
        }
        updateAppSettings(             // ← throws silently if DataStore fails
            AppSettings(
                musicBrainzUserAgent = uiState.value.musicBrainzUserAgentInput,
                voiceOverDelayMs = delayMs
            )
        )
    }
}
```

### Required `saveSettings()` fix

```kotlin
fun saveSettings() {
    viewModelScope.launch {
        val delayMs = uiState.value.voiceOverDelayMsInput.toLongOrNull()
        if (delayMs == null) {
            _uiState.update { it.copy(errorMessage = "Delay must be a whole number in milliseconds") }
            return@launch
        }
        runCatching {
            updateAppSettings(
                AppSettings(
                    musicBrainzUserAgent = uiState.value.musicBrainzUserAgentInput,
                    voiceOverDelayMs = delayMs
                )
            )
        }.onFailure { throwable ->
            if (throwable is CancellationException) throw throwable
            _uiState.update { it.copy(errorMessage = "Failed to save settings: ${throwable.message}") }
        }
    }
}
```

`CancellationException` is already imported in `MainViewModel.kt` (present via Kotlin stdlib).
Verify with grep before adding a duplicate import.

### Current `SettingsPanel` delay field — no keyboard options

```kotlin
OutlinedTextField(
    value = uiState.voiceOverDelayMsInput,
    onValueChange = onVoiceOverDelayChanged,
    label = { Text(text = stringResource(R.string.settings_voice_over_delay_ms_label)) },
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
)
```

### Required delay field fix (Q4 = b)

```kotlin
val focusManager = LocalFocusManager.current   // declared at top of SettingsPanel composable

OutlinedTextField(
    value = uiState.voiceOverDelayMsInput,
    onValueChange = onVoiceOverDelayChanged,
    label = { Text(text = stringResource(R.string.settings_voice_over_delay_ms_label)) },
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
)
```

New imports required in `MainScreen.kt`:
- `androidx.compose.foundation.text.KeyboardActions`
- `androidx.compose.foundation.text.KeyboardOptions`
- `androidx.compose.ui.platform.LocalFocusManager`
- `androidx.compose.ui.text.input.ImeAction`
- `androidx.compose.ui.text.input.KeyboardType`

### Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin (JVM 17) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Build | Gradle (Kotlin DSL) |
| Testing (unit) | JUnit 5 + MockK + Turbine + Robolectric |
| Testing (UI) | Compose UI Test (deferred to follow-on plan) |

---

## Plan of Work

> **TDD Order Mandate:** For every code-producing step, the sequence is always:
> 1. `@testing` writes failing tests (RED)
> 2. `@developer` writes implementation (GREEN)
> 3. `@testing` verifies all pass (REFACTOR)
> 4. `@code-reviewer` reviews
>
> Do NOT write implementation before tests exist.

### Milestone 1 — Thin use-case unit tests
Create `ObserveAppSettingsUseCaseTest.kt` and `UpdateAppSettingsUseCaseTest.kt`.

*`ObserveAppSettingsUseCaseTest`*
1. `invoke delegates to repository observeSettings` — mock repository returns a specific
   `Flow<AppSettings>`; assert the use case emits the same value via Turbine `awaitItem()`.
2. `invoke emits default settings when repository emits defaults` — repository emits `AppSettings()`;
   assert emitted item equals `AppSettings()` (verifies no transformation applied).

*`UpdateAppSettingsUseCaseTest`*
1. `invoke delegates settings to repository updateSettings` — mock `updateSettings` as `coJustRun`;
   call use case with a non-default `AppSettings`; `coVerify` called with exact same object.
2. `invoke propagates repository exception` — mock throws `IOException("disk full")`; assert
   `assertThrows<IOException>` when calling the use case.

### Milestone 2 — Save-failure error mapping
Wrap `updateAppSettings(…)` in `runCatching` in `MainViewModel.saveSettings()`.
Add a unit test to `MainViewModelTest.SettingsIntegration`.

*`MainViewModelTest.SettingsIntegration` addition*
- `saveSettings sets errorMessage when repository throws` — `coEvery { updateAppSettings(any()) } throws IOException("disk full")`; set valid input state; call `vm.saveSettings()`; `advanceUntilIdle()`; assert `vm.uiState.value.errorMessage != null`; assert input fields are unchanged (Q1 = retain).

### Milestone 3 — Numeric keyboard type + ImeAction.Done on delay field
Single-file change to `SettingsPanel` in `MainScreen.kt`:
- Declare `val focusManager = LocalFocusManager.current` at the top of the composable.
- Add `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)`.
- Add `keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })`.
- Add all five missing imports (see Context section).
- No new unit tests required; Compose UI instrumented test is tracked separately.

---

## Concrete Steps

### Step 1 — RED: write failing use-case tests
- **Agent:** @testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/domain/usecase/ObserveAppSettingsUseCaseTest.kt` (new)
  - `app/src/test/kotlin/com/capeddle/namethattunelab/domain/usecase/UpdateAppSettingsUseCaseTest.kt` (new)
- **Action:** Write 2 tests each per Milestone 1 spec above. Confirm compilation succeeds but at least
  one assertion fails before proceeding (RED). Use `runTest` + Turbine for Flow assertions.
- **Depends on:** None

### Step 2 — RED: write failing save-failure test
- **Agent:** @testing
- **Files:**
  - `app/src/test/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModelTest.kt` (modify)
- **Action:** Inside the existing `@Suppress("FunctionNaming") inner class SettingsIntegration`, add the
  test described in Milestone 2. Import `java.io.IOException`. Confirm test compiles and fails RED before
  proceeding.
- **Depends on:** None (parallel with Step 1)

### Step 3 — GREEN: fix saveSettings() exception handling
- **Agent:** @developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainViewModel.kt`
- **Action:** Apply the `runCatching` fix from the Context section. Verify `CancellationException` is
  not double-imported. Do not change any other logic in the file.
- **Depends on:** Step 2

### Step 4 — GREEN: add keyboard options to delay field
- **Agent:** @developer
- **Files:**
  - `app/src/main/kotlin/com/capeddle/namethattunelab/presentation/screen/MainScreen.kt`
- **Action:** In `SettingsPanel`, add `val focusManager = LocalFocusManager.current`, then add
  `keyboardOptions` and `keyboardActions` to the delay `OutlinedTextField` per the Context section.
  Add all five missing imports in lexicographic order.
- **Depends on:** None (independent; can parallelize with Step 3)

### Step 5 — REFACTOR: verify all tests pass + quality gates
- **Agent:** @testing
- **Files:** all modified/created files
- **Action:** Run `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug`.
  Resolve any formatting or static-analysis violations before requesting code review.
- **Depends on:** Steps 1–4

### Step 6 — Mandatory code review
- **Agent:** @code-reviewer
- **Files:** all new/modified files from Steps 1–4
- **Action:** Review for Kotlin/Compose idioms, Clean Architecture compliance, test quality, and import
  ordering. Produce findings in blocker / nit format.
- **Depends on:** Step 5

### Step 7 — Address findings and close
- **Agent:** @developer + @testing + Overlord
- **Files:** as directed by review
- **Action:** Resolve blockers, re-run quality gates, update Progress, fill Retrospective, commit and push.
  Create follow-on ExecPlan for Compose UI test infrastructure (Q3 deferred scope).
- **Depends on:** Step 6

---

## Validation and Acceptance

| Criterion | Command / Evidence | Status |
|-----------|-------------------|--------|
| `ObserveAppSettingsUseCase` delegation + neutrality verified | `ObserveAppSettingsUseCaseTest` — 2 tests green | ✅ |
| `UpdateAppSettingsUseCase` delegation + exception propagation verified | `UpdateAppSettingsUseCaseTest` — 2 tests green | ✅ |
| Save failure sets `errorMessage` and retains pending inputs | `MainViewModelTest.SettingsIntegration` save-failure test green | ✅ |
| Delay field has `KeyboardType.Number` | Code inspection — `keyboardOptions` present in `SettingsPanel` | ✅ |
| Delay field has `ImeAction.Done` + `clearFocus()` | Code inspection — `keyboardActions` wired to `focusManager.clearFocus()` | ✅ |
| All unit tests pass | `./gradlew testDebugUnitTest` — zero failures | ✅ |
| No ktlint violations | `./gradlew ktlintCheck` — zero violations | ✅ |
| No detekt violations | `./gradlew detekt` — zero findings | ✅ |
| Build succeeds | `./gradlew assembleDebug` — `BUILD SUCCESSFUL` | ✅ |
| Code review completed | `@code-reviewer` — no blockers (4 nits addressed) | ✅ |
| TDD cycle completed | RED → GREEN → REFACTOR for Milestones 1 and 2 | ✅ |

---

## Idempotence and Recovery

- All changes are additive (new test files) or surgical single-block modifications.
- Re-running any step is safe — no migrations, no DB schema changes, no DI graph changes.
- If `runCatching` in `saveSettings()` causes a regression, revert `MainViewModel.kt` to commit
  `bf05921` state and inspect the `CancellationException` re-throw path.
- `LocalFocusManager` is a composition-local — it is safe to read inside any `@Composable` function;
  no lifecycle concerns.

---

## Artifacts and Notes

- **Parent plan:** `.github/planning/execplans/2026-03-21-configurable-runtime-settings.md`
- **Reference commit:** `bf05921` — all quality gates were green at this state
- **Follow-on plan required (Q3):** Create `2026-03-22-compose-ui-test-infrastructure.md` (or similar)
  to stand up Compose UI instrumented test baseline and add the keyboard-semantics test for `SettingsPanel`.
  This is explicitly out of scope for this plan.
