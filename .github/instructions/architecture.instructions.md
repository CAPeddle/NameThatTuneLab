---
applyTo: "**/*.kt"
---

# Clean Architecture Standards

## Layer Overview

```
┌─────────────────────┐
│    presentation/     │  ← ViewModels, UI State, Compose Screens
│    (depends on ↓)    │
├─────────────────────┤
│      domain/         │  ← Use Cases, Entities, Repository Interfaces
│  (depends on NOTHING)│
├─────────────────────┤
│       data/          │  ← Repository Impls, Data Sources, Mappers
│    (depends on ↑)    │
└─────────────────────┘
```

## Dependency Direction

**The dependency rule is absolute:** dependencies point inward toward `domain/`.

- `domain/` depends on **nothing** — no Android imports, no data layer imports, no presentation imports.
- `data/` depends on `domain/` (implements repository interfaces).
- `presentation/` depends on `domain/` (uses use cases, observes entities).
- `presentation/` **never** depends on `data/` directly.
- Cross-layer dependencies are wired via **Hilt** (bind interface in domain to implementation in data).

## Domain Layer (`domain/`)

### Rules
- **Pure Kotlin only** — no `android.*`, `androidx.*`, or framework imports.
- **No annotations from external frameworks** (except `@Inject` for use case constructors).
- Repository interfaces live here — implementations live in `data/`.
- Entities are plain Kotlin data classes or value classes.

### Use Cases
- One use case per business operation.
- Name pattern: `[Verb][Noun]UseCase` (e.g., `FetchUserUseCase`, `ValidateEmailUseCase`).
- Accept dependencies via constructor injection (`@Inject constructor`).
- Implement `operator fun invoke()` for clean call-site syntax.
- Return `Result<T>`, `Flow<T>`, or a sealed error type — never throw exceptions.

```kotlin
class FetchUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(id: String): Result<User> =
        userRepository.getUser(id)
}
```

### Repository Interfaces
- Define in `domain/repository/`.
- Return `Flow<T>` for observable data, `Result<T>` or sealed types for one-shot operations.
- Do not expose data-layer details (Room entities, Retrofit models, pagination tokens).

```kotlin
interface UserRepository {
    fun observeUsers(): Flow<List<User>>
    suspend fun getUser(id: String): Result<User>
    suspend fun saveUser(user: User): Result<Unit>
}
```

## Data Layer (`data/`)

### Rules
- Implements domain repository interfaces.
- Contains data sources (`local/`, `remote/`), repository implementations, and mappers.
- Data models (Room entities, API DTOs) live here — never leak into domain.
- **Map between data models and domain entities** using explicit mapper functions or extension functions in `mapper/`.

### Repository Implementations
- Live in `data/repository/`.
- Annotated with `@Inject constructor` and bound to the domain interface via Hilt module.
- Coordinate between local and remote data sources.
- Handle caching strategy, offline-first logic, and error mapping.

```kotlin
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource,
    private val mapper: UserMapper,
) : UserRepository {

    override fun observeUsers(): Flow<List<User>> =
        localDataSource.observeAll().map { entities ->
            entities.map(mapper::toDomain)
        }
}
```

### Mappers
- Explicit, testable mapping functions between layers.
- Name pattern: `[Entity]Mapper` with `toDomain()` and `toData()` / `toEntity()` / `toDto()` functions.
- Never use `copy()` across layer boundaries — always map explicitly.

## Presentation Layer (`presentation/`)

### Rules
- Contains ViewModels, UI state classes, Compose screens, components, navigation, and theme.
- ViewModels use use cases from `domain/` — never access repositories or data sources directly.
- Expose UI state via `StateFlow<UiState>` from ViewModel.

### ViewModel Pattern
- Annotated with `@HiltViewModel`.
- Accept use cases via `@Inject constructor`.
- Expose a single `StateFlow<XxxUiState>` for the screen's UI state.
- Handle user actions via public functions that update state.

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val fetchUserUseCase: FetchUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun loadUser(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            fetchUserUseCase(id)
                .onSuccess { user -> _uiState.update { it.copy(user = user, isLoading = false) } }
                .onFailure { error -> _uiState.update { it.copy(error = error.message, isLoading = false) } }
        }
    }
}
```

## Error Handling Across Layers

| Layer | Error Representation |
|-------|---------------------|
| Domain | Sealed class/interface or `Result<T>` |
| Data | Catch exceptions from APIs/DB → map to domain errors |
| Presentation | Map domain errors to user-facing strings in ViewModel |

- Data layer catches `IOException`, `HttpException`, etc. and maps to domain error types.
- Presentation layer maps domain errors to UI strings — domain never contains user-facing messages.

## Hilt Module Organization

- One module per architectural boundary: `DomainModule`, `DataModule`, `PresentationModule` (if needed).
- Bind domain interfaces to data implementations in `DataModule`.
- Use `@Binds` over `@Provides` when binding an interface to an implementation.
- Use `@Singleton` for repository implementations.
- Install modules in the appropriate component: `@InstallIn(SingletonComponent::class)` for app-scoped.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
```

## Common Agent Mistakes in Architecture

1. **ViewModel accessing data layer directly** — agents skip use cases and inject repositories into ViewModels.
2. **Domain entities with Android imports** — agents add `Parcelable` or `@Entity` to domain models.
3. **Leaking data models** — agents return Room entities or API DTOs from repository interfaces.
4. **Missing mappers** — agents reuse data models as domain entities instead of mapping.
5. **Business logic in ViewModel** — agents put validation/transformation logic in ViewModels instead of use cases.
6. **Circular dependencies** — agents create imports from domain to data or presentation to data.
