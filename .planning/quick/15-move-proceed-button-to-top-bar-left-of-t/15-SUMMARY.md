---
phase: quick-15
plan: "01"
subsystem: main-screen-ui
tags: [top-bar, proceed-button, layout, cleanup]
dependency_graph:
  requires: [quick-14]
  provides: [proceed-in-top-bar]
  affects: [MainScreen, MainTopBar, MainScreenTokens]
tech_stack:
  added: []
  patterns: [Button with pill shape in TopBar Row]
key_files:
  created: []
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTokens.kt
decisions:
  - "Proceed button moved into MainTopBar as Material3 Button composable; testTag preserved so existing test assertions require no changes"
  - "Button uses containerColor=proceedSurface, contentColor=Color.White (white text, not proceedText light-blue)"
  - "PaddingValues(horizontal=16.dp, vertical=6.dp) keeps button compact within 36.dp topBarHeight"
metrics:
  duration: "5 min"
  completed: "2026-03-16"
  tasks_completed: 2
  files_modified: 2
---

# Quick Task 15: Move Proceed Button to Top Bar Summary

**One-liner:** Pill-shaped Proceed Button inserted in MainTopBar Row (left of three-dot MoreVert), replacing the bottom-of-screen ProceedAffordance composable.

## What Was Done

Relocated the Proceed action from the bottom Column of MainScreen into the MainTopBar composable, positioned in a new right-side Row before the existing three-dot DropdownMenu. The bottom `ProceedAffordance` composable was deleted entirely. The `navigationBarsPadding` import and `proceedTopPadding` token were removed as they were exclusively used by the deleted composable.

## Tasks Completed

| Task | Description | Commit |
|------|-------------|--------|
| 1 | Move Proceed button to top bar, remove bottom affordance | 86afb95 |
| 2 | Verify MainScreenTest compiles — no test changes needed | cc6e933 |

## Deviations from Plan

None - plan executed exactly as written. The test file required no changes because `MainScreenTags.ProceedAffordance` testTag was kept on the new Button composable, making all existing tag-based assertions valid without modification.

## Checkpoint Pending

Task 3 is a `checkpoint:human-verify` gate. Visual verification is required:
1. Build and install: `./gradlew :app:installDebug`
2. Open app, grant media permission
3. Confirm top bar: "DtMF" (yellow, left) — [Proceed] (blue pill) — [⋮] (far right)
4. Proceed disabled when no photos staged, enabled after staging one
5. Tap Proceed navigates to Review screen
6. Bottom area shows only Undo and Skip buttons

## Self-Check

### Created files exist
- `.planning/quick/15-move-proceed-button-to-top-bar-left-of-t/15-SUMMARY.md` — this file

### Commits exist
- 86afb95 — feat(quick-15): move Proceed button to top bar, remove bottom ProceedAffordance
- cc6e933 — chore(quick-15): verify MainScreenTest compiles with moved Proceed button

## Self-Check: PASSED
