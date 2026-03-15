<#
.SYNOPSIS
    Quality gate hook -- runs ktlint + detekt on modified .kt files at session Stop.
.DESCRIPTION
    Reads JSON from stdin (hook protocol). Checks stop_hook_active to prevent
    infinite loops. Runs ktlint and detekt on git-modified .kt files.
    Emits JSON to stdout per the VS Code hook protocol.
#>

$ErrorActionPreference = 'Stop'

# Read JSON from stdin
$inputJson = $null
try {
    $inputJson = [Console]::In.ReadToEnd() | ConvertFrom-Json
} catch {
    # If stdin is empty or invalid, proceed with defaults
    $inputJson = @{}
}

$hookEvent = $inputJson.hookEventName
$stopHookActive = $inputJson.stop_hook_active

# Prevent infinite loops: if stop_hook_active is true, do NOT block
if ($stopHookActive -eq $true) {
    $result = @{
        continue = $true
        systemMessage = "Quality gate skipped -- stop_hook_active is true (loop prevention)."
        hookSpecificOutput = @{
            decision = "allow"
            reason = "Loop prevention: stop_hook_active was true."
        }
    }
    $result | ConvertTo-Json -Depth 5
    exit 0
}

# Collect modified .kt files via git diff
$modifiedFiles = @()
try {
    $staged = git diff --cached --name-only --diff-filter=ACMR 2>$null | Where-Object { $_ -match '\.kt$' }
    $unstaged = git diff --name-only --diff-filter=ACMR 2>$null | Where-Object { $_ -match '\.kt$' }
    $modifiedFiles = @($staged) + @($unstaged) | Select-Object -Unique | Where-Object { $_ -and (Test-Path $_) }
} catch {
    $modifiedFiles = @()
}

if ($modifiedFiles.Count -eq 0) {
    $result = @{
        continue = $true
        systemMessage = "Quality gate passed -- no modified .kt files to check."
        hookSpecificOutput = @{
            decision = "allow"
            reason = "No modified Kotlin files found."
            ktlint = "skipped"
            detekt = "skipped"
        }
    }
    $result | ConvertTo-Json -Depth 5
    exit 0
}

$fileList = $modifiedFiles -join ' '
$failures = @()
$gatesPassed = 0
$gatesTotal = 2

# Gate 1: ktlint
try {
    $ktlintOutput = & ktlint $modifiedFiles 2>&1
    if ($LASTEXITCODE -eq 0) {
        $gatesPassed++
    } else {
        $failures += "ktlint: $($ktlintOutput -join '; ')"
    }
} catch {
    # ktlint not installed -- try via Gradle
    try {
        $ktlintOutput = & ./gradlew ktlintCheck --quiet 2>&1
        if ($LASTEXITCODE -eq 0) {
            $gatesPassed++
        } else {
            $failures += "ktlint (gradle): $($ktlintOutput -join '; ')"
        }
    } catch {
        $failures += "ktlint: not available (install ktlint or add Spotless/ktlint Gradle plugin)"
    }
}

# Gate 2: detekt
try {
    $detektOutput = & detekt --input ($modifiedFiles -join ',') --config detekt.yml 2>&1
    if ($LASTEXITCODE -eq 0) {
        $gatesPassed++
    } else {
        $failures += "detekt: $($detektOutput -join '; ')"
    }
} catch {
    # detekt not installed -- try via Gradle
    try {
        $detektOutput = & ./gradlew detekt --quiet 2>&1
        if ($LASTEXITCODE -eq 0) {
            $gatesPassed++
        } else {
            $failures += "detekt (gradle): $($detektOutput -join '; ')"
        }
    } catch {
        $failures += "detekt: not available (install detekt CLI or add detekt Gradle plugin)"
    }
}

# Emit result
if ($failures.Count -eq 0) {
    $result = @{
        continue = $true
        systemMessage = "Quality gate PASSED -- $gatesPassed/$gatesTotal gates passed for $($modifiedFiles.Count) file(s)."
        hookSpecificOutput = @{
            decision = "allow"
            reason = "All gates passed."
            filesChecked = $modifiedFiles.Count
            ktlint = "passed"
            detekt = "passed"
        }
    }
    $result | ConvertTo-Json -Depth 5
    exit 0
} else {
    $result = @{
        continue = $true
        stopReason = "Quality gate FAILED -- fix the following issues before completing."
        systemMessage = "Quality gate FAILED ($gatesPassed/$gatesTotal passed). Failures:`n$($failures -join "`n")`nFix these issues and try again."
        hookSpecificOutput = @{
            decision = "block"
            reason = $failures -join "; "
            filesChecked = $modifiedFiles.Count
            gatesPassed = $gatesPassed
            gatesTotal = $gatesTotal
        }
    }
    $result | ConvertTo-Json -Depth 5
    exit 0
}
