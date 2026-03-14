# Android Governance Kit — Copilot Agent Configuration for Android App Template

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** 2026-03-14  
**Status:** 🔄 In Progress  
**Owner:** Copilot Agent (with user authorization)  
**Refs:** Initial repository setup  
**Revision:** v2 — incorporates findings from evaluation against 7 external sources (see [Evaluation Sources](#evaluation-sources))

---

## Purpose / Big Picture

Create a production-ready **GitHub Copilot governance kit** for Android app development — analogous to the existing Generic C++ kit in `C:\projects\zoom_copilot_config\Generic` — adapted for **Kotlin + Jetpack Compose + MVVM/Clean Architecture + Hilt**. Future Android projects will fork or copy from this repository to carry on development.

The kit provides agents, skills, hooks, instructions, and planning infrastructure so that Copilot agents produce consistent, high-quality Android code from day one. This is an **agent-native** initiative — all governance artifacts target autonomous agent workflows, not human developers.

**Observable outcome:** An agent operating in a fork of this repo immediately has working Copilot governance — agents that orchestrate work via handoffs, lifecycle hooks that enforce quality gates at multiple points, and coding standards that produce idiomatic Kotlin/Compose code. Placeholders are replaced by the bootstrapping agent during project setup.

**Term definitions:**
- *Governance kit:* The set of `.github/` files (agents, instructions, hooks, skills, planning) that configure Copilot behaviour.
- *Generic template:* The existing C++23 governance kit at `C:\projects\zoom_copilot_config\Generic`.
- *Quality gate:* An automated check that runs at agent lifecycle events (`Stop`, `PostToolUse`) to verify code passes formatting and linting rules.
- *ExecPlan:* A self-contained implementation plan (this document format).
- *chub:* Context Hub CLI (`@aisuite/chub`) — curates versioned API docs for coding agents to reduce hallucination.
- *MVVM:* Model-View-ViewModel architectural pattern.
- *Clean Architecture:* Layered architecture with domain, data, and presentation layers.
- *Hilt:* Google's dependency injection framework for Android, built on Dagger.
- *Handoff:* A button rendered at the end of an agent response that transitions to another agent with a pre-filled prompt.
- *Lifecycle hook:* A script that runs at a defined point in the agent session lifecycle (e.g., `Stop`, `PostToolUse`, `SessionStart`).
- *Compound loop:* A process where review findings are captured and fed back into governance artifacts to improve future cycles.

---

## Progress

- [x] (2026-03-14 10:00 UTC) Research phase — read all Generic template files (20 files).
- [x] (2026-03-14 10:15 UTC) Gap analysis — C++ vs Android technology mapping complete.
- [x] (2026-03-14 10:20 UTC) Clarifying questions answered: Kotlin-only, Compose-only, MVVM+Clean, Governance-only, API 33, Hilt, GitHub Actions, push to remote.
- [x] (2026-03-14 10:25 UTC) Git repo initialized, remote set to `CAPeddle/android_app_template`.
- [x] (2026-03-14 10:30 UTC) chub research — confirmed no Android/Kotlin/Compose/Hilt docs exist in Context Hub yet.
- [x] (2026-03-14 10:35 UTC) GitHub Copilot customization docs researched — instructions, agents, hooks, skills format confirmed.
- [x] (2026-03-14 11:00 UTC) ExecPlan evaluated against 7 external sources — 20 adjustments identified and incorporated (v2).
- [ ] ExecPlan v2 approved by user.
- [ ] Milestone 1 — Core governance files created.
- [ ] Milestone 2 — Repository-wide Copilot instructions created.
- [ ] Milestone 3 — Path-specific instructions created.
- [ ] Milestone 4 — Agent definitions created.
- [ ] Milestone 5 — Lifecycle hooks created.
- [ ] Milestone 6 — Skills created.
- [ ] Milestone 7 — Planning infrastructure created.
- [ ] Milestone 8 — GitHub Actions CI workflow created.
- [ ] Milestone 9 — Static analysis configuration created.
- [ ] Milestone 10 — Atomic commits pushed to GitHub.
- [ ] Milestone 11 — Validation and README finalized.

---

## Surprises & Discoveries

- **chub has zero Android ecosystem docs.** No Kotlin, Gradle, Compose, Hilt, JUnit, Espresso, Room, Robolectric, detekt, or ktlint content. The `get-api-docs` skill will return no results for any Android-related search. This means agents must rely on their training knowledge for Android APIs, and we should include comprehensive coding standards in path-specific instructions to compensate.

- **Generic template uses a `chatagent` fenced code block format for agent files, which is not recognized by current Copilot/VS Code tooling.** The current spec uses `.agent.md` files (or plain `.md` in `.github/agents/`) with standard YAML `---` frontmatter. Supported fields include `name`, `description`, `tools`, `agents`, `model`, `handoffs`, `hooks`, and others. Path-specific instruction files use `NAME.instructions.md` with `applyTo` (and optionally `excludeAgent`) in YAML frontmatter.

- **Quality gate in Generic uses clang-format + clang-tidy.** Android equivalent: `ktlint` (or Spotless) + `detekt` + Android Lint. All three have CLI invocations suitable for a hook script.

- **`AGENTS.md`** can be placed at repo root — GitHub Copilot reads the nearest `AGENTS.md` in the directory tree. Also supports `CLAUDE.md` and `GEMINI.md` as alternatives.

- **The VS Code hook system uses 8 lifecycle events — not just session-end.** Events: `SessionStart`, `UserPromptSubmit`, `PreToolUse`, `PostToolUse`, `PreCompact`, `SubagentStart`, `SubagentStop`, `Stop`. The Generic template only uses the session-end hook; we can leverage `PostToolUse` for auto-formatting and `SessionStart` for context injection.

- **Hooks communicate via structured JSON stdin/stdout, not just exit codes.** Input arrives on stdin with fields like `hookEventName`, `sessionId`, `stop_hook_active`. Output on stdout includes `continue`, `stopReason`, `systemMessage`, and `hookSpecificOutput`. Exit code 0 = success, 2 = blocking error.

- **The `Stop` hook can block the agent from stopping** by returning `hookSpecificOutput.decision: "block"`, creating a quality gate loop. It must check `stop_hook_active` to prevent infinite loops.

- **GitHub recommends `copilot-instructions.md` be ~2 pages max.** Detailed coding standards should go in path-specific instructions. The repo-wide file should focus on build/test commands, project layout, and key file locations.

- **Agent Skills follow an open standard at agentskills.io.** The `name` field in `SKILL.md` must match the parent directory name (lowercase, hyphens). Skills are portable across VS Code, Copilot CLI, and the coding agent.

---

## Decision Log

- **Decision:** Kotlin-only, no Java, no NDK/C++.  
  Rationale: User confirmed. Simplifies governance — single language, single set of lint rules.  
  Date: 2026-03-14

- **Decision:** Jetpack Compose only, no XML Views.  
  Rationale: User confirmed. Modern declarative UI, Compose-specific testing.  
  Date: 2026-03-14

- **Decision:** MVVM + Clean Architecture enforced.  
  Rationale: User confirmed. Google-recommended, clear layer boundaries, testable.  
  Date: 2026-03-14

- **Decision:** API 33 (Android 13) minimum SDK.  
  Rationale: User confirmed. Latest features, narrower reach acceptable.  
  Date: 2026-03-14

- **Decision:** Hilt for dependency injection.  
  Rationale: User confirmed. Compile-time safe, Google-recommended for Android.  
  Date: 2026-03-14

- **Decision:** GitHub Actions for CI.  
  Rationale: User confirmed. Native to GitHub, good for open-source.  
  Date: 2026-03-14

- **Decision:** Governance-only scope (no skeleton app code).  
  Rationale: User confirmed. Forks add their own app code.  
  Date: 2026-03-14

- **Decision:** Include `chub` get-api-docs skill even though no Android docs exist yet.  
  Rationale: Chub content is growing; agents should have the skill ready for when Android docs arrive. The skill also works for non-Android APIs agents may need (HTTP clients, JSON, etc.).  
  Date: 2026-03-14

- **Decision:** Use `Stop` lifecycle event (not `agentStop`) for the quality gate hook.  
  Rationale: Evaluation against VS Code docs confirmed there is no `agentStop` event. The 8 lifecycle events are: `SessionStart`, `UserPromptSubmit`, `PreToolUse`, `PostToolUse`, `PreCompact`, `SubagentStart`, `SubagentStop`, `Stop`.  
  Date: 2026-03-14

- **Decision:** Add `PostToolUse` hook for auto-formatting alongside `Stop` quality gate.  
  Rationale: Catching formatting violations immediately after file edits is more efficient than only at session end. The `PostToolUse` event provides `tool_name` to filter for file-editing tools.  
  Date: 2026-03-14

- **Decision:** Add `SessionStart` hook for context injection (optional, recommended).  
  Rationale: Inspired by GSD wave-execution pattern. Injecting project metadata at session start gives agents immediate orientation without relying on them to discover it.  
  Date: 2026-03-14

- **Decision:** Constrain `copilot-instructions.md` to ~2 pages; move detailed standards to path-specific instructions.  
  Rationale: GitHub's own guidance limits repo-wide instructions to ~2 pages focused on build/test commands and project layout. Detailed coding standards belong in path-specific files.  
  Date: 2026-03-14

- **Decision:** Use agent `handoffs` for workflow chaining.  
  Rationale: VS Code supports `handoffs` in agent frontmatter — buttons that transition between agents with pre-filled prompts. This formalizes the Overlord→Developer→Testing→CodeReviewer workflow.  
  Date: 2026-03-14

- **Decision:** Atomic commits per batch, not monolithic.  
  Rationale: Enables `git bisect`, independent reversion, and clear history. Aligns with GSD and Compound Engineering best practices.  
  Date: 2026-03-14

- **Decision:** Add compound learning loop to Code Reviewer agent.  
  Rationale: Inspired by EveryInc/compound-engineering-plugin's Brainstorm→Plan→Work→Review→Compound cycle. Review findings should be captured and fed back into governance artifacts.  
  Date: 2026-03-14

---

## Outcomes & Retrospective

*(Complete after plan closes.)*

**What was achieved:**

**What remains (if anything):**

**Patterns to promote:**

**Reusable findings:**

**New anti-patterns:**

---

## Context and Orientation

### Source Material

The Generic C++23 governance kit at `C:\projects\zoom_copilot_config\Generic` provides the structural patterns. It contains:

| File | Purpose | Android Equivalent |
|------|---------|-------------------|
| `AGENTS.md` | Root agent guidance + ExecPlan trigger policy | **Keep, adapt language** |
| `.clang-format` | Code formatting config | **`.editorconfig` + ktlint config** |
| `.clang-tidy` | Static analysis config | **`detekt.yml` + `lint.xml`** |
| `.github/copilot-instructions.md` | Copilot system-wide instructions | **Rewrite for Kotlin/Compose/MVVM (~2 pages)** |
| `.github/agents/*.agent.md` | 5 agent definitions (Overlord, Developer, Debugger, Testing, Code Reviewer) | **Adapt all 5 for Kotlin/Android; add `handoffs`, `agents` fields** |
| `.github/hooks/quality-gate.json` | `Stop` hook config | **Keep structure; add `PostToolUse` and `SessionStart` hooks** |
| `.github/hooks/scripts/quality-gate.{ps1,sh}` | Hook scripts running clang-format + clang-tidy | **Replace with ktlint + detekt; implement JSON stdin/stdout protocol** |
| `.github/instructions/cpp.instructions.md` | Path-specific C++ file rules | **Split into `kotlin.instructions.md`, `compose.instructions.md`, `architecture.instructions.md`** |
| `.github/planning/PLANS.md` | ExecPlan authoring standard | **Keep, adapt TDD references** |
| `.github/planning/execplans/_TEMPLATE.md` | ExecPlan template | **Keep mostly intact, adapt tech references, add compound loop emphasis** |
| `.github/skills/build-and-test/SKILL.md` | Build and test skill | **Rewrite for Gradle; ensure `name` matches directory** |

### Evaluation Sources

This ExecPlan v2 was evaluated against these external sources to correct errors and incorporate best practices:

| # | Source | Key Findings Applied |
|---|--------|---------------------|
| 1 | [GitHub Copilot Docs](https://docs.github.com/en/copilot) | Agent `.agent.md` format, 8 hook lifecycle events, JSON stdin/stdout protocol, `excludeAgent`, skill spec |
| 2 | [VS Code — Custom Agents](https://code.visualstudio.com/docs/copilot/customization/custom-agents) | Full agent frontmatter fields: `handoffs`, `agents`, `model`, `hooks`, `user-invocable`, `mcp-servers` |
| 3 | [VS Code — Hooks](https://code.visualstudio.com/docs/copilot/customization/hooks) | 8 lifecycle events, `Stop` (not `agentStop`), `stop_hook_active` loop prevention, structured JSON protocol |
| 4 | [VS Code — Agent Skills](https://code.visualstudio.com/docs/copilot/customization/agent-skills) | `name` must match directory, agentskills.io open standard, `argument-hint` field |
| 5 | [Cursor — Scaling Agents](https://cursor.com/blog/scaling-agents) | Planner/worker hierarchy, prompts matter most, removing complexity beats adding it |
| 6 | [OpenAI — Codex ExecPlans](https://developers.openai.com/cookbook/articles/codex_exec_plans) | Living-document requirements, observable acceptance criteria, prose-first approach |
| 7 | [EveryInc — Compound Engineering Plugin](https://github.com/EveryInc/compound-engineering-plugin) | Brainstorm→Plan→Work→Review→Compound cycle, learning capture |
| 8 | [gsd-build/get-shit-done](https://github.com/gsd-build/get-shit-done) | Context engineering, atomic commits, state tracking, `SessionStart` context injection |
| 9 | [github/awesome-copilot](https://github.com/github/awesome-copilot) | Community catalogue, `llms.txt` discoverability, MCP as future integration |

### Technology Stack (Target)

| Area | Choice |
|------|--------|
| Language | Kotlin (latest stable) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture (domain/data/presentation layers) |
| DI | Hilt (Dagger) |
| Min SDK | API 33 (Android 13) |
| Build | Gradle (Kotlin DSL) |
| Formatting | ktlint (via Spotless Gradle plugin or standalone) |
| Static Analysis | detekt + Android Lint |
| Unit Testing | JUnit 5 + MockK + Turbine (for Flow testing) |
| UI Testing | Compose UI Test (AndroidJUnit4 + `createComposeRule`) |
| Integration Testing | Robolectric (for non-instrumented tests) |
| CI | GitHub Actions |

### Copilot File Format Reference

From GitHub Copilot docs and VS Code customization docs (March 2026):

**Repository-wide instructions:** `.github/copilot-instructions.md`
- Markdown, no frontmatter required.
- Concise (~2 pages): repo summary, build/test commands, project layout, key file locations.
- Applied to all Copilot requests in this repository.

**Path-specific instructions:** `.github/instructions/NAME.instructions.md`
- Requires YAML frontmatter with `applyTo` glob (comma-separated for multiple patterns).
- Optional `excludeAgent: "code-review"` or `"coding-agent"` to scope to one consumer.
- Additive with repo-wide instructions (both apply when a matching file is active).

**Agent definitions:** `.github/agents/NAME.agent.md` (or plain `.md`)
- YAML `---` frontmatter (not `chatagent` fenced blocks) with supported fields:

| Field | Type | Purpose |
|-------|------|---------|
| `name` | string | Display name (falls back to filename if omitted) |
| `description` | string | Placeholder text shown in chat input |
| `argument-hint` | string | Hint text in chat input guiding user interaction |
| `tools` | YAML array | Tool/toolset names available to this agent |
| `agents` | YAML array | Subagent allowlist (`*` for all, `[]` for none) |
| `model` | string or array | AI model (single or prioritized fallback list) |
| `user-invocable` | boolean | Whether agent appears in dropdown (default `true`) |
| `disable-model-invocation` | boolean | Prevents agent from being invoked as subagent (default `false`) |
| `target` | string | Target environment (`vscode` or `github-copilot`) |
| `mcp-servers` | array | MCP server configs (for `target: github-copilot`; future) |
| `handoffs` | array | Suggested next-action buttons to transition between agents |
| `hooks` | object | Agent-scoped hooks (Preview; requires `chat.useCustomAgentHooks`) |

- `handoffs` entries: `label` (button text), `agent` (target), `prompt` (pre-filled), `send` (auto-submit, default `false`), `model` (optional override).
- Body contains Markdown instructions for the agent persona.

**Hooks:** `.github/hooks/*.json`
- JSON with `hooks` object keyed by event name.
- **8 lifecycle events:**

| Event | When it fires |
|-------|---------------|
| `SessionStart` | User submits the first prompt of a new session |
| `UserPromptSubmit` | User submits a prompt |
| `PreToolUse` | Before agent invokes any tool |
| `PostToolUse` | After tool completes successfully |
| `PreCompact` | Before conversation context is compacted |
| `SubagentStart` | Subagent is spawned |
| `SubagentStop` | Subagent completes |
| `Stop` | Agent session ends |

- Each entry: `type: "command"`, `command` (default/Linux/macOS), optional `windows`/`linux`/`osx` overrides, `timeout` (default 30s), `cwd`, `env`.
- **Protocol:** Hook reads JSON from stdin (`hookEventName`, `sessionId`, `stop_hook_active`, etc.). Hook emits JSON to stdout (`continue`, `stopReason`, `systemMessage`, `hookSpecificOutput`).
- **Exit codes:** `0` = success (parse stdout JSON), `2` = blocking error, other = non-blocking warning.
- **`Stop` loop prevention:** The `Stop` hook receives `stop_hook_active` — if `true`, do NOT block again (prevents infinite loops).
- Agent-scoped hooks can also be defined in `.agent.md` frontmatter (`hooks` field).

**Skills:** `.github/skills/NAME/SKILL.md`
- YAML frontmatter: `name` (required, must match parent directory name, lowercase+hyphens, max 64 chars), `description` (required, max 1024 chars), `argument-hint`, `user-invocable` (default `true`), `disable-model-invocation` (default `false`).
- Follows the [agentskills.io](https://agentskills.io/) open standard. Portable across VS Code, Copilot CLI, and coding agent.
- Can include scripts, examples, and resource files alongside `SKILL.md`.

**Agent guidance:** `AGENTS.md` at repo root
- Nearest in directory tree takes precedence.
- Alternatives: `CLAUDE.md`, `GEMINI.md` (for Claude Code / Gemini CLI compatibility).

---

## Plan of Work

### Milestone 1 — Core Governance Files (root-level)
Create the foundational files that configure the repository for Copilot.

1. `AGENTS.md` — Root agent guidance, ExecPlan trigger policy (adapt from Generic)
2. `.editorconfig` — Kotlin/Android editor configuration (replaces `.clang-format` role)
3. `README.md` — Template README documenting the kit (include MCP as future extension point, note `CLAUDE.md`/`GEMINI.md` alternatives)
4. `.gitignore` — Android/Gradle gitignore
5. `llms.txt` — AI discoverability file per [llmstxt.org](https://llmstxt.org/) spec (lists all agents, instructions, skills, hooks with short descriptions)

### Milestone 2 — Repository-wide Copilot Instructions
Create the concise repo-wide instructions (~2 pages max).

6. `.github/copilot-instructions.md` — Focused on: repository purpose, technology stack summary, directory structure (Clean Architecture layout), build/test/lint commands (`./gradlew build`, `./gradlew test`, `./gradlew ktlintCheck`, `./gradlew detekt`), CI workflow reference, key file locations (`detekt.yml`, `.editorconfig`, agent roster). **Not** detailed coding standards — those go in path-specific instructions.

### Milestone 3 — Path-Specific Instructions
Create focused, always-on rules for each file pattern. These absorb the detailed coding standards that would overflow the repo-wide file.

7. `.github/instructions/kotlin.instructions.md` — `applyTo: "**/*.kt"` — Kotlin coding standards: null safety, coroutines, sealed classes for errors, Result type, naming conventions, Hilt patterns, testing mandates, AI failure modes to watch for.
8. `.github/instructions/compose.instructions.md` — `applyTo: "**/ui/**/*.kt, **/composable/**/*.kt"` — Compose patterns: stateless composables, state hoisting, side effects, Material 3, preview functions, accessibility, Compose UI testing.
9. `.github/instructions/gradle.instructions.md` — `applyTo: "**/*.gradle.kts"` — `excludeAgent: "code-review"` — Gradle rules: version catalogs, convention plugins, dependency management, build logic.
10. `.github/instructions/architecture.instructions.md` — `applyTo: "**/*.kt"` — Clean Architecture layer rules: domain (no Android imports), data (repository implementations, mappers), presentation (ViewModels, UI state, MVVM flow). Error handling patterns. Layer dependency direction.

### Milestone 4 — Agent Definitions
Create the 5 agent definitions adapted for Android/Kotlin, with `handoffs` for workflow chaining.

11. `.github/agents/overlord.agent.md` — Orchestrator. Fields: `name`, `description`, `tools`, `agents: [developer, debugger, testing, code-reviewer]`, `handoffs` → Developer ("Implement this plan"), Testing ("Validate implementation"), Code Reviewer ("Review changes"). Optional: `model` (prefer strong reasoning model).
12. `.github/agents/developer.agent.md` — Implementation specialist (Kotlin/Compose patterns). Fields: `name`, `description`, `tools`, `handoffs` → Testing ("Run tests for my changes"), Code Reviewer ("Review my changes").
13. `.github/agents/debugger.agent.md` — Investigation specialist (Logcat, LeakCanary, StrictMode). Fields: `name`, `description`, `tools`, `handoffs` → Developer ("Apply this fix"), Testing ("Verify the fix").
14. `.github/agents/testing.agent.md` — TDD specialist (JUnit 5, MockK, Compose UI tests). Fields: `name`, `description`, `tools`, `handoffs` → Developer ("Fix failing tests"), Code Reviewer ("Tests pass — review ready").
15. `.github/agents/code-reviewer.agent.md` — Review checklist (Kotlin idioms, Compose best practices). Fields: `name`, `description`, `tools`, `handoffs` → Developer ("Address review comments"). **Compound loop:** Instructions direct the agent to output a "Learnings" summary identifying patterns to promote to instructions, anti-patterns to prohibit, and unclear guidance that caused issues.
16. `.github/agents/README.md` — Agent roster, workflow diagram showing handoff paths, and description of the compound learning loop.

### Milestone 5 — Lifecycle Hooks
Create hooks for three lifecycle events: `Stop` (quality gate), `PostToolUse` (auto-format), and `SessionStart` (context injection).

17. `.github/hooks/quality-gate.json` — Hook configuration with three event entries:
    ```json
    {
      "hooks": {
        "Stop": [
          {
            "type": "command",
            "command": "./.github/hooks/scripts/quality-gate.sh",
            "windows": "powershell -File .github\\hooks\\scripts\\quality-gate.ps1",
            "timeout": 120
          }
        ],
        "PostToolUse": [
          {
            "type": "command",
            "command": "./.github/hooks/scripts/auto-format.sh",
            "windows": "powershell -File .github\\hooks\\scripts\\auto-format.ps1",
            "timeout": 30
          }
        ],
        "SessionStart": [
          {
            "type": "command",
            "command": "./.github/hooks/scripts/session-context.sh",
            "windows": "powershell -File .github\\hooks\\scripts\\session-context.ps1",
            "timeout": 10
          }
        ]
      }
    }
    ```
18. `.github/hooks/scripts/quality-gate.ps1` — PowerShell: read JSON from stdin, check `stop_hook_active` (skip if `true` to prevent infinite loops), run `ktlint --reporter=json` + `detekt --report json`, on failure emit `hookSpecificOutput.decision: "block"` with failure details, exit 0. On pass, emit `continue: true`, exit 0.
19. `.github/hooks/scripts/quality-gate.sh` — Bash: same logic as PowerShell equivalent.
20. `.github/hooks/scripts/auto-format.ps1` — PowerShell: read JSON stdin, check `tool_name` (only act on file-editing tools like `editFiles`, `create_file`), run `ktlint --format` on the edited file, emit result, exit 0.
21. `.github/hooks/scripts/auto-format.sh` — Bash: same logic as PowerShell equivalent.
22. `.github/hooks/scripts/session-context.ps1` — PowerShell: read JSON stdin, gather project metadata (branch, last build status), emit `hookSpecificOutput.additionalContext` with project summary.
23. `.github/hooks/scripts/session-context.sh` — Bash: same logic as PowerShell equivalent.

### Milestone 6 — Skills
Create agent skills for common workflows, following the [agentskills.io](https://agentskills.io/) open standard.

24. `.github/skills/build-and-test/SKILL.md` — Gradle build, test, lint skill. Frontmatter: `name: build-and-test` (must match directory), `description` (under 1024 chars, describes what and when), `argument-hint: "[module] [task]"`.
25. `.github/skills/get-api-docs/SKILL.md` — chub integration skill (copy from Context Hub). Frontmatter: `name: get-api-docs` (must match directory), `description` (under 1024 chars).

### Milestone 7 — Planning Infrastructure
Create the ExecPlan system with compound learning loop emphasis.

26. `.github/planning/PLANS.md` — ExecPlan authoring standard (adapt from Generic, replace C++ tech references). Add section emphasizing compound learning: review findings should update governance artifacts.
27. `.github/planning/execplans/_TEMPLATE.md` — ExecPlan template (adapt tech references). Emphasize "Reusable findings" in Outcomes & Retrospective.

### Milestone 8 — GitHub Actions CI
Create CI workflow for automated quality checks.

28. `.github/workflows/android-ci.yml` — Build, test, lint, detekt on push/PR.

### Milestone 9 — Static Analysis Configuration
Create the detekt and lint configuration files.

29. `detekt.yml` — detekt configuration (replaces `.clang-tidy` role), strict-from-start philosophy.
30. `.editorconfig` — ktlint uses this for formatting rules (created in Milestone 1, refined here if needed).

### Milestone 10 — Commit and Push
Atomic commits per batch for clean history.

31. Commit after parallel batch 1: `feat: add root governance files, hooks, skills, planning, CI, static analysis`
32. Commit after Milestone 2: `feat: add copilot-instructions.md`
33. Commit after Milestone 3: `feat: add path-specific instructions`
34. Commit after Milestone 4: `feat: add agent definitions with handoffs`
35. Final push: `git push -u origin main`

---

## Concrete Steps

### Step 1 — Create root-level governance files
- **Agent:** developer
- **Files:** `AGENTS.md`, `.editorconfig`, `.gitignore`, `README.md`, `llms.txt`
- **Action:** Adapt `AGENTS.md` from Generic (replace C++ references with Kotlin/Android; note that `CLAUDE.md` and `GEMINI.md` are supported alternatives). Create `.editorconfig` with Kotlin formatting rules. Create standard Android `.gitignore`. Create README explaining the kit (document MCP as future extension point, note skill portability via agentskills.io, mention `CLAUDE.md`/`GEMINI.md` alternatives). Create `llms.txt` listing all agents, instructions, skills, and hooks.
- **Depends on:** None
- **Working directory:** repo root

### Step 2 — Create copilot-instructions.md
- **Agent:** developer
- **Files:** `.github/copilot-instructions.md`
- **Action:** Write a concise (~2 page) repo-wide instruction file. **Do not** put detailed coding standards here — those go in path-specific instructions (Step 3).
- **Depends on:** Step 1
- **Key content (keep brief):**
  - Repository purpose (1–2 sentences)
  - Technology stack summary (compact table)
  - Directory structure (Clean Architecture: domain/data/presentation)
  - Build commands: `./gradlew build`, `./gradlew test`, `./gradlew ktlintCheck`, `./gradlew detekt`
  - CI workflow reference
  - Key file locations (`detekt.yml`, `.editorconfig`, `.github/agents/`, `.github/hooks/`)
  - Agent roster and quick reference
- **What NOT to include:** Naming conventions, Compose patterns, error handling patterns, Hilt patterns, testing mandates, AI failure modes — all of these go in path-specific instructions.

### Step 3 — Create path-specific instructions
- **Agent:** developer
- **Files:** `.github/instructions/kotlin.instructions.md`, `.github/instructions/compose.instructions.md`, `.github/instructions/gradle.instructions.md`, `.github/instructions/architecture.instructions.md`
- **Action:** Create focused, always-on rules for each file type. These absorb the detailed content that would overflow `copilot-instructions.md`. Path-specific instructions are **additive** — both repo-wide and path-specific apply together when a matching file is active.
- **Frontmatter requirements:**
  - Kotlin: `applyTo: "**/*.kt"` — null safety, coroutines, sealed classes, Result, naming conventions, Hilt, testing mandates, AI failure modes.
  - Compose: `applyTo: "**/ui/**/*.kt, **/composable/**/*.kt"` — stateless composables, state hoisting, side effects, Material 3, previews, accessibility.
  - Gradle: `applyTo: "**/*.gradle.kts"`, `excludeAgent: "code-review"` — version catalogs, convention plugins.
  - Architecture: `applyTo: "**/*.kt"` — Clean Architecture layers, dependency direction, error handling patterns.
- **Depends on:** Step 2

### Step 4 — Create agent definitions
- **Agent:** developer
- **Files:** `.github/agents/*.agent.md` (5 agents + README = 6 files)
- **Action:** Adapt all 5 agents from Generic for Kotlin/Android. Use standard YAML `---` frontmatter (**not** `chatagent` fenced blocks). Include:
  - All agents: `name`, `description`, `tools`
  - Overlord: `agents: [developer, debugger, testing, code-reviewer]`, `handoffs` to developer/testing/code-reviewer
  - Developer: `handoffs` to testing/code-reviewer
  - Debugger: `handoffs` to developer/testing
  - Testing: `handoffs` to developer (fix failures)/code-reviewer (tests pass)
  - Code Reviewer: `handoffs` to developer (address comments); include **compound loop** instructions (output "Learnings" summary at end of each review)
  - Consider `model` for Overlord (stronger reasoning model preference)
  - Consider `hooks` in Overlord frontmatter as supplement to global hook file
- **Depends on:** Step 2
- **Handoff workflow diagram for README.md:**
  ```
  Overlord → Developer → Testing → Code Reviewer → Developer (fixes)
       ↓                    ↓                              ↓
    Testing             Developer                   Compound Loop
       ↓               (fix failures)              (update governance)
  Code Reviewer
  ```

### Step 5 — Create lifecycle hooks
- **Agent:** developer
- **Files:** `.github/hooks/quality-gate.json`, `.github/hooks/scripts/quality-gate.{ps1,sh}`, `.github/hooks/scripts/auto-format.{ps1,sh}`, `.github/hooks/scripts/session-context.{ps1,sh}` (7 files)
- **Action:** Create hook configuration with three lifecycle events (`Stop`, `PostToolUse`, `SessionStart`). All scripts must implement the **JSON stdin/stdout protocol**:
  - **Input (stdin):** JSON object with `hookEventName`, `sessionId`, `timestamp`, `cwd`, `transcript_path`, and event-specific fields.
  - **Output (stdout):** JSON object with `continue` (boolean), `stopReason` (string), `systemMessage` (string), `hookSpecificOutput` (object).
  - **Exit codes:** `0` = success, `2` = blocking error, other = non-blocking warning.
  - **`Stop` hook specifics:**
    - Check `stop_hook_active` in stdin — if `true`, do NOT block (prevents infinite loops).
    - Run ktlint + detekt on modified `.kt` files.
    - On failure: emit `hookSpecificOutput.decision: "block"` with `reason` describing failures.
    - On success: emit `continue: true`, exit 0.
    - Use `timeout: 120` (Gradle-based lint can be slow).
  - **`PostToolUse` hook specifics:**
    - Check `tool_name` in stdin — only run on file-editing tools (`editFiles`, `create_file`).
    - Run `ktlint --format` on the edited file path (from `tool_input`).
    - Use `timeout: 30`.
  - **`SessionStart` hook specifics:**
    - Gather project metadata: current branch, min SDK, build/test commands.
    - Emit via `hookSpecificOutput.additionalContext`.
    - Use `timeout: 10`.
  - **OS overrides:** Use `command` for Bash (default/Linux/macOS) with `windows` override for PowerShell. Optionally add `linux` and `osx` if scripts differ.
- **Depends on:** None

### Step 6 — Create skills
- **Agent:** developer
- **Files:** `.github/skills/build-and-test/SKILL.md`, `.github/skills/get-api-docs/SKILL.md`
- **Action:** Build-and-test skill: Gradle setup, build, test, lint steps with placeholders. Get-api-docs skill: copy from Context Hub `cli/skills/get-api-docs/SKILL.md`.
- **Frontmatter validation:**
  - `name` **must match parent directory**: `build-and-test` for `build-and-test/`, `get-api-docs` for `get-api-docs/`.
  - `name` must be lowercase with hyphens, max 64 characters.
  - `description` must be under 1024 characters; describe both what the skill does and when to use it.
  - Include `argument-hint` (e.g., `"[module] [task]"` for build-and-test).
- **Depends on:** None

### Step 7 — Create planning infrastructure
- **Agent:** developer
- **Files:** `.github/planning/PLANS.md`, `.github/planning/execplans/_TEMPLATE.md`
- **Action:** Adapt from Generic. Replace C++ tech references with Kotlin/Android. Keep ExecPlan authoring standard, mandatory sections, quality bar. Add emphasis on compound learning loop: the "Reusable findings" section in Outcomes & Retrospective should capture governance updates identified during review. Add section describing how Code Reviewer learnings feed back into instructions and `AGENTS.md`.
- **Depends on:** None

### Step 8 — Create GitHub Actions CI
- **Agent:** developer
- **Files:** `.github/workflows/android-ci.yml`
- **Action:** Workflow on push/PR: checkout, set up JDK 17+, Gradle cache, build, unit tests, ktlint check, detekt, Android lint.
- **Depends on:** None

### Step 9 — Create static analysis config
- **Agent:** developer
- **Files:** `detekt.yml`
- **Action:** Comprehensive detekt configuration — strict from project start, all findings as errors. Comparable philosophy to the Generic `.clang-tidy`.
- **Depends on:** None

### Step 10 — Commit and push (atomic)
- **Agent:** developer
- **Action:** Atomic commits per batch, then push:
  ```bash
  # After parallel batch 1 (Steps 1, 5, 6, 7, 8, 9):
  git add -A && git commit -m "feat: add root governance files, hooks, skills, planning, CI, static analysis"
  
  # After Step 2:
  git add -A && git commit -m "feat: add copilot-instructions.md"
  
  # After Step 3:
  git add -A && git commit -m "feat: add path-specific instructions"
  
  # After Step 4:
  git add -A && git commit -m "feat: add agent definitions with handoffs"
  
  # Push all commits:
  git push -u origin main
  ```
- **Depends on:** Steps 1–9
- **Expected output:** All files visible at `https://github.com/CAPeddle/android_app_template`

---

## Validation and Acceptance

- [ ] All 30+ files created in correct locations
- [ ] `AGENTS.md` at repo root references Kotlin/Android (not C++)
- [ ] `llms.txt` at repo root lists all governance artifacts
- [ ] `.github/copilot-instructions.md` is concise (~2 pages), focused on build/test commands and project layout
- [ ] Agent definitions use `.agent.md` files with YAML `---` frontmatter (no `chatagent` fences)
- [ ] Agent definitions include `handoffs` for workflow chaining where applicable
- [ ] Agent definitions include `agents` field (subagent allowlist) where applicable
- [ ] Code Reviewer agent includes compound learning loop instructions
- [ ] Path-specific instructions use correct `applyTo` frontmatter with comma-separated globs
- [ ] Path-specific instructions use `excludeAgent` where appropriate (e.g., `gradle.instructions.md`)
- [ ] Path-specific instructions absorb detailed coding standards from `copilot-instructions.md`
- [ ] Hook file uses `Stop` event (not `agentStop`)
- [ ] Hook file includes `PostToolUse` entry for auto-formatting
- [ ] Hook file includes `SessionStart` entry for context injection
- [ ] Hook scripts read JSON from stdin and emit JSON to stdout (structured protocol)
- [ ] Hook scripts check `stop_hook_active` to prevent infinite quality gate loops
- [ ] Hook commands use `windows` override for cross-platform support
- [ ] Quality gate hooks reference ktlint + detekt (not clang-format + clang-tidy)
- [ ] Skill `name` fields match their parent directory names (lowercase, hyphens)
- [ ] Skill `description` fields are under 1024 characters
- [ ] Skills include `argument-hint` field
- [ ] Skills follow agentskills.io open standard
- [ ] ExecPlan template references Kotlin/Android tech stack
- [ ] ExecPlan template emphasizes compound learning loop in Outcomes & Retrospective
- [ ] GitHub Actions workflow is valid YAML with correct Gradle steps
- [ ] `detekt.yml` has strict-from-start philosophy
- [ ] No C++ references remain in any file (search for: `clang`, `cmake`, `cpp`, `C++23`, `std::`, `new/delete`, `chatagent`)
- [ ] All `[PLACEHOLDER]` markers are documented in README for fork customization
- [ ] README documents MCP as future extension point
- [ ] README notes `CLAUDE.md`/`GEMINI.md` alternatives
- [ ] Repository pushed successfully to GitHub with atomic commits per batch

---

## Idempotence and Recovery

- All steps create new files — re-running is safe (overwrite).
- Atomic commits: if a commit fails, fix and amend that specific commit.
- Git push: if push fails due to remote having commits, `git pull --rebase` then retry.
- If any file has wrong content, edit, amend the relevant commit, and force-push if needed.
- Hook scripts are stateless — safe to re-run at any time.

---

## Artifacts and Notes

- **Source template:** `C:\projects\zoom_copilot_config\Generic` (20 files)
- **Remote:** `https://github.com/CAPeddle/android_app_template`
- **Branch:** `main`
- **chub status:** No Android docs available as of 2026-03-14. Skill included for future use.
- **MCP status:** Not implemented in this kit. Documented as future extension point in README. The `mcp-servers` agent frontmatter field is available for `target: github-copilot` when needed.

### External URLs for Reference

| URL | Value |
|-----|-------|
| https://docs.github.com/en/copilot | Canonical Copilot docs — agent format, hooks spec, skills spec |
| https://code.visualstudio.com/docs/copilot/customization/custom-agents | Full agent `.agent.md` spec with all frontmatter fields |
| https://code.visualstudio.com/docs/copilot/customization/hooks | Full hook lifecycle spec — 8 events, JSON protocol |
| https://code.visualstudio.com/docs/copilot/customization/agent-skills | Full skill spec — agentskills.io standard |
| https://agentskills.io | Agent Skills open standard |
| https://llmstxt.org | `llms.txt` discoverability standard |
| https://github.com/github/awesome-copilot | Agent, skill, hook, instruction catalogue; MCP discovery |
| https://cursor.com/blog/scaling-agents | Planner/worker orchestration pattern |
| https://developers.openai.com/cookbook/articles/codex_exec_plans | ExecPlan spec |
| https://github.com/EveryInc/compound-engineering-plugin | Compound learning loop pattern |
| https://github.com/gsd-build/get-shit-done | Context engineering, atomic commits, state tracking |
| https://developer.android.com/develop/ui/compose | Official Compose documentation |
| https://developer.android.com/training/dependency-injection/hilt-android | Official Hilt documentation |
| https://detekt.dev | detekt static analysis documentation |
| https://pinterest.github.io/ktlint | ktlint formatting documentation |
| https://developer.android.com/studio/test | Official Android testing documentation |
| https://mockk.io | MockK mocking library documentation |

---

## Interfaces and Dependencies

| Component | Type | Impact |
|-----------|------|--------|
| `AGENTS.md` | Root guidance | Sets ExecPlan trigger policy, notes alternatives (`CLAUDE.md`, `GEMINI.md`) |
| `llms.txt` | AI discoverability | Lists all governance artifacts for LLM consumption |
| `.github/copilot-instructions.md` | System instructions | All agents reference this (~2 pages, operational focus) |
| `.github/instructions/*.instructions.md` | Path rules | Applied to matching files (additive with repo-wide); detailed coding standards |
| `.github/agents/*.agent.md` | Agent definitions | 5 agents + README; include `handoffs` and `agents` fields |
| `.github/hooks/quality-gate.json` | Lifecycle hooks | `Stop` (quality gate), `PostToolUse` (auto-format), `SessionStart` (context) |
| `.github/hooks/scripts/*.{ps1,sh}` | Hook scripts | 6 scripts implementing JSON stdin/stdout protocol |
| `.github/skills/` | Skills | Agents invoke these procedurally; agentskills.io standard |
| `.github/planning/` | Planning | ExecPlan standard + template with compound loop emphasis |
| `.github/workflows/` | CI | Automated checks on push/PR |
| `detekt.yml` | Static analysis | Referenced by hooks + CI |
| `.editorconfig` | Formatting | Referenced by ktlint |

---

## Parallelization Notes

Steps 1, 5, 6, 7, 8, 9 have **no dependencies** and can be executed in parallel.
Steps 2, 3, 4 are sequential (each builds on prior content).
Step 10 depends on all prior steps.

**Recommended execution order for efficiency:**
1. **Parallel batch 1:** Steps 1 + 5 + 6 + 7 + 8 + 9  
   → Commit: `feat: add root governance files, hooks, skills, planning, CI, static analysis`
2. **Sequential:** Step 2 → Commit: `feat: add copilot-instructions.md`
3. **Sequential:** Step 3 → Commit: `feat: add path-specific instructions`
4. **Sequential:** Step 4 → Commit: `feat: add agent definitions with handoffs`
5. **Push:** `git push -u origin main`
