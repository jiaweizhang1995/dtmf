---
phase: quick-4
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
  - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTest.kt
autonomous: true
requirements: []
must_haves:
  truths:
    - "The 'Organise into albums' banner row is no longer visible on the main screen"
    - "The dark-background 'Proceed' text (inside the banner row) is gone"
    - "The blue pill-shaped ProceedAffordance button is still present and functional"
    - "The existing instrumented tests compile and pass"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt"
      provides: "MainScreen without PremiumBannerRow"
      contains: "ProceedAffordance"
  key_links:
    - from: "MainScreen"
      to: "ProceedAffordance"
      via: "direct composable call, unchanged"
      pattern: "ProceedAffordance\\("
---

<objective>
Remove the "Organise into albums" banner row and its embedded "Proceed" text from MainScreen.kt, keeping only the blue ProceedAffordance button. Update the instrumented test that asserts on the now-deleted UI elements.

Purpose: The banner row (`PremiumBannerRow`) is a dark-background placeholder card that shows "Organise into albums" on the left and a dim grey "Proceed" text on the right. It looks like a black/dim proceed button and is no longer wanted. The real blue proceed affordance (`ProceedAffordance`) must remain intact.
Output: Cleaned MainScreen.kt, updated MainScreenTest.kt.
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
  <name>Task 1: Delete PremiumBannerRow from MainScreen</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt</files>
  <action>
    Make two deletions in MainScreen.kt:

    1. In the `MainScreen` composable body (around line 102), remove the call to `PremiumBannerRow()` — the entire line including any adjacent blank lines that were used purely for spacing around it.

    2. Delete the entire `PremiumBannerRow` private composable function definition (lines 278–302 in the current file), which is the Row that renders "Organise into albums" on the left and a dim "Proceed" text on the right using `MainScreenTokens.chromeSurface` background.

    3. In the `MainScreenTags` object, remove the `BannerRow` constant (`const val BannerRow = "main_banner_row"`). This is the only place it is defined; the only consumer is the test file which will be updated in Task 2.

    4. In `MainScreenTokens.kt`, the `footerRowHeight` token was used exclusively for the deleted banner row's height. Remove the line `val footerRowHeight = 42.dp` from `MainScreenTokens`. If it is also referenced elsewhere in the file or in other files, leave it; only delete it if it has no remaining usages (verify with a search before deleting).

    Do NOT touch `ProceedAffordance`, `ProceedAffordance`'s call site, or any tokens used by `ProceedAffordance`.
  </action>
  <verify>
    Build the debug variant:
    `./gradlew :app:compileDebugKotlin`
    Must complete with zero errors. Any "unresolved reference: BannerRow" or "unresolved reference: footerRowHeight" means those references were not fully cleaned up.
  </verify>
  <done>
    MainScreen no longer contains the PremiumBannerRow composable or its call site. The BannerRow tag constant is gone. Kotlin compilation succeeds.
  </done>
</task>

<task type="auto">
  <name>Task 2: Update MainScreenTest to remove deleted element assertions</name>
  <files>app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTest.kt</files>
  <action>
    Remove the two test assertions that refer to the now-deleted UI elements:

    1. Line `composeRule.onNodeWithTag(MainScreenTags.BannerRow).assertIsDisplayed()` — delete this line entirely.

    2. Line `composeRule.onNodeWithText("Organise into albums").assertIsDisplayed()` — delete this line entirely.

    Leave all other assertions untouched, including `composeRule.onNodeWithText("Proceed").assertIsDisplayed()` which asserts on the text inside ProceedAffordance (that composable still uses the word "Proceed").

    Do not add any new assertions.
  </action>
  <verify>
    Run the instrumented test suite for the main feature:
    `./gradlew :app:connectedDebugAndroidTest --tests "*.MainScreenTest"`
    All tests in MainScreenTest must pass. If a device/emulator is not available, verify compilation succeeds:
    `./gradlew :app:compileDebugAndroidTestKotlin`
  </verify>
  <done>
    MainScreenTest compiles cleanly with no references to MainScreenTags.BannerRow. All remaining assertions in the test pass.
  </done>
</task>

</tasks>

<verification>
After both tasks:
- `./gradlew :app:compileDebugKotlin` — zero errors
- `./gradlew :app:compileDebugAndroidTestKotlin` — zero errors
- Visually: MainScreen shows ThumbnailStrip, hero card, Undo/Skip buttons, and the blue ProceedAffordance pill — no dark banner row below the action buttons
</verification>

<success_criteria>
- "Organise into albums" text is gone from MainScreen
- The dark chromeSurface banner row with its dim "Proceed" text is removed
- The blue ProceedAffordance pill button (using proceedSurface color 0xFF123B57) is untouched and still interactive
- Kotlin compilation succeeds for both main and androidTest source sets
</success_criteria>

<output>
After completion, create `.planning/quick/4-remove-organise-into-albums-section-and-/4-SUMMARY.md`
</output>
