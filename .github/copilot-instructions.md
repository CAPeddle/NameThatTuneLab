# Copilot Instructions — Android App Template

This is an **agent-native** governance kit for Android development. All agents, instructions, hooks, and skills are designed for autonomous agent workflows.

## Repository Purpose

A template repository providing Copilot governance for Android apps built with Kotlin, Jetpack Compose, and Clean Architecture. Fork this repo, replace `[PLACEHOLDER]` markers, and agents immediately have working governance.

## Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin (latest stable) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Min SDK | API 33 |
| Build | Gradle (Kotlin DSL) |
| Formatting | ktlint (via `.editorconfig`) |
| Static Analysis | detekt (`detekt.yml`) + Android Lint |
| Unit Testing | JUnit 5 + MockK + Turbine |
| UI Testing | Compose UI Test + Robolectric |

## Directory Structure (Clean Architecture)

```
app/
├── src/main/kotlin/[PLACEHOLDER:package-name]/
│   ├── domain/          ← Business logic, use cases, repository interfaces
│   │   ├── model/       ← Domain entities (pure Kotlin, no Android imports)
│   │   ├── repository/  ← Repository interfaces
│   │   └── usecase/     ← Use cases (single responsibility)
│   ├── data/            ← Repository implementations, data sources, mappers
│   │   ├── local/       ← Room DAOs, DataStore
│   │   ├── remote/      ← Retrofit services, API models
│   │   ├── repository/  ← Repository implementations
│   │   └── mapper/      ← Data ↔ Domain mappers
│   ├── presentation/    ← ViewModels, UI state, Compose screens
│   │   ├── screen/      ← Screen composables + ViewModels
│   │   ├── component/   ← Reusable UI components
│   │   ├── navigation/  ← Navigation graph
│   │   └── theme/       ← Material 3 theme
│   └── di/              ← Hilt modules
├── src/test/            ← Unit tests (JUnit 5 + MockK)
└── src/androidTest/     ← Instrumented tests (Compose UI Test)
```

## Build & Test Commands

```bash
./gradlew build                # Full build
./gradlew test                 # Unit tests
./gradlew connectedAndroidTest # UI tests (requires emulator)
./gradlew ktlintCheck          # Formatting check
./gradlew ktlintFormat         # Auto-format
./gradlew detekt               # Static analysis
./gradlew lint                 # Android Lint
```

## Key File Locations

| File | Purpose |
|------|---------|
| `detekt.yml` | Static analysis config (strict-from-start) |
| `.editorconfig` | Formatting rules (ktlint reads this) |
| `.github/agents/` | Agent definitions (Overlord, Developer, Debugger, Testing, Code Reviewer) |
| `.github/hooks/` | Lifecycle hooks (quality gate, auto-format, session context) |
| `.github/instructions/` | Path-specific coding standards |
| `.github/skills/` | Agent skills (build-and-test, get-api-docs) |
| `.github/planning/` | ExecPlan standard + templates |
| `.github/workflows/android-ci.yml` | CI pipeline |

## Agent Roster

| Agent | Role | Invoke |
|-------|------|--------|
| Overlord | Orchestrator — planning, delegation, review gate | `@overlord` |
| Developer | Implementation — Kotlin/Compose/Architecture | `@developer` |
| Debugger | Investigation — Logcat, LeakCanary, StrictMode | `@debugger` |
| Testing | TDD — JUnit 5, MockK, Compose UI Test | `@testing` |
| Code Reviewer | Review — idioms, patterns, compound learning | `@code-reviewer` |

## CI Workflow

The `android-ci.yml` workflow runs on push/PR to `main` and `develop`:
1. Build (`assembleDebug`)
2. Unit tests (`testDebugUnitTest`)
3. ktlint check
4. detekt
5. Android Lint

## Planning

Tasks touching >3 files require an **ExecPlan**. See `.github/planning/PLANS.md` for the authoring standard and `.github/planning/execplans/_TEMPLATE.md` for the template.
