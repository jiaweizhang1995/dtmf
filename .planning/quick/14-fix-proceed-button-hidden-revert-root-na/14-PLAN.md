---
phase: quick-14
plan: 14
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
autonomous: true
requirements: [QUICK-14]
must_haves:
  truths:
    - "Proceed button is visible on screen above the home indicator"
    - "Overall screen layout is not clipped or shrunk by navigation bar inset"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt"
      provides: "Fixed inset application — root has no navigationBarsPadding, ProceedAffordance Column has navigationBarsPadding"
  key_links:
    - from: "BoxWithConstraints modifier chain"
      to: "ProceedAffordance Column modifier"
      via: "navigationBarsPadding moved from root to button wrapper"
      pattern: "navigationBarsPadding"
---

<objective>
Revert `navigationBarsPadding()` from the root `BoxWithConstraints` in `MainScreen.kt` and instead apply it directly to the `ProceedAffordance` Column so the Proceed button lifts above the home indicator without shrinking the overall layout height.

Purpose: Quick task 13 applied `navigationBarsPadding()` to the root container which reduced the available height for the Column's children and caused the Proceed button to be pushed off-screen. The inset must be consumed only at the button level.
Output: `MainScreen.kt` with corrected inset placement — Proceed button visible above the home indicator.
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
  <name>Task 1: Move navigationBarsPadding from root container to ProceedAffordance</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt</files>
  <action>
    In `MainScreen.kt`, make exactly two targeted changes:

    1. Remove `.navigationBarsPadding()` from the `BoxWithConstraints` modifier chain (currently line 73, between `.statusBarsPadding()` and `.testTag(MainScreenTags.Root)`).
       The modifier chain should read: `.background(...).statusBarsPadding().testTag(...).semantics {...}.padding(...)`.
       Do NOT remove `.statusBarsPadding()` — that stays on the root.

    2. Add `.navigationBarsPadding()` to the `ProceedAffordance` composable's Column modifier, appended after `.padding(top = MainScreenTokens.proceedTopPadding)`.
       The modifier should read: `Modifier.fillMaxWidth().padding(top = MainScreenTokens.proceedTopPadding).navigationBarsPadding().semantics { ... }`.

    No other changes. The import `androidx.compose.foundation.layout.navigationBarsPadding` is already present — do not add a duplicate import.
  </action>
  <verify>
    <automated>./gradlew :app:compileDebugKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>
    Compilation succeeds with no errors. `navigationBarsPadding()` appears exactly once in MainScreen.kt, on the ProceedAffordance Column modifier. The root BoxWithConstraints modifier chain has no `navigationBarsPadding()` call.
  </done>
</task>

<task type="checkpoint:human-verify" gate="blocking">
  <what-built>Moved navigationBarsPadding from root BoxWithConstraints to ProceedAffordance Column. Root layout height is no longer reduced by the nav bar inset; only the Proceed button gains bottom padding to clear the home indicator.</what-built>
  <how-to-verify>
    1. Build and run on a device/emulator with gesture navigation (home indicator visible).
    2. Confirm the Proceed button is fully visible above the home indicator — not hidden off-screen.
    3. Confirm the rest of the layout (hero card, thumbnails, action buttons) is not clipped or shifted.
    4. Confirm that the Proceed button bottom edge does not overlap the home indicator.
  </how-to-verify>
  <resume-signal>Type "approved" if the button is visible and correct, or describe the issue.</resume-signal>
</task>

</tasks>

<verification>
- `./gradlew :app:compileDebugKotlin` exits 0
- `navigationBarsPadding()` appears exactly once in MainScreen.kt
- Root `BoxWithConstraints` modifier chain does NOT contain `navigationBarsPadding()`
- `ProceedAffordance` Column modifier DOES contain `navigationBarsPadding()`
</verification>

<success_criteria>
Proceed button is fully visible on screen, lifts above the Android home indicator, and the overall layout is not clipped. Only one line added and one line removed in MainScreen.kt.
</success_criteria>

<output>
After completion, create `.planning/quick/14-fix-proceed-button-hidden-revert-root-na/14-SUMMARY.md`
</output>
