---
phase: quick-11
plan: "01"
subsystem: review
tags: [ui, review, cleanup, cta]
dependency_graph:
  requires: []
  provides: [single-cta-review-bottom-area]
  affects: [ReviewScreen, ReviewScreenTest]
tech_stack:
  added: []
  patterns: [single-centered-cta]
key_files:
  created: []
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt
    - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt
decisions:
  - Replaced Row with two weighted Box children with a single fillMaxWidth Box so Delete Forever spans the full CTA width
  - ReviewRoute.kt required no change — it already had no onDecideLater argument
metrics:
  duration: "3 min"
  completed: "2026-03-15"
---

# Quick Task 11: Remove Decide Later Button from Review Page Summary

**One-liner:** Removed the Decide Later CTA from ReviewScreen and centered the Delete Forever button as the sole full-width action in the bottom area.

## What Was Done

Simplified the review page bottom action area by removing the "No, I want to move to trash" (Decide Later) button and centering the remaining "Delete forever" button across the full width.

## Tasks Completed

| # | Task | Commit | Files |
|---|------|--------|-------|
| 1 | Remove Decide Later button and center Delete Forever in ReviewScreen | 4857c4f | ReviewScreen.kt |
| 2 | Remove decideLaterButtonIsDisplayed test from ReviewScreenTest | 6a7e7ab | ReviewScreenTest.kt |

## Changes Made

**ReviewScreen.kt:**
- Removed `DecideLaterButton` constant from `ReviewScreenTags` object
- Removed `onDecideLater: () -> Unit = {}` parameter from `ReviewScreen` composable signature
- Replaced the `Row` CTA block (two `Box` children with `weight(1f)`) with a single `Box` using `fillMaxWidth()` for the Delete Forever button
- Removed `onDecideLater` argument from the `BottomActionArea` call site
- Removed `onDecideLater` parameter from `BottomActionArea` composable signature
- All Decide Later token references (`T.DecideLaterColor`, `T.DecideLaterLabel`, `T.DecideLaterTextColor`) removed from `BottomActionArea`

**ReviewScreenTest.kt:**
- Removed the `decideLaterButtonIsDisplayed` test entirely

**ReviewRoute.kt:**
- No changes required — the route already had no `onDecideLater` argument in its `ReviewScreen` call

## Verification

- `./gradlew :app:compileDebugKotlin` — PASS
- `./gradlew :app:assembleDebug :app:assembleDebugAndroidTest` — PASS
- Grep confirms `DecideLaterButton` and `onDecideLater` are fully absent from production code

## Deviations from Plan

None — plan executed exactly as written.

## Self-Check: PASSED

- ReviewScreen.kt modified: FOUND
- ReviewScreenTest.kt modified: FOUND
- Commit 4857c4f: FOUND
- Commit 6a7e7ab: FOUND
- DecideLaterButton absent from production code: CONFIRMED
- onDecideLater absent from production code: CONFIRMED
