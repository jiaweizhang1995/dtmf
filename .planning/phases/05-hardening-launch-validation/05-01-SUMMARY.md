---
phase: 05-hardening-launch-validation
plan: "01"
subsystem: ui
tags: [compose, material3, material-icons-extended, coroutines, mediastore, unit-test, instrumented-test]

# Dependency graph
requires:
  - phase: 04-review-permanent-delete
    provides: ReviewRoute, ReviewViewModel, ReviewScreen, ReviewScreenTags, DeleteRequestCoordinator

provides:
  - EntryScreen with polished Empty (icon+copy+scan button), NeedsPermission+settings-hint deep-link, and LoadingBatch (CircularProgressIndicator) branches
  - ReviewRoute post-delete re-query accuracy layer (partial-failure detection via loadReviewPhotos)
  - ReviewScreen EmptyGridMessage branch with tagged Text for empty staged grid
  - Wave 0 test coverage: partial-delete unit test, empty-grid composable test, settings-hint composable test

affects: [05-02, 05-03]

# Tech tracking
tech-stack:
  added: [material-icons-extended (BOM-versioned)]
  patterns:
    - Post-delete re-query pattern: re-fetch from MediaStore after RESULT_OK to determine actually-deleted vs submitted IDs
    - TDD test-before-production approach for new composable branches and ViewModel behaviors

key-files:
  created: []
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreen.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt
    - app/src/test/java/com/jimmymacmini/wishdtmf/feature/review/ReviewViewModelTest.kt
    - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt
    - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreenTest.kt
    - app/build.gradle.kts

key-decisions:
  - "Post-delete re-query runs in coroutineScope.launch on the composable scope; loadReviewPhotos handles IO switching internally — no explicit Dispatchers.IO needed at the call site"
  - "material-icons-extended added as BOM-versioned dependency to provide Icons.Outlined.PhotoLibrary for the Empty branch icon"
  - "EntryScreenTest emptyStateShowsRetryAction updated to reflect polished copy ('No photos to clean up' / 'Scan again') — existing test was asserting on old placeholder copy"
  - "onDeleteConfirmed guard (isNotEmpty check) lives in ReviewRoute, not ReviewViewModel — ViewModel emits DeleteConfirmed unconditionally, Route filters"

patterns-established:
  - "Re-query accuracy pattern: loadReviewPhotos(submittedIds) -> stillPresent; actuallyDeleted = submitted - stillPresent; call confirmed only if actuallyDeleted.isNotEmpty()"
  - "EmptyGridMessage: triggered when both uiState.stagedPhotos.isEmpty() AND stagedPhotoIds.isEmpty() AND !uiState.isLoading"

requirements-completed: [MEDIA-03, REVW-04]

# Metrics
duration: 12min
completed: 2026-03-15
---

# Phase 5 Plan 01: MEDIA-03 + REVW-04 Hardening Summary

**Polished EntryScreen states (icon, settings deep-link, progress indicator) and accurate post-delete re-query in ReviewRoute so only actually-deleted IDs propagate, with Wave 0 test coverage for all three behaviors**

## Performance

- **Duration:** ~12 min
- **Started:** 2026-03-15T08:00:00Z
- **Completed:** 2026-03-15T08:06:53Z
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments
- EntryScreen Empty branch now shows PhotoLibrary icon + "No photos to clean up" heading + explanatory body + "Scan again" button (MEDIA-03)
- EntryScreen NeedsPermission(showSettingsHint=true) shows "Open app settings" button that deep-links to ACTION_APPLICATION_DETAILS_SETTINGS (MEDIA-03)
- EntryScreen LoadingBatch branch shows CircularProgressIndicator alongside loading text (MEDIA-03)
- ReviewRoute RESULT_OK handler re-queries MediaStore via loadReviewPhotos to determine actually-deleted IDs; only calls onDeleteConfirmed when isNotEmpty (REVW-04)
- ReviewScreen shows "No photos staged for deletion." (tagged review_empty_grid_message) when both staged lists are empty and not loading (REVW-04)
- Four new Wave 0 tests: 2 unit (ReviewViewModel partial-delete, empty-set edge case) + 2 instrumented (empty-grid, settings-hint)

## Task Commits

Each task was committed atomically:

1. **Task 1: Wave 0 — add three missing test cases** - `ef22e0f` (test)
2. **Task 2: Polish EntryScreen states and add ReviewScreen empty-grid branch** - `e0d5e00` (feat)
3. **Task 3: Add partial-delete accuracy re-query to ReviewRoute** - `4cf4280` (feat)

## Files Created/Modified
- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreen.kt` - Polished Empty, NeedsPermission+settings-hint, and LoadingBatch branches
- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt` - Post-delete re-query accuracy layer
- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt` - EmptyGridMessage tag constant and empty-grid item branch
- `app/src/test/java/com/jimmymacmini/wishdtmf/feature/review/ReviewViewModelTest.kt` - Two new partial-delete unit tests
- `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt` - Empty-grid composable test
- `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreenTest.kt` - Settings-hint button test + copy update for empty state
- `app/build.gradle.kts` - Added material-icons-extended dependency

## Decisions Made
- Post-delete re-query runs in rememberCoroutineScope.launch; loadReviewPhotos handles its own IO dispatcher switching internally
- material-icons-extended added (BOM-versioned) to provide PhotoLibrary icon — no explicit version pin needed
- ViewModel guard responsibility: ViewModel emits DeleteConfirmed unconditionally with whatever set is passed; ReviewRoute is responsible for the "isNotEmpty" guard before calling onDeleteConfirmed

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added material-icons-extended dependency**
- **Found during:** Task 2 (EntryScreen polish — Icons.Outlined.PhotoLibrary)
- **Issue:** Icons.Outlined.PhotoLibrary requires material-icons-extended which was not in build.gradle.kts; compilation failed with "Unresolved reference: PhotoLibrary"
- **Fix:** Added `implementation("androidx.compose.material:material-icons-extended")` under composeBom; version resolved automatically from BOM
- **Files modified:** app/build.gradle.kts
- **Verification:** `./gradlew testDebugUnitTest` — BUILD SUCCESSFUL
- **Committed in:** e0d5e00 (Task 2 commit)

**2. [Rule 1 - Bug] Updated EntryScreenTest emptyStateShowsRetryAction to match polished copy**
- **Found during:** Task 2 (EntryScreen Empty branch copy change)
- **Issue:** Existing test asserted "No eligible photos were found on this device." and "Retry" — both changed by polished Empty branch to "No photos to clean up" / "Scan again"
- **Fix:** Updated test assertions to match new production copy
- **Files modified:** app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreenTest.kt
- **Verification:** Build passes, composable test aligned with production behavior
- **Committed in:** e0d5e00 (Task 2 commit)

---

**Total deviations:** 2 auto-fixed (1 blocking dependency, 1 test copy update)
**Impact on plan:** Both fixes necessary for compilation correctness and test validity. No scope creep.

## Issues Encountered
- Icons.Outlined.PhotoLibrary not available without material-icons-extended — resolved by adding the dependency (BOM handles versioning)

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- MEDIA-03 and REVW-04 requirements are satisfied; EntryScreen and ReviewScreen UI are production-ready for v1
- Instrumented composable tests (EntryScreenTest, ReviewScreenTest) require a connected device/emulator to run the new settings-hint and empty-grid assertions
- Phase 5 plans 02/03 can proceed with confidence in the hardened delete flow accuracy

## Self-Check: PASSED

All files exist and all task commits verified in git log.

---
*Phase: 05-hardening-launch-validation*
*Completed: 2026-03-15*
