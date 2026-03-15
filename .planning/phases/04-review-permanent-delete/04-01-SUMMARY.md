---
phase: 04-review-permanent-delete
plan: 01
subsystem: feature/review + data/media
tags: [review-screen, display-model, tokens, instrumentation-tests, coil, media-store]
dependency_graph:
  requires: [03-02]
  provides: [review-display-model, review-screen-tokens, review-instrumentation-coverage]
  affects: [feature/review, data/media]
tech_stack:
  added: [ReviewPhoto display model, ReviewScreenTokens, ReviewPhotoQuerySource, ContentResolverReviewPhotoQuerySource]
  patterns: [review-boundary resolution, tokens centralization, composable-level LaunchedEffect media query]
key_files:
  created:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTokens.kt
    - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/data/media/PhotoRepository.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/data/media/MediaStorePhotoRepository.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt
decisions:
  - "ReviewRoute resolves staged IDs at the review boundary via LaunchedEffect + MediaStorePhotoRepository rather than a full ViewModel, keeping the handoff minimal and consistent with the no-Hilt single-module pattern"
  - "ReviewPhoto is a dedicated lightweight display model (id + contentUri only) rather than re-using LocalPhoto, keeping review rendering decoupled from the full media metadata contract"
  - "ContentResolverReviewPhotoQuerySource uses a WHERE IN query rather than N individual lookups, preserving session order through a post-query map join"
  - "All visual constants live in ReviewScreenTokens so later tuning stays localized; screen composable contains no hardcoded literals"
  - "Selection affordances (checkmark badges, teal tile borders) are present in the UI now while full toggle logic is deferred to plan 04-02"
metrics:
  duration: "5 min"
  completed_date: "2026-03-15"
  tasks_completed: 3
  files_changed: 6
---

# Phase 4 Plan 01: Review Screen Shell and Display Model Summary

**One-liner:** Screenshot-faithful review shell with image-backed grid, Coil AsyncImage tiles, teal check badges, and dedicated ReviewScreenTokens built on a MediaStore-resolved ReviewPhoto display model.

## Tasks Completed

| # | Task | Commit | Key Files |
|---|------|--------|-----------|
| 1 | Replace placeholder review route with real staged-photo display model | 92020d5 | PhotoRepository.kt, MediaStorePhotoRepository.kt, ReviewRoute.kt |
| 2 | Build screenshot-faithful review screen and centralized token set | 78e01c9 | ReviewScreen.kt, ReviewScreenTokens.kt |
| 3 | Add instrumentation coverage for review hierarchy and staged-photo rendering | 37f793f | ReviewScreenTest.kt |

## What Was Built

### Task 1: Review Display Model and Boundary Resolution

- `ReviewPhoto(id, contentUri)` added to `PhotoRepository.kt` — lightweight display model for review grid tiles
- `PhotoRepository.loadReviewPhotos(orderedIds)` interface method added with explicit ordering contract
- `ReviewPhotoQuerySource` internal interface added for test seam isolation
- `ContentResolverReviewPhotoQuerySource` implements a single `WHERE IN` MediaStore query, then joins results back into caller-provided order
- `EmptyReviewPhotoQuerySource` provided as a default for `forTesting()` constructor
- `ReviewUiState` updated: now `data class ReviewUiState(stagedPhotos: List<ReviewPhoto>, isLoading: Boolean)`
- `ReviewRoute` uses `LaunchedEffect(stagedPhotoIds)` to trigger boundary resolution and update state; renders `ReviewScreen` with both raw IDs (for semantics) and resolved photos (for tiles)

### Task 2: Screenshot-Faithful Review Screen

Layout hierarchy matches `Delete-staging-area..jpg`:

1. **App bar row:** `<` back affordance + `REVIEW` title (left-aligned, not centered)
2. **Destructive prompt:** left teal border accent + "Permanently delete N items?" heading
3. **Helper link:** "No, I want to move to trash" in teal below prompt
4. **LazyVerticalGrid (2 columns):** `AsyncImage`-backed `ReviewPhotoTile` per resolved photo, each with a teal `CheckBadge` (checkmark, top-left corner) — selection affordance present before toggle logic
5. **`PlaceholderTile`** rendered while photos are loading (IDs present, `stagedPhotos` empty)
6. **Bottom area:** "You can unselect the one's you wish to keep" helper copy + "Decide Later" (dark) and "Delete forever" (teal) CTAs

`ReviewScreenTokens` object centralises all colors, spacing, corner radii, type sizes, grid config, and copy strings. No hardcoded literals exist in the composable.

### Task 3: Instrumentation Coverage

14 tests in `ReviewScreenTest`:

| Test | What it pins |
|------|-------------|
| `reviewRootIsDisplayed` | Root container visible |
| `titleIsDisplayed` | "REVIEW" title tag |
| `backButtonIsDisplayed` | Back affordance present |
| `destructivePromptIsDisplayed` | Prompt heading visible |
| `helperLinkIsDisplayed` | "move to trash" link visible |
| `photoGridIsDisplayed` | Grid container visible |
| `bottomHelperIsDisplayed` | Helper copy visible |
| `decideLaterButtonIsDisplayed` | Decide Later CTA visible |
| `deleteForeverButtonIsDisplayed` | Delete forever CTA visible |
| `eachStagedPhotoRendersATile` | Per-ID tile tags present |
| `eachStagedPhotoHasACheckBadge` | Per-ID check badge tags present |
| `singleStagedPhotoRendersOneTile` | Single-photo edge case |
| `rootHasStagedIdsInStateDescription_singleId` | Semantics contract (single) |
| `rootHasStagedIdsInStateDescription_multipleIds` | Semantics contract (multiple) |
| `backButtonCallsOnBack` | Tap back invokes callback |
| `placeholderTilesShownWhilePhotosLoading` | Loading state rendering |

Existing `AppNavGraphTest` (2 tests) continues to pass unchanged — `"staged:10"` state description and back-stack continuity contracts are preserved.

## Deviations from Plan

None — plan executed exactly as written.

## Self-Check

### Files Created/Modified

- [x] `app/src/main/java/com/jimmymacmini/wishdtmf/data/media/PhotoRepository.kt` — exists, adds `ReviewPhoto` + `loadReviewPhotos`
- [x] `app/src/main/java/com/jimmymacmini/wishdtmf/data/media/MediaStorePhotoRepository.kt` — exists, implements `loadReviewPhotos`
- [x] `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt` — exists, boundary resolution via `LaunchedEffect`
- [x] `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt` — exists, screenshot-faithful hierarchy
- [x] `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTokens.kt` — created, all tokens centralized
- [x] `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt` — created, 14 tests

### Build

- [x] `./gradlew assembleDebug` — BUILD SUCCESSFUL
- [x] `./gradlew compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL

### Commits

- [x] 92020d5 — Task 1 feat
- [x] 78e01c9 — Task 2 feat
- [x] 37f793f — Task 3 test

## Self-Check: PASSED
