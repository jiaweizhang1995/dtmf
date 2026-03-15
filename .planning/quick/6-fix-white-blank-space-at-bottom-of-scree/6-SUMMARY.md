---
phase: quick-6
plan: 01
subsystem: ui/system-bars
tags: [navigation-bar, edge-to-edge, system-bar-style, dark-theme]
dependency_graph:
  requires: []
  provides: [dark-navigation-bar]
  affects: [MainActivity]
tech_stack:
  added: []
  patterns: [SystemBarStyle.dark for transparent nav bar]
key_files:
  created: []
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/MainActivity.kt
decisions:
  - "Used SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) instead of NavigationBarStyle (which is not a real class) to force a fully transparent dark nav bar"
metrics:
  duration: "3 min"
  completed_date: "2026-03-15T14:31:28Z"
  tasks_completed: 1
  files_modified: 1
---

# Quick Task 6: Fix White Blank Space at Bottom of Screen — Summary

**One-liner:** Force `SystemBarStyle.dark(TRANSPARENT)` on `enableEdgeToEdge()` so Android never applies a light scrim over the navigation bar regardless of system theme.

## What Was Changed

`MainActivity.kt` — single-file change:

1. Added import `androidx.activity.SystemBarStyle`
2. Replaced bare `enableEdgeToEdge()` with:
   ```kotlin
   enableEdgeToEdge(
       navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
   )
   ```

## Why

`enableEdgeToEdge()` called without arguments defaults to `NavigationBarStyle.Auto`, which lets Android choose a white/light scrim on devices running in light system mode. Since the app's surface is `Color(0xFF111111)` (near-black), this mismatch produces a visible white bar at the bottom of the screen. Passing `SystemBarStyle.dark(TRANSPARENT)` locks the nav bar to a fully transparent dark overlay, letting the app's black `Surface` show through.

## Verification

- `./gradlew :app:compileDebugKotlin` — BUILD SUCCESSFUL, zero errors
- Visual confirmation required on a light-mode device: navigation bar area should appear black/dark, not white

## Deviations from Plan

None — plan executed exactly as written.

## Self-Check

- [x] `MainActivity.kt` modified with correct API
- [x] Commit `4226bfe` exists
- [x] Compile succeeded with BUILD SUCCESSFUL
