---
phase: quick-18
plan: 01
subsystem: onboarding
tags: [welcome-screen, permissions, first-launch, navigation, shared-preferences]
dependency_graph:
  requires: []
  provides: [first-launch-onboarding, auto-permission-trigger]
  affects: [AppNavGraph, AppShell, entry-feature]
tech_stack:
  added: []
  patterns: [SharedPreferences, LaunchedEffect, NavHost startDestination conditional]
key_files:
  created:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/FirstLaunchPreferences.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/WelcomeScreen.kt
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/app/navigation/AppNavGraph.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/app/AppShell.kt
decisions:
  - SharedPreferences-backed FirstLaunchPreferences tracks first-launch boolean; isFirstLaunch getter checks negation of stored boolean so default (absent) returns true
  - WelcomeScreen uses LaunchedEffect(Unit) to fire permission request without any user tap; uiState change drives navigation to entry so there is no timer or arbitrary delay
  - WELCOME_ROUTE added as optional startDestination in AppNavGraph; startDestination computed from firstLaunchPreferences.isFirstLaunch before NavHost so all NavHost internal routing logic remains unchanged
  - onRequestPermission passed as separate parameter to AppNavGraph (distinct from onGrantAccess) so the welcome screen can trigger the same permission launcher registered at AppShell level
  - firstLaunchPreferences.markFirstLaunchDone() called inside the WelcomeScreen onRequestPermission callback (not at the start destination check) so it is only marked done when the user actually reaches the permission dialog
metrics:
  duration: ~8 min
  completed: 2026-03-16
  tasks_completed: 2
  files_created: 2
  files_modified: 2
---

# Phase quick-18: Create Welcome Screen with Photo Library Permission Summary

**One-liner:** Black-background welcome screen with centered app icon that auto-fires the photo library permission dialog on first launch using LaunchedEffect and SharedPreferences gating.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | FirstLaunchPreferences + WelcomeScreen | a7d1724 | FirstLaunchPreferences.kt, WelcomeScreen.kt |
| 2 | Wire WelcomeScreen into AppNavGraph with first-launch routing | 292c44c | AppNavGraph.kt, AppShell.kt |

## What Was Built

### FirstLaunchPreferences.kt
SharedPreferences wrapper that persists a boolean flag. `isFirstLaunch` returns `true` until `markFirstLaunchDone()` is called. Uses `wish_dtmf_prefs` preferences file with `first_launch_done` key.

### WelcomeScreen.kt
Composable with black full-screen background and centered `ic_launcher` mipmap image (120dp). Two LaunchedEffects:
1. `LaunchedEffect(Unit)` — fires `onRequestPermission()` immediately on first composition, then sets `permissionRequested = true`
2. `LaunchedEffect(permissionRequested, uiState)` — once permission has been requested, watches for uiState to change from the initial `NeedsPermission(showSettingsHint=false)` state; any other state (deny=showSettingsHint:true, LoadingBatch, Ready, Empty, Error) triggers `onNavigateToEntry()`

### AppNavGraph.kt changes
- Added `WELCOME_ROUTE = "welcome"` constant
- Added `onRequestPermission: () -> Unit = {}` and `firstLaunchPreferences: FirstLaunchPreferences? = null` parameters (both optional for backward compatibility)
- `startDestination` computed before NavHost: `WELCOME_ROUTE` if `firstLaunchPreferences?.isFirstLaunch == true`, else `ENTRY_ROUTE`
- Added `composable(WELCOME_ROUTE)` destination with `WelcomeScreen`
- `popUpTo(startDestination)` in the Ready state navigation uses the computed startDestination so the back stack is popped from the correct root

### AppShell.kt changes
- Added `FirstLaunchPreferences` import
- Added `val context = LocalContext.current` at top of composable
- Constructs `firstLaunchPreferences = remember { FirstLaunchPreferences(context) }`
- Passes `onRequestPermission = permissionController.requestPermission` and `firstLaunchPreferences = firstLaunchPreferences` to `AppNavGraph`

## Verification

- `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL (3s)

## Deviations from Plan

None - plan executed exactly as written. The final clean design from Task 2 spec was used verbatim.

## Checkpoint Pending

Task 3 is a `checkpoint:human-verify` gate. The user must install/clear app data and verify the welcome screen behavior on device.

## Self-Check

Files created:
- app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/FirstLaunchPreferences.kt — FOUND
- app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/WelcomeScreen.kt — FOUND
- app/src/main/java/com/jimmymacmini/wishdtmf/app/navigation/AppNavGraph.kt — MODIFIED
- app/src/main/java/com/jimmymacmini/wishdtmf/app/AppShell.kt — MODIFIED

Commits:
- a7d1724 — feat(quick-18): add FirstLaunchPreferences and WelcomeScreen
- 292c44c — feat(quick-18): wire WelcomeScreen into AppNavGraph with first-launch routing
