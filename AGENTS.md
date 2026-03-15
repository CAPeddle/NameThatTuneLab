# Agent Guidance — Android App Template

This repository is an **agent-native** governance kit. All instructions, agents, hooks, skills, and planning artifacts are designed for autonomous Copilot agent workflows.

## Repository Structure

```
.github/
  agents/          → Agent definitions (Overlord, Developer, Debugger, Testing, Code Reviewer)
  hooks/           → Lifecycle hooks (quality gate, auto-format, session context)
  instructions/    → Path-specific coding standards (Kotlin, Compose, Gradle, Architecture)
  planning/        → ExecPlan authoring standard and templates
  skills/          → Agent skills (build-and-test, get-api-docs, git-commit)
  workflows/       → GitHub Actions CI
  copilot-instructions.md → Repository-wide agent instructions
```

## ExecPlans

### When Required

An ExecPlan is **mandatory** when the task involves:

- Creating or modifying more than 3 files
- Introducing a new module, layer, or architectural boundary
- Changing build configuration or CI pipeline
- Any task estimated to exceed a single agent session

### When NOT Required

- Bug fixes contained to a single file
- Adding a single test
- Documentation-only updates
- Renaming or moving a single symbol

### Common Failure Mode

Agents that skip the planning step and begin coding immediately tend to produce incomplete implementations that violate architectural boundaries. **Always check if a plan is needed before writing code.**

Read the full ExecPlan standard at `.github/planning/PLANS.md`.

## Quality Gate

Every agent session ends with an automated quality gate (`Stop` lifecycle hook) that verifies:

1. **Formatting** — `ktlint` passes on all modified `.kt` files
2. **Static Analysis** — `detekt` passes on all modified `.kt` files

If the gate fails, the agent session is blocked from completing until violations are resolved.

Additional hooks run at `PostToolUse` (auto-format after file edits) and `SessionStart` (context injection).

## Alternative Agent Guidance Files

This repository uses `AGENTS.md` for Copilot. For compatibility with other agent runtimes:
- **Claude Code:** Create `CLAUDE.md` at repo root with equivalent guidance.
- **Gemini CLI:** Create `GEMINI.md` at repo root with equivalent guidance.
