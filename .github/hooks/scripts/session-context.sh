#!/usr/bin/env bash
# Session context hook — injects project metadata at SessionStart.
# Reads JSON from stdin (hook protocol). Gathers project metadata
# and emits context via hookSpecificOutput.

set -euo pipefail

# Read JSON from stdin (consume it even if unused)
cat > /dev/null

# Gather project metadata
BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
LAST_COMMIT=$(git log -1 --oneline 2>/dev/null || echo "unknown")

HAS_GRADLE="false"
[ -f "./gradlew" ] && HAS_GRADLE="true"

HAS_DETEKT="false"
[ -f "./detekt.yml" ] && HAS_DETEKT="true"

HAS_EDITORCONFIG="false"
[ -f "./.editorconfig" ] && HAS_EDITORCONFIG="true"

GRADLE_STATUS="✗ gradlew not found"
[ "$HAS_GRADLE" = "true" ] && GRADLE_STATUS="✓ gradlew found"

DETEKT_STATUS="✗ config not found"
[ "$HAS_DETEKT" = "true" ] && DETEKT_STATUS="✓ config found"

EC_STATUS="✗"
[ "$HAS_EDITORCONFIG" = "true" ] && EC_STATUS="✓"

cat <<EOF
{
  "continue": true,
  "systemMessage": "## Project Context (auto-injected at session start)\\n\\n- **Branch:** ${BRANCH}\\n- **Last commit:** ${LAST_COMMIT}\\n- **Min SDK:** API 33 (Android 13)\\n- **Language:** Kotlin (Jetpack Compose, Material 3)\\n- **Architecture:** MVVM + Clean Architecture (domain/data/presentation)\\n- **DI:** Hilt\\n- **Build system:** Gradle (Kotlin DSL) ${GRADLE_STATUS}\\n- **Static analysis:** detekt ${DETEKT_STATUS}, ktlint via .editorconfig ${EC_STATUS}\\n\\n### Quick Commands\\n- Build: \`./gradlew build\`\\n- Test: \`./gradlew test\`\\n- Lint: \`./gradlew ktlintCheck\`\\n- Detekt: \`./gradlew detekt\`\\n\\n### Agent Roster\\n- **@overlord** — Orchestrator\\n- **@developer** — Implementation\\n- **@debugger** — Investigation\\n- **@testing** — TDD\\n- **@code-reviewer** — Review + compound loop\\n\\n### Key Files\\n- Agent definitions: \`.github/agents/\`\\n- Instructions: \`.github/instructions/\`\\n- Hooks: \`.github/hooks/\`\\n- Planning: \`.github/planning/\`\\n- detekt config: \`detekt.yml\`",
  "hookSpecificOutput": {
    "branch": "${BRANCH}",
    "lastCommit": "${LAST_COMMIT}",
    "hasGradle": ${HAS_GRADLE},
    "hasDetekt": ${HAS_DETEKT},
    "minSdk": 33
  }
}
EOF
exit 0
