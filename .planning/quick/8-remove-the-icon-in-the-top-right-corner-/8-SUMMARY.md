---
phase: quick-8
plan: "01"
subsystem: main-ui
tags: [ui, hero-card, content-scale, cleanup]
dependency_graph:
  requires: []
  provides: [uncropped-hero-image, no-icon-badge]
  affects: [CurrentPhotoCard]
tech_stack:
  added: []
  patterns: [ContentScale.Fit with black background for letterbox fill]
key_files:
  created: []
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/CurrentPhotoCard.kt
decisions:
  - Replaced Color(0xFF2A261F) outer Box background with Color.Black to provide pure black letterbox/pillarbox fill alongside ContentScale.Fit
metrics:
  duration: "3 min"
  completed: "2026-03-15"
  tasks_completed: 1
  files_modified: 1
---

# Phase quick-8 Plan 01: Remove Icon Badge and Switch to Uncropped Hero Image Summary

**One-liner:** Removed unused "◪" icon badge from hero card top-right and switched AsyncImage from ContentScale.Crop to ContentScale.Fit with Color.Black background for full uncropped photo display.

## What Was Built

The hero card in `CurrentPhotoCard.kt` had two issues: an unused "◪" icon badge overlaid in the top-right corner, and the AsyncImage was set to `ContentScale.Crop` which silently cut off photo edges. Both issues were resolved in a single targeted edit.

## Tasks Completed

| # | Task | Commit | Files |
|---|------|--------|-------|
| 1 | Remove icon overlay and switch to uncropped image display | 5377367 | CurrentPhotoCard.kt |

## Changes Made

**`CurrentPhotoCard.kt`:**
- Deleted the entire icon `Box` block (the "◪" badge with rounded background at `Alignment.TopEnd`)
- Changed `contentScale = ContentScale.Crop` to `contentScale = ContentScale.Fit` on AsyncImage
- Changed outer Box background from `Color(0xFF2A261F)` to `Color.Black` for clean letterbox/pillarbox fill
- Removed unused imports: `androidx.compose.foundation.layout.size` and `androidx.compose.ui.text.font.FontWeight`

## Deviations from Plan

None - plan executed exactly as written.

## Verification

Kotlin compilation: `./gradlew :app:compileDebugKotlin` — BUILD SUCCESSFUL, no warnings.

## Self-Check: PASSED

- File modified: `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/CurrentPhotoCard.kt` — FOUND
- Commit 5377367 — FOUND
- No icon Box in file — CONFIRMED
- ContentScale.Fit in file — CONFIRMED
- Color.Black background — CONFIRMED
