---
applyTo: "**/*.gradle.kts"
excludeAgent: "code-review"
---

# Gradle Coding Standards

## Version Catalogs

- **Always use Gradle version catalogs** (`gradle/libs.versions.toml`) for dependency management.
- Declare all dependency versions in the `[versions]` section.
- Group related dependencies with bundle aliases.
- Never hardcode version strings in `build.gradle.kts` files.

```toml
# gradle/libs.versions.toml
[versions]
kotlin = "2.0.0"
compose-bom = "2024.06.00"
hilt = "2.51.1"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }

[plugins]
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

## Convention Plugins

- Use **convention plugins** in `build-logic/` for shared build configuration.
- Move repeated `android {}` blocks, dependency declarations, and compiler options into convention plugins.
- Keep `build.gradle.kts` files thin — they should apply plugins and declare module-specific dependencies only.

## Dependency Management

- Use `implementation` for internal dependencies (not exposed to consumers).
- Use `api` only when the dependency is part of the module's public API.
- Use `testImplementation` for test-only dependencies.
- Use `ksp` (not `kapt`) for annotation processors where supported.
- Always use the Compose BOM for Compose dependency versions.

## Build Configuration

- Set `compileSdk`, `minSdk`, `targetSdk` in a shared convention plugin or `gradle/libs.versions.toml`.
- Enable Compose in the `android` block with `buildFeatures { compose = true }`.
- Configure the Kotlin compiler for Compose: use the Compose compiler Gradle plugin.
- Enable `buildConfig = true` only if `BuildConfig` is needed.

## Kotlin DSL Conventions

- Use `plugins { }` block (not `apply plugin:`).
- Use type-safe accessors for tasks and extensions.
- Prefer `val` property delegates for lazy configuration.
- Use trailing lambda syntax for configuration blocks.

```kotlin
// ✓ Preferred
plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
}

android {
    namespace = "[PLACEHOLDER:package-name]"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

## Performance

- Enable Gradle configuration cache: `org.gradle.configuration-cache=true` in `gradle.properties`.
- Enable build cache: `org.gradle.caching=true`.
- Use `--parallel` for multi-module builds.
- Set adequate JVM heap: `org.gradle.jvmargs=-Xmx4g` in `gradle.properties`.
