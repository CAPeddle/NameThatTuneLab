# Agent Workflow Documentation

## Agent Roster

| Agent | File | Role |
|-------|------|------|
| **Overlord** | `overlord.agent.md` | Orchestrator — requirements refinement, ExecPlan gate, agent delegation, mandatory code review |
| **Developer** | `developer.agent.md` | Implementation specialist — Kotlin/Compose/Clean Architecture/Hilt |
| **Debugger** | `debugger.agent.md` | Investigation specialist — Logcat, LeakCanary, StrictMode, crash diagnosis |
| **Testing** | `testing.agent.md` | TDD specialist — JUnit 5, MockK, Turbine, Compose UI Test |
| **Code Reviewer** | `code-reviewer.agent.md` | Review — Kotlin idioms, Compose patterns, architecture compliance, compound learning |

## Workflow Diagram

```
User
  │
  ▼
Overlord (plan & delegate)
  │
  ├──► Developer (implement)
  │         │
  │         ▼
  │    Testing (verify)
  │         │
  │         ├── FAIL ──► Developer (fix)
  │         │                  │
  │         │                  ▼
  │         │            Testing (re-verify)
  │         │
  │         ▼ PASS
  │    Code Reviewer (review)
  │         │
  │         ├── CHANGES REQUESTED ──► Developer (address)
  │         │                               │
  │         │                               ▼
  │         │                         Testing (re-verify)
  │         │                               │
  │         │                               ▼
  │         │                         Code Reviewer (re-review)
  │         │
  │         ▼ APPROVED
  │    Compound Loop (update governance)
  │
  ├──► Debugger (investigate)
  │         │
  │         ▼
  │    Developer (apply fix)
  │         │
  │         ▼
  │    Testing (verify fix)
  │
  ▼
Done
```

## Handoff Reference

| From | To | Trigger | Handoff Label |
|------|----|---------|---------------|
| Overlord | Developer | Implementation needed | "Implement this plan" |
| Overlord | Testing | Validation needed | "Validate implementation" |
| Overlord | Code Reviewer | Review needed | "Review changes" |
| Developer | Testing | Implementation complete | "Run tests for my changes" |
| Developer | Code Reviewer | Tests pass | "Review my changes" |
| Debugger | Developer | Fix identified | "Apply this fix" |
| Debugger | Testing | Fix applied | "Verify the fix" |
| Testing | Developer | Tests fail | "Fix failing tests" |
| Testing | Code Reviewer | Tests pass | "Tests pass — review ready" |
| Code Reviewer | Developer | Changes requested | "Address review comments" |

## Skills Available to All Agents

| Skill | Directory | Purpose |
|-------|-----------|---------|
| `build-and-test` | `.github/skills/build-and-test/` | Gradle build, test, lint, detekt workflow |
| `get-api-docs` | `.github/skills/get-api-docs/` | Context Hub (chub) API documentation retrieval |
| `git-commit` | `.github/skills/git-commit/` | Conventional Commits — analyze diff, generate message, commit |

## Compound Learning Loop

The Code Reviewer agent runs a **compound learning loop** after every review. This produces a "Learnings" section identifying:

1. **Patterns to promote** — Good patterns to add to instruction files.
2. **Anti-patterns to prohibit** — Bad patterns to add to instruction files.
3. **Unclear guidance** — Gaps in governance that need clarification.
4. **Governance update actions** — Concrete changes to instruction files, `AGENTS.md`, or `detekt.yml`.

The Overlord (or a human reviewer) evaluates proposed governance updates and applies approved changes. This makes the governance kit **self-improving** over time.

```
Review → Findings → Learnings → Governance Update → Better Reviews → ...
```

## Model Selection Guidance

| Task Type | Recommended Model | Rationale |
|-----------|------------------|-----------|
| Planning, complex reasoning | Claude Opus | Stronger reasoning for architecture decisions |
| Implementation, review | Claude Sonnet | Fast, cost-effective for code generation |
| Debugging, investigation | Claude Sonnet | Good balance of speed and analysis depth |
| Testing, TDD | Claude Sonnet | Pattern-based test generation |
