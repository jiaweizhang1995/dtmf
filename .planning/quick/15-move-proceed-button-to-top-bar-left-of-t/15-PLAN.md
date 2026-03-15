---
phase: quick-15
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTokens.kt
  - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTest.kt
autonomous: false
requirements: [quick-15]

must_haves:
  truths:
    - "Top bar shows: DtMF title (left), Proceed button (right side, before three-dot), three-dot menu (far right)"
    - "Proceed button has blue background and white text, pill/rounded shape"
    - "Proceed button is enabled only when canProceed is true"
    - "No Proceed button exists at the bottom of the screen"
    - "navigationBarsPadding added in tasks 13/14 is removed; statusBarsPadding from task 12 remains"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt"
      provides: "Updated MainTopBar with inline Proceed button; ProceedAffordance composable deleted"
  key_links:
    - from: "MainTopBar"
      to: "onProceed callback"
      via: "canProceed + onProceed params threaded into MainTopBar"
---

<objective>
Move the Proceed action from the bottom of the screen into the top bar, left of the three-dot menu. Remove the bottom ProceedAffordance composable and revert the navigationBarsPadding changes that were added specifically for it.

Purpose: Cleaner bottom area; Proceed action is always visible in the top bar without consuming vertical space.
Output: Updated MainScreen.kt with the Proceed button in the top bar and all bottom-proceed dead code removed.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md

Key facts extracted from codebase:

- `MainScreen` passes `onProceed: () -> Unit` and `uiState.canProceed: Boolean` down to `ProceedAffordance`.
- `MainTopBar` currently takes only `showThumbnails` and `onToggleThumbnails`.
- `ProceedAffordance` uses `MainScreenTokens.proceedSurface = Color(0xFF123B57)` (dark blue) and `MainScreenTokens.proceedText = Color(0xFF8CCEFF)` (light blue). Per the task the button must have blue background and WHITE text — use a new/adjusted color for text or reuse existing tokens appropriately.
- `navigationBarsPadding()` appears only in `ProceedAffordance` Column modifier and in the import list (line 17). `statusBarsPadding()` on the root BoxWithConstraints modifier (line 72) must be kept.
- `MainScreenTokens.proceedTopPadding` is used only in `ProceedAffordance` — can be removed.
- Tests reference `MainScreenTags.ProceedAffordance` and `onNodeWithText("Proceed")` — these must be updated to find the button in its new location or removed/adjusted.
</context>

<tasks>

<task type="auto">
  <name>Task 1: Move Proceed button to top bar, remove bottom affordance</name>
  <files>
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTokens.kt
  </files>
  <action>
**MainScreen.kt changes:**

1. Add `canProceed: Boolean` and `onProceed: () -> Unit` parameters to `MainTopBar`.

2. Inside `MainTopBar`, update the right-side Row/Box so the layout is:
   - "DtMF" title stays on the left.
   - Right side becomes a `Row` with `verticalAlignment = Alignment.CenterVertically`:
     - First child: the Proceed button (see below).
     - Second child: the existing `Box` containing the `IconButton` + `DropdownMenu`.

3. The Proceed button inside the top bar:
   - Use a `Button` composable with `shape = RoundedCornerShape(999.dp)` (pill shape).
   - `colors = ButtonDefaults.buttonColors(containerColor = MainScreenTokens.proceedSurface, contentColor = Color.White)`.
   - `enabled = canProceed`, `onClick = onProceed`.
   - Label text: `"Proceed"`, `fontWeight = FontWeight.SemiBold`.
   - Keep the `testTag(MainScreenTags.ProceedAffordance)` on this Button so tests still find it.
   - `contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)` to keep it compact next to the three-dot icon.

4. Thread `canProceed` and `onProceed` from `MainScreen` into the `MainTopBar(...)` call at line ~90.

5. Remove the `ProceedAffordance(...)` call from the Column in `MainScreen` (lines ~119-122).

6. Delete the entire `ProceedAffordance` private composable function (lines ~321-355).

7. Remove the `navigationBarsPadding` import (line 17) — it is no longer referenced after step 6. Keep `statusBarsPadding` import (line 18).

**MainScreenTokens.kt changes:**

8. Remove `val proceedTopPadding = 8.dp` — no longer referenced.
   Keep `proceedText` and `proceedSurface` (still used by the top-bar button).
  </action>
  <verify>
    <automated>./gradlew :app:assembleDebug 2>&1 | tail -20</automated>
  </verify>
  <done>Build succeeds with no unresolved references. `ProceedAffordance` composable no longer exists. `navigationBarsPadding` is not imported. `MainTopBar` accepts canProceed + onProceed and renders the Proceed button left of the three-dot icon.</done>
</task>

<task type="auto">
  <name>Task 2: Update MainScreenTest for new Proceed button location</name>
  <files>
    app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTest.kt
  </files>
  <action>
The tests reference `MainScreenTags.ProceedAffordance` to assert the button's existence and enabled state. The tag is preserved on the new top-bar Button (Task 1 step 3), so tag-based assertions still resolve.

Review each Proceed-related test assertion and fix any that break due to the layout change:

- `assertIsDisplayed()` on `ProceedAffordance` tag — should still pass since the button is still in the layout.
- `assertIsNotEnabled()` / `assertIsEnabled()` — should still pass because `enabled = canProceed` is wired.
- `performClick()` test — should still pass.
- `onNodeWithText("Proceed")` assertions — still valid since the button text is unchanged.

If any test previously asserted the button is in the bottom area (e.g., checking position or that it appears after the BottomActionRow) — remove or rewrite those assertions. The button is now in the top bar.

Run `./gradlew :app:connectedDebugAndroidTest` locally is not required; build success from Task 1 is sufficient automated gate. Ensure there are no compile errors in the test file.
  </action>
  <verify>
    <automated>./gradlew :app:assembleDebugAndroidTest 2>&1 | tail -20</automated>
  </verify>
  <done>Test module compiles without errors. No Proceed-related test assertions reference removed composable structure.</done>
</task>

<task type="checkpoint:human-verify" gate="blocking">
  <what-built>
    Proceed button moved to top bar (left of three-dot MoreVert icon). Blue background, white text, pill shape. Bottom ProceedAffordance removed. navigationBarsPadding dead code removed. Build and test module both compile clean.
  </what-built>
  <how-to-verify>
    1. Build and install: `./gradlew :app:installDebug`
    2. Open the app and grant media permission.
    3. Confirm top bar shows: "DtMF" (yellow, left) — [Proceed] (blue pill, right) — [⋮] (far right).
    4. Proceed button should appear dimmed/disabled when no photos are staged.
    5. Stage at least one photo (swipe left). Proceed button should become fully opaque/enabled.
    6. Tap Proceed — should navigate to the Review screen.
    7. Confirm the bottom area shows only Undo and Skip buttons — no Proceed button below them.
  </how-to-verify>
  <resume-signal>Type "approved" or describe any visual issues.</resume-signal>
</task>

</tasks>

<verification>
- `./gradlew :app:assembleDebug` passes (no compile errors).
- `./gradlew :app:assembleDebugAndroidTest` passes (test module compiles).
- Visual: Proceed appears in top bar, not at the bottom.
</verification>

<success_criteria>
Top bar contains the Proceed button left of the three-dot menu. Button has blue background and white text. Bottom screen no longer has a Proceed button. navigationBarsPadding removed. statusBarsPadding intact. Build and test module compile cleanly.
</success_criteria>

<output>
After completion, create `.planning/quick/15-move-proceed-button-to-top-bar-left-of-t/15-SUMMARY.md`
</output>
