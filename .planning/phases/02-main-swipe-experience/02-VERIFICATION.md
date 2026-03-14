---
status: gaps_found
phase: "02"
phase_name: "Main Swipe Experience"
phase_goal: "Recreate the core look and feel of `main.jpg` with a smooth swipe-first main screen."
requirement_ids:
  - SWIPE-01
  - SWIPE-02
  - SWIPE-03
  - UX-01
verified_on: 2026-03-14
---

## GAPS FOUND

Phase 2 was initially verified and approved, but subsequent UAT uncovered open presentation gaps that supersede that sign-off until gap-closure plan `02-04` is executed.

## Evidence Summary

- `MainScreen.kt`, `CurrentPhotoCard.kt`, `ThumbnailStrip.kt`, and `MainScreenTokens.kt` implement the Phase 2 shell, hero photo, thumbnail rail, metadata chips, bottom controls, banner row, and the bottom-right affordance area that UAT later reopened.
- `MainViewModel.kt`, `MainUiState.kt`, `PhotoPresentationMapper.kt`, and `SwipeDecisionReducer.kt` implement session-backed current-photo presentation plus committed left/right swipe progression.
- `SwipePhotoCard.kt` keeps drag translation and rotation in the UI layer, derives thresholds from measured card width, and commits state only after an accepted swipe.
- Automated checks passed on 2026-03-14:
  - `./gradlew testDebugUnitTest --tests '*SwipeDecisionReducer*' --tests '*ThumbnailWindowTest*'`
  - `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.main.MainScreenTest`
  - `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.main.SwipeGestureTest`

## Requirement Coverage

### SWIPE-01

Status: Code-verified

- The main screen shows one current photo at a time with supporting thumbnails and primary controls.
- Evidence:
  - `PhotoPresentationMapper.map(...)` derives `currentPhoto` and `visibleThumbnails`.
  - `MainScreen(...)` renders thumbnail rail, metadata row, hero photo, bottom controls, banner row, and the bottom-right affordance area for the main swipe screen.
  - `MainScreenTest.readyStateUsesSessionBackedPhotoDescriptionsAndExpectedSectionOrder()` verifies hero-photo and thumbnail presence plus section ordering.

### SWIPE-02

Status: Code-verified

- Left swipe stages the current photo for deletion and advances to the next photo.
- Evidence:
  - `SwipeDecisionReducer.stageCurrentPhoto(...)` adds the current photo ID to `stagedPhotoIds` and advances `currentIndex`.
  - `SwipePhotoCard(...)` commits `onStagePhoto()` only after a left swipe crosses the threshold.
  - `SwipeDecisionReducerTest.stageCurrentPhoto_addsCurrentPhotoAndAdvances()` and `SwipeGestureTest.swipeLeft_stagesCurrentPhotoAndAdvances()` both pass.

### SWIPE-03

Status: Code-verified

- Right swipe skips the current photo without adding it to delete staging.
- Evidence:
  - `SwipeDecisionReducer.skipCurrentPhoto(...)` advances the index without mutating `stagedPhotoIds`.
  - `SwipePhotoCard(...)` commits `onSkipPhoto()` only after a right swipe crosses the threshold.
  - `SwipeDecisionReducerTest.skipCurrentPhoto_leavesStagedIdsUntouchedAndAdvances()` and `SwipeGestureTest.swipeRight_keepsStagedIdsUntouchedAndAdvances()` both pass.

### UX-01

Status: Reopened by UAT gaps

- The structure, hierarchy, color direction, and action placement are implemented to track `main.jpg`.
- Evidence:
  - `MainScreen.kt` reproduces the reference hierarchy: top bar, thumbnail rail, metadata chips, large hero card, bottom circular actions, low-emphasis banner row, and a bottom-right affordance area.
  - `MainScreenTokens.kt` centralizes spacing, colors, and shape tuning aligned to the reference’s dark chrome and accent treatment.
  - `MainScreenTest.readyStateShowsMainScreenSectionsAndAffordances()` confirms required visible regions and affordances exist.
- Reopened by UAT:
  - `02-UAT.md` records that the hero photo is not visibly rendering the actual active image and the bottom-right treatment still needs to change from `PREMIUM` to blue `Proceed`.

## Scope Boundary Check

- No code gap was found for the planned Phase 2 swipe flow.
- The bottom-right affordance remains presentational only and should stay that way during gap closure; `02-04` fixes its copy/treatment without pulling Phase 3 proceed navigation forward.
- Undo is also presentational only in Phase 2, which is consistent with `SWIPE-04` belonging to Phase 3.

## Open Gaps

- The center hero surface does not reliably render the real active session photo even though thumbnails load.
- The hero image can diverge from the active thumbnail/session selection.
- The bottom-right area still needs the approved blue `Proceed` treatment instead of the stale `PREMIUM` label.

## Verdict

`SWIPE-02` and `SWIPE-03` remain code-verified, but `SWIPE-01` and `UX-01` are reopened by UAT and now require gap-closure plan `02-04`. Phase 2 should not be treated as closed until that plan is executed and re-verified.
