# Agent Guidance — NameThatTuneLab

This repository is an **agent-native** governance kit. All instructions, agents, hooks, skills, and planning artifacts are designed for autonomous Copilot agent workflows.

## Repository Purpose

NameThatTuneLab is an Android app built with Kotlin, Jetpack Compose, and Clean Architecture.
This repository also contains governance assets (agents, instructions, hooks, skills, planning templates)
so Copilot agents can implement, test, and review changes consistently.

## Repository Structure

```
.github/
  agents/          → Agent definitions (Overlord, Developer, Debugger, Testing, Code Reviewer)
  hooks/           → Lifecycle hooks (quality gate, auto-format, session context)
  instructions/    → Path-specific coding standards (Kotlin, Compose, Gradle, Architecture) + deployment instruction
  planning/        → ExecPlan authoring standard and templates
  skills/          → Agent skills (build-and-test, get-api-docs, git-commit, deploy-to-device, review-upstream-sources, validate-agent-tools, adopt-template-updates)
  workflows/       → GitHub Actions CI
  copilot-instructions.md → Repository-wide agent instructions
```

## ExecPlans

### When Required

Create an ExecPlan **before any work begins** — including investigation, evaluation, or research steps that precede implementation — when any of the following applies:

- Creating or modifying more than 3 files
- Introducing a new module, layer, or architectural boundary
- Changing build configuration or CI pipeline
- Any task estimated to exceed a single agent session
- Integrating a new third-party dependency
- Modifying agent definitions, hooks, or skills
- **Investigation-first tasks** — tasks where the first step is to explore, evaluate options, or compile findings before implementing. The ExecPlan must exist before the investigation begins, not only before the implementation phase.

> **Common failure mode:** A prompt phrased as "investigate first, then implement" may be treated as not requiring an ExecPlan until implementation starts. This is incorrect — any multi-step task (including one that begins with research or evaluation) requires an ExecPlan before the first step.

### When NOT Required

- Bug fixes contained to a single file
- Adding a single test
- Documentation-only updates
- Renaming or moving a single symbol
- Trivial formatting changes

Read the full ExecPlan standard at `.github/planning/PLANS.md`.

## Overlord Planning Edit Rule

- The `Overlord` agent is the owner of planning decisions and ExecPlan quality.
- `Overlord` may use edit capabilities **only** for planning artifacts in `.github/planning/execplans/*.md`.
- `Overlord` must not use edit capabilities for production source code, tests, build scripts, CI, or hook scripts.
- If `Overlord` cannot edit in the current environment, it must delegate ExecPlan drafting to `Developer` and then resume orchestration and quality gating.

Required fallback sequence when edit is unavailable:
1. `Overlord` creates a handoff to `Developer` to draft the ExecPlan from the template.
2. `Overlord` reviews and finalizes acceptance criteria/milestones in chat.
3. `Overlord` delegates implementation/testing/review as normal.

## Quality Gate

Every agent session ends with an automated quality gate (`agentStop` lifecycle hook) that verifies:

1. **Formatting** — `ktlint` passes on all modified `.kt` files
2. **Static Analysis** — `detekt` passes on all modified `.kt` files

If the gate fails, the agent session is blocked from completing until violations are resolved.

Additional hooks run at `PostToolUse` (auto-format after file edits) and `SessionStart` (context injection).
## Workflow Completion Rule

For orchestrated implementation flows, completion requires the full gate sequence:
1. Developer implementation
2. Testing validation
3. Code review approval
4. Git check-in and push

A workflow is not complete until changes are committed and pushed.
If push is blocked by environment or permissions, document the blocker explicitly and keep the task open.

## Device Deployment

Use the project deployment instruction and skill for connected-device installs:
- .github/instructions/deployment.instructions.md
- .github/skills/deploy-to-device/SKILL.md

## Alternative Agent Guidance Files

This repository uses `AGENTS.md` for Copilot. For compatibility with other agent runtimes:
- **Claude Code:** Create `CLAUDE.md` at repo root with equivalent guidance.
- **Gemini CLI:** Create `GEMINI.md` at repo root with equivalent guidance.


