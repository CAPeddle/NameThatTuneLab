````skill
---
name: adopt-template-updates
description: Step-by-step process for evaluating and adopting updates from the Generic governance template into this project repository. Use this when pulling newer template improvements without blindly overwriting local customizations.
---

# Adopt Generic Template Updates

This project's governance files originate from a Generic governance template. As the template evolves, this skill defines a safe process for pulling improvements without overwriting Android/Kotlin-specific customizations.

---

## Inputs Required

- Path to project repository (this repo: `c:\projects\personal\NameThatTuneLab`)
- Path to latest Generic template (`c:\projects\zoom_copilot_config\Generic`) or remote source URL
- Optional list of files to evaluate first (recommended)

---

## Step 1 — Identify candidate files

Start with governance artifacts:
- `.github/copilot-instructions.md`
- `.github/agents/*.agent.md`
- `.github/skills/**/SKILL.md`
- `.github/hooks/**`
- `.github/instructions/*.instructions.md`
- `.github/planning/PLANS.md`
- `.github/planning/execplans/_TEMPLATE.md`
- `AGENTS.md`

---

## Step 2 — Compare template vs project

Use hash check first (fast), then content diff for differences.

### Hash comparison (PowerShell)

```powershell
$template = "C:\projects\zoom_copilot_config\Generic\.github\copilot-instructions.md"
$project  = "C:\projects\personal\NameThatTuneLab\.github\copilot-instructions.md"
$h1 = (Get-FileHash $template -Algorithm SHA256).Hash
$h2 = (Get-FileHash $project -Algorithm SHA256).Hash
Write-Host "Same: $($h1 -eq $h2)"
```

### Content diff

```powershell
$t = Get-Content "<template_file>"
$p = Get-Content "<project_file>"
Compare-Object $t $p | ForEach-Object { "$($_.SideIndicator) $($_.InputObject)" }
```

Interpretation:
- `=>` line exists only in project (local customization)
- `<=` line exists only in template (potential adoption)

---

## Step 3 — Classify each delta

| Classification | Criteria | Action |
|---|---|---|
| **Adopt** | Template improvement applies cleanly | Bring into project |
| **Discard** | Template change conflicts with intentional Android/Kotlin customizations | Keep project version |
| **Refine-then-adopt** | Useful idea but requires adaptation to Kotlin/Compose/Gradle conventions | Adapt then merge |

### Heuristic for this project
- **Keep:** Android-specific commands (`./gradlew`), Kotlin/Compose patterns, Hilt DI references, JUnit 5 + MockK + Turbine test stack
- **Adopt:** Structural improvements to agent workflows, context pressure tracking, worker response formats, ExecPlan quality bar enhancements, new skills
- **Discard:** C++ specific content (clang-format, clang-tidy, sanitizers, CMake, PIMPL, `std::expected`)

---

## Step 4 — Apply updates safely

Preferred: update files selectively, section-by-section.

For wholesale replacement (only when project file has minimal local customization):

```powershell
Copy-Item "<template_file>" "<project_file>" -Force
```

After each update, validate:
- Placeholders replaced with project-specific values
- Android/Kotlin commands still correct (`./gradlew`, `adb`, etc.)
- No references to template-only files remain (e.g. `.clang-format`, `.clang-tidy`)

---

## Step 5 — Validate

Run project quality checks relevant to changed files:

```bash
# If agent/skill/instruction files changed
# Reload VS Code and check for yellow underlines in agent files

# If build/test files changed
./gradlew ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug
```

If changing agent files, ensure frontmatter parses and tools are recognized (use `validate-agent-tools` skill).

---

## Step 6 — Commit

Use a descriptive commit message that references template adoption.

Example:

```
docs(governance): adopt Generic template updates

- Added review-upstream-sources skill
- Added validate-agent-tools skill
- Updated PLANS.md with investigation-first ExecPlan rule
- Enhanced agent files with context pressure tracking
```

---

## Optional: maintain a template-version marker

To simplify future upgrades, the file `.github/skills/review-upstream-sources/source-tracking.json` tracks the `generic-governance-template` source with its last review date and version marker.

---

## Completion checklist

- [ ] Candidate files identified
- [ ] File hashes compared
- [ ] Deltas classified (adopt/discard/refine)
- [ ] Updates applied with project-specific adaptation
- [ ] Validation checks run
- [ ] Commit created with clear rationale

````
