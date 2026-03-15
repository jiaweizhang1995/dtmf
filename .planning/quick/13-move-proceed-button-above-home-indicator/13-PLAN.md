---
phase: quick-13
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
autonomous: true
requirements: []
must_haves:
  truths:
    - "Proceed button is fully visible above the Android home indicator / gesture navigation bar"
    - "No content is obscured by the bottom system UI on gesture-navigation devices"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt"
      provides: "navigationBarsPadding applied to MainScreen outer container"
      contains: "navigationBarsPadding"
  key_links:
    - from: "MainScreen BoxWithConstraints modifier"
      to: "Android navigation bar inset"
      via: "navigationBarsPadding()"
      pattern: "navigationBarsPadding"
---

<objective>
Apply `navigationBarsPadding()` to MainScreen's outer container so the Proceed button sits above the Android home indicator / gesture navigation bar.

Purpose: AppShell zeroes Scaffold insets (quick tasks 6/7), so each screen must self-consume window insets. Quick task 12 fixed the status bar with `statusBarsPadding()` using the same pattern — this task applies the equivalent fix to the bottom edge.

Output: MainScreen.kt with `navigationBarsPadding()` added to the BoxWithConstraints modifier chain, mirroring the existing `statusBarsPadding()` call.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add navigationBarsPadding to MainScreen outer container</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt</files>
  <action>
In `MainScreen`, locate the `BoxWithConstraints` modifier chain (around line 69). It currently reads:

```kotlin
modifier = modifier
    .background(MainScreenTokens.appBackground)
    .statusBarsPadding()
    .testTag(MainScreenTags.Root)
    ...
    .padding(horizontal = MainScreenTokens.screenPadding, vertical = 14.dp),
```

Add `navigationBarsPadding()` immediately after `statusBarsPadding()`:

```kotlin
modifier = modifier
    .background(MainScreenTokens.appBackground)
    .statusBarsPadding()
    .navigationBarsPadding()
    .testTag(MainScreenTags.Root)
    ...
    .padding(horizontal = MainScreenTokens.screenPadding, vertical = 14.dp),
```

Also add the import if not already present:
```kotlin
import androidx.compose.foundation.layout.navigationBarsPadding
```

Do NOT add a separate bottom padding — `navigationBarsPadding()` already provides the correct inset height for all Android navigation modes (gesture, 3-button, 2-button). This is the identical pattern used for `statusBarsPadding()` in quick task 12.
  </action>
  <verify>
    <automated>cd /Users/jimmymacmini/Desktop/codex-project/wish-dtmf && ./gradlew :app:compileDebugKotlin --quiet 2>&1 | tail -20</automated>
  </verify>
  <done>MainScreen.kt compiles with `navigationBarsPadding()` in the modifier chain; the Proceed button no longer overlaps the home indicator on gesture-navigation devices.</done>
</task>

<task type="checkpoint:human-verify" gate="blocking">
  <what-built>Added navigationBarsPadding() to MainScreen so the Proceed button clears the Android home indicator.</what-built>
  <how-to-verify>
    1. Build and install: `./gradlew :app:installDebug`
    2. Open the app on a device or emulator with gesture navigation enabled.
    3. Navigate to the main swipe screen.
    4. Confirm the blue Proceed button is fully visible above the home indicator bar — not overlapping it.
    5. Also confirm the top status bar area is still correctly inset (no regression from task 12).
  </how-to-verify>
  <resume-signal>Type "approved" or describe any remaining issues</resume-signal>
</task>

</tasks>

<verification>
- `./gradlew :app:compileDebugKotlin` exits 0 — no Kotlin errors
- `navigationBarsPadding` import and call present in MainScreen.kt
- `statusBarsPadding` call still present (no regression)
</verification>

<success_criteria>
Proceed button is fully visible above the Android navigation bar / home indicator on all Android navigation modes, with no regression to the top status bar inset applied in quick task 12.
</success_criteria>

<output>
After completion, create `.planning/quick/13-move-proceed-button-above-home-indicator/13-SUMMARY.md`
</output>
