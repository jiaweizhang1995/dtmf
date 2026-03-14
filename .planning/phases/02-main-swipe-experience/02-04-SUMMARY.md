---
phase: 02-main-swipe-experience
plan: 04
subsystem: ui-gap-closure
tags: [android, compose, coil, regression, gap-closure]
requires:
  - phase: 02-02
    provides: session-backed hero photo rendering, thumbnail context, and presentation mapping
  - phase: 02-03
    provides: reducer-backed swipe progression and gesture handling
provides:
  - one canonical active-photo contract for hero, metadata, and thumbnail highlighting
  - a visible blue Proceed treatment without the stale PREMIUM badge
  - regression coverage for hero-photo sync before and after swipe transitions
affects: [phase-03-session-controls-navigation, swipe-flow, review-handoff]
tech-stack:
  added: []
  patterns: [single active-photo projection, keyed hero-image recomposition, reducer-driven gesture test harness]
key-files:
  created:
    - .planning/phases/02-main-swipe-experience/02-04-SUMMARY.md
  modified:
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/PhotoPresentationMapper.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainUiState.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainViewModel.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainRoute.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/CurrentPhotoCard.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
    - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTokens.kt
    - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTest.kt
    - app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/SwipeGestureTest.kt
    - app/src/test/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailWindowTest.kt
key-decisions:
  - "Drive hero content, metadata, and thumbnail highlighting from one active-photo projection instead of separate current-photo derivations."
  - "Keep the Proceed affordance presentational in Phase 2 even while updating its copy and color treatment."
  - "Use a keyed hero AsyncImage and a reducer-backed test harness to verify hero sync across swipe transitions."
patterns-established:
  - "When the active hero image changes, recompose the hero image subtree on photo ID rather than relying on cached image request state."
  - "Use direct reducer-backed Compose state inside gesture instrumentation tests when route/ViewModel lifecycle wiring obscures the UI regression being verified."
requirements-completed: [SWIPE-01, UX-01]
duration: 33 min
completed: 2026-03-14
---

# Phase 2 Plan 04: Gap Closure Summary

**Canonical active-photo presentation, visible hero-image recovery, and blue Proceed treatment locked down with regression coverage**

## Performance

- **Duration:** 33 min
- **Started:** 2026-03-14T15:40:00.000Z
- **Completed:** 2026-03-14T16:13:28.000Z
- **Tasks:** 3
- **Files modified:** 10

## Accomplishments

- Unified the active-photo contract so hero content, metadata labels, and thumbnail highlighting all follow the same current item.
- Restored visible hero-image updates and replaced the stale `PREMIUM` badge with the approved blue `Proceed` affordance.
- Added regression coverage that checks hero visibility, hero/thumbnail sync, and the updated proceed copy before and after swipe transitions.

## Task Commits

Each task was committed atomically:

1. **Task 1: Rebuild the active-photo presentation contract around one canonical current item** - `4c8ff39` (`feat`)
2. **Task 2: Fix hero-photo rendering and the bottom-right affordance in the composed screen** - `955d26c` (`fix`)
3. **Task 3: Add regression coverage for hero visibility, active-photo sync, and updated copy** - `d427601` (`fix`)

## Files Created/Modified

- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/PhotoPresentationMapper.kt` - Carries `activePhotoIndex` and `activePhoto` as the canonical presentation source.
- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainUiState.kt` - Maps UI state from the same active-photo projection used by the thumbnail strip and hero card.
- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainViewModel.kt` - Rebuilds screen state from the reducer index without drifting from the active session item.
- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainRoute.kt` - Uses a stable route key for the active session.
- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/CurrentPhotoCard.kt` - Forces hero-image remounting on photo-ID changes so the visible hero content tracks the active item.
- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt` - Removes the stale `PREMIUM` badge and shows the approved blue `Proceed` pill.
- `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTokens.kt` - Tunes the proceed surface and hero overlay treatment for the reopened UAT issues.
- `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTest.kt` - Verifies `Proceed`, absence of `PREMIUM`, and initial hero/thumbnail alignment.
- `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/main/SwipeGestureTest.kt` - Verifies post-swipe hero advancement, thumbnail sync, and staged-state semantics.
- `app/src/test/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailWindowTest.kt` - Verifies the canonical mapper keeps active-photo metadata and thumbnail selection aligned.

## Decisions Made

- Kept the fix additive to Phase 2 instead of rewriting the earlier plans.
- Preserved the completed swipe reducer behavior from `02-03`; only the presentation binding and affordance treatment changed.
- Kept proceed navigation out of scope for this plan so Phase 3 still owns the actual handoff into review.

## Deviations from Plan

### Auto-fixed Issues

**1. [Blocking] Pre-created the Android test results directory before targeted connected tests**
- **Found during:** Task 3
- **Issue:** `connectedDebugAndroidTest` initially crashed in the UTP device info plugin before any test methods ran because the expected per-device output directory was missing.
- **Fix:** Created `app/build/outputs/androidTest-results/connected/debug/Pixel_9(AVD) - 16` before targeted connected-test runs in this environment.
- **Verification:** `mkdir -p 'app/build/outputs/androidTest-results/connected/debug/Pixel_9(AVD) - 16' && ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.main.MainScreenTest`

**2. [Non-blocking] Simplified gesture instrumentation to focus on the reducer-driven UI contract**
- **Found during:** Task 3
- **Issue:** The route/ViewModel harness made the swipe instrumentation failure mode harder to isolate than the reopened UI regression itself.
- **Fix:** Drove `MainScreen` from local reducer-backed Compose state in `SwipeGestureTest` while keeping the same swipe gestures and assertions.
- **Verification:** `mkdir -p 'app/build/outputs/androidTest-results/connected/debug/Pixel_9(AVD) - 16' && ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.main.SwipeGestureTest`

## Issues Encountered

- Targeted unit tests initially failed on stale Gradle test output metadata until `cleanTestDebugUnitTest` was run first.
- The Android UTP harness in this environment requires the connected-test output directory to exist before targeted test runs.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 2 is closed again with the hero-photo and proceed-treatment gaps resolved.
- Phase 3 can now focus on undo and proceed behavior rather than revisiting hero-photo data flow.

## Self-Check: PASSED

- Verified `./gradlew cleanTestDebugUnitTest testDebugUnitTest --tests '*ThumbnailWindowTest*'`
- Verified `./gradlew assembleDebug`
- Verified `mkdir -p 'app/build/outputs/androidTest-results/connected/debug/Pixel_9(AVD) - 16' && ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.main.MainScreenTest`
- Verified `mkdir -p 'app/build/outputs/androidTest-results/connected/debug/Pixel_9(AVD) - 16' && ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.main.SwipeGestureTest`
- Verified `4c8ff39`, `955d26c`, and `d427601` exist in git history.

---
*Phase: 02-main-swipe-experience*
*Completed: 2026-03-14*
