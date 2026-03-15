# Phase 5: Hardening & Launch Validation - Research

**Researched:** 2026-03-15
**Domain:** Android Compose error-state UX, delete-flow edge-case hardening, lifecycle resilience, device-variance testing, and visual acceptance against reference screenshots
**Confidence:** HIGH

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| MEDIA-03 | User sees a clear empty or unavailable state when the device has no eligible photos or the app cannot build a valid session | LaunchUiState.Empty and LaunchUiState.Error already exist in LaunchViewModel; EntryScreen already renders both states with functional Retry buttons; Phase 5 must ensure the rendered copy, visual treatment, and recovery path are clear and complete rather than building new state machinery |
| REVW-04 | If deletion is cancelled or partially fails, the app preserves non-deleted items and reflects the final result accurately | ReviewViewModel cancel path is already non-mutating by construction; the partial-failure surface (MediaStore returning success for a subset of URIs) has no detection layer yet; Phase 5 must add detection of partial-success outcomes and ensure the post-delete refresh accurately reflects what was actually removed |
</phase_requirements>

## Summary

Phase 5 closes the two remaining v1 requirements (MEDIA-03 and REVW-04) and then validates the whole app against the real-world conditions that unit tests cannot cover: device-variance, configuration-change sequences, performance with large batches, and visual alignment with the two reference screenshots.

The codebase entering Phase 5 is functionally complete. Every screen, state machine, and navigation seam is built. The work is therefore **hardening, polish, and evidence collection** rather than new feature development.

Three areas need substantive implementation work:

1. **MEDIA-03 — Empty and denied state polish.** `LaunchUiState.Empty`, `LaunchUiState.NeedsPermission`, and `LaunchUiState.Error` already exist and render in `EntryScreen`, but the current copy and visual treatment are minimal ("No eligible photos were found on this device." + a plain Retry button). The requirement calls for a _clear_ state, which means intentional copy, a recognizable icon or illustration, and a retry/settings path that is easy to discover.

2. **REVW-04 — Partial-delete accuracy.** The cancel path is provably correct: `ReviewViewModel.onDeleteForever()` only emits `RequestDelete`, and neither `ReviewViewModel` nor `MainViewModel` mutates state until `onDeleteConfirmed()` is called with an explicit ID set. What is not yet handled is the case where the platform reports RESULT_OK but MediaStore actually deleted fewer items than were requested (common when a URI is no longer accessible at deletion time). The current flow treats any RESULT_OK as "all selected items deleted", which may leave stale entries in the LaunchSession refresh and misrepresent the library to the user. Phase 5 must add a post-delete re-query to determine what was actually deleted and pass that accurate set to the post-delete refresh.

3. **Launch validation — lifecycle, device variance, and visual acceptance.** Phase 4 UAT was run on an emulator only; the blockers/concerns section of STATE.md explicitly flags the need for a real-device validation pass. Phase 5 plan 05-02 and 05-03 must cover configuration-change sequences, background/foreground transitions, and side-by-side visual comparison against `main.jpg` and `Delete-staging-area..jpg`.

**Primary recommendation:** Keep plan 05-01 purely focused on state hardening (MEDIA-03 polish + REVW-04 partial-failure detection), plan 05-02 on lifecycle/device/performance validation (automated and manual), and plan 05-03 on UI polish + final acceptance sign-off.

## Standard Stack

All libraries are already in the project. Phase 5 introduces no new dependencies.

### Core (already in project)
| Library | Purpose in Phase 5 |
|---------|-------------------|
| Jetpack Compose + Material 3 | Polish empty/denied state composables; add icon or illustration treatment |
| `LaunchViewModel` + `LaunchUiState` | Already owns all entry states; Phase 5 adds visual polish only |
| `ReviewViewModel` + `DeleteRequestCoordinator` | Phase 5 adds post-delete re-query for partial-failure detection |
| `MediaStorePhotoRepository.loadEligiblePhotos()` | Re-used as the post-delete accuracy check (query remaining media) |
| JUnit 4 + AndroidX Compose UI Test | Already in use; Phase 5 adds state-coverage tests for empty/denied and partial-delete paths |
| `./gradlew connectedDebugAndroidTest` | Already the instrumented test command |

### Supporting
| Library | Purpose |
|---------|---------|
| Coil 3 (`AsyncImage`) | Already renders all photo content; no change needed |
| Navigation Compose | No new routes; existing graph handles all navigation |
| `SavedStateHandle` | Already persists all relevant state; Phase 5 validates survival through lifecycle events |

**Installation:** None required. All dependencies are already resolved.

## Architecture Patterns

### Current Code Reality Entering Phase 5

**What works correctly and must not be disturbed:**
- `LaunchUiState.Empty` and `LaunchUiState.Error` — state machine correct, UI minimal
- `LaunchUiState.NeedsPermission(showSettingsHint = true)` — permission-denied path already wired through `PermissionCoordinator`
- `ReviewViewModel` cancel path — non-mutating by construction; no changes needed
- `onDeleteConfirmed` pipeline: `ReviewRoute` → `AppNavGraph.onDeleteConfirmed` → `navController.popBackStack()` + `onRefreshAfterDelete()` → `LaunchViewModel.refreshAfterDelete()` → `loadBatch()`
- `MainViewModel.onDeleteConfirmed()` — clears swipe state; called via `DELETED_PHOTO_IDS_KEY` relay in `MainRoute`
- All `SavedStateHandle` persistence — already tested; survives rotation

**What is incomplete or missing:**

| Gap | Current State | Required State |
|-----|--------------|----------------|
| Empty state visual quality | Plain text body + generic Retry button | Clear icon/illustration + specific copy + accessible retry path |
| Denied state visual quality | Plain text body, no settings deep-link affordance | Clear denied copy + "Open settings" deep-link affordance when `showSettingsHint = true` |
| Zero-staged-item review entry | `onProceedToReview()` guards against it; UI shows "Nothing staged yet" via `proceedMessage`; the review route would render an empty grid if reached | Covered by existing guard; Phase 5 must confirm the empty-grid path shows a clear message rather than a blank screen |
| Partial-delete result accuracy | RESULT_OK treated as complete success; no re-query | Post-delete re-query to verify which IDs actually remain absent from MediaStore |
| Real-device validation | Only emulator tested | At least one real-device pass required per STATE.md blocker |

### Pattern 1: Polish Entry States Without New State Machinery

`EntryScreen` already has a complete `when (uiState)` branch for every `LaunchUiState` variant. Phase 5 should extend the visual treatment inside each existing branch rather than adding new state types or new routes.

Additions that belong in plan 05-01:
- Add a simple `Icon` composable (or local SVG/drawable resource) to the `Empty` branch
- Add settings deep-link affordance to the `NeedsPermission(showSettingsHint = true)` branch using `Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)`
- Improve copy specificity: "No eligible photos found" is technically correct; Phase 5 copy should explain what "eligible" means (not hidden, not trashed, not zero-byte)
- Add a loading indicator (CircularProgressIndicator) to the `LoadingBatch` branch if one is not already present

```kotlin
// Pattern for settings deep-link (Android standard)
val intent = Intent(
    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
    Uri.fromParts("package", context.packageName, null)
)
context.startActivity(intent)
```

### Pattern 2: Partial-Delete Accuracy via Post-Delete Re-Query

The existing flow assumes RESULT_OK means all selected URIs were deleted. Android does not guarantee this — individual URIs can fail silently if the file was moved, already deleted, or on a locked volume.

The correct Phase 5 pattern is a post-confirmation re-query to determine which IDs are actually absent:

```kotlin
// In ReviewRoute or a new helper, after RESULT_OK:
// 1. Re-query the IDs that were submitted for deletion
val stillPresent = repository.loadReviewPhotos(submittedIds.toList()).map { it.id }.toSet()
val actuallyDeleted = submittedIds - stillPresent
// 2. Pass actuallyDeleted (not submittedIds) to onDeleteConfirmed
viewModel.onDeleteConfirmed(actuallyDeleted)
```

This keeps `ReviewViewModel` unchanged; only `ReviewRoute`'s RESULT_OK handler gets the re-query step before calling `onDeleteConfirmed`.

**Important:** The re-query must happen on a background/IO dispatcher, not on the main thread. Use a `LaunchedEffect` coroutine scope or a `viewModelScope.launch` block.

### Pattern 3: Zero-Staged-Item Review Entry Guard

`ReviewRoute` receives `stagedPhotoIds` from the nav graph. If this list is empty (which should not happen in normal flow given `MainViewModel.onProceedToReview()` guards it), `ReviewScreen` renders an empty grid — the `LazyVerticalGrid` simply has no items and the destructive prompt reads "Permanently delete 0 items?".

Phase 5 plan 05-01 should add a dedicated empty-grid message branch in `ReviewScreen` to ensure the user sees a clear explanation rather than a blank grid area with a disabled delete button.

```kotlin
// In ReviewScreen, inside the LazyVerticalGrid composable:
if (uiState.stagedPhotos.isEmpty() && stagedPhotoIds.isEmpty() && !uiState.isLoading) {
    // Render an empty-state item spanning both columns
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = "No photos staged for deletion.",
            modifier = Modifier.padding(T.HorizontalPadding).testTag(ReviewScreenTags.EmptyGridMessage),
            color = T.SubtleTextColor,
        )
    }
}
```

### Pattern 4: Lifecycle and Configuration-Change Validation

All state is already backed by `SavedStateHandle`. The Phase 5 plan 05-02 validation pass should confirm the following sequences work correctly end-to-end:

| Sequence | Expected Result | How to Verify |
|----------|----------------|----------------|
| Rotate device mid-swipe session | Active card, staged count, and undo availability preserved | Manual on device + existing MainScreenTest |
| Rotate device while review is open with deselected items | Deselected items remain deselected after rotation | ReviewViewModelTest already covers SavedStateHandle; manual confirms on device |
| Background app during MediaStore delete confirmation dialog | Return to review with state intact (cancel path) | Manual on device |
| Return to app after system kills process (via "Don't keep activities") | App returns to entry and rebuilds session | Manual on device |
| Post-delete: app rebuilds empty library | LaunchUiState.Empty rendered correctly | Unit test + manual |
| Post-delete: permission revoked while loading | LaunchUiState.NeedsPermission rendered | Manual on device (revoke via settings mid-use) |

### Pattern 5: Visual Acceptance Validation

The two reference screenshots are `main.jpg` (main swipe screen) and `Delete-staging-area..jpg` (review screen). Phase 5 plan 05-03 is the explicit sign-off gate.

The acceptance check is manual — automated screenshot tests are not in the stack and would be brittle. The planner should define a specific checklist for each screen:

**Main screen (`main.jpg`) checklist:**
- Dark background fills the full viewport
- Top bar: back chevron, centered title, overflow menu icon
- Thumbnail strip with correct active-photo highlight
- Metadata chip row (info icon, file size, MIME type, position)
- Hero photo card with rounded corners occupying the correct proportion
- Action row: delete, undo (enabled/disabled), skip, confirm — all in correct positions
- Banner row with "Proceed" text affordance
- Blue "Proceed" pill affordance at bottom-right with supporting copy

**Review screen (`Delete-staging-area..jpg`) checklist:**
- Dark background
- App bar: back arrow `<`, `REVIEW` title
- Left-border destructive prompt: "Permanently delete N items?"
- Teal helper link below prompt
- 2-column image grid with teal borders and checkmark badges on selected tiles; deselected tiles dimmed
- Bottom helper copy
- Two full-width CTAs: `Decide Later` (grey) and `Delete forever` (teal when enabled)

## Don't Hand-Roll

| Problem | Don't Build | Use Instead |
|---------|-------------|-------------|
| Settings deep-link | Custom settings navigation | Standard `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` intent |
| Post-delete verification | File-system stat checks | `MediaStorePhotoRepository.loadReviewPhotos()` re-query on the same IDs |
| Screenshot comparison | Custom bitmap diffing | Manual visual comparison against reference images (sufficient for this scope) |
| Lifecycle test scaffolding | Custom Activity recreation helpers | AndroidX test `ActivityScenario.recreate()` if needed; existing compose rule covers most cases |
| New state types | New sealed class variants | Existing `LaunchUiState` variants cover all MEDIA-03 cases |

## Common Pitfalls

### Pitfall 1: Assuming RESULT_OK Means Complete Success
**What goes wrong:** The post-delete flow calls `onDeleteConfirmed(selectedPhotoIds)` and removes all selected IDs from app state, but MediaStore may have deleted only a subset (e.g., one URI was on an unmounted SD card). The next session then tries to load photos that no longer exist.
**Why it happens:** `MediaStore.createDeleteRequest` returns RESULT_OK even if some URIs could not be deleted on some Android versions.
**How to avoid:** Re-query the submitted IDs after RESULT_OK to determine which are actually absent. Only pass confirmed-absent IDs to `onDeleteConfirmed`.
**Warning signs:** Post-delete session contains Coil 404 image errors or MediaStore queries returning empty results for IDs in the batch.

### Pitfall 2: Settings Deep-Link Not Available in All Contexts
**What goes wrong:** `startActivity(settingsIntent)` throws `ActivityNotFoundException` in restrictive environments or profile-managed devices.
**How to avoid:** Wrap in `try/catch ActivityNotFoundException` and fall back gracefully (e.g., show a toast or show the retry-permission button only).
**Warning signs:** Crash on devices with work profiles or kiosk mode.

### Pitfall 3: Zero-Staged Review Entry Not Tested
**What goes wrong:** The guard in `MainViewModel.onProceedToReview()` prevents normal zero-staged entry, but deep-link testing, back-stack manipulation, or future changes could reach `ReviewRoute` with an empty list. The screen shows a disabled delete button with "Permanently delete 0 items?" — confusing but not a crash.
**How to avoid:** Add an explicit empty-grid composable branch and a test case that asserts the empty message is displayed when both `stagedPhotoIds` and `uiState.stagedPhotos` are empty.

### Pitfall 4: Stale Delete State After Process Death
**What goes wrong:** If the app process is killed while the system delete confirmation dialog is visible, the activity result is never delivered. On restart, `ReviewViewModel` re-initialises with the original selection, but the underlying MediaStore may have partially deleted some photos already.
**How to avoid:** On `ReviewRoute` re-entry (identified by `stagedPhotoIds` arriving again via `LaunchedEffect(stagedPhotoIds)`), the existing `loadReviewPhotos` call silently drops IDs that are no longer in MediaStore. This is already correct behavior — stale IDs are pruned automatically. Document this as expected behavior in plan 05-02.
**Warning signs:** None — this is already handled by the intersection logic in `ReviewViewModel.onPhotosResolved()`.

### Pitfall 5: `refreshAfterDelete` Called Before Session Clears
**What goes wrong:** `onRefreshAfterDelete()` triggers `LaunchViewModel.loadBatch()` immediately, but `MainViewModel.onDeleteConfirmed()` may not have been called yet if the relay through `DELETED_PHOTO_IDS_KEY` + `MainRoute` hasn't completed. The next session could include deleted IDs if MediaStore propagation is slow.
**How to avoid:** `loadBatch()` runs on the IO dispatcher and queries MediaStore fresh — by the time it completes, MediaStore propagation is typically complete. If not, the `PhotoQueryMapper.mapEligiblePhoto` filter will drop pending/zero-byte entries anyway. No additional delay is needed, but the behavior should be confirmed on a real device under test.

### Pitfall 6: Empty State Copy Is Technically Correct But User-Unfriendly
**What goes wrong:** "No eligible photos were found on this device." leaves users wondering what "eligible" means and whether they can do anything about it.
**How to avoid:** Phase 5 copy should specify: photos must be visible (not hidden), not in Trash, and not currently uploading. Provide a Retry button with a clear label explaining what retry does ("Scan again").

## Code Examples

### Entry State — Settings Deep-Link (Denied State)
```kotlin
// Source: Android developer docs — ACTION_APPLICATION_DETAILS_SETTINGS
// Pattern to add inside EntryScreen's NeedsPermission(showSettingsHint = true) branch
val context = LocalContext.current
Button(
    modifier = Modifier.fillMaxWidth(),
    onClick = {
        runCatching {
            context.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    },
) {
    Text("Open app settings")
}
```

### Post-Delete Partial-Failure Detection
```kotlin
// In ReviewRoute's deleteLauncher callback, after isDeleteConfirmed returns true:
if (DeleteRequestCoordinator.isDeleteConfirmed(result.resultCode)) {
    val submittedIds = viewModel.uiState.value.selectedPhotoIds
    // Re-query to find which IDs are actually gone from MediaStore
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        val stillPresent = repository.loadReviewPhotos(submittedIds.toList())
            .map { it.id }.toSet()
        val actuallyDeleted = submittedIds - stillPresent
        viewModel.onDeleteConfirmed(actuallyDeleted)
    }
}
```

Note: `rememberCoroutineScope()` must be called at the composable level, not inside the lambda. The actual implementation should hoist the scope from the route's composable body.

### Empty-Grid Message in ReviewScreen
```kotlin
// Add inside the LazyVerticalGrid's content lambda:
if (!uiState.isLoading && uiState.stagedPhotos.isEmpty() && stagedPhotoIds.isEmpty()) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = "No photos staged for deletion.",
            color = T.SubtleTextColor,
            modifier = Modifier
                .padding(T.HorizontalPadding)
                .testTag(ReviewScreenTags.EmptyGridMessage),
        )
    }
}
```

### LoadingBatch Visual Indicator
```kotlin
// EntryScreen LoadingBatch branch — add CircularProgressIndicator
LaunchUiState.LoadingBatch -> {
    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
    Text(
        text = "Preparing a fresh photo batch...",
        style = MaterialTheme.typography.bodyLarge,
    )
}
```

## State of the Art

| Old Approach | Current Approach | Impact for Phase 5 |
|--------------|------------------|-------------------|
| ContentResolver.delete() for shared media | MediaStore.createDeleteRequest() (API 30+; minSdk 30 means always available) | No legacy fallback needed; RESULT_OK semantics are platform-managed |
| Passing full photo objects through navigation | Pass IDs only; resolve at destination boundary | Already in place; Phase 5 does not change handoff |
| Mutable shared state between screens | Isolated ViewModel ownership per feature | Already in place; Phase 5 only adds post-delete re-query in route layer |

## Open Questions

1. **Does MediaStore.createDeleteRequest return RESULT_OK for partial success?**
   - What we know: Android docs do not explicitly guarantee atomicity. Community reports (Stack Overflow, Android issue tracker) indicate partial success is possible in practice.
   - What's unclear: Whether this is reproducible on API 30-36 emulators or requires specific hardware/storage configurations.
   - Recommendation: Implement the re-query defensively regardless. Cost is one extra MediaStore cursor query; benefit is correctness under adversarial conditions.

2. **What is the correct post-delete state when ALL items fail to delete?**
   - What we know: If `actuallyDeleted` is empty after re-query, calling `onDeleteConfirmed(emptySet())` would exit review and trigger a refresh that finds all photos still present.
   - What's unclear: Whether the user should be shown an error vs. silently returned to review with state intact.
   - Recommendation: If `actuallyDeleted` is empty, do not call `onDeleteConfirmed` — stay in review and show a dismissable error banner. Plan 05-01 should define this branch.

3. **Can the reference screenshots be viewed on-device during acceptance testing?**
   - What we know: `main.jpg` and `Delete-staging-area..jpg` are in the project root.
   - What's unclear: Whether the test/review workflow expects a side-by-side pixel comparison or a holistic "close enough" judgment.
   - Recommendation: Plan 05-03 should specify the acceptance criteria precisely (e.g., "layout hierarchy matches, no element is in a wrong position, colors are consistent") so the validator can sign off without ambiguity.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 + AndroidX Compose UI Test |
| Config file | `app/build.gradle.kts` |
| Quick run command | `./gradlew testDebugUnitTest` |
| Full suite command | `./gradlew testDebugUnitTest connectedDebugAndroidTest` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| MEDIA-03 | Empty state renders clear message + Retry | androidTest | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.entry.EntryScreenTest` | ✅ exists (`EntryScreenTest`) |
| MEDIA-03 | NeedsPermission (denied) renders settings hint | androidTest | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.entry.EntryScreenTest` | ✅ exists (`EntryScreenTest`) |
| MEDIA-03 | Error state renders message + Retry | androidTest | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.entry.EntryScreenTest` | ✅ exists (`EntryScreenTest`) |
| REVW-04 | Cancel leaves review state unchanged | unit | `./gradlew testDebugUnitTest --tests '*ReviewViewModelTest*'` | ✅ exists |
| REVW-04 | Partial delete: only actually-deleted IDs propagate | unit | `./gradlew testDebugUnitTest --tests '*ReviewViewModelTest*'` | ❌ Wave 0 — add partial-delete test |
| REVW-04 | Zero-staged review shows empty message | androidTest | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.review.ReviewScreenTest` | ✅ exists (`ReviewScreenTest`) — add empty-grid case |

### Sampling Rate
- **Per task commit:** `./gradlew testDebugUnitTest`
- **Per wave merge:** `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- **Phase gate:** Full suite green before `/gsd:verify-work`

### Wave 0 Gaps
- [ ] `app/src/test/java/com/jimmymacmini/wishdtmf/feature/review/ReviewViewModelTest.kt` — add test for partial-delete scenario: `onDeleteConfirmed(actuallyDeleted)` where `actuallyDeleted` is a strict subset of `selectedPhotoIds`
- [ ] `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt` — add empty-grid test case for `stagedPhotoIds = emptyList()` with `isLoading = false`
- [ ] `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreenTest.kt` — verify settings-deep-link affordance is present when `showSettingsHint = true` (assert button/text with tag is visible)

## Recommended Planning Split

The roadmap's three-plan split is correct:

- **05-01** — State hardening: MEDIA-03 polish (empty, denied, zero-staged) + REVW-04 partial-delete detection + Wave 0 test gaps
- **05-02** — Lifecycle, device-variance, and performance validation: configuration-change sequences, background/foreground, process-death resilience, performance with 30-photo batch on mid-range device
- **05-03** — UI polish, visual acceptance sign-off against `main.jpg` and `Delete-staging-area..jpg`, final launch-readiness checklist

Plans 05-02 and 05-03 are substantially manual-verification-driven. Each should include a structured checklist artifact that the validator fills in, not just code changes.

## Sources

### Primary (HIGH confidence)
- Direct code inspection of all production Kotlin files listed above — state shapes, navigation wiring, and test coverage verified by reading source
- `REQUIREMENTS.md` and `STATE.md` — requirements traceability and outstanding blockers
- Phase 4 plans (04-01, 04-02, 04-03) and RESEARCH.md — confirmed what Phase 4 built and what it explicitly deferred to Phase 5

### Secondary (MEDIUM confidence)
- Android developer documentation (standard patterns): `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`, `MediaStore.createDeleteRequest` result semantics
- Phase 3 CONTEXT.md — confirmed zero-staged-item UX is handled by existing guard in `MainViewModel.onProceedToReview()`

### Tertiary (LOW confidence)
- Community reports on MediaStore partial-delete behavior — unverified on specific API levels; treated as a risk worth defending against

## Metadata

**Confidence breakdown:**
- State hardening scope (MEDIA-03, REVW-04): HIGH — all existing state machinery verified by source inspection; gaps are clearly bounded
- Partial-delete accuracy: MEDIUM — MediaStore partial-success behavior is not officially documented as guaranteed; the defensive re-query pattern is standard practice
- Lifecycle resilience: HIGH — SavedStateHandle backing is verified; specific failure modes are confirmed via code inspection
- Visual acceptance: HIGH — reference images exist; acceptance criteria are deterministic (layout hierarchy match)

**Research date:** 2026-03-15
**Valid until:** Stable — no fast-moving dependencies; valid for the duration of Phase 5 execution
