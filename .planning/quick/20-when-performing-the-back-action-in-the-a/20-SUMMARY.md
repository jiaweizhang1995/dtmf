---
phase: quick-20
plan: 01
subsystem: navigation
tags: [back-navigation, android-ux, compose, backhandler]
dependency_graph:
  requires: []
  provides: [double-back-press-to-exit, exit-hint-overlay]
  affects: [MainRoute.kt]
tech_stack:
  added: []
  patterns: [BackHandler, AnimatedVisibility, finishAffinity, coroutine-timer]
key_files:
  created: []
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainRoute.kt
decisions:
  - BackHandler(enabled = true) intercepts all back presses at MainScreen, preventing Navigation Compose from popping to ENTRY_ROUTE
  - finishAffinity() used to exit app cleanly on double back press
  - showExitHint state with 2-second coroutine delay manages the double-press window
metrics:
  duration: "3 min"
  completed_date: "2026-03-16"
  tasks_completed: 1
  files_modified: 1
---

# Phase quick-20 Plan 01: Double-Back-Press-to-Exit Summary

**One-liner:** BackHandler with 2-second double-press window showing Chinese exit hint overlay, calling finishAffinity() on confirm.

## What Was Built

Added double-back-press-to-exit UX pattern to MainRoute.kt. The MainScreen is now the root working screen of the app — pressing back once shows a centered semi-transparent Chinese message "再次返回回到桌面" for 2 seconds. Pressing back a second time within that window calls `finishAffinity()` to exit the app. The timer resets if 2 seconds elapse without a second press.

The BackHandler is set with `enabled = true` which intercepts all back presses at this destination, completely preventing Navigation Compose from popping back to the Entry/LaunchSession screen.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Add double-back-press-to-exit with overlay toast in MainRoute | 175a90a | MainRoute.kt |

## Deviations from Plan

None - plan executed exactly as written.

## Verification

- `./gradlew :app:compileDebugKotlin` completed with BUILD SUCCESSFUL, zero errors
- Manual device check required to confirm runtime behavior (overlay appearance, 2-second timer, app exit)

## Self-Check: PASSED

- File exists: app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainRoute.kt - FOUND
- Commit 175a90a - FOUND
