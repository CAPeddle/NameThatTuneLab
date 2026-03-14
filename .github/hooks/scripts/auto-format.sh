#!/usr/bin/env bash
# Auto-format hook — runs ktlint --format on edited files at PostToolUse.
# Reads JSON from stdin (hook protocol). Only acts on file-editing tools.
# Emits JSON to stdout per the VS Code hook protocol.

set -euo pipefail

# Read JSON from stdin
INPUT_JSON=$(cat)

# Parse tool_name (lightweight grep-based parsing)
TOOL_NAME=$(echo "$INPUT_JSON" | grep -o '"tool_name"\s*:\s*"[^"]*"' | head -1 | sed 's/.*: *"//;s/"//')

# Only act on file-editing tools
case "$TOOL_NAME" in
    editFiles|create_file|replace_string_in_file|multi_replace_string_in_file|insert_edit)
        ;;
    *)
        cat <<EOF
{
  "continue": true,
  "hookSpecificOutput": {
    "action": "skipped",
    "reason": "Tool '${TOOL_NAME}' is not a file-editing tool."
  }
}
EOF
        exit 0
        ;;
esac

# Extract file path from tool_input.filePath or tool_input.path
FILE_PATH=$(echo "$INPUT_JSON" | grep -o '"filePath"\s*:\s*"[^"]*"' | head -1 | sed 's/.*: *"//;s/"//')
if [ -z "$FILE_PATH" ]; then
    FILE_PATH=$(echo "$INPUT_JSON" | grep -o '"path"\s*:\s*"[^"]*"' | head -1 | sed 's/.*: *"//;s/"//')
fi

# Only format .kt files
if [ -z "$FILE_PATH" ] || [[ "$FILE_PATH" != *.kt ]]; then
    cat <<EOF
{
  "continue": true,
  "hookSpecificOutput": {
    "action": "skipped",
    "reason": "Not a Kotlin file: ${FILE_PATH:-unknown}"
  }
}
EOF
    exit 0
fi

# Check file exists
if [ ! -f "$FILE_PATH" ]; then
    cat <<EOF
{
  "continue": true,
  "hookSpecificOutput": {
    "action": "skipped",
    "reason": "File not found: ${FILE_PATH}"
  }
}
EOF
    exit 0
fi

# Run ktlint --format
FORMATTED=false
if command -v ktlint &>/dev/null; then
    if ktlint --format "$FILE_PATH" 2>/dev/null; then
        FORMATTED=true
    fi
elif ./gradlew ktlintFormat --quiet 2>/dev/null; then
    FORMATTED=true
fi

if [ "$FORMATTED" = true ]; then
    cat <<EOF
{
  "continue": true,
  "systemMessage": "Auto-formatted ${FILE_PATH} with ktlint.",
  "hookSpecificOutput": {
    "action": "formatted",
    "file": "${FILE_PATH}",
    "tool": "${TOOL_NAME}"
  }
}
EOF
else
    cat <<EOF
{
  "continue": true,
  "hookSpecificOutput": {
    "action": "skipped",
    "file": "${FILE_PATH}",
    "tool": "${TOOL_NAME}",
    "reason": "ktlint not available"
  }
}
EOF
fi
exit 0
