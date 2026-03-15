---
phase: quick-17
plan: "01"
subsystem: thumbnail-strip
tags: [compose, modifier-order, border, clip, thumbnail]
dependency_graph:
  requires: []
  provides: [visible-active-thumbnail-border]
  affects: [ThumbnailStrip]
tech_stack:
  added: []
  patterns: [compose-modifier-order-border-before-clip]
key_files:
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailStrip.kt
decisions:
  - "Border modifier must precede clip modifier in Compose so it renders in the unclipped layer and remains visible"
metrics:
  duration: "3 min"
  completed_date: "2026-03-16"
---

# Quick Task 17: Fix Thumbnail Yellow Border Not Showing — Summary

**One-liner:** Moved `.border()` before `.clip()` in ThumbnailBox so the active yellow border renders outside the clipped layer and is visible.

## What Was Done

In Compose, modifiers are applied in order. When `.clip()` precedes `.border()`, the border is drawn inside the already-clipped region and becomes invisible (masked by the clip shape). The fix is straightforward: move `.border()` before `.clip()` so the border is drawn in the unclipped layer and then the clip masks only the image content inside it.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Fix modifier order — border before clip | 09b9ed6 | ThumbnailStrip.kt |

## Changes Made

**ThumbnailStrip.kt — ThumbnailBox modifier chain:**

Before (broken):
```
.size(...)
.clip(RoundedCornerShape(10.dp))    // (1) clips everything after this
.background(...)
.border(...)                         // (2) invisible — inside clip
```

After (fixed):
```
.size(...)
.border(...)                         // (1) drawn before clip — visible
.clip(RoundedCornerShape(10.dp))    // (2) clips content/background only
.background(...)
```

All other modifiers (`.semantics`, `.alpha`, `.testTag`) remained in their existing positions.

## Deviations from Plan

None - plan executed exactly as written.

## Self-Check: PASSED

- [x] ThumbnailStrip.kt modified with border before clip
- [x] Commit 09b9ed6 exists
- [x] `./gradlew :app:compileDebugKotlin` — BUILD SUCCESSFUL
