<#
.SYNOPSIS
    Session context hook — injects project metadata at SessionStart.
.DESCRIPTION
    Reads JSON from stdin (hook protocol). Gathers project metadata
    (branch, build commands, min SDK). Emits context via hookSpecificOutput.
#>

$ErrorActionPreference = 'Stop'

# Read JSON from stdin
$inputJson = $null
try {
    $inputJson = [Console]::In.ReadToEnd() | ConvertFrom-Json
} catch {
    $inputJson = @{}
}

# Gather project metadata
$branch = "unknown"
try {
    $branch = (git branch --show-current 2>$null).Trim()
} catch {}

$lastCommit = "unknown"
try {
    $lastCommit = (git log -1 --oneline 2>$null).Trim()
} catch {}

$hasGradle = Test-Path "./gradlew" -or Test-Path "./gradlew.bat"
$hasDetekt = Test-Path "./detekt.yml"
$hasEditorConfig = Test-Path "./.editorconfig"

$context = @"
## Project Context (auto-injected at session start)

- **Branch:** $branch
- **Last commit:** $lastCommit
- **Min SDK:** API 33 (Android 13)
- **Language:** Kotlin (Jetpack Compose, Material 3)
- **Architecture:** MVVM + Clean Architecture (domain/data/presentation)
- **DI:** Hilt
- **Build system:** Gradle (Kotlin DSL) $(if ($hasGradle) { '✓ gradlew found' } else { '✗ gradlew not found' })
- **Static analysis:** detekt $(if ($hasDetekt) { '✓ config found' } else { '✗ config not found' }), ktlint via .editorconfig $(if ($hasEditorConfig) { '✓' } else { '✗' })

### Quick Commands
- Build: ``./gradlew build``
- Test: ``./gradlew test``
- Lint: ``./gradlew ktlintCheck``
- Detekt: ``./gradlew detekt``

### Agent Roster
- **@overlord** — Orchestrator (requirements, planning, delegation)
- **@developer** — Implementation (Kotlin/Compose)
- **@debugger** — Investigation (Logcat, LeakCanary)
- **@testing** — TDD (JUnit 5, MockK, Compose UI Test)
- **@code-reviewer** — Review + compound learning loop

### Key Files
- Agent definitions: ``.github/agents/``
- Instructions: ``.github/instructions/``
- Hooks: ``.github/hooks/``
- Planning: ``.github/planning/``
- detekt config: ``detekt.yml``
"@

$result = @{
    continue = $true
    systemMessage = $context
    hookSpecificOutput = @{
        branch = $branch
        lastCommit = $lastCommit
        hasGradle = $hasGradle
        hasDetekt = $hasDetekt
        minSdk = 33
    }
}
$result | ConvertTo-Json -Depth 5
exit 0
