---
phase: 04-review-permanent-delete
plan: 02
subsystem: ui
tags: [android, compose, viewmodel, savedstatehandle, selection-state, review]

# Dependency graph
requires:
  - phase: 04-01
    provides: ReviewScreen shell with check badges and ReviewScreenTokens; ReviewPhoto display model; ReviewRoute MediaStore resolution
provides:
  - ReviewViewModel: SavedStateHandle-backed review-local selection state owner
  - ReviewUiState: stable model with selectedPhotoIds, selectedCount, isDeleteEnabled, destructivePromptText
  - Interactive ReviewScreen: deselect/reselect tiles with teal border + badge, dimmed deselected tiles
  - Count-driven destructive prompt and delete CTA enablement from selected subset
  - ReviewViewModelTest: unit coverage for selection semantics and state restoration
  - ReviewScreenTest: extended instrumentation coverage for toggle affordances
affects: [04-03-permanent-delete, phase-05]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - ReviewViewModel backed by SavedStateHandle using LongArray serialization for selected IDs
    - Review-local state isolated from MainViewModel staged set until destructive action confirmed
    - Tile semantics expose "selected"/"deselected" stateDescription for testable affordances
    - Delete CTA uses "enabled"/"disabled" stateDescription instead of enabled property for Compose semantics

key-files:
  created:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewViewModel.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewUiState.kt
    - app/src/test/java/com/jimmymacmini/wishdtmf/feature/review/ReviewViewModelTest.kt
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt
    - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt
    - app/src/test/java/com/jimmymacmini/wishdtmf/feature/entry/LaunchStateTest.kt

key-decisions:
  - "ReviewViewModel persists selectedPhotoIds as LongArray in SavedStateHandle, intersected with resolved IDs on restoration so stale MediaStore entries are pruned automatically"
  - "Deselected tiles are dimmed (alpha 0.4) rather than hidden so users can reselect items they kept by mistake"
  - "Delete CTA disabled state uses visual dimming + removing clickable modifier rather than Compose enabled= because Box does not natively propagate enabled semantics"
  - "Review selection state is fully isolated: togglePhotoSelection mutates review-local selectedPhotoIds only; MainViewModel stagedPhotoIds remain unchanged until plan 04-03"

patterns-established:
  - "Selection persistence: SavedStateHandle.get<LongArray>(KEY) + .intersect(resolvedIds) for safe cross-session restoration"
  - "Tile toggle semantics: semantics { stateDescription = selected/deselected; role = Role.Checkbox } for testable affordances without debug UI"
  - "CTA disabled pattern: remove clickable modifier + set background/text to subtle colors + expose disabled stateDescription"

requirements-completed: [REVW-02]

# Metrics
duration: 3min
completed: 2026-03-15
---

# Phase 4 Plan 02: Review Selection State Summary

**SavedStateHandle-backed ReviewViewModel with deselect/reselect behavior, count-driven copy, and disabled delete CTA when nothing is selected**

## Performance

- **Duration:** 3 min
- **Started:** 2026-03-15T06:49:40Z
- **Completed:** 2026-03-15T06:52:40Z
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments
- ReviewViewModel owns review-local selectedPhotoIds, persisting through SavedStateHandle so rotation does not reset keep/delete choices
- Interactive ReviewPhotoTile fires toggle callbacks; deselected tiles are dimmed with no check badge; selected tiles show teal border and checkmark
- Destructive prompt and delete CTA copy reflect the currently selected subset (not the total staged count)
- Delete forever CTA is visually disabled and non-interactive when no photos remain selected
- 15 ReviewViewModelTest unit tests cover default-all-selected, deselect, reselect, count, SavedStateHandle restoration, stale-ID pruning, and CTA eligibility
- ReviewScreenTest extended with toggle callback, badge absence, state descriptions, and CTA state assertions

## Task Commits

1. **Task 1: SavedStateHandle-backed ReviewViewModel and ReviewUiState** - `79dd9a7` (feat)
2. **Task 2: Wire deselect/reselect behavior and count-driven copy in ReviewScreen** - `fcb9f5a` (feat)
3. **Task 3: Add automated coverage for selection semantics** - `17b31c9` (test)

## Files Created/Modified
- `ReviewViewModel.kt` - SavedStateHandle-backed review-local state owner; onPhotosResolved initialises all-selected; togglePhotoSelection mutates only review-local set
- `ReviewUiState.kt` - Stable model with selectedPhotoIds, selectedCount, isDeleteEnabled, destructivePromptText, isSelected(id)
- `ReviewRoute.kt` - Updated to own ReviewViewModel lifecycle; passes uiState and toggle callback to ReviewScreen
- `ReviewScreen.kt` - Takes ReviewUiState instead of raw list; tiles are clickable with selected/deselected semantics; CTA has enabled/disabled state
- `ReviewViewModelTest.kt` - 15 unit tests for selection semantics and SavedStateHandle restoration
- `ReviewScreenTest.kt` - Extended to use new ReviewUiState signature; new tests for toggle affordances, CTA state
- `LaunchStateTest.kt` - FakePhotoRepository now implements loadReviewPhotos (blocking fix)

## Decisions Made
- `ReviewViewModel` persists `selectedPhotoIds` as `LongArray` in `SavedStateHandle` and intersects with resolved IDs on restoration so stale MediaStore entries are pruned automatically
- Deselected tiles are dimmed (alpha 0.4) rather than hidden so users can reselect items they kept by mistake
- Delete CTA disabled state uses visual dimming + removing the clickable modifier because Compose `Box` does not natively propagate `enabled=` semantics
- Review selection state stays fully isolated: `togglePhotoSelection` mutates review-local `selectedPhotoIds` only; `MainViewModel.stagedPhotoIds` remain unchanged until plan 04-03

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] FakePhotoRepository missing loadReviewPhotos override**
- **Found during:** Task 3 (unit test compilation)
- **Issue:** `LaunchStateTest.kt`'s `FakePhotoRepository` implements `PhotoRepository` but `loadReviewPhotos` was added to the interface in plan 04-01 and the fake was never updated; compilation failed
- **Fix:** Added `override suspend fun loadReviewPhotos(...) = emptyList()` to `FakePhotoRepository`
- **Files modified:** `app/src/test/java/com/jimmymacmini/wishdtmf/feature/entry/LaunchStateTest.kt`
- **Verification:** `./gradlew testDebugUnitTest --tests '*ReviewViewModelTest*'` BUILD SUCCESSFUL
- **Committed in:** `79dd9a7` (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Fix required to unblock unit test compilation. No scope creep.

## Issues Encountered
None beyond the FakePhotoRepository blocking issue documented above.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Review selection state contract is locked by unit tests before destructive delete integration (plan 04-03)
- `ReviewViewModel` exposes `uiState.selectedPhotoIds` for plan 04-03 to consume when wiring the actual delete action
- `onDeleteForever` callback is already threaded through `ReviewRoute` → `ReviewScreen` → `BottomActionArea` as an empty lambda, ready to be connected

---
*Phase: 04-review-permanent-delete*
*Completed: 2026-03-15*
