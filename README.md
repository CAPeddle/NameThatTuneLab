# Android App Template — Agent-Native Governance Kit

A production-ready **GitHub Copilot governance kit** for Android app development. This is an **agent-native** initiative — all governance artifacts target autonomous agent workflows.

Agents operating in a fork of this repo immediately have working Copilot governance — agents that orchestrate work via handoffs, lifecycle hooks that enforce quality gates, and coding standards that produce idiomatic Kotlin/Compose code.

## What Is Included

| Component | Location | Purpose |
|-----------|----------|---------|
| Root agent guidance | `AGENTS.md` | ExecPlan trigger policy, quality gate description |
| AI discoverability | `llms.txt` | Lists all governance artifacts for LLM consumption |
| Repository instructions | `.github/copilot-instructions.md` | Build/test commands, project layout, agent roster |
| Path-specific instructions | `.github/instructions/` | Kotlin, Compose, Gradle, Architecture coding standards |
| Agent definitions | `.github/agents/` | Overlord, Developer, Debugger, Testing, Code Reviewer |
| Lifecycle hooks | `.github/hooks/` | Quality gate (`Stop`), auto-format (`PostToolUse`), context (`SessionStart`) |
| Agent skills | `.github/skills/` | `build-and-test`, `get-api-docs`, `git-commit` (agentskills.io standard) |
| Planning infrastructure | `.github/planning/` | ExecPlan authoring standard + template |
| CI workflow | `.github/workflows/` | Build, test, lint, detekt on push/PR |
| Static analysis | `detekt.yml` | Strict-from-start detekt configuration |
| Formatting | `.editorconfig` | ktlint formatting rules |

## Design Principles

| Principle | Source |
|-----------|--------|
| Planner/worker agent hierarchy | [Cursor — Scaling Agents](https://cursor.com/blog/scaling-agents) |
| Living-document ExecPlans | [OpenAI — Codex ExecPlans](https://developers.openai.com/cookbook/articles/codex_exec_plans) |
| Compound learning loop | [EveryInc — Compound Engineering](https://github.com/EveryInc/compound-engineering-plugin) |
| Context engineering + atomic commits | [gsd-build/get-shit-done](https://github.com/gsd-build/get-shit-done) |
| Agent/hook/skill spec compliance | [GitHub Copilot Docs](https://docs.github.com/en/copilot) |
| Community patterns | [github/awesome-copilot](https://github.com/github/awesome-copilot) |

## Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin (latest stable) |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt (Dagger) |
| Min SDK | API 33 (Android 13) |
| Build | Gradle (Kotlin DSL) |
| Formatting | ktlint (via Spotless or standalone) |
| Static Analysis | detekt + Android Lint |
| Unit Testing | JUnit 5 + MockK + Turbine |
| UI Testing | Compose UI Test |
| Integration Testing | Robolectric |
| CI | GitHub Actions |

## Getting Started (Agent Bootstrap)

1. **Fork or copy** this repository into your new Android project.
2. **Replace `[PLACEHOLDER]` markers** — search all files for `[PLACEHOLDER]` and replace with project-specific values (package name, module names, etc.).
3. **Initialize Gradle** — add your `build.gradle.kts` files. The governance kit expects a standard Gradle project structure.
4. **Verify hooks** — open VS Code with the Copilot extension, start a chat session, and confirm the `SessionStart` hook fires (check terminal output).
5. **Run the quality gate** — make a small `.kt` file change and end the session to verify the `Stop` hook runs ktlint + detekt.
6. **Review agent roster** — invoke `@overlord` in Copilot chat to verify the agent hierarchy is functional.

## Placeholder Markers

All files use `[PLACEHOLDER]` markers for values that must be customized per project:

| Marker | Where | Replace With |
|--------|-------|-------------|
| `[PLACEHOLDER:package-name]` | Various | Your root package (e.g., `com.example.myapp`) |
| `[PLACEHOLDER:app-module]` | Skills, CI | Your app module name (e.g., `:app`) |
| `[PLACEHOLDER:min-sdk]` | Instructions | Your minimum SDK (default: 33) |
| `[PLACEHOLDER:repo-url]` | README | Your GitHub repository URL |

## Agent Workflow

```
User → Overlord → Developer → Testing → Code Reviewer → Developer (fixes)
                       ↓                        ↓
                    Testing                Compound Loop
                  (fix failures)         (update governance)
```

See `.github/agents/README.md` for the full agent roster and handoff documentation.

## Future Extensions

- **MCP Servers:** The `mcp-servers` agent frontmatter field is available for `target: github-copilot` when server integrations become available. No MCP servers are configured in this initial kit.
- **Context Hub (`chub`):** The `get-api-docs` skill is included for when Android/Kotlin docs are added to Context Hub. Currently returns no results for Android-related queries.
- **Alternative agent runtimes:** This kit uses `AGENTS.md` for Copilot. Create `CLAUDE.md` or `GEMINI.md` at repo root for Claude Code or Gemini CLI compatibility.

## References

- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
- [VS Code — Custom Agents](https://code.visualstudio.com/docs/copilot/customization/custom-agents)
- [VS Code — Hooks](https://code.visualstudio.com/docs/copilot/customization/hooks)
- [VS Code — Agent Skills](https://code.visualstudio.com/docs/copilot/customization/agent-skills)
- [agentskills.io](https://agentskills.io/)
- [llmstxt.org](https://llmstxt.org/)
- [detekt](https://detekt.dev)
- [ktlint](https://pinterest.github.io/ktlint)
