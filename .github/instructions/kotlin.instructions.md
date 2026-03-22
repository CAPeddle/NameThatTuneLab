---
applyTo: "**/*.kt"
---

# Kotlin Coding Standards

## Language Version

Use the latest stable Kotlin version. Target JVM 17+.

## Null Safety

- **Never use `!!` (not-null assertion)** unless in test code with a clear comment explaining why.
- Prefer `?.let {}`, `?: defaultValue`, or `?: return` over null checks with `if`.
- Use `requireNotNull()` or `checkNotNull()` for fail-fast validation at function entry points.
- Declare function parameters as non-null by default. Use nullable types only when null is a meaningful domain value.

## Error Handling

- Use **sealed classes** or **sealed interfaces** for domain error types — not exceptions for control flow.
- Use `kotlin.Result` for operations that can fail in a recoverable way.
- Reserve exceptions for truly exceptional, unrecoverable situations.
- Never catch `Exception` or `Throwable` broadly — catch specific types.

```kotlin
// ✓ Preferred: sealed class for domain errors
sealed interface UserError {
    data class NotFound(val id: String) : UserError
    data class Unauthorized(val reason: String) : UserError
}

// ✓ Preferred: Result for fallible operations
suspend fun fetchUser(id: String): Result<User>
```

## Coroutines

- Inject `CoroutineDispatcher` via Hilt — never hardcode `Dispatchers.IO` or `Dispatchers.Main`.
- Use `viewModelScope` in ViewModels, `lifecycleScope` in UI.
- Prefer `Flow` over `LiveData` for reactive data streams.
- Use `StateFlow` for UI state, `SharedFlow` for events.
- Always handle cancellation properly — use `withContext` for dispatcher switching.
- Never use `GlobalScope`.
## ViewModel Exception Handling

When a `viewModelScope.launch` block calls a `suspend` function that can throw (e.g., a
DataStore write, a network request through a use case), use `runCatching` and chain
`.onSuccess` / `.onFailure`:

```kotlin
// ✓ Correct: safe coroutine exception handling in ViewModel
fun saveSettings() {
    viewModelScope.launch {
        runCatching {
            updateAppSettingsUseCase(newSettings)
        }.onSuccess {
            _uiState.update { it.copy(errorMessage = null) }
        }.onFailure { throwable ->
            if (throwable is CancellationException) throw throwable   // never swallow
            _uiState.update { it.copy(errorMessage = "Save failed: ${throwable.message}") }
        }
    }
}
```

Rules:
- **Always re-throw `CancellationException`** — swallowing it prevents structured concurrency from working.
- **Clear `errorMessage` on success** — stale error banners persist if `onSuccess` omits the clear.
- **Do not import `CancellationException` if it is already available via `kotlinx.coroutines`** — check
  before adding a duplicate import.

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Class / Interface | PascalCase | `UserRepository`, `FetchUserUseCase` |
| Function | camelCase | `fetchUser()`, `validateEmail()` |
| Property / Variable | camelCase | `userName`, `isLoading` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT` |
| Package | lowercase, dot-separated | `com.example.app.domain.model` |
| Type parameter | Single uppercase letter or PascalCase | `T`, `Key`, `Value` |
| Boolean property | `is`/`has`/`can`/`should` prefix | `isLoading`, `hasError` |

## Hilt / Dependency Injection

- Annotate all injectable classes with `@Inject constructor`.
- Use `@HiltViewModel` for ViewModels.
- Define modules in the `di/` package with `@Module` and `@InstallIn`.
- Prefer constructor injection over field injection (`@Inject lateinit var` is a last resort).
- Scope bindings appropriately: `@Singleton`, `@ViewModelScoped`, `@ActivityScoped`.

```kotlin
// ✓ Correct: constructor injection with Hilt
@HiltViewModel
class UserViewModel @Inject constructor(
    private val fetchUserUseCase: FetchUserUseCase,
) : ViewModel()
```

## Collections and Functional Style

- Prefer `List`, `Set`, `Map` (read-only) over mutable variants. Use `MutableList` only when mutation is required.
- Use `sequence {}` for large collections with chained operations.
- Prefer `map`, `filter`, `fold` over manual loops.
- Use `buildList`, `buildMap`, `buildSet` for constructing collections.

## Testing Mandates

- Every public function must have at least one unit test.
- Use **JUnit 5** (`@Test`, `@BeforeEach`, `@Nested`) — not JUnit 4.
- Use **MockK** for mocking — not Mockito.
- Use **Turbine** for testing `Flow` emissions.
- Follow **Triple-A** pattern: Arrange, Act, Assert.
- Test names: `should [expected behavior] when [condition]`.

```kotlin
@Test
fun `should return NotFound when user does not exist`() {
    // Arrange
    coEvery { repository.findById("123") } returns null
    // Act
    val result = useCase("123")
    // Assert
    result shouldBe UserError.NotFound("123")
}
```

## AI Agent Failure Modes

Watch for these common mistakes when reviewing agent-generated Kotlin code:

1. **Unnecessary `!!` operators** — agents often add not-null assertions instead of proper null handling.
2. **Hardcoded dispatchers** — agents write `Dispatchers.IO` directly instead of injecting.
3. **Missing `@Inject` annotations** — agents forget constructor injection annotations.
4. **JUnit 4 imports** — agents default to `org.junit.Test` instead of `org.junit.jupiter.api.Test`.
5. **Mockito instead of MockK** — agents suggest Mockito by default; this project uses MockK.
6. **Mutable collections in public APIs** — agents expose `MutableList` instead of `List`.
7. **Catching generic exceptions** — agents write `catch (e: Exception)` instead of specific types.
8. **Missing sealed class exhaustive `when`** — agents add `else` branches instead of handling all cases.
9. **Swallowing `CancellationException`** — agents write `catch (e: Exception)` or bare `onFailure` in coroutines without re-throwing `CancellationException`, breaking structured concurrency.
