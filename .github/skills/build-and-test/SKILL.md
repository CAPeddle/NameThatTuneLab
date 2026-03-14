---
name: build-and-test
description: "Build, test, and lint an Android project using Gradle. Use this skill when you need to compile the project, run unit tests, execute UI tests, check code formatting with ktlint, or run static analysis with detekt. Supports targeting specific modules or running all checks."
argument-hint: "[module] [task] — e.g., ':app test' or 'detekt'"
---

# Build and Test — Android (Gradle)

This skill runs Gradle tasks to build, test, and lint an Android project.

## Prerequisites

- JDK 17+ installed and available on `PATH` (or set via `JAVA_HOME`)
- Android SDK installed (or `local.properties` pointing to SDK location)
- Gradle wrapper (`gradlew` / `gradlew.bat`) present at project root

## Step 1 — Verify Environment

```bash
# Check JDK
java -version

# Check Gradle wrapper
./gradlew --version
```

If `gradlew` is missing, initialize with: `gradle wrapper --gradle-version [PLACEHOLDER:gradle-version]`

## Step 2 — Build

```bash
# Full build (all modules)
./gradlew build

# Specific module
./gradlew [PLACEHOLDER:app-module]:assembleDebug
```

## Step 3 — Run Unit Tests

```bash
# All unit tests
./gradlew test

# Specific module
./gradlew [PLACEHOLDER:app-module]:testDebugUnitTest

# With coverage report
./gradlew [PLACEHOLDER:app-module]:testDebugUnitTest jacocoTestReport
```

## Step 4 — Run UI Tests (Instrumented)

```bash
# Requires running emulator or device
./gradlew connectedAndroidTest

# Specific module
./gradlew [PLACEHOLDER:app-module]:connectedDebugAndroidTest
```

## Step 5 — Check Formatting (ktlint)

```bash
# Check only (no changes)
./gradlew ktlintCheck

# Auto-format
./gradlew ktlintFormat
```

## Step 6 — Static Analysis (detekt)

```bash
# Run detekt with project config
./gradlew detekt

# Generate HTML report
./gradlew detekt --report html:build/reports/detekt/report.html
```

## Step 7 — Android Lint

```bash
# Run Android Lint
./gradlew lint

# Specific module
./gradlew [PLACEHOLDER:app-module]:lintDebug
```

## Full Quality Check (All Steps)

```bash
./gradlew build test ktlintCheck detekt lint
```

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `JAVA_HOME not set` | Set `JAVA_HOME` to JDK 17+ directory |
| `SDK not found` | Create `local.properties` with `sdk.dir=/path/to/sdk` |
| `gradlew permission denied` | Run `chmod +x gradlew` (macOS/Linux) |
| `ktlintCheck fails` | Run `./gradlew ktlintFormat` first, then re-check |
| `detekt fails` | Review `detekt.yml` — set `maxIssues: 0` for strict mode |
| `OOM during build` | Add `org.gradle.jvmargs=-Xmx4g` to `gradle.properties` |
| `Compose compiler error` | Ensure Compose compiler version matches Kotlin version in version catalog |
