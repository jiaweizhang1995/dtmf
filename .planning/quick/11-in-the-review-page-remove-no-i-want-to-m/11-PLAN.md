---
phase: quick-11
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt
  - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt
autonomous: true
requirements: [QUICK-11]

must_haves:
  truths:
    - "Review page bottom area shows only the Delete Forever button"
    - "Delete Forever button is horizontally centered"
    - "No Decide Later button exists anywhere on the review screen"
    - "All dead code for Decide Later is removed"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt"
      provides: "Updated BottomActionArea with single centered Delete Forever button"
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt"
      provides: "ReviewRoute without onDecideLater callback"
    - path: "app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt"
      provides: "Updated tests — decideLaterButtonIsDisplayed removed"
  key_links:
    - from: "ReviewScreen.kt BottomActionArea"
      to: "ReviewScreenTags.DeleteForeverButton"
      via: "single Box with fillMaxWidth"
      pattern: "fillMaxWidth.*DeleteForeverButton"
---

<objective>
Remove the "No, I want to move to trash" (Decide Later) button from the review page and center the remaining "Delete forever" button.

Purpose: Simplify the review page CTA area so the only action is the irreversible delete.
Output: ReviewScreen.kt with single centered Delete Forever button; dead code and stale tests removed.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@.planning/quick/11-in-the-review-page-remove-no-i-want-to-m/11-CONTEXT.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: Remove Decide Later button and center Delete Forever in ReviewScreen</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt</files>
  <action>
    In `ReviewScreen.kt` make the following changes:

    1. `ReviewScreenTags` object — remove the `DecideLaterButton` constant entirely. Keep all other tags.

    2. `ReviewScreen` composable signature — remove the `onDecideLater: () -> Unit = {}` parameter.

    3. `BottomActionArea` composable:
       - Remove the `onDecideLater: () -> Unit` parameter from the signature.
       - Replace the `Row` CTA block (which contains two `Box` children with `weight(1f)` each) with a single `Box` that fills the full width (`fillMaxWidth()`) and is horizontally centered. Use `Modifier.fillMaxWidth()` on the Box so the button spans the same width as the helper text above it. Keep all other modifiers on the Delete Forever box (corner radius, background color logic, clickable, padding, testTag, semantics) unchanged.
       - Remove all Decide Later-specific tokens references: `T.DecideLaterColor`, `T.DecideLaterLabel`, `T.DecideLaterTextColor` are no longer referenced in this composable.

    4. `BottomActionArea` call site inside `ReviewScreen` — remove the `onDecideLater = onDecideLater` argument.

    The resulting `BottomActionArea` renders:
    ```
    Column {
        Text (helper text)
        Box (fillMaxWidth, centered — Delete Forever button)
    }
    ```
  </action>
  <verify>
    <automated>./gradlew :app:compileDebugKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>ReviewScreen.kt compiles without errors; BottomActionArea has no Decide Later code; Delete Forever button fills full width.</done>
</task>

<task type="auto">
  <name>Task 2: Remove onDecideLater from ReviewRoute and update failing tests</name>
  <files>
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt
    app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt
  </files>
  <action>
    **ReviewRoute.kt:**
    - Remove the `onDecideLater = ...` named argument from the `ReviewScreen(...)` call at the bottom of the file. No other changes needed — the route never had an `onDecideLater` callback wired to anything meaningful.

    **ReviewScreenTest.kt:**
    - Remove the `decideLaterButtonIsDisplayed` test entirely (lines 152-159 — the test that calls `assertIsDisplayed()` on `ReviewScreenTags.DecideLaterButton`).
    - No other test changes are required; `DecideLaterButton` tag is not referenced anywhere else in the test file.
    - The `setReviewScreen` helper does not pass `onDecideLater` so its signature needs no update.

    Do not modify any other test or source file.
  </action>
  <verify>
    <automated>./gradlew :app:assembleDebug :app:assembleDebugAndroidTest 2>&1 | tail -20</automated>
  </verify>
  <done>Full debug + androidTest build passes with no compilation errors. The `decideLaterButtonIsDisplayed` test no longer exists. The `ReviewRoute` call to `ReviewScreen` has no `onDecideLater` argument.</done>
</task>

</tasks>

<verification>
After both tasks:
- `./gradlew :app:assembleDebug` succeeds.
- `./gradlew :app:assembleDebugAndroidTest` succeeds.
- Grep confirms `DecideLaterButton` tag and `onDecideLater` parameter are fully absent from production code.
</verification>

<success_criteria>
- Only one CTA button appears in the review page bottom area — Delete Forever — and it is horizontally centered.
- All Decide Later dead code (tag constant, parameter, Row layout, test) is removed.
- Build is green.
</success_criteria>

<output>
After completion, create `.planning/quick/11-in-the-review-page-remove-no-i-want-to-m/11-SUMMARY.md`
</output>
