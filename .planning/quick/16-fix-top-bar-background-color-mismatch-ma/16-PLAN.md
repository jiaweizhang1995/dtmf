---
phase: quick-16
plan: 16
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
autonomous: true
requirements: []

must_haves:
  truths:
    - "MainTopBar background matches the screen background — no visible darker band at the top"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt"
      provides: "MainTopBar with corrected containerColor"
      contains: "MainScreenTokens.appBackground"
  key_links:
    - from: "MainTopBar Row modifier"
      to: "MainScreenTokens.appBackground"
      via: ".background(MainScreenTokens.appBackground)"
      pattern: "background\\(MainScreenTokens\\.appBackground\\)"
---

<objective>
Fix the top bar color mismatch in MainScreen.

The `MainTopBar` Row uses `.background(Color.Black)` (`#000000`) while the screen root uses
`.background(MainScreenTokens.appBackground)` (`#111111`). The result is a visibly darker band
at the top of the screen. Replace `Color.Black` with `MainScreenTokens.appBackground` so the
top bar blends seamlessly with the rest of the screen.

Purpose: Remove the jarring color discontinuity at the top of the main screen.
Output: Single-line change in MainScreen.kt; no new files.
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
  <name>Task 1: Replace Color.Black with MainScreenTokens.appBackground in MainTopBar</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt</files>
  <action>
    In `MainTopBar` (line ~173), change the Row modifier from:

        .background(Color.Black)

    to:

        .background(MainScreenTokens.appBackground)

    `MainScreenTokens.appBackground` is already imported (used on the root BoxWithConstraints
    above), so no new import is needed. Do not touch any other line.
  </action>
  <verify>
    <automated>grep -n "background(MainScreenTokens.appBackground)" app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt | wc -l</automated>
    Expect 2 matches (root BoxWithConstraints + MainTopBar Row). Also confirm no remaining
    `Color.Black` reference in the file:
    <automated>grep -n "Color.Black" app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt</automated>
    Expect zero matches.
  </verify>
  <done>MainTopBar Row background is MainScreenTokens.appBackground; Color.Black no longer
  appears in MainScreen.kt; project builds without errors.</done>
</task>

<task type="checkpoint:human-verify" gate="blocking">
  <what-built>Changed MainTopBar background from Color.Black (#000000) to MainScreenTokens.appBackground (#111111).</what-built>
  <how-to-verify>
    1. Build and run the app on a device or emulator.
    2. Open the main swipe screen.
    3. Confirm the top bar ("DtMF" title + Proceed button + three-dot icon) is the same shade
       as the rest of the dark background — no visible darker stripe at the top.
  </how-to-verify>
  <resume-signal>Type "approved" if the bar blends in, or describe the remaining issue.</resume-signal>
</task>

</tasks>

<verification>
- `grep "Color.Black" MainScreen.kt` returns no matches
- `grep "MainScreenTokens.appBackground" MainScreen.kt` returns exactly 2 matches
- App builds cleanly (`./gradlew assembleDebug`)
</verification>

<success_criteria>
Top bar is visually indistinguishable from the screen background; no darker band visible at the top of the main screen.
</success_criteria>

<output>
After completion, create `.planning/quick/16-fix-top-bar-background-color-mismatch-ma/16-SUMMARY.md`
</output>
