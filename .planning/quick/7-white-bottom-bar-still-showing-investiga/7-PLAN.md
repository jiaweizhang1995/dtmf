# Quick Task 7 Plan: white bottom bar still showing investigate and fix properly

**Date:** 2026-03-15

## Root Cause

`Scaffold` in `AppShell.kt` defaults `containerColor` to `MaterialTheme.colorScheme.background`, which is white on light-mode devices. This overpaints the `Surface(color = 0xFF111111)` behind it, including in the transparent nav bar area.

## Task

**File:** `app/src/main/java/com/jimmymacmini/wishdtmf/app/AppShell.kt`
**Change:** Add `containerColor = Color.Transparent` to `Scaffold` so the dark Surface shows through everywhere.
