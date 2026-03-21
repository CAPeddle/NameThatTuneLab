# [Title — Descriptive Name]

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` up to date as work proceeds.

**Date:** YYYY-MM-DD  
**Status:** 🆕 Not Started  
**Owner:** [Agent or user name]  
**Refs:** [Links to issues, PRs, related plans]  
**Revision:** v1

---

## Purpose / Big Picture

[1–3 paragraphs: Why does this work matter? What is the observable outcome when complete?]

**Observable outcome:** [Concrete, **testable** statement. Should be answerable with a yes/no by running a command or observing a specific behavior. Avoid vague language like "it works" or "it's better".]

**Term definitions:**
> Define every term that could be ambiguous within this plan. If two people might read a word differently, define it here.
- *[Term 1]:* [Definition — be precise, not colloquial]
- *[Term 2]:* [Definition — be precise, not colloquial]

---

## Progress

- [ ] [Milestone or task description]

---

## Surprises & Discoveries

*(Record unexpected findings as they occur.)*

---

## Decision Log

- **Decision:** [What was decided]  
  Rationale: [Why]  
  Date: YYYY-MM-DD

---

## Outcomes & Retrospective

*(Complete after plan closes.)*

**What was achieved:**

**What remains (if anything):**

**Patterns to promote:**

**Reusable findings:**
[Governance updates identified during review — changes to instructions, agent guidance, or detekt rules that should be applied to improve future cycles.]

**New anti-patterns:**

---

## Context and Orientation

[Background, dependencies, source material. Include technology stack, relevant files, and any constraints.]

### Technology Stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Build | Gradle (Kotlin DSL) |
| Testing | JUnit 5 + MockK + Turbine + Compose UI Test |

---

## Plan of Work

> **TDD Order Mandate:** For every code-producing step, the sequence is always:
> 1. `@testing` writes failing tests (RED)
> 2. `@developer` writes implementation (GREEN)
> 3. `@testing` verifies all pass (REFACTOR)
> 4. `@code-reviewer` reviews
>
> Do NOT write implementation before tests exist.

> **Investigation-First Rule:** If this plan involves fixing a bug or diagnosing an unexpected behavior, invoke `@debugger` to produce an investigation report **before** creating any implementation steps. The report's findings should drive the concrete steps below.

### Milestone 1 — [Name]
[Description of what this milestone achieves.]

1. [File or action]
2. [File or action]

### Milestone 2 — [Name]
[Description]

---

## Concrete Steps

### Step 1 — [Name]
- **Agent:** [developer | testing | debugger | code-reviewer]
- **Files:** [List of files to create/modify]
- **Action:** [What to do]
- **Depends on:** [Step N | None]

### Step 2 — [Name]
- **Agent:** [agent]
- **Files:** [files]
- **Action:** [action]
- **Depends on:** [dependency]

---

## Validation and Acceptance

> Use observable, command-verifiable criteria. Prefer: "Running X produces output Y" over "the feature works correctly".

| Criterion | Command / Evidence | Status |
|-----------|-------------------|--------|
| [Criterion 1 — observable] | [Command or output to check] | - |
| [Criterion 2 — observable] | [Command or output to check] | - |
| All unit tests pass | `./gradlew testDebugUnitTest` — zero failures | - |
| No ktlint violations | `./gradlew ktlintCheck` — zero warnings | - |
| No detekt violations | `./gradlew detekt` — zero findings | - |
| Build succeeds | `./gradlew assembleDebug` — `BUILD SUCCESSFUL` | - |
| Code review completed | `@code-reviewer` — no blockers | - |
| TDD cycle completed | RED → GREEN → REFACTOR for all new code | - |

---

## Idempotence and Recovery

[How to safely re-run or recover from failures. For example: "All steps create new files — re-running is safe." or "Step 3 is a database migration — has rollback script at X."]

---

## Artifacts and Notes

[Links, references, important context.]
