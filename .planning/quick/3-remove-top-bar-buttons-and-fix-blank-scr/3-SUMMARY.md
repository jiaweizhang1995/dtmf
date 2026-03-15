---
phase: quick-3
plan: "01"
subsystem: UI / Window
tags: [top-bar, edge-to-edge, system-bars, quick-task]
one_liner: "Removed decorative ‹ and ⋮ buttons from MainTopBar and enabled edge-to-edge display so status/navigation bar areas render black"
dependency_graph:
  requires: []
  provides: [clean-top-bar, black-system-bars]
  affects: [MainScreen, AppShell, MainActivity]
tech_stack:
  added: []
  patterns:
    - "enableEdgeToEdge() for transparent system bars"
    - "Scaffold contentWindowInsets=WindowInsets(0) to suppress inset padding"
    - "Surface color override to match app background behind system bar areas"
key_files:
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/MainActivity.kt
    - app/src/main/res/values/themes.xml
    - app/src/main/java/com/jimmymacmini/wishdtmf/app/AppShell.kt
decisions:
  - "enableEdgeToEdge() is used instead of manually setting window flags — simpler and forwards-compatible with Activity 1.8+"
  - "Surface color set to 0xFF111111 matching MainScreenTokens.appBackground so areas behind system bars are black without additional inset handling in each screen"
  - "contentWindowInsets=WindowInsets(0) on Scaffold removes the default inset padding since all screens are full-bleed dark composables"
metrics:
  duration_minutes: 2
  completed_date: "2026-03-15"
  tasks_completed: 2
  files_modified: 4
---

# Phase quick-3: Remove Top Bar Buttons and Fix Blank Screen Edges Summary

**One-liner:** Removed decorative ‹ and ⋮ buttons from MainTopBar and enabled edge-to-edge display so status/navigation bar areas render black.

## What Was Built

### Task 1: Remove back and more-options buttons from MainTopBar

`MainTopBar` previously rendered a three-element `SpaceBetween` Row containing a decorative back chevron (`‹`), the session title, and a more-options indicator (`⋮`). These were non-functional text characters causing visual clutter.

Changed to a single `Arrangement.Center` Row containing only the title Text. The `MainScreenTags.TopBar` test tag is preserved on the Row. The `ReviewScreen`'s `ReviewAppBar` with its functional `<` navigation button was not touched.

### Task 2: Enable edge-to-edge display and black system bars

Three coordinated changes eliminate white gaps at screen top and bottom:

- **MainActivity.kt** — Added `enableEdgeToEdge()` call between `super.onCreate()` and `setContent { }`. This sets transparent system bars and appropriate icon contrast automatically via `androidx.activity:activity-compose`.
- **themes.xml** — Removed the `<item name="android:statusBarColor">` override that was setting the status bar to `?android:colorBackground` (which rendered as white on light system themes). The style now has no explicit bar color items; `enableEdgeToEdge()` manages this at runtime.
- **AppShell.kt** — Added `contentWindowInsets = WindowInsets(0)` to the `Scaffold` so the Scaffold does not consume system window insets (which would push content away from edges). Added `color = Color(0xFF111111)` to the `Surface` so the area behind system bars matches the app's dark background (`MainScreenTokens.appBackground`).

## Deviations from Plan

None - plan executed exactly as written.

## Verification

- `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL
- MainTopBar renders only the centered session title
- ReviewScreen back button is unaffected
- Build compiles cleanly with no new warnings

## Commits

| Task | Commit | Description |
|------|--------|-------------|
| 1 | ef5bfdb | feat(quick-3-01): remove back and more-options buttons from MainTopBar |
| 2 | 9204d74 | feat(quick-3-02): enable edge-to-edge display with black system bars |

## Self-Check: PASSED

- [x] MainScreen.kt modified — confirmed
- [x] MainActivity.kt modified — confirmed
- [x] themes.xml modified — confirmed
- [x] AppShell.kt modified — confirmed
- [x] Commit ef5bfdb exists
- [x] Commit 9204d74 exists
- [x] Both builds: BUILD SUCCESSFUL
