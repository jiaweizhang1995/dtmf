# Phase 1: Foundation & Media Access - Research

**Researched:** 2026-03-14
**Domain:** Modern Android local-media access, launch orchestration, and lifecycle-safe session state
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- First open should go straight into a minimal permission-requesting entry flow, not an intro page and not a shell homepage
- Once permission is granted, the app should immediately generate the current batch and enter the main flow without an extra “start” step
- Reopening the app should start a new random batch by default rather than asking to resume a prior launch session
- Entry-state copy should feel like a minimal utility tool: short, calm, and not brand-heavy
- The launch batch should be drawn from the full local photo library, not only recent items
- v1 should sample ordinary photos only; videos are excluded
- The candidate pool should avoid hidden / trash / non-normal gallery content where possible
- If fewer than 30 eligible photos exist, the app should proceed with the available count rather than blocking the user
- Rotation should preserve the exact current position in the active batch
- Backgrounding and returning to the app should preserve the current batch, current item, and staged decisions
- Phase 1 does not need to guarantee restoration after full process death or full app relaunch; starting a fresh batch is acceptable there
- Staged decisions only need to persist for the currently active session, not across hard restarts
- If no eligible photos exist, show a minimal utility-style message with retry / exit behavior rather than an educational flow
- If permission is denied, stay on a minimal explanation state with a clear way to request permission again
- If gallery loading fails after permission is granted, use a short, tool-like error state rather than a verbose explanation
- The primary recovery action for abnormal entry states should be retry; do not overcomplicate the screen with multiple competing actions

### Claude's Discretion
- Exact wording of minimal entry / empty / error copy
- Whether “exit” is an explicit button or a passive back/system behavior in no-photo states
- Exact visual treatment of loading / permission states before the screenshot-faithful UI is built

### Deferred Ideas (OUT OF SCOPE)
- Cross-relaunch session restore: captured as future safety enhancement, not required in Phase 1
- Trash-mode option instead of permanent delete: later phase / future version
- Filters by month / album or smarter candidate selection: later phase / future version
- Video cleanup support: out of Phase 1 and out of v1 scope

</user_constraints>

<research_summary>
## Summary

Phase 1 should use the standard current Android stack for a local-media app: Kotlin, Jetpack Compose, a single-activity shell, `ViewModel` state ownership, `SavedStateHandle` for lifecycle restoration, and `MediaStore` via `ContentResolver` for image enumeration. The key platform constraint is permission branching: on Android 13+ image access uses `READ_MEDIA_IMAGES`, while Android 12 and below still use the older read-storage permission model. Because this app must randomly sample from the whole local gallery, Android Photo Picker is not a fit for the primary flow; it is user-driven selection, not full-library enumeration.

The safest architecture for planning is to separate the phase into three concerns: project scaffold and app state shell, media access/querying, and launch/session orchestration. This matches the roadmap and keeps future swipe/review phases from reworking the entry model. For lifecycle behavior, official Android guidance favors `ViewModel` for screen state and `SavedStateHandle` / saveable state only for UI or transient restoration. That aligns with the user's decision to preserve the active batch across rotation and background/foreground transitions, but not necessarily across process death or full relaunch.

**Primary recommendation:** Build a single-activity Compose app targeting current stable Android SDK, query local images through a `MediaStore` repository behind a clean interface, and drive the launch flow from a `ViewModel` state machine that restores in-session state but intentionally starts a new batch on normal app relaunch.
</research_summary>

<standard_stack>
## Standard Stack

The established libraries/tools for this domain:

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin | 2.x stable | Primary Android language | First-class Jetpack and coroutine support |
| Android SDK | `compileSdk` / `targetSdk` 36, `minSdk` 30 | Platform baseline | Current stable API level and enough modern storage behavior to simplify the app |
| Jetpack Compose BOM | Current stable at scaffold time | App UI and screen state rendering | Standard native UI toolkit for new Android apps |
| Lifecycle ViewModel | Current stable Jetpack | Own launch/session state | Official guidance for lifecycle-safe UI state |
| Activity Result APIs | Current stable AndroidX | Runtime permission requests | Standard replacement for deprecated request flows |
| MediaStore APIs | Platform APIs | Enumerate local image media | Official shared-media access path |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Navigation Compose | Current stable | Move from entry state into future main/review flows | Use once the app has more than one screen state or route |
| Coil 3 Compose | Current stable | Render image thumbnails / previews from `Uri` | Needed once Phase 2 starts showing real photos |
| Room | Current stable | Persist state beyond process lifetime | Only if later phases require hard session restore |
| JUnit 4/5 + Compose UI Test | Current stable | Unit/UI regression coverage | Needed from Wave 0 onward |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `MediaStore` gallery query | Android Photo Picker | Picker is better for explicit user selection, but does not support app-driven full-library random sampling |
| Single-activity Compose shell | Multi-activity app | Multi-activity adds coordination cost with no current benefit |
| `ViewModel` + `SavedStateHandle` | Composable-local state only | Simpler at first, but weaker for the lifecycle guarantees the user wants |

**Installation:**
```bash
# Determined during execution when the Android project is scaffolded
# Core stack:
# - Kotlin
# - Android SDK 36
# - Jetpack Compose
# - Lifecycle ViewModel
# - Activity Result APIs
# - MediaStore access
```
</standard_stack>

<architecture_patterns>
## Architecture Patterns

### Recommended Project Structure
```text
app/
├── src/main/java/.../app/         # Application, activity, navigation shell
├── src/main/java/.../data/media/  # MediaStore queries, mappers, repository
├── src/main/java/.../domain/      # Session generation and launch use cases
├── src/main/java/.../feature/entry/# Permission and launch state UI
├── src/main/java/.../feature/main/ # Placeholder route for later main flow handoff
└── src/androidTest/               # Compose UI tests
```

### Pattern 1: ViewModel-Owned Launch State Machine
**What:** Represent launch states explicitly, e.g. `NeedsPermission`, `LoadingBatch`, `Ready(batch)`, `Empty`, `Error`.
**When to use:** Permission-gated entry flows where multiple outcomes all land in distinct user-visible states.
**Example:**
```kotlin
sealed interface LaunchUiState {
    data object NeedsPermission : LaunchUiState
    data object LoadingBatch : LaunchUiState
    data class Ready(val batch: LaunchBatch, val currentIndex: Int) : LaunchUiState
    data object Empty : LaunchUiState
    data class Error(val retryable: Boolean) : LaunchUiState
}
```

### Pattern 2: Repository Boundary Around MediaStore
**What:** Keep `ContentResolver` / `MediaStore` queries out of UI and expose a small repository contract returning app models.
**When to use:** Any app reading shared media, especially when later phases will reuse the same media IDs / URIs for display and deletion.
**Example:**
```kotlin
interface PhotoRepository {
    suspend fun loadEligiblePhotos(limitHint: Int? = null): List<LocalPhoto>
}
```

### Pattern 3: Save Business State Above Compose
**What:** Use `ViewModel` and `SavedStateHandle` for state that must survive configuration changes; reserve `rememberSaveable` for strictly UI-local state.
**When to use:** Current batch, selected index, and staged decisions that should survive rotation/backgrounding.
**Example:**
```kotlin
class LaunchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val photoRepository: PhotoRepository,
) : ViewModel() {
    // Restore batch metadata and current index from savedStateHandle.
}
```

### Anti-Patterns to Avoid
- **Using Photo Picker as the primary data source:** it does not match the app's “open app -> random 30 from local gallery” behavior.
- **Querying raw file paths directly:** modern Android shared storage is `Uri`/`MediaStore` centric.
- **Keeping launch/session state only in composables:** risks losing state on configuration change and makes future phases harder to build on.
</architecture_patterns>

<dont_hand_roll>
## Don't Hand-Roll

Problems that look simple but have existing solutions:

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Permission callbacks | Manual deprecated request code | Activity Result permission contracts | Handles lifecycle integration cleanly |
| Shared media indexing | Direct file-system traversal | `MediaStore` query via `ContentResolver` | Correctly reflects shared media visibility and permissions |
| Process/config state save | Custom singleton cache | `ViewModel` + `SavedStateHandle` | Integrates with lifecycle and recreation |

**Key insight:** The platform already has the right primitives for permissions, media enumeration, and in-session restoration. Custom alternatives create more edge cases than they remove.
</dont_hand_roll>

<common_pitfalls>
## Common Pitfalls

### Pitfall 1: Using the Wrong Permission Flow Per Android Version
**What goes wrong:** The app works on one device/API level but fails or returns no images on others.
**Why it happens:** Media access behavior changed on Android 13+ to granular permissions such as `READ_MEDIA_IMAGES`.
**How to avoid:** Plan API-level-aware permission handling up front and keep it centralized.
**Warning signs:** Permission granted but the query result is empty only on some Android versions.

### Pitfall 2: Treating Photo Picker as a Full-Library Query API
**What goes wrong:** The product flow no longer matches the app vision because the user must manually choose media first.
**Why it happens:** Photo Picker is attractive and modern, but solves a different problem.
**How to avoid:** Use `MediaStore` for library enumeration and keep Photo Picker out of Phase 1.
**Warning signs:** Planner starts discussing “select photos to begin” or other user-selection-first flows.

### Pitfall 3: Losing the Active Batch on Rotation
**What goes wrong:** The current photo index or staged data resets after configuration change.
**Why it happens:** State lives too low in the UI tree or isn't saveable.
**How to avoid:** Put active batch metadata and current index in `ViewModel` / saved state.
**Warning signs:** Rotation re-triggers batch creation or restarts permission flow.
</common_pitfalls>

## Validation Architecture

Phase 1 should establish both lightweight unit coverage and a minimal UI-state verification path:

- Unit tests for batch generation rules: full-library random sampling, “use fewer than 30 if necessary”, and exclusion logic that the repository exposes to the domain layer
- Unit tests for launch-state transitions: permission denied -> retry, permission granted -> loading -> ready / empty / error
- Instrumented or Compose UI tests for entry-state rendering and configuration-change preservation once the app shell exists
- Build verification through Gradle assemble / test tasks, with Wave 0 responsible for creating the test harness because no codebase exists yet

Nyquist implication: `01-VALIDATION.md` should explicitly mark Wave 0 as responsible for test infrastructure before later plans rely on it.

<code_examples>
## Code Examples

Verified patterns from official sources:

### Runtime permission request
```kotlin
// Source: https://developer.android.com/training/permissions/requesting
val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // load batch
        } else {
            // show denied state
        }
    }
```

### MediaStore image query
```kotlin
// Source: https://developer.android.com/training/data-storage/shared/media
val projection = arrayOf(
    MediaStore.Images.Media._ID,
    MediaStore.Images.Media.DATE_TAKEN
)
val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
context.contentResolver.query(
    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    projection,
    null,
    null,
    sortOrder
)
```

### Save UI state across recreation
```kotlin
// Source: https://developer.android.com/develop/ui/compose/state-saving
class ConversationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val message = savedStateHandle.saveable { mutableStateOf("") }
}
```
</code_examples>

<sota_updates>
## State of the Art (2024-2026)

What's changed recently:

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `READ_EXTERNAL_STORAGE` only | `READ_MEDIA_IMAGES` on Android 13+ | Android 13 | Permission handling must branch by API level |
| XML-first app scaffolds | Compose-first new app scaffolds | Ongoing Jetpack shift | Plan around Compose and lifecycle-aware state from the start |
| Ad hoc state retention | `SavedStateHandle` / saveable state guidance in Compose | Modern lifecycle guidance | Important for preserving the user's in-session continuity requirements |

**New tools/patterns to consider:**
- Current stable Android platform/API 36: use as the compile/target baseline for new project scaffold
- Compose-first testing: build the shell with UI testability in mind rather than retrofitting later

**Deprecated/outdated:**
- Broad legacy storage assumptions as the only permission model
- Building the flow around Android Photo Picker for a non-picker product
</sota_updates>

<open_questions>
## Open Questions

1. **How reliably can hidden/trash content be excluded across OEM devices?**
   - What we know: `MediaStore` exposes normal shared media; OEM-specific hidden/trash behaviors vary
   - What's unclear: exact query filters may not behave identically across all vendor galleries
   - Recommendation: phase planning should avoid over-promising and keep the rule as “avoid where possible,” then validate on real devices later

2. **What exact restore granularity should Phase 1 keep after process death?**
   - What we know: the user accepts a fresh batch after full relaunch or system kill
   - What's unclear: whether Android task recreation might still surface partial saved state unexpectedly
   - Recommendation: planner should explicitly scope restoration to configuration change and normal background/foreground within the active process
</open_questions>

<sources>
## Sources

### Primary (HIGH confidence)
- https://developer.android.com/training/permissions/requesting - runtime permission guidance and Activity Result examples
- https://developer.android.com/about/versions/13/behavior-changes-13#granular-media-permissions - `READ_MEDIA_IMAGES` behavior on Android 13+
- https://developer.android.com/training/data-storage/shared/media - `MediaStore` shared media query guidance
- https://developer.android.com/develop/ui/compose/state-saving - `rememberSaveable` and `SavedStateHandle` guidance
- https://developer.android.com/tools/releases/platforms - current Android platform/API baseline

### Secondary (MEDIUM confidence)
- Existing project research summary in `.planning/research/SUMMARY.md` - stack and roadmap implications already synthesized at project level

### Tertiary (LOW confidence - needs validation)
- None for this phase; core guidance came from official Android docs
</sources>

<metadata>
## Metadata

**Research scope:**
- Core technology: Android runtime permissions, `MediaStore`, lifecycle-safe session state
- Ecosystem: Compose, Lifecycle ViewModel, Activity Result APIs
- Patterns: launch state machine, repository boundary, session restoration
- Pitfalls: permission branching, picker mismatch, lost state

**Confidence breakdown:**
- Standard stack: HIGH - official Android guidance
- Architecture: HIGH - current Android best practices
- Pitfalls: HIGH - directly tied to platform behavior
- Code examples: HIGH - official docs patterns

**Research date:** 2026-03-14
**Valid until:** 2026-04-13
</metadata>

---

*Phase: 01-foundation-media-access*
*Research completed: 2026-03-14*
*Ready for planning: yes*
