#!/usr/bin/env bash
# Quality gate hook — runs ktlint + detekt on modified .kt files at session Stop.
# Reads JSON from stdin (hook protocol). Checks stop_hook_active to prevent
# infinite loops. Emits JSON to stdout per the VS Code hook protocol.

set -euo pipefail

# Read JSON from stdin
INPUT_JSON=$(cat)

# Parse fields using lightweight JSON parsing
STOP_HOOK_ACTIVE=$(echo "$INPUT_JSON" | grep -o '"stop_hook_active"\s*:\s*true' || true)

# Prevent infinite loops
if [ -n "$STOP_HOOK_ACTIVE" ]; then
    cat <<'EOF'
{
  "continue": true,
  "systemMessage": "Quality gate skipped — stop_hook_active is true (loop prevention).",
  "hookSpecificOutput": {
    "decision": "allow",
    "reason": "Loop prevention: stop_hook_active was true."
  }
}
EOF
    exit 0
fi

# Collect modified .kt files
MODIFIED_FILES=()
while IFS= read -r file; do
    [ -n "$file" ] && [ -f "$file" ] && MODIFIED_FILES+=("$file")
done < <(
    {
        git diff --cached --name-only --diff-filter=ACMR 2>/dev/null || true
        git diff --name-only --diff-filter=ACMR 2>/dev/null || true
    } | grep '\.kt$' | sort -u
)

if [ ${#MODIFIED_FILES[@]} -eq 0 ]; then
    cat <<'EOF'
{
  "continue": true,
  "systemMessage": "Quality gate passed — no modified .kt files to check.",
  "hookSpecificOutput": {
    "decision": "allow",
    "reason": "No modified Kotlin files found.",
    "ktlint": "skipped",
    "detekt": "skipped"
  }
}
EOF
    exit 0
fi

FAILURES=()
GATES_PASSED=0
GATES_TOTAL=2

# Gate 1: ktlint
if command -v ktlint &>/dev/null; then
    if ktlint "${MODIFIED_FILES[@]}" 2>&1; then
        GATES_PASSED=$((GATES_PASSED + 1))
    else
        FAILURES+=("ktlint: formatting violations found")
    fi
elif ./gradlew ktlintCheck --quiet 2>&1; then
    GATES_PASSED=$((GATES_PASSED + 1))
else
    FAILURES+=("ktlint: not available or violations found")
fi

# Gate 2: detekt
INPUT_PATHS=$(IFS=','; echo "${MODIFIED_FILES[*]}")
if command -v detekt &>/dev/null; then
    if detekt --input "$INPUT_PATHS" --config detekt.yml 2>&1; then
        GATES_PASSED=$((GATES_PASSED + 1))
    else
        FAILURES+=("detekt: analysis violations found")
    fi
elif ./gradlew detekt --quiet 2>&1; then
    GATES_PASSED=$((GATES_PASSED + 1))
else
    FAILURES+=("detekt: not available or violations found")
fi

# Emit result
FILE_COUNT=${#MODIFIED_FILES[@]}

if [ ${#FAILURES[@]} -eq 0 ]; then
    cat <<EOF
{
  "continue": true,
  "systemMessage": "Quality gate PASSED — ${GATES_PASSED}/${GATES_TOTAL} gates passed for ${FILE_COUNT} file(s).",
  "hookSpecificOutput": {
    "decision": "allow",
    "reason": "All gates passed.",
    "filesChecked": ${FILE_COUNT},
    "ktlint": "passed",
    "detekt": "passed"
  }
}
EOF
    exit 0
else
    FAILURE_MSG=$(IFS='; '; echo "${FAILURES[*]}")
    cat <<EOF
{
  "continue": true,
  "stopReason": "Quality gate FAILED — fix the following issues before completing.",
  "systemMessage": "Quality gate FAILED (${GATES_PASSED}/${GATES_TOTAL} passed). Failures: ${FAILURE_MSG}. Fix these issues and try again.",
  "hookSpecificOutput": {
    "decision": "block",
    "reason": "${FAILURE_MSG}",
    "filesChecked": ${FILE_COUNT},
    "gatesPassed": ${GATES_PASSED},
    "gatesTotal": ${GATES_TOTAL}
  }
}
EOF
    exit 0
fi
