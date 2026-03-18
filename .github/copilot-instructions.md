# Copilot Instructions — NameThatTuneLab

This repository is an Android app project with **agent-native governance** for Copilot workflows.

## Repository Purpose

NameThatTuneLab is an Android app built with Kotlin, Jetpack Compose, and Clean Architecture.
This repository also contains governance assets (agents, instructions, hooks, skills, planning templates)
so Copilot agents can implement, test, and review changes consistently.

## Project Identity

| Field | Value |
|------|--------|
| Project | NameThatTuneLab |
| Root module | `:app` |
| Package / namespace | `com.capeddle.namethattunelab` |
| Application ID | `com.capeddle.namethattunelab` |
| Debug app ID suffix | `.debug` |

## Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin (JVM 17) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Min SDK | API 33 |
| Build | Gradle (Kotlin DSL) |
| Formatting | ktlint (via `.editorconfig`) |
| Static Analysis | detekt (`detekt.yml`) + Android Lint |
| Unit Testing | JUnit 5 + MockK + Turbine + Robolectric |
| UI Testing | Compose UI Test |

## Directory Structure

```text
app/
├── src/main/kotlin/com/capeddle/namethattunelab/
│   ├── domain/          ← Business logic, use cases, repository interfaces
│   ├── data/            ← Repository implementations, data sources, mappers
│   ├── presentation/    ← ViewModels, UI state, Compose screens/components
│   └── di/              ← Hilt modules
├── src/test/            ← Unit tests (JUnit 5 + MockK + Turbine)
└── src/androidTest/     ← Instrumented tests (Compose UI Test)
```

## Build & Validation Commands

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew ktlintCheck
./gradlew detekt
./gradlew lintDebug
```

Common local quality gate:

```bash
./gradlew ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug
```

## Key File Locations

| File | Purpose |
|------|---------|
| `detekt.yml` | Static analysis configuration |
| `.editorconfig` | Formatting rules (ktlint) |
| `.github/agents/` | Agent definitions (Overlord, Developer, Debugger, Testing, Code Reviewer) |
| `.github/hooks/` | Lifecycle hooks (session context, auto-format, quality gate) |
| `.github/instructions/` | Path-specific coding standards + deployment instruction |
| `.github/skills/` | Agent skills (`build-and-test`, `get-api-docs`, `git-commit`, `deploy-to-device`) |
| `.github/planning/` | ExecPlan standard + templates |
| `.github/workflows/android-ci.yml` | CI workflow |
| `app/build.gradle.kts` | App module build config |
| `gradle/libs.versions.toml` | Version catalog |

## Agent Roster

| Agent | Role | Invoke |
|-------|------|--------|
| Overlord | Orchestrator — planning, delegation, review gate | `@overlord` |
| Developer | Implementation — Kotlin/Compose/Architecture | `@developer` |
| Debugger | Investigation — runtime/service/debug workflows | `@debugger` |
| Testing | TDD — unit/instrumented verification | `@testing` |
| Code Reviewer | Review — idioms, patterns, compound learning | `@code-reviewer` |

## CI Workflow

The `android-ci.yml` workflow runs on push/PR to `main` and `develop`:
1. Build (`assembleDebug`)
2. Unit tests (`testDebugUnitTest`)
3. Formatting check (`ktlintCheck`)
4. Static analysis (`detekt`)
5. Android lint (`lintDebug`)

## Planning

Tasks touching more than 3 files require an **ExecPlan**.
See `.github/planning/PLANS.md` and `.github/planning/execplans/_TEMPLATE.md`.

## Overlord Planning Edit Rule

- The `Overlord` agent owns planning decisions and ExecPlan quality.
- `Overlord` may use edit capabilities **only** for planning artifacts in `.github/planning/execplans/*.md`.
- `Overlord` must not use edit capabilities for production code, tests, build scripts, CI, or hook scripts.
- If `Overlord` cannot edit in the current environment, it must delegate ExecPlan drafting to `Developer`, then resume orchestration and quality gating.

Fallback sequence when edit is unavailable:
1. `Overlord` hands off to `Developer` to draft the ExecPlan from `.github/planning/execplans/_TEMPLATE.md`.
2. `Overlord` reviews and finalizes acceptance criteria and milestones.
3. `Overlord` delegates implementation, testing, and code review as normal.

## Device Deployment

Use the deployment instruction and skill for connected-phone installs:
- Instruction: `.github/instructions/deployment.instructions.md`
- Skill: `.github/skills/deploy-to-device/SKILL.md`

Quick commands:

```bash
adb devices
./gradlew :app:installDebug
```
