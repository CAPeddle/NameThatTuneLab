<#
.SYNOPSIS
    Auto-format hook — runs ktlint --format on edited files at PostToolUse.
.DESCRIPTION
    Reads JSON from stdin (hook protocol). Only acts on file-editing tools.
    Runs ktlint --format on the edited file. Emits JSON to stdout.
#>

$ErrorActionPreference = 'Stop'

# Read JSON from stdin
$inputJson = $null
try {
    $inputJson = [Console]::In.ReadToEnd() | ConvertFrom-Json
} catch {
    $inputJson = @{}
}

$toolName = $inputJson.tool_name
$toolInput = $inputJson.tool_input

# Only act on file-editing tools
$editingTools = @('editFiles', 'create_file', 'replace_string_in_file', 'multi_replace_string_in_file', 'insert_edit')
if ($toolName -notin $editingTools) {
    $result = @{
        continue = $true
        hookSpecificOutput = @{
            action = "skipped"
            reason = "Tool '$toolName' is not a file-editing tool."
        }
    }
    $result | ConvertTo-Json -Depth 5
    exit 0
}

# Extract file path from tool input
$filePath = $null
if ($toolInput -and $toolInput.filePath) {
    $filePath = $toolInput.filePath
} elseif ($toolInput -and $toolInput.path) {
    $filePath = $toolInput.path
}

# Only format .kt files
if (-not $filePath -or $filePath -notmatch '\.kt$') {
    $result = @{
        continue = $true
        hookSpecificOutput = @{
            action = "skipped"
            reason = "Not a Kotlin file: $filePath"
        }
    }
    $result | ConvertTo-Json -Depth 5
    exit 0
}

# Check file exists
if (-not (Test-Path $filePath)) {
    $result = @{
        continue = $true
        hookSpecificOutput = @{
            action = "skipped"
            reason = "File not found: $filePath"
        }
    }
    $result | ConvertTo-Json -Depth 5
    exit 0
}

# Run ktlint --format
$formatted = $false
try {
    & ktlint --format $filePath 2>&1 | Out-Null
    $formatted = $true
} catch {
    # Try Gradle fallback
    try {
        & ./gradlew ktlintFormat --quiet 2>&1 | Out-Null
        $formatted = $true
    } catch {
        # ktlint not available
    }
}

$result = @{
    continue = $true
    hookSpecificOutput = @{
        action = if ($formatted) { "formatted" } else { "skipped" }
        file = $filePath
        tool = $toolName
    }
}
if ($formatted) {
    $result.systemMessage = "Auto-formatted $filePath with ktlint."
}
$result | ConvertTo-Json -Depth 5
exit 0
