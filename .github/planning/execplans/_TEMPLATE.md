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

**Observable outcome:** [Concrete, testable statement of what success looks like.]

**Term definitions:**
- *[Term 1]:* [Definition]
- *[Term 2]:* [Definition]

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

- [ ] [Acceptance criterion 1]
- [ ] [Acceptance criterion 2]
- [ ] All tests pass (`./gradlew test`)
- [ ] No ktlint violations (`./gradlew ktlintCheck`)
- [ ] No detekt violations (`./gradlew detekt`)
- [ ] Build succeeds (`./gradlew build`)
- [ ] Code review completed (via `code-reviewer` agent)
- [ ] TDD cycle completed for all code changes

---

## Idempotence and Recovery

[How to safely re-run or recover from failures. For example: "All steps create new files — re-running is safe." or "Step 3 is a database migration — has rollback script at X."]

---

## Artifacts and Notes

[Links, references, important context.]
