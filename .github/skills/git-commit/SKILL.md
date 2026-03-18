````skill
---
name: git-commit
description: "Execute git commit and push with conventional commit message analysis, intelligent staging, and push to remote. Use this skill when asked to commit changes, push to remote, create a git commit, or when the user says 'commit and push'. Supports: (1) auto-detecting type and scope from staged/unstaged diff, (2) generating conventional commit messages, (3) intelligent file staging that excludes generated artifacts, (4) push after commit."
license: MIT
allowed-tools: Bash
---

# Git Commit and Push — Conventional Commits

## Overview

Stage, commit, and push changes using the Conventional Commits specification.
Analyze the actual diff to determine the appropriate type, scope, and message.

## Conventional Commit Format

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

## Commit Types

| Type       | Purpose                           |
| ---------- | --------------------------------- |
| `feat`     | New feature                       |
| `fix`      | Bug fix                           |
| `docs`     | Documentation only                |
| `style`    | Formatting/style (no logic)       |
| `refactor` | Code refactor (no feature/fix)    |
| `perf`     | Performance improvement           |
| `test`     | Add/update tests                  |
| `build`    | Build system/dependencies         |
| `ci`       | CI/config changes                 |
| `chore`    | Maintenance/misc                  |
| `revert`   | Revert a previous commit          |

### Breaking Changes

```
# Exclamation mark after type/scope
feat!: remove deprecated API

# Or BREAKING CHANGE footer
feat(api): redesign response shape

BREAKING CHANGE: response fields renamed
```

## Workflow

### Step 1 — Inspect Current State

```bash
git status --short
git diff --staged --stat
git diff --stat
```

### Step 2 — Stage Files (if nothing staged)

Exclude generated artifacts — this project's `.gitignore` covers `**/build/`,
`.kotlin/`, `*.apk`, `*.aab`, `local.properties`, `.idea/`. Only stage
source files:

```bash
# Stage tracked modifications + new source files
git add -u
git add app/src/ app/schemas/ app/build.gradle.kts \
        build.gradle.kts settings.gradle.kts gradle.properties \
        gradle/libs.versions.toml gradle/wrapper/ gradlew gradlew.bat \
        .github/ detekt.yml .editorconfig 2>/dev/null || true

# Verify nothing from build/ slipped in
git diff --cached --name-only | grep -E "^app/build/|^build/" && {
  echo "⚠ Build artifacts staged — unstaging"
  git restore --staged app/build/ build/ 2>/dev/null || true
}
```

**Never stage:**
- `local.properties` (SDK path, may contain secrets)
- `*.jks` / `*.keystore` (signing keys)
- `google-services.json` (API keys)
- Any file under `**/build/`

### Step 3 — Analyze the Diff

```bash
# Read staged diff to understand what changed
git diff --cached
```

Determine:
- **Type**: What category of change? (see table above)
- **Scope**: Which module or layer is affected?
  Common scopes for this project: `viewmodel`, `domain`, `data`, `nowplaying`,
  `speech`, `di`, `compose`, `tests`, `ci`, `gradle`, `detekt`
- **Description**: One-line summary, present tense, imperative mood, ≤72 chars

### Step 4 — Commit

```bash
# Single-line message
git commit -m "<type>[scope]: <description>"

# Multi-line with body
git commit -m "$(cat <<'EOF'
<type>[scope]: <description>

<optional body — explain WHY, not what>

<optional footer: Closes #123, BREAKING CHANGE: ...>
EOF
)"
```

### Step 5 — Push

```bash
git push
```

If the branch has no upstream yet:
```bash
git push --set-upstream origin "$(git branch --show-current)"
```

### Step 6 — Confirm

```bash
git log --oneline -3
git status
```

## Quality Gate Reminder

Before committing, confirm the quality gate passed in this session:
- `./gradlew ktlintCheck` — formatting
- `./gradlew detekt` — static analysis
- `./gradlew testDebugUnitTest` — unit tests

If the gate has not been run yet, run it first:
```bash
./gradlew ktlintCheck detekt testDebugUnitTest
```

## Git Safety Protocol

- **NEVER** update git config globally
- **NEVER** run destructive commands (`--force`, hard reset) without explicit user request
- **NEVER** skip hooks (`--no-verify`) unless the user explicitly asks
- **NEVER** force-push to `main` or `master`
- **NEVER** commit secrets (API keys, keystore passwords, `local.properties`)
- If a commit fails due to hooks, fix the violation and create a NEW commit — do not amend

## Example Messages

```
feat(viewmodel): add placeholder fallback to recentTracks on metadata failure

fix(speech): prevent duplicate TTS announcements for same track

test(nowplaying): add Robolectric lifecycle tests for NowPlayingListenerService

build(gradle): add Robolectric and JUnit4 Vintage engine dependencies

chore(detekt): extract string literals to constants in MainViewModelTest

refactor(data): split PipelineLoggerTest to satisfy TooManyFunctions rule
```
````