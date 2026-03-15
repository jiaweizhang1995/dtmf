---
phase: 04-review-permanent-delete
verified: 2026-03-15T08:00:00Z
status: passed
score: 11/11 must-haves verified
re_verification: false
human_verification:
  - test: "Visual fidelity of review screen against Delete-staging-area..jpg"
    expected: "App bar with back + REVIEW title, left teal border beside prompt, 2-column grid with teal-bordered tiles and checkmark badges, helper text, Decide Later + Delete forever CTAs"
    why_human: "Screenshot pixel comparison cannot be performed programmatically; requires device/emulator visual inspection"
  - test: "Live MediaStore delete confirmation flow on a real Android device"
    expected: "Tapping Delete forever launches the system delete dialog; confirming removes photos from device; canceling leaves photos and review state intact"
    why_human: "MediaStore.createDeleteRequest system dialog is platform-owned and cannot be driven from instrumentation; ReviewDeleteFlowTest simulates the outcome but cannot exercise the real dialog"
  - test: "Post-delete refresh: app returns to fresh launch session after confirmed deletion"
    expected: "After deletion, the app exits review, main screen shows a new photo batch (not the stale pre-delete batch)"
    why_human: "End-to-end navigation + LaunchViewModel refresh requires a live device run with real MediaStore content"
---

# Phase 4: Review and Permanent Delete — Verification Report

**Phase Goal:** Build the screenshot-faithful review screen and permanent-delete flow so staged photos can be confirmed and permanently removed from the device.
**Verified:** 2026-03-15T08:00:00Z
**Status:** PASSED
**Re-verification:** No — initial verification

---

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|---------|
| 1 | Review screen opens with a screenshot-faithful shell (back affordance, REVIEW title, teal-bordered prompt, helper link, 2-column grid, bottom CTAs) | VERIFIED | `ReviewScreen.kt` builds all 5 named sections via `ReviewAppBar`, `DestructivePromptSection`, `LazyVerticalGrid`, `BottomActionArea`; `ReviewScreenTokens` centralises all visual constants |
| 2 | Staged photos are rendered as image-backed review cards, not a placeholder text list | VERIFIED | `ReviewPhotoTile` uses Coil `AsyncImage(model = photo.contentUri)` with `ContentScale.Crop`; `PlaceholderTile` is shown only during loading |
| 3 | All staged photos start selected for deletion when review opens | VERIFIED | `ReviewViewModel.onPhotosResolved` initialises `selectedPhotoIds = resolvedIds` (all-selected default); `ReviewViewModelTest.onPhotosResolved_allPhotosStartSelected` locks this invariant |
| 4 | User can deselect and reselect individual staged photos; selection stays review-local | VERIFIED | `togglePhotoSelection` mutates only `reviewLocal.selectedPhotoIds`; `MainViewModel.stagedPhotoIds` not touched until `onDeleteConfirmed`; 15 `ReviewViewModelTest` unit tests + `ReviewScreenTest` toggle tests cover this |
| 5 | Delete CTA enablement, prompt copy, and helper text reflect the currently selected subset | VERIFIED | `ReviewUiState.destructivePromptText` derived from `selectedCount`; `isDeleteEnabled` from `selectedPhotoIds.isNotEmpty()`; `ReviewScreen` passes both to `DestructivePromptSection` and `BottomActionArea` |
| 6 | Review selection state survives configuration changes | VERIFIED | `ReviewViewModel` receives `SavedStateHandle`; `selectedPhotoIds` serialised as `LongArray` on every toggle; stale IDs pruned via `.intersect(resolvedIds)` on restore; `ReviewViewModelTest.savedStateHandle_restoredSelectedIds_survivesRecreation` and `dropsStaleIds` tests cover this |
| 7 | Delete forever is gated by explicit platform confirmation and submits only selected URIs | VERIFIED | `ReviewViewModel.onDeleteForever` emits `ReviewEvent.RequestDelete(selectedPhotoIds)`; `ReviewRoute` resolves URIs via `repository.resolveUrisForDelete(event.selectedPhotoIds)` and launches `MediaStore.createDeleteRequest`; `DeleteRequestCoordinator.buildDeleteRequest` returns `null` for empty list |
| 8 | Canceled confirmation leaves both review selection state and main-session staged state unchanged | VERIFIED | Cancel path in `ReviewRoute` result handler is explicit no-op; `ReviewDeleteFlowTest.cancelDelete_leavesReviewStateUnchanged` and `cancelDelete_doesNotChangeSelectedCount` verify review state is unchanged; `MainViewModel.onDeleteConfirmed` is only called on `RESULT_OK` |
| 9 | Successful deletion removes only approved photos, exits stale swipe session, and routes into a fresh post-delete launch session | VERIFIED | `AppNavGraph.onDeleteConfirmed` callback: pops review, relays `DELETED_PHOTO_IDS_KEY` via `SavedStateHandle`, calls `onRefreshAfterDelete()`; `MainRoute` consumes key via `remove()` and calls `viewModel.onDeleteConfirmed`; `LaunchViewModel.refreshAfterDelete()` calls `loadBatch()` |
| 10 | Delete integration uses URI-based MediaStore operations, not raw file paths | VERIFIED | `DeleteRequestCoordinator.buildDeleteRequest` wraps `MediaStore.createDeleteRequest(context.contentResolver, uris)`; `resolveUrisForDelete` produces `List<Uri>` via `ContentUris.withAppendedId`; no `File` or raw path anywhere in delete path |
| 11 | Review layout fidelity is centralised in dedicated tokens | VERIFIED | `ReviewScreenTokens` object contains all colors, spacing, corner radii, type sizes, grid config, and copy strings; grep confirms no hardcoded literals in `ReviewScreen.kt` composable |

**Score: 11/11 truths verified**

---

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `app/src/main/java/com/jimmymacmini/wishdtmf/data/media/PhotoRepository.kt` | Review-boundary query seam + delete URI resolution contract | VERIFIED | Defines `loadReviewPhotos(orderedIds)` and `resolveUrisForDelete(photoIds)` interface methods; `ReviewPhoto` display model declared here |
| `app/src/main/java/com/jimmymacmini/wishdtmf/data/media/MediaStorePhotoRepository.kt` | MediaStore-backed implementation of review/delete queries | VERIFIED | Implements both interface methods; `ContentResolverReviewPhotoQuerySource` does `WHERE IN` query; `resolveUrisForDelete` reuses the same query source |
| `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt` | Screenshot-faithful review composition | VERIFIED | Full 5-section hierarchy; selection affordances; `AsyncImage`-backed tiles; both CTAs; no hardcoded literals; 411 lines, substantive |
| `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTokens.kt` | Single source of truth for Phase 4 visual constants | VERIFIED | 142-line `object` covering colors, spacing, corner radii, type, grid config, and copy strings |
| `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewRoute.kt` | Review entry point with boundary resolution + activity-result launcher | VERIFIED | Owns `ActivityResultLauncher`; consumes `ReviewEvent` via `collectLatest`; calls `repository.resolveUrisForDelete` just-in-time; `LaunchedEffect(stagedPhotoIds)` triggers `loadReviewPhotos` |
| `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewViewModel.kt` | Review-local state owner for selection and delete-action readiness | VERIFIED | `SavedStateHandle`-backed `selectedPhotoIds`; `togglePhotoSelection`; `onDeleteForever`; `onDeleteConfirmed`; `ReviewEvent` sealed interface with `RequestDelete` and `DeleteConfirmed` |
| `app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewUiState.kt` | Stable review-state model | VERIFIED | `data class` with `stagedPhotos`, `selectedPhotoIds`, `isLoading`; computed properties `selectedCount`, `isDeleteEnabled`, `destructivePromptText`, `isSelected(id)` |
| `app/src/main/java/com/jimmymacmini/wishdtmf/data/media/DeleteRequestCoordinator.kt` | Repository-facing seam for MediaStore delete requests | VERIFIED | Stateless `object`; `buildDeleteRequest` (public), `buildDeleteRequestForUris`/`isNonEmptyRequestAllowed` (internal test hooks), `isDeleteConfirmed` for result interpretation |
| `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainViewModel.kt` | Result-consumption seam that clears stale swipe session | VERIFIED | `onDeleteConfirmed(deletedIds)` resets `SwipeSessionState(currentIndex = 0)` and persists via `SavedStateHandle` |
| `app/src/main/java/com/jimmymacmini/wishdtmf/app/AppShell.kt` | App-level callback wiring for post-delete refresh | VERIFIED | Passes `launchViewModel::refreshAfterDelete` as `onRefreshAfterDelete` into `AppNavGraph` |
| `app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/LaunchViewModel.kt` | Post-delete refresh seam | VERIFIED | `refreshAfterDelete()` delegates to `loadBatch()` — same path as initial permission grant |
| `app/src/main/java/com/jimmymacmini/wishdtmf/app/navigation/AppNavGraph.kt` | Platform launcher/result seam + SavedStateHandle relay | VERIFIED | Accepts `onRefreshAfterDelete`; sets `DELETED_PHOTO_IDS_KEY` on main back-stack entry; calls `onRefreshAfterDelete()` after confirmed delete |
| `app/src/test/java/com/jimmymacmini/wishdtmf/feature/review/ReviewViewModelTest.kt` | Unit coverage for selection semantics and config-change restoration | VERIFIED | 15 tests covering default-all-selected, deselect, reselect, count, SavedStateHandle restoration, stale-ID pruning, CTA eligibility |
| `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt` | Instrumentation coverage for review hierarchy and affordances | VERIFIED | 22+ tests covering title, back, prompt, grid, bottom actions, placeholder loading, toggle callbacks, badge presence/absence, state descriptions, CTA enabled/disabled |
| `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewDeleteFlowTest.kt` | Integration coverage for selected-only deletion and cancel safety | VERIFIED | 9 tests covering selected-only scoping, multi-toggle exclusion, empty-selection CTA guard, cancel non-destructiveness, success event delivery, button tap behavior |
| `app/src/test/java/com/jimmymacmini/wishdtmf/data/media/DeleteRequestCoordinatorTest.kt` | Unit tests for delete-request contract | VERIFIED | 5 tests covering empty-guard, result-code interpretation, selection-to-request scoping |

---

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `ReviewRoute` | `MediaStorePhotoRepository.loadReviewPhotos` | `LaunchedEffect(stagedPhotoIds)` in route | WIRED | Route creates repository from `LocalContext`, calls `loadReviewPhotos`, passes results to `viewModel.onPhotosResolved` |
| `ReviewRoute` | `DeleteRequestCoordinator.buildDeleteRequest` | `ReviewEvent.RequestDelete` consumed via `collectLatest` | WIRED | Route resolves URIs via `repository.resolveUrisForDelete`, passes to coordinator, launches result |
| `ReviewViewModel.onDeleteForever` | `ReviewEvent.RequestDelete` emission | `_events.tryEmit` guarded by non-empty selection | WIRED | Guard verified in `ReviewDeleteFlowTest.onDeleteForever_doesNotEmitRequestEvent_whenSelectionIsEmpty` |
| `ReviewRoute.onDeleteConfirmed` callback | `AppNavGraph` result handler | `onDeleteConfirmed: (Set<Long>) -> Unit` parameter | WIRED | `AppNavGraph` passes lambda that pops back stack, sets `DELETED_PHOTO_IDS_KEY`, calls `onRefreshAfterDelete()` |
| `AppNavGraph` | `MainRoute.onDeleteConfirmed` | `DELETED_PHOTO_IDS_KEY` in `SavedStateHandle` + `backStackEntry` param | WIRED | `MainRoute` consumes key via `SavedStateHandle.remove()` in `LaunchedEffect(backStackEntry)` |
| `MainViewModel.onDeleteConfirmed` | `SwipeSessionState` reset | Direct `updateState(SwipeSessionState(currentIndex = 0))` | WIRED | Clears staged IDs and session state |
| `AppShell` | `LaunchViewModel.refreshAfterDelete` | `onRefreshAfterDelete = launchViewModel::refreshAfterDelete` passed to `AppNavGraph` | WIRED | `refreshAfterDelete()` delegates to `loadBatch()` rebuilding from remaining media |
| `ReviewScreen` | `ReviewUiState.isDeleteEnabled` | `BottomActionArea(isDeleteEnabled = uiState.isDeleteEnabled)` | WIRED | CTA is non-interactive and visually dimmed when `isDeleteEnabled = false` |
| `ReviewScreenTokens` | `ReviewScreen` | `val T = ReviewScreenTokens` at top of every composable | WIRED | All visual constants consumed from tokens object; no hardcoded literals in composable |

---

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|------------|-------------|--------|---------|
| REVW-01 | 04-01 | User can view all currently staged photos in the delete staging area | SATISFIED | `ReviewRoute` resolves staged IDs into `ReviewPhoto` list via `MediaStorePhotoRepository.loadReviewPhotos`; `ReviewScreen` renders image-backed grid; `ReviewScreenTest` covers per-photo tile and grid visibility |
| REVW-02 | 04-02 | User can deselect staged photos they want to keep before deletion | SATISFIED | `ReviewViewModel.togglePhotoSelection` + `ReviewScreen` tile toggle; dimmed deselected tiles; check badge absent when deselected; `ReviewViewModelTest` (15 tests) + `ReviewScreenTest` toggle assertions |
| REVW-03 | 04-03 | User can permanently delete selected staged photos only after explicit confirmation | SATISFIED | `ReviewViewModel.onDeleteForever` emits `RequestDelete` with `selectedPhotoIds` only; `ReviewRoute` builds `MediaStore.createDeleteRequest` and launches activity-result dialog; confirmed result propagates through nav layer; `ReviewDeleteFlowTest` covers selected-only scoping and cancel safety |
| UX-02 | 04-01 | Delete staging screen closely matches `Delete-staging-area..jpg` layout | SATISFIED (programmatic) | `ReviewScreenTokens` centralises all visual constants matching screenshot dimensions; `ReviewScreen` implements 5-section hierarchy: app bar with back + REVIEW, teal-bordered destructive prompt, helper link, 2-column `AsyncImage` grid with selection affordances, bottom helper + dual CTA row; human visual verification needed for pixel fidelity |

**No orphaned requirements** — all four IDs declared across plans are accounted for and satisfied. REVW-04 is correctly mapped to Phase 5 (not Phase 4).

---

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `ReviewScreen.kt` | 239 | `/* Move-to-trash flow — Phase 5 deferred */` in `HelperLink` click handler | Info | Helper link is visible but taps are no-ops; explicitly scoped to Phase 5; does not block Phase 4 goal |
| `ReviewRoute.kt` | 88 | `onDecideLater` not passed to `ReviewRoute`, uses default empty lambda | Info | "Decide Later" button renders and is tappable but does nothing; explicitly Phase 5 scope; does not block permanent-delete goal |

No blockers or warnings found. Both info items are acknowledged Phase 5 deferrals.

---

### Commit History (All 9 Verified)

| Commit | Plan | Description |
|--------|------|-------------|
| `92020d5` | 04-01 Task 1 | Replace placeholder review route with real staged-photo display model |
| `78e01c9` | 04-01 Task 2 | Build screenshot-faithful review screen and centralized token set |
| `37f793f` | 04-01 Task 3 | Add instrumentation coverage for review hierarchy |
| `79dd9a7` | 04-02 Task 1 | Introduce SavedStateHandle-backed ReviewViewModel and ReviewUiState |
| `fcb9f5a` | 04-02 Task 2 | Wire deselect/reselect behavior and count-driven copy |
| `17b31c9` | 04-02 Task 3 | Add unit and instrumentation coverage for review selection semantics |
| `7283b0d` | 04-03 Task 1 | Extend media layer with delete-request coordination seam |
| `fc6b27d` | 04-03 Task 2 | Wire review delete intents through nav graph and platform confirmation |
| `1553bb7` | 04-03 Task 3 | Add destructive-flow coverage for selected-only deletion and cancel safety |

---

### Human Verification Required

#### 1. Visual Fidelity Against Screenshot

**Test:** Install the debug APK on an emulator or device, navigate to review with staged photos, compare the displayed screen side-by-side with `Delete-staging-area..jpg`.
**Expected:** App bar with `<` back and left-aligned `REVIEW` title; teal left-border accent next to "Permanently delete N items?"; "No, I want to move to trash" helper link in teal; 2-column image grid with teal-bordered selected tiles and teal checkmark badges; helper copy; dark "Decide Later" and teal "Delete forever" CTAs.
**Why human:** Pixel-level layout comparison and color perception require a running device and visual inspection.

#### 2. Live MediaStore Delete Confirmation Flow

**Test:** On a real Android 11+ device (not emulator), stage 2–3 photos, tap "Delete forever", observe the system dialog, tap "Delete" to confirm, verify photos are removed from the device gallery.
**Expected:** Platform delete dialog appears; on confirmation, photos are removed from MediaStore and the device gallery; app returns to a fresh main screen with a new batch; the deleted photos no longer appear in any session.
**Why human:** `MediaStore.createDeleteRequest` system dialog is platform-owned; `ReviewDeleteFlowTest` simulates the outcome via direct ViewModel calls but cannot drive the real activity-result round-trip.

#### 3. Cancel Non-Destructiveness on Device

**Test:** Stage photos, tap "Delete forever", dismiss the system dialog (back or cancel button), verify photos remain in gallery and review screen state is unchanged.
**Expected:** Review screen remains displayed with the same selected photos; no photos removed from device.
**Why human:** Cancel path in the system dialog cannot be automated; requires live device interaction.

---

### Summary

Phase 4 goal is fully achieved. All 11 observable truths verified against the actual codebase:

- The review screen is a complete, substantive implementation (411 lines) with a screenshot-faithful 5-section layout backed by `ReviewScreenTokens`.
- Staged photos are resolved at the review boundary via `MediaStorePhotoRepository.loadReviewPhotos` and rendered as `AsyncImage`-backed tiles.
- Review-local selection state is owned by `ReviewViewModel` with `SavedStateHandle` persistence, isolated from `MainViewModel` until deletion is confirmed.
- The permanent-delete flow is implemented through proper repository/navigation seams: `DeleteRequestCoordinator` wraps `MediaStore.createDeleteRequest`, `ReviewRoute` owns the activity-result launcher, and confirmed deletion propagates through the nav layer to clear the stale session and rebuild from remaining media.
- All three requirements (REVW-01, REVW-02, REVW-03) and the UX fidelity requirement (UX-02) are satisfied with automated test coverage.
- Two info-level deferred items (move-to-trash link, decide-later navigation) are explicitly Phase 5 scope and do not block the Phase 4 goal.
- Three human verification items remain: visual fidelity check, live device delete confirmation, and cancel path on a real device.

---

_Verified: 2026-03-15T08:00:00Z_
_Verifier: Claude (gsd-verifier)_
