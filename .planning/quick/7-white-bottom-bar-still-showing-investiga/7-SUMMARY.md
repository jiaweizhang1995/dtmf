# Quick Task 7 Summary: white bottom bar still showing investigate and fix properly

**Date:** 2026-03-15
**Commit:** a8e5f3a

## What Was Done

Root cause: `Scaffold` in `AppShell.kt` draws its own background using `MaterialTheme.colorScheme.background`, which is white on light-mode devices. Even though the `Surface` behind it is `Color(0xFF111111)`, the Scaffold's `containerColor` was overpainting the dark surface — including the area behind the transparent nav bar — making it appear white.

**Fix:** Added `containerColor = Color.Transparent` to the `Scaffold` in `AppShell.kt` so it no longer paints a background, letting the dark `Surface` show through everywhere including the navigation bar area.

## Files Changed

- `app/src/main/java/com/jimmymacmini/wishdtmf/app/AppShell.kt` — added `containerColor = Color.Transparent` to Scaffold
