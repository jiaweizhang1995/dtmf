---
phase: quick-12
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreen.kt
autonomous: true
requirements: [QUICK-12]
must_haves:
  truths:
    - "Back button on ReviewScreen is fully visible and not hidden behind the status bar"
    - "DtMF title and More options icon on MainScreen are fully visible below the status bar"
    - "EntryScreen content is not obscured by the status bar"
    - "No screen has UI clipped or overlapped by the system status bar on any Android API level"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt"
      provides: "ReviewScreen with statusBarsPadding on root Column"
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt"
      provides: "MainScreen with statusBarsPadding on root BoxWithConstraints"
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreen.kt"
      provides: "EntryScreen with statusBarsPadding on root Column"
  key_links:
    - from: "MainActivity"
      to: "each screen root"
      via: "enableEdgeToEdge() already active — screens must consume WindowInsets.statusBars"
      pattern: "statusBarsPadding"
---

<objective>
Fix the back button obstruction on ReviewScreen and apply consistent status-bar inset handling app-wide so no UI element on any screen is hidden behind the system status bar.

Purpose: enableEdgeToEdge() is already active in MainActivity, which means content draws edge-to-edge but screens must consume WindowInsets.statusBars themselves. Currently all screens use fixed pixel/dp padding that is insufficient to clear the status bar.

Output: Three screen files updated with statusBarsPadding() at their root layout. No visual style changes — correct inset handling only.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/PROJECT.md
@.planning/STATE.md

Key facts for the executor:
- MainActivity calls enableEdgeToEdge() — content draws behind status bar and nav bar by design.
- AppShell sets contentWindowInsets = WindowInsets(0) on the root Scaffold, which suppresses all automatic inset padding from Scaffold. This was intentional (quick task 6/7 fixed the nav bar white gap). Do NOT change AppShell.
- Because innerPadding from AppShell is zero, each screen must self-manage insets.
- The correct Compose modifier is statusBarsPadding() — imported from androidx.compose.foundation.layout.
- Do NOT use windowInsetsPadding(WindowInsets.statusBars) unless statusBarsPadding() is unavailable — they are equivalent but statusBarsPadding() is simpler.
- Do NOT change ReviewScreenTokens.AppBarTopPadding — it controls spacing between the app bar top edge and its content AFTER the status bar is already cleared. Keep the token as-is; it adds inner padding on top of the system inset, which is fine.
- The nav bar bottom inset is already handled (transparent nav bar + existing bottom padding values). Only the STATUS BAR top inset needs to be fixed.
</context>

<tasks>

<task type="auto">
  <name>Task 1: Apply statusBarsPadding to ReviewScreen and MainScreen</name>
  <files>
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
  </files>
  <action>
    ReviewScreen.kt:
    - Add import: androidx.compose.foundation.layout.statusBarsPadding
    - In the ReviewScreen composable, locate the root Column modifier chain. After .fillMaxSize() and .background(T.BackgroundColor), add .statusBarsPadding(). The modifier chain should read:
        Modifier
            .fillMaxSize()
            .background(T.BackgroundColor)
            .statusBarsPadding()
            .semantics { ... }
            .testTag(ReviewScreenTags.Root)
    - No other changes to ReviewScreen.kt.

    MainScreen.kt:
    - Add import: androidx.compose.foundation.layout.statusBarsPadding
    - In the MainScreen composable, locate the root BoxWithConstraints modifier chain. After .background(MainScreenTokens.appBackground), add .statusBarsPadding(). The modifier chain should read:
        modifier
            .background(MainScreenTokens.appBackground)
            .statusBarsPadding()
            .testTag(MainScreenTags.Root)
            .semantics { ... }
            .padding(horizontal = MainScreenTokens.screenPadding, vertical = 14.dp)
    - No other changes to MainScreen.kt.
  </action>
  <verify>
    <automated>./gradlew :app:compileDebugKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>Both files compile cleanly. statusBarsPadding() appears in the root modifier of ReviewScreen's Column and MainScreen's BoxWithConstraints.</done>
</task>

<task type="auto">
  <name>Task 2: Apply statusBarsPadding to EntryScreen and run full build check</name>
  <files>
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreen.kt
  </files>
  <action>
    EntryScreen.kt:
    - Add import: androidx.compose.foundation.layout.statusBarsPadding
    - In the EntryScreen composable, locate the root Column modifier chain. After .fillMaxSize() and .padding(24.dp), add .statusBarsPadding(). Place statusBarsPadding() BEFORE the static padding so the system inset is resolved first, then inner padding is applied on top. The modifier chain should read:
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    - No other changes to EntryScreen.kt.

    After editing all three files, run the full debug build to confirm no regressions:
        ./gradlew :app:assembleDebug
  </action>
  <verify>
    <automated>./gradlew :app:assembleDebug 2>&1 | tail -30</automated>
  </verify>
  <done>BUILD SUCCESSFUL. All three screens have statusBarsPadding() in their root modifier. The APK builds without errors or warnings related to insets.</done>
</task>

<task type="checkpoint:human-verify" gate="blocking">
  <what-built>Added statusBarsPadding() to the root layout of ReviewScreen, MainScreen, and EntryScreen so all screen content sits below the system status bar. enableEdgeToEdge() remains active. AppShell is unchanged.</what-built>
  <how-to-verify>
    Install the debug APK on a device or emulator:
        ./gradlew :app:installDebug

    1. Open the app. On the main screen: confirm the "DtMF" title and the three-dot menu icon are fully visible below the status bar — not hidden or clipped by it.
    2. Stage at least one photo and tap Proceed to open the review screen.
    3. On the review screen: confirm the back arrow ("<") and "REVIEW" title are fully visible below the status bar — not hidden behind it.
    4. Confirm no white bar or unexpected gap appears at the bottom of any screen.
    5. Rotate to landscape and confirm the top bar content still clears the status bar.
  </how-to-verify>
  <resume-signal>Type "approved" if the back button and top bars are clear of the status bar on all screens, or describe any remaining obstruction.</resume-signal>
</task>

</tasks>

<verification>
- ./gradlew :app:assembleDebug passes (BUILD SUCCESSFUL)
- ReviewScreen root Column has .statusBarsPadding() in its modifier chain
- MainScreen root BoxWithConstraints has .statusBarsPadding() in its modifier chain
- EntryScreen root Column has .statusBarsPadding() in its modifier chain
- AppShell.kt is UNCHANGED (contentWindowInsets = WindowInsets(0) stays)
- No new imports other than statusBarsPadding from androidx.compose.foundation.layout
</verification>

<success_criteria>
Back button on ReviewScreen fully visible below status bar. MainScreen top bar (DtMF title + menu icon) fully visible below status bar. EntryScreen content not clipped by status bar. All three screens self-consume WindowInsets.statusBars via statusBarsPadding(). Bottom nav bar gap fix from quick task 6/7 unaffected.
</success_criteria>

<output>
After completion, create `.planning/quick/12-fix-top-bar-system-insets-back-button-ob/12-SUMMARY.md`
</output>
