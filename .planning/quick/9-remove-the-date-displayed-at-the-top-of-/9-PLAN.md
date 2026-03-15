---
phase: quick-9
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainUiState.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/PhotoPresentationMapper.kt
  - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTest.kt
autonomous: true
requirements: [QUICK-9]
must_haves:
  truths:
    - "No date text appears at the top of the main screen"
    - "The app compiles and all existing tests pass"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt"
      provides: "Main screen without top bar"
      contains: "no MainTopBar call"
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainUiState.kt"
      provides: "Cleaned MainUiState without title field"
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/PhotoPresentationMapper.kt"
      provides: "Mapper without buildTitle and title field"
  key_links:
    - from: "MainScreen.kt Column"
      to: "ThumbnailStrip (was second child, now first)"
      via: "direct Column children"
---

<objective>
Remove the date label ("Mar 2026") displayed at the top of the main screen.

Purpose: The date display is unwanted UI that takes up vertical space and shows irrelevant metadata.
Output: MainTopBar composable deleted, title field removed from state/mapper, test updated.
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
  <name>Task 1: Remove MainTopBar from MainScreen and delete the composable</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt</files>
  <action>
    Make the following changes to MainScreen.kt:

    1. In the `MainScreen` composable Column body (around line 76), delete the line:
       `MainTopBar(title = uiState.title)`

    2. Delete the entire `MainTopBar` private composable function (lines 143-159 inclusive):
       ```
       @Composable
       private fun MainTopBar(title: String) { ... }
       ```

    3. Delete the `TopBar` constant from `MainScreenTags` (line 35):
       `const val TopBar = "main_top_bar"`
       The `TopBar` entry is no longer referenced anywhere once the composable is gone.

    4. Review imports at the top of the file — after removing `MainTopBar`, `height` from `androidx.compose.foundation.layout.height` may become unused if nothing else uses it. Check: `ThumbnailStrip` and other composables still use `height`. Keep all imports that remain referenced. Remove only genuinely unused imports (the compiler/IDE will flag them).

    Do NOT change any other composable or logic.
  </action>
  <verify>
    <automated>./gradlew :app:compileDebugKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>MainScreen.kt compiles with no reference to MainTopBar or TopBar tag. The Column's first child is now ThumbnailStrip.</done>
</task>

<task type="auto">
  <name>Task 2: Remove title field from MainUiState, MainPresentationState, and PhotoPresentationMapper</name>
  <files>
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainUiState.kt
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/PhotoPresentationMapper.kt
  </files>
  <action>
    In MainUiState.kt:
    1. Remove `val title: String,` from the `MainUiState` data class constructor (line 8).
    2. In `MainUiState.fromPresentation`, remove the `title = presentation.title,` line from the `MainUiState(...)` constructor call (line 45).

    In PhotoPresentationMapper.kt:
    1. Remove `val title: String,` from the `MainPresentationState` data class constructor (line 11).
    2. In `PhotoPresentationMapper.map`, remove `title = buildTitle(sourcePhoto),` from the `MainPresentationState(...)` constructor call (line 48).
    3. Delete the entire `buildTitle` private function (lines 62-71 inclusive):
       ```
       private fun buildTitle(photo: LocalPhoto): String { ... }
       ```
    4. Remove unused imports that `buildTitle` was the sole consumer of:
       - `import java.text.SimpleDateFormat`
       - `import java.util.Date`
       Only remove these if nothing else in the file uses them (they are only used by `buildTitle`).
    5. `import com.jimmymacmini.wishdtmf.data.media.LocalPhoto` — verify it is still needed by `PhotoPresentationMapper.map` (it is, via `sourcePhoto: LocalPhoto`). Keep it.

    After edits, run the full project compile to confirm no remaining references.
  </action>
  <verify>
    <automated>./gradlew :app:compileDebugKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>Neither MainUiState nor MainPresentationState contains a title field. buildTitle is deleted. Project compiles cleanly.</done>
</task>

<task type="auto">
  <name>Task 3: Update MainScreenTest to remove TopBar assertion</name>
  <files>app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTest.kt</files>
  <action>
    In `readyStateShowsMainScreenSectionsAndAffordances` (around line 45), delete this single assertion line:
    ```kotlin
    composeRule.onNodeWithTag(MainScreenTags.TopBar).assertIsDisplayed()
    ```

    No other changes — all remaining assertions remain valid. The `MainScreenTags.TopBar` constant was also deleted from MainScreen.kt in Task 1, so the reference must be removed here to allow compilation.

    After the edit, run the instrumented tests to confirm all pass.
  </action>
  <verify>
    <automated>./gradlew :app:connectedDebugAndroidTest 2>&1 | tail -30</automated>
  </verify>
  <done>MainScreenTest compiles and all tests pass. No assertion references MainScreenTags.TopBar.</done>
</task>

</tasks>

<verification>
After all tasks complete:
- `./gradlew :app:compileDebugKotlin` — zero errors
- No occurrences of `MainTopBar`, `buildTitle`, `.title` (in main feature files), `TopBar` constant remain in the feature/main package
- The main screen renders with ThumbnailStrip as the topmost visible element
</verification>

<success_criteria>
- The date label no longer appears at the top of the main screen
- All instrumented tests pass
- No dead code remains (no title field, no buildTitle, no MainTopBar composable, no TopBar tag constant)
</success_criteria>

<output>
After completion, create `.planning/quick/9-remove-the-date-displayed-at-the-top-of-/9-SUMMARY.md`
</output>
