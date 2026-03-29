````skill
---
name: review-upstream-sources
description: Reviews upstream AI governance sources for changes, classifies findings (breaking/enhancement/info), updates local governance files when needed, and can bootstrap a new GitHub repository with the governance template.
---

# Review Upstream Sources

This skill keeps governance files aligned with evolving external best practices and specifications.

It also supports optional **self-population** of a new GitHub repository when no governance baseline exists yet.

---

## Seed Source Registry

### Canonical references (likely to require adoption)

| Source | Contributes | URL |
|---|---|---|
| VS Code Agent Skills | `SKILL.md` format, frontmatter schema, loading behaviour | https://code.visualstudio.com/docs/copilot/customization/agent-skills |
| VS Code Agents Overview | Agent types, session patterns, handoff model | https://code.visualstudio.com/docs/copilot/agents/overview |
| VS Code Custom Agents | `.agent.md` frontmatter schema, tools list, handoffs | https://code.visualstudio.com/docs/copilot/customization/custom-agents |
| VS Code Chat Tools Reference | Built-in tool names for `tools:` arrays | https://code.visualstudio.com/docs/copilot/reference/copilot-vscode-features#_chat-tools |
| GitHub Prompt Files Tutorial | Prompt/customization file model and slash command behaviour | https://docs.github.com/en/copilot/tutorials/customization-library/prompt-files/your-first-prompt-file |
| Awesome Copilot Learning Hub | Agents vs skills vs instructions taxonomy | https://awesome-copilot.github.com/learning-hub/what-are-agents-skills-instructions/ |

### Pattern sources (inspirational, selective adoption)

| Source | Contributes | URL |
|---|---|---|
| OpenAI ExecPlans Cookbook | `PLANS.md` quality model and living-plan structure | https://developers.openai.com/cookbook/articles/codex_exec_plans |
| Cursor: Scaling Agents | Planner/worker patterns, context management | https://cursor.com/blog/scaling-agents |
| Compound Engineering Plugin | Plan→Work→Review→Compound philosophy | https://github.com/EveryInc/compound-engineering-plugin |
| GSD | Context engineering and spec-driven wave execution | https://github.com/gsd-build/get-shit-done |
| Context Hub BYOD | Source/distribution layering patterns | https://github.com/andrewyng/context-hub/blob/main/docs/byod-guide.md |

### Android-specific sources

| Source | Contributes | URL |
|---|---|---|
| Android Developer Guides | Compose, Architecture Components, Hilt | https://developer.android.com/develop |
| Kotlin Coding Conventions | Official style guide | https://kotlinlang.org/docs/coding-conventions.html |
| detekt Rules | Static analysis rule set documentation | https://detekt.dev/docs/rules/ |

---

## Tracking Baseline

Use `.github/skills/review-upstream-sources/source-tracking.json` as the review baseline.

For each source, track:
- `last_reviewed`
- `version_marker`
- `key_sections_at_review`
- `github_release_tag` (when applicable)
- `notes`

---

## Review Procedure

### Step 0 — Load baseline

Read `source-tracking.json` and note previous state for each source.

### Step 1 — Fetch and compare

For each source:
- Fetch current content
- Compare headings/structure with `key_sections_at_review`
- Check release tags/changelog for GitHub repositories

Record findings as:

```
Source: <tracking-key>
Previous state: <version_marker>
Current state: <observed now>
Delta: <specific changes>
```

### Step 2 — Classify findings

| Classification | Criteria | Action |
|---|---|---|
| Breaking | Schema/format/term changed in a way that invalidates local governance artifacts | Must adopt |
| Enhancement | New practice improves quality/safety/maintainability | Evaluate cost-benefit; adopt if net-positive |
| Informational | Interesting but no immediate governance impact | Track in notes |
| Not applicable | Outside project stack/context (e.g. C++ specific) | Skip |

### Step 3 — Determine scope

For breaking/enhancement items:
1. Identify affected local files
2. Apply context-load heuristic: reactive diagnostics should live in a skill/knowledge file, not always-on instructions
3. Plan edits (ExecPlan for complex changes)

### Step 4 — Implement and validate

1. Update impacted governance files
2. Validate structure and commands:
   - Agent files: confirm `tools:` values are valid VS Code built-in tool names
   - Skills: confirm SKILL.md frontmatter parses
   - Hooks: confirm quality-gate.json is valid JSONC
3. Run project quality gate: `./gradlew ktlintCheck detekt lintDebug testDebugUnitTest`
4. Commit with a message referencing source key + what changed

Example:

```
docs(governance): adopt updates from vscode-custom-agents

Upstream change: new agent frontmatter fields added (user-invocable, disable-model-invocation).
Local adoption: updated agent files and validate-agent-tools skill.
```

### Step 5 — Update tracking

For reviewed sources:
- update `last_reviewed`
- update `version_marker`
- update `key_sections_at_review` if needed
- update `github_release_tag` if changed
- append notes

Commit tracking updates even when no governance changes were adopted.

---

## Optional Extension — Self-Populate a GitHub Repository

Use this when starting a new project and no governance baseline exists.

### Preconditions
- You have a local repository root with the governance template content ready
- You have GitHub access and `gh` or equivalent configured

### Workflow

1. **Create repository** (or use existing empty repo)
2. **Copy governance baseline** into repo root:
   - `AGENTS.md`
   - `.github/copilot-instructions.md`
   - `.github/agents/`
   - `.github/skills/`
   - `.github/hooks/`
   - `.github/instructions/`
   - `.github/planning/`
3. **Replace placeholders** (`[PROJECT_NAME]`, `[NAMESPACE]`, `[PLACEHOLDER:...]`, etc.)
4. **Validate local commands** in build-and-test skill
5. **Create initial commit + push**
6. **(Optional) Add branch protection and CI required checks**

### Recommended follow-up
- Add CODEOWNERS for governance files
- Enable required status checks (ktlint/detekt/test/lint workflows)
- Schedule periodic `review-upstream-sources` execution (monthly/quarterly)

---

## Review Triggers

Invoke this skill:
- periodically (monthly/quarterly)
- before large governance refactors
- when a source has announced updates
- when adding a new governance artifact type
- when bootstrapping a new repository

---

## Review Log

Record completed reviews at the bottom of this file:
`YYYY-MM-DD: summary`

- 2026-03-21: Initial adoption from Generic template. Source registry established with Android-specific additions.

````
