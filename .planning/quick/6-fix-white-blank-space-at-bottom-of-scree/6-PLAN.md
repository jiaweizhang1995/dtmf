---
phase: quick-6
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/MainActivity.kt
autonomous: true
requirements: [QUICK-6]

must_haves:
  truths:
    - "The Android navigation bar at the bottom of the screen is black, not white"
    - "The navigation bar background matches the app's dark background in both light and dark system modes"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/MainActivity.kt"
      provides: "enableEdgeToEdge call with NavigationBarStyle.Dark"
      contains: "NavigationBarStyle.Dark"
  key_links:
    - from: "MainActivity.kt"
      to: "system navigation bar"
      via: "enableEdgeToEdge(navigationBarStyle = NavigationBarStyle.Dark)"
      pattern: "NavigationBarStyle\\.Dark"
---

<objective>
Force the Android system navigation bar to render with a black/dark background instead of the default white scrim that appears in light-mode.

Purpose: The bottom white bar is caused by `enableEdgeToEdge()` defaulting to `NavigationBarStyle.Auto`, which lets Android choose a light scrim on light-mode devices. Passing `NavigationBarStyle.Dark` explicitly locks the nav bar to a dark transparent style regardless of system theme.
Output: `MainActivity.kt` updated so the navigation bar is visually black, matching the app's `Color(0xFF111111)` surface.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/PROJECT.md
@.planning/STATE.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: Force dark navigation bar style in MainActivity</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/MainActivity.kt</files>
  <action>
    Update the `enableEdgeToEdge()` call to explicitly pass `navigationBarStyle = NavigationBarStyle.Dark`.

    Current call (line 12):
    ```kotlin
    enableEdgeToEdge()
    ```

    Replace with:
    ```kotlin
    enableEdgeToEdge(
        navigationBarStyle = NavigationBarStyle.Dark,
    )
    ```

    Add the required import at the top of the file:
    ```kotlin
    import androidx.activity.SystemBarStyle
    ```
    Note: `NavigationBarStyle` is a nested object on `SystemBarStyle` — accessed as `SystemBarStyle.Dark` via the `NavigationBarStyle` alias that `enableEdgeToEdge`'s overload accepts. The actual import needed is:
    ```kotlin
    import androidx.activity.SystemBarStyle
    ```
    And the call becomes:
    ```kotlin
    enableEdgeToEdge(
        navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )
    ```

    Use the `SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)` form — this is the correct API that forces a fully transparent dark nav bar, revealing the app's black `Surface` behind it. Do NOT use `NavigationBarStyle` (that is not a real class).

    Final MainActivity.kt should have these imports added:
    - `import androidx.activity.SystemBarStyle`

    And `enableEdgeToEdge` called as:
    ```kotlin
    enableEdgeToEdge(
        navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )
    ```

    No other files need changing. The Scaffold already has `contentWindowInsets = WindowInsets(0)` and the Surface background is already `Color(0xFF111111)`.
  </action>
  <verify>
    <automated>cd /Users/jimmymacmini/Desktop/codex-project/wish-dtmf && ./gradlew :app:compileDebugKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>
    - `MainActivity.kt` compiles without errors
    - `enableEdgeToEdge` is called with `navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)`
    - Running the app on a light-mode device shows a black navigation bar at the bottom, not white
  </done>
</task>

</tasks>

<verification>
Run `./gradlew :app:compileDebugKotlin` — must complete with BUILD SUCCESSFUL and zero errors.
Install the debug APK on device and confirm the bottom navigation bar area is black/dark rather than white.
</verification>

<success_criteria>
The white bar at the bottom of the screen is replaced by a black/transparent navigation bar that blends with the app's dark surface. This must hold on a device in light system mode (where the bug was visible).
</success_criteria>

<output>
After completion, create `.planning/quick/6-fix-white-blank-space-at-bottom-of-scree/6-SUMMARY.md` with what was changed and the result.
</output>
