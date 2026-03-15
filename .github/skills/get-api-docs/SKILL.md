---
name: get-api-docs
description: "Retrieve curated API documentation from Context Hub (chub) to reduce hallucination when working with external libraries. Use this skill when you need accurate, versioned API references for dependencies. Note: Android/Kotlin/Compose/Hilt docs are not yet available in chub — the skill is included for future use and works for non-Android APIs."
argument-hint: "[library-name] — e.g., 'okhttp' or 'retrofit'"
---

# Get API Docs — Context Hub (chub)

This skill retrieves curated, versioned API documentation from [Context Hub](https://github.com/aisuite/chub) to provide agents with accurate reference material and reduce hallucination.

## Prerequisites

- `chub` CLI installed: `npm install -g @aisuite/chub`
- Internet access for fetching docs (first time only; cached locally)

## Usage

### Search for Available Docs

```bash
chub search <query>
```

Example:
```bash
chub search okhttp
chub search retrofit
chub search kotlin
```

### Retrieve Docs for a Library

```bash
chub get <package-name>
```

Example:
```bash
chub get com.squareup.okhttp3:okhttp
```

### List Cached Docs

```bash
chub list
```

## Current Android Ecosystem Status

As of 2026-03-14, **no Android/Kotlin ecosystem docs are available** in Context Hub:

- ❌ Kotlin stdlib
- ❌ Jetpack Compose
- ❌ Hilt / Dagger
- ❌ Room
- ❌ Retrofit / OkHttp
- ❌ JUnit 5
- ❌ MockK
- ❌ detekt
- ❌ ktlint

This skill is included for **future readiness** — chub content is growing. It also works for non-Android libraries that may be available (HTTP clients, JSON parsers, etc.).

## When to Use

- Before implementing code that uses an unfamiliar API
- When you need exact method signatures, parameter types, or return types
- When training knowledge may be outdated for a specific library version
- To verify behavior before writing tests

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `chub: command not found` | Install with `npm install -g @aisuite/chub` |
| `No docs found for X` | Library not yet curated — rely on training knowledge |
| `Network error` | Check internet connection; cached docs work offline |
