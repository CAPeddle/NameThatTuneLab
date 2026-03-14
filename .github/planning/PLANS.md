# ExecPlan Authoring Standard

This document defines when and how to write ExecPlans for this repository. All agents must follow this standard.

## When an ExecPlan is Required

An ExecPlan **must** be created before implementation begins when the task involves:

- Creating or modifying **more than 3 files**
- Introducing a **new module, layer, or architectural boundary**
- Changing **build configuration or CI pipeline**
- Any task estimated to exceed **a single agent session**
- Integrating a **new third-party dependency**
- Modifying **agent definitions, hooks, or skills**

## When an ExecPlan is NOT Required

- Bug fixes contained to **a single file**
- Adding a **single test**
- Documentation-only updates
- Renaming or moving a single symbol
- Trivial formatting changes

## Repository Requirements

All code produced under an ExecPlan must follow:

1. **TDD** — Write tests first (RED), implement (GREEN), refactor. See `testing.agent.md`.
2. **Code Review** — Every ExecPlan must include a Code Review milestone. See `code-reviewer.agent.md`.
3. **Clean Architecture** — Respect layer boundaries (domain → data → presentation). See `architecture.instructions.md`.
4. **Kotlin Idioms** — Follow conventions in `kotlin.instructions.md`.
5. **Compose Patterns** — Follow conventions in `compose.instructions.md` when touching UI.

### Term Definitions

Include a "Term definitions" section in every ExecPlan. Define jargon, acronyms, and domain-specific vocabulary. **Do not assume the reader shares your context.**

## Mandatory Sections

Every ExecPlan must include these sections:

| # | Section | Purpose |
|---|---------|---------|
| 1 | **Title** | Descriptive name |
| 2 | **Metadata** | Date, Status, Owner, Refs, Revision |
| 3 | **Purpose / Big Picture** | Why this work matters, observable outcome, term definitions |
| 4 | **Progress** | Checkbox list updated as work proceeds |
| 5 | **Surprises & Discoveries** | Unexpected findings recorded as they occur |
| 6 | **Decision Log** | Decisions with rationale and date |
| 7 | **Outcomes & Retrospective** | Completed after plan closes — achievements, reusable findings, anti-patterns |
| 8 | **Context and Orientation** | Background, dependencies, source material |
| 9 | **Plan of Work** | Milestones with numbered file/action lists |
| 10 | **Concrete Steps** | Detailed steps with agent, files, action, dependencies |
| 11 | **Validation and Acceptance** | Checkbox list of acceptance criteria |
| 12 | **Idempotence and Recovery** | How to safely re-run or recover from failures |

## Required Quality Bar

- [ ] All acceptance criteria met
- [ ] TDD cycle completed (RED → GREEN → REFACTOR) for every code change
- [ ] Code review completed (via `code-reviewer` agent)
- [ ] No ktlint violations
- [ ] No detekt violations (with `detekt.yml` config)
- [ ] No Android Lint errors
- [ ] All tests pass (`./gradlew test`)
- [ ] Build succeeds (`./gradlew build`)

## Mandatory Progress Checkpoints

For every code change within an ExecPlan, record progress through the TDD cycle:

1. **RED** — Test written and failing. Record: `- [ ] RED: [test name] — [what it verifies]`
2. **GREEN** — Minimal implementation passes. Record: `- [x] GREEN: [test name] passes`
3. **REFACTOR** — Code cleaned up, no regression. Record: `- [x] REFACTOR: [what was cleaned]`
4. **Code Review** — Changes reviewed. Record: `- [x] REVIEW: [findings summary]`

## Compound Learning Loop

After Code Review, the `code-reviewer` agent outputs a **Learnings** summary. These learnings must be evaluated and may result in updates to:

- `kotlin.instructions.md` — New Kotlin patterns to promote or anti-patterns to prohibit
- `compose.instructions.md` — New Compose patterns discovered
- `architecture.instructions.md` — Architectural boundaries clarified
- `AGENTS.md` — Agent guidance refined
- `detekt.yml` — New rules enabled or thresholds adjusted

Record any governance updates in the ExecPlan's "Reusable findings" section.

## Jargon Policy

**Do not use unexplained jargon.** Every ExecPlan must define domain-specific terms in the "Term definitions" subsection of Purpose. If a reviewer cannot understand a term without external context, it must be defined.

## Location and Naming

- **Directory:** `.github/planning/execplans/`
- **Naming:** `YYYY-MM-DD-short-description.md` (e.g., `2026-03-15-add-user-profile.md`)
- **Template:** Copy from `.github/planning/execplans/_TEMPLATE.md`

## Living Document Policy

ExecPlans are **living documents**. Update them as work proceeds:
- Check off progress items as milestones complete
- Add surprises and discoveries as they occur
- Log decisions with rationale immediately
- Complete the retrospective when the plan closes

## Task Sizing

### Atomic Task Rule

Each step in "Concrete Steps" should be **atomic** — completable in a single focused session without context loss.

### Signs a Step is Too Large

- It touches more than 5 files
- It requires more than 2 agent handoffs
- It cannot be validated with a single test run
- It mixes concerns (e.g., UI + data layer in one step)

Split large steps into smaller, independently verifiable sub-steps.
