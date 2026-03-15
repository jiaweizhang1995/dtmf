---
phase: 04-review-permanent-delete
plan: 03
subsystem: ui
tags: [android, compose, mediastore, delete-flow, navigation, viewmodel, coroutines]

# Dependency graph
requires:
  - phase: 04-02
    provides: ReviewViewModel with SavedStateHandle-backed selectedPhotoIds, ReviewUiState, ReviewScreen with selection affordances

provides:
  - DeleteRequestCoordinator: URI-based MediaStore delete-request builder and result interpreter
  - PhotoRepository.resolveUrisForDelete: resolves selected staged IDs to content URIs for deletion
  - ReviewViewModel.onDeleteForever / onDeleteConfirmed: delete event emission seams
  - ReviewEvent sealed interface: RequestDelete and DeleteConfirmed one-shot events
  - ReviewRoute: owns ActivityResultLauncher for platform delete confirmation, wires cancel/success back to nav layer
  - MainViewModel.onDeleteConfirmed: clears stale swipe session after confirmed deletion
  - LaunchViewModel.refreshAfterDelete: rebuilds fresh launch session post-deletion
  - AppNavGraph.onRefreshAfterDelete callback: app-level post-delete refresh seam
  - DELETED_PHOTO_IDS_KEY: SavedStateHandle relay for confirmed IDs from review result to main route
  - ReviewDeleteFlowTest: integration coverage for selected-only deletion and cancel safety

affects: [05-partial-failure-hardening]

# Tech tracking
tech-stack:
  added: [ActivityResultContracts.StartIntentSenderForResult, MediaStore.createDeleteRequest (API 30+)]
  patterns:
    - SharedFlow-based one-shot events from ViewModel (ReviewEvent)
    - Activity-result launcher owned by Composable route layer, not ViewModel
    - SavedStateHandle relay for cross-destination result passing (DELETED_PHOTO_IDS_KEY)
    - Post-delete refresh via LaunchViewModel.refreshAfterDelete seam called from AppShell

key-files:
  created:
    - app/src/main/java/com/jimmymacmini/wishdtmf/data/media/DeleteRequestCoordinator.kt
    - app/src/test/java/com/jimmymacmini/wishdtmf/data/media/DeleteRequestCoordinatorTest.kt
    - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewDeleteFlowTest.kt
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/data/media/PhotoRepository.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/data/media/MediaStorePhotoRepository.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewViewModel.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainViewModel.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainRoute.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/LaunchViewModel.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/app/AppShell.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/app/navigation/AppNavGraph.kt
    - app/src/androidTest/java/com/jimmymacmini/wishdtmf/app/navigation/AppNavGraphTest.kt
    - app/src/test/java/com/jimmymacmini/wishdtmf/feature/entry/LaunchStateTest.kt

key-decisions:
  - "DeleteRequestCoordinator is a stateless object (not a class) since it wraps pure MediaStore calls with no per-instance state"
  - "ReviewRoute owns the ActivityResultLauncher so the ViewModel stays platform-free and testable"
  - "ReviewViewModel emits ReviewEvent.RequestDelete with selectedPhotoIds so the route resolves URIs just-in-time (avoids storing URIs in state)"
  - "Post-delete navigation pops review first, then relays deleted IDs via main back-stack entry SavedStateHandle so MainRoute can clear the stale session on resume"
  - "LaunchViewModel.refreshAfterDelete reuses the same loadBatch() path as initial permission grant so the post-delete rebuild is deterministic and consistent"
  - "minSdk 30 means MediaStore.createDeleteRequest is available unconditionally — no API version branching needed"

patterns-established:
  - "Platform API (MediaStore delete) lives exclusively in repository/coordinator layer, never in composables"
  - "Result interpretation (Activity.RESULT_OK) is a named method in the coordinator for testability"
  - "Cross-destination result relay uses SavedStateHandle.remove() on the receiving side (consumed once)"

requirements-completed: [REVW-03]

# Metrics
duration: 9min
completed: 2026-03-15
---

# Phase 4 Plan 03: Delete Request Coordination and Confirmed Deletion Summary

**URI-based MediaStore delete confirmation flow with platform launcher/result wiring, stale-session cleanup, and post-delete launch-session refresh**

## Performance

- **Duration:** ~9 min
- **Started:** 2026-03-15T06:56:46Z
- **Completed:** 2026-03-15T07:05:44Z
- **Tasks:** 3 of 3
- **Files modified:** 11

## Accomplishments

- Media layer extended with `resolveUrisForDelete` and a dedicated `DeleteRequestCoordinator` that builds `MediaStore.createDeleteRequest` intents and interprets result codes
- Review-to-platform delete flow fully wired: `ReviewRoute` owns the `ActivityResultLauncher`, emits `ReviewEvent.RequestDelete` for selected IDs only, and routes the confirmed result back through the nav layer
- Post-delete state is clean: `MainViewModel.onDeleteConfirmed` clears the stale swipe session, `LaunchViewModel.refreshAfterDelete` rebuilds from remaining media, and `AppNavGraph` relays confirmed IDs via `SavedStateHandle` so the main route clears on resume
- Automated coverage in `ReviewDeleteFlowTest` proves selected-only scoping, cancel non-destructiveness, empty-selection guard, and delete-confirmed event delivery; `AppNavGraphTest` extended with callback wiring verification

## Task Commits

1. **Task 1: Extend media layer with delete-request coordination seam** - `7283b0d` (feat)
2. **Task 2: Wire review delete intents through nav graph and platform confirmation result** - `fc6b27d` (feat)
3. **Task 3: Add destructive-flow coverage for selected-only deletion and cancel safety** - `1553bb7` (test)

## Files Created/Modified

- `DeleteRequestCoordinator.kt` - Stateless object wrapping `MediaStore.createDeleteRequest` with testable empty-guard and result-code interpretation seams
- `PhotoRepository.kt` - Added `resolveUrisForDelete(photoIds: Set<Long>): List<Uri>` to the contract
- `MediaStorePhotoRepository.kt` - Implemented `resolveUrisForDelete` via existing `reviewQuerySource`
- `ReviewViewModel.kt` - Added `ReviewEvent` sealed interface, `onDeleteForever`, `onDeleteConfirmed`, and SharedFlow `events`
- `ReviewRoute.kt` - Rewrote to own `ActivityResultLauncher`, consume `ReviewEvent` via `collectLatest`, and call `onDeleteConfirmed` callback
- `MainViewModel.kt` - Added `onDeleteConfirmed(deletedIds)` that resets `SwipeSessionState` to zero
- `MainRoute.kt` - Added `backStackEntry` param; consumes `DELETED_PHOTO_IDS_KEY` from `SavedStateHandle.remove()` to clear stale session
- `LaunchViewModel.kt` - Added `refreshAfterDelete()` delegating to `loadBatch()`
- `AppShell.kt` - Passes `launchViewModel::refreshAfterDelete` into `AppNavGraph`
- `AppNavGraph.kt` - Added `onRefreshAfterDelete` param, wires `DELETED_PHOTO_IDS_KEY` relay and refresh on delete-confirmed result; passes `backStackEntry` to `MainRoute`
- `DeleteRequestCoordinatorTest.kt` - Unit tests: empty-guard, result-code interpretation, selection-to-request contract
- `ReviewDeleteFlowTest.kt` - Integration tests: selected-only scoping, multiple-toggle exclusion, empty-selection CTA guard, cancel safety, success event, button tap behavior
- `AppNavGraphTest.kt` - Extended with `onRefreshAfterDelete` wiring test
- `LaunchStateTest.kt` - Updated `FakePhotoRepository` to implement `resolveUrisForDelete`

## Decisions Made

- `DeleteRequestCoordinator` is a stateless singleton object since it wraps pure platform calls with no per-instance state. This keeps the delete logic testable and avoids lifecycle management overhead.
- `ReviewRoute` owns the `ActivityResultLauncher` so `ReviewViewModel` stays platform-free and unit-testable. The ViewModel only cares about selected IDs, not Android intents or result codes.
- `ReviewEvent.RequestDelete` carries `selectedPhotoIds` (not pre-resolved URIs) so URIs are resolved just-in-time in the route. Storing URIs in state would risk staleness between selection and confirmation.
- `DELETED_PHOTO_IDS_KEY` relay via `SavedStateHandle.remove()` (consumed once) prevents the stale session from being cleared on every recomposition after the initial consumption.
- `minSdk 30` means `MediaStore.createDeleteRequest` is available unconditionally — no legacy `ContentResolver.delete()` fallback is needed.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Added testable internal seams to DeleteRequestCoordinator**
- **Found during:** Task 1 (Unit test authoring for `DeleteRequestCoordinatorTest`)
- **Issue:** `buildDeleteRequest` calls `MediaStore.createDeleteRequest` which requires a real `ContentResolver` not available in JVM unit tests. Without testable seams, the empty-guard contract was unverifiable.
- **Fix:** Added `internal fun buildDeleteRequestForUris(uris: List<*>): Any?` and `internal fun isNonEmptyRequestAllowed(items: List<*>): Boolean` as thin test hooks that expose the empty-list short-circuit without touching platform APIs.
- **Files modified:** `DeleteRequestCoordinator.kt`
- **Verification:** `DeleteRequestCoordinatorTest` passes; seams are `internal` so not exposed in public API.
- **Committed in:** `7283b0d` (Task 1 commit)

**2. [Rule 1 - Bug] Fixed FakePhotoRepository in LaunchStateTest failing to compile**
- **Found during:** Task 1 (First build after adding `resolveUrisForDelete` to `PhotoRepository`)
- **Issue:** `FakePhotoRepository` in `LaunchStateTest.kt` implements `PhotoRepository` but didn't implement the new `resolveUrisForDelete` method, causing a compile error.
- **Fix:** Added `override suspend fun resolveUrisForDelete(...): List<Uri> = emptyList()`.
- **Files modified:** `LaunchStateTest.kt`
- **Verification:** Full unit test suite passes.
- **Committed in:** `7283b0d` (Task 1 commit)

**3. [Rule 1 - Bug] Fixed Uri.parse() returning null in JVM unit tests**
- **Found during:** Task 1 (First test run of `DeleteRequestCoordinatorTest`)
- **Issue:** `Uri.parse()` is an Android platform API that returns null in JVM unit tests (robolectric not used). The original `selectedSubset_doesNotIncludeUnselectedIds` test used `Uri.parse()` which caused an NPE.
- **Fix:** Rewrote the test to use string URI comparisons with `endsWith("/$id")` instead of `Uri.lastPathSegment`.
- **Files modified:** `DeleteRequestCoordinatorTest.kt`
- **Verification:** All unit tests pass.
- **Committed in:** `7283b0d` (Task 1 commit)

---

**Total deviations:** 3 auto-fixed (1 missing testable seam, 1 compile bug, 1 platform API incompatibility in unit tests)
**Impact on plan:** All auto-fixes essential for correctness and testability. No scope creep.

## Issues Encountered

- The JVM unit test environment does not support `Uri.parse()` — the empty-guard tests for `DeleteRequestCoordinator` had to be rewritten to avoid Android platform URI parsing while still exercising the correct contract boundaries.

## User Setup Required

None — no external service configuration required.

## Next Phase Readiness

- Phase 4 is complete: review layout (04-01), selection state (04-02), and permanent-delete flow (04-03) are all shipped and tested
- Phase 5 can deepen partial-failure and cancel-path hardening (REVW-04) on top of the solid Phase 4 delete seam
- Validate delete-flow behavior on at least one real Android device, not only emulator (existing blocker from STATE.md)

---
*Phase: 04-review-permanent-delete*
*Completed: 2026-03-15*
