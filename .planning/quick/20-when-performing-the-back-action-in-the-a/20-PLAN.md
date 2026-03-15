---
phase: quick-20
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainRoute.kt
autonomous: true
requirements: [QUICK-20]

must_haves:
  truths:
    - "Pressing Back on the MainScreen does NOT navigate to the Entry/LaunchSession screen"
    - "First back press shows a centered semi-transparent message '再次返回回到桌面' for 2 seconds"
    - "Second back press within 2 seconds exits the app entirely"
    - "If 2 seconds elapse without a second press the message disappears and the gesture resets"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainRoute.kt"
      provides: "BackHandler with double-back-press logic and overlay toast"
  key_links:
    - from: "MainRoute.kt BackHandler"
      to: "Activity.finishAffinity()"
      via: "LocalContext.current cast to Activity"
      pattern: "finishAffinity"
---

<objective>
Replace the default back-navigation behavior on the MainScreen (which currently pops to the Entry screen showing "Local Cleanup Tool" / "X images loaded") with a double-back-press-to-exit pattern. The first back press shows a centered semi-transparent Chinese message for 2 seconds; the second press within that window exits the app.

Purpose: Prevent the user from accidentally landing on the entry screen mid-session. The "exit on double-back" pattern is standard Android UX for the root screen of an app.
Output: Modified MainRoute.kt with BackHandler, coroutine timer, and overlay composable.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@.planning/ROADMAP.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add double-back-press-to-exit with overlay toast in MainRoute</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainRoute.kt</files>
  <action>
Modify MainRoute.kt to intercept all back presses on the MainScreen using BackHandler(enabled = true). The current behavior (Navigation Compose auto-pops to ENTRY_ROUTE) must be fully suppressed.

Implementation steps:

1. Add imports:
   - `androidx.activity.compose.BackHandler`
   - `androidx.compose.animation.AnimatedVisibility`
   - `androidx.compose.animation.fadeIn`
   - `androidx.compose.animation.fadeOut`
   - `androidx.compose.foundation.background`
   - `androidx.compose.foundation.layout.Box`
   - `androidx.compose.foundation.layout.fillMaxSize`
   - `androidx.compose.foundation.layout.padding`
   - `androidx.compose.foundation.shape.RoundedCornerShape`
   - `androidx.compose.material3.Text`
   - `androidx.compose.runtime.mutableStateOf`
   - `androidx.compose.runtime.remember`
   - `androidx.compose.runtime.rememberCoroutineScope`
   - `androidx.compose.ui.Alignment`
   - `androidx.compose.ui.graphics.Color`
   - `androidx.compose.ui.text.font.FontWeight`
   - `androidx.compose.ui.unit.dp`
   - `androidx.compose.ui.unit.sp`
   - `kotlinx.coroutines.delay`
   - `kotlinx.coroutines.launch`
   - `androidx.compose.ui.platform.LocalContext`
   - `androidx.activity.ComponentActivity`

2. Inside `MainRoute`, before the `MainScreen(...)` call, add:
   ```kotlin
   var showExitHint by remember { mutableStateOf(false) }
   val scope = rememberCoroutineScope()
   val context = LocalContext.current

   BackHandler(enabled = true) {
       if (showExitHint) {
           (context as? ComponentActivity)?.finishAffinity()
       } else {
           showExitHint = true
           scope.launch {
               delay(2000L)
               showExitHint = false
           }
       }
   }
   ```

3. Wrap the existing `MainScreen(...)` call in a `Box(modifier = Modifier.fillMaxSize())`:
   ```kotlin
   Box(modifier = modifier.fillMaxSize()) {
       MainScreen(
           uiState = uiState.value,
           onStageCurrentPhoto = viewModel::stageCurrentPhoto,
           onSkipCurrentPhoto = viewModel::skipCurrentPhoto,
           onUndoLastDecision = viewModel::undoLastDecision,
           onProceed = viewModel::onProceedToReview,
           // NOTE: do NOT pass modifier here — the outer Box owns layout
       )

       AnimatedVisibility(
           visible = showExitHint,
           modifier = Modifier.align(Alignment.Center),
           enter = fadeIn(),
           exit = fadeOut(),
       ) {
           Text(
               text = "再次返回回到桌面",
               color = Color.White,
               fontSize = 16.sp,
               fontWeight = FontWeight.Medium,
               modifier = Modifier
                   .background(
                       color = Color(0xCC000000),   // semi-transparent black (80% opacity)
                       shape = RoundedCornerShape(8.dp),
                   )
                   .padding(horizontal = 20.dp, vertical = 10.dp),
           )
       }
   }
   ```

4. Remove the `modifier` parameter from the inner `MainScreen` call since the outer `Box` now owns the full-size layout. The outer `Box` should use `modifier = modifier.fillMaxSize()`.

Important: The `BackHandler(enabled = true)` intercepts ALL back presses at this destination, preventing Navigation Compose from popping to ENTRY_ROUTE. This is intentional — MainScreen is the app's root working screen and back should exit, not return to entry.
  </action>
  <verify>
    <automated>./gradlew :app:compileDebugKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>
    - `./gradlew :app:compileDebugKotlin` completes with no errors
    - Pressing Back once on MainScreen shows the Chinese hint message centered on screen
    - Pressing Back a second time within 2 seconds exits the app
    - Waiting 2 seconds after the first press dismisses the message without navigating
    - The Entry ("Local Cleanup Tool") screen is never shown when pressing Back from MainScreen
  </done>
</task>

</tasks>

<verification>
Run `./gradlew :app:compileDebugKotlin` — must pass with zero errors.

Manual device check:
1. Launch app, reach MainScreen
2. Press Back once — centered semi-transparent message "再次返回回到桌面" appears
3. Wait 2 seconds — message fades out, app stays on MainScreen
4. Press Back again immediately after first press — app exits to Android home
5. Confirm the Entry screen ("Local Cleanup Tool") never appears at any point
</verification>

<success_criteria>
BackHandler intercepts all back presses on MainScreen. First press shows "再次返回回到桌面" overlay for 2 seconds. Second press within 2 seconds calls finishAffinity() to exit. Entry screen is never reachable via back navigation from MainScreen.
</success_criteria>

<output>
After completion, create `.planning/quick/20-when-performing-the-back-action-in-the-a/20-SUMMARY.md`
</output>
