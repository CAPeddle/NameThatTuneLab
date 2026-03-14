---
applyTo: "**/ui/**/*.kt,**/composable/**/*.kt,**/component/**/*.kt,**/screen/**/*.kt,**/theme/**/*.kt"
---

# Compose Coding Standards

## Composable Function Design

- **Stateless by default.** Composables should receive state as parameters and emit events via callbacks.
- **State hoisting:** Lift state to the caller. The composable that owns the state should be the highest component that needs it.
- **Single responsibility:** Each composable does one thing. Extract sub-composables when a function exceeds ~40 lines.

```kotlin
// ✓ Stateless composable with hoisted state
@Composable
fun UserCard(
    user: UserUiState,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // UI only — no state management here
}

// ✓ Stateful wrapper (if needed)
@Composable
fun UserCardRoute(
    viewModel: UserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UserCard(
        user = uiState,
        onEditClick = viewModel::onEditClick,
    )
}
```

## Modifier Convention

- **Always accept `modifier: Modifier = Modifier` as the first optional parameter** (after required params).
- Apply the passed modifier to the root layout element.
- Chain modifiers in the caller, not inside the composable.
- Never apply `modifier` to a child element — only the root.

## Side Effects

- Use `LaunchedEffect` for coroutine-based side effects tied to composition.
- Use `DisposableEffect` for effects needing cleanup (listeners, callbacks).
- Use `SideEffect` for non-suspend side effects on every recomposition.
- Use `rememberUpdatedState` to capture the latest value in long-running effects.
- **Never call suspend functions directly in a composable body** — always use `LaunchedEffect`.
- Use `derivedStateOf` to avoid unnecessary recompositions.

```kotlin
// ✓ Correct side effect usage
@Composable
fun UserScreen(userId: String, viewModel: UserViewModel = hiltViewModel()) {
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }
}
```

## Material 3

- Use `MaterialTheme` tokens for colors, typography, and shapes — never hardcode.
- Use `Surface`, `Card`, `TopAppBar`, and other Material 3 components.
- Define custom theme in `theme/` package with `Theme.kt`, `Color.kt`, `Type.kt`, `Shape.kt`.
- Support dynamic color (`dynamicDarkColorScheme` / `dynamicLightColorScheme`) on Android 12+.

```kotlin
// ✓ Use theme tokens
Text(
    text = title,
    style = MaterialTheme.typography.headlineMedium,
    color = MaterialTheme.colorScheme.onSurface,
)
```

## Preview Functions

- **Every public composable must have at least one `@Preview` function.**
- Name previews descriptively: `Preview[ComposableName][Variant]`.
- Use `@PreviewLightDark` for light/dark theme coverage.
- Provide sample data via preview parameter providers or hardcoded preview data.
- Keep previews next to their composable (same file).

```kotlin
@Preview(showBackground = true)
@Composable
private fun PreviewUserCardLoading() {
    AppTheme {
        UserCard(
            user = UserUiState(isLoading = true),
            onEditClick = {},
        )
    }
}
```

## Accessibility

- Set `contentDescription` on all `Image` and `Icon` composables (use `null` for decorative elements).
- Use `semantics` modifier for custom accessibility information.
- Ensure touch targets are at least 48dp × 48dp.
- Test with TalkBack in mind — verify content descriptions make sense when read aloud.

## UI State Pattern

- Define UI state as a **data class** per screen: `data class XxxUiState(...)`.
- Expose state from ViewModel as `StateFlow<XxxUiState>`.
- Collect in composables with `collectAsStateWithLifecycle()`.
- Use a sealed interface for one-shot events (navigation, snackbar).

```kotlin
data class UserUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
)
```

## Compose UI Testing

- Use `createComposeRule()` for unit-style Compose tests.
- Use `onNodeWithText`, `onNodeWithContentDescription`, `onNodeWithTag` for finding nodes.
- Set `testTag` via `Modifier.testTag("tag")` for elements without visible text.
- Assert with `assertIsDisplayed()`, `assertTextEquals()`, `assertIsEnabled()`.
- Use `performClick()`, `performTextInput()` for interactions.

```kotlin
@Test
fun userCard_displaysUserName() {
    composeTestRule.setContent {
        UserCard(user = testUser, onEditClick = {})
    }
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
}
```

## Navigation

- Use Compose Navigation (`NavHost`, `composable`, `navigate`).
- Define routes as sealed classes or string constants in `navigation/` package.
- Pass arguments via navigation routes — not shared ViewModels.
- Handle deep links in the navigation graph.

## Common Agent Mistakes in Compose

1. **State in composable body** — agents declare `var state by remember { mutableStateOf(...) }` when state should be hoisted.
2. **Missing `modifier` parameter** — agents omit the `modifier: Modifier = Modifier` parameter.
3. **Hardcoded colors/dimensions** — agents use `Color(0xFF...)` instead of theme tokens.
4. **Missing previews** — agents forget to add `@Preview` functions.
5. **`collectAsState` instead of `collectAsStateWithLifecycle`** — agents use the non-lifecycle-aware variant.
6. **Side effects outside `LaunchedEffect`** — agents call suspend functions directly.
