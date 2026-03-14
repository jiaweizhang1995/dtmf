# Phase 2: Main Swipe Experience - Research

**Researched:** 2026-03-14
**Domain:** Jetpack Compose swipe-card interaction, screenshot-faithful Android gallery UI, and responsive local-photo rendering
**Confidence:** HIGH

<user_constraints>
## User Constraints (from REQUIREMENTS.md, ROADMAP.md, PROJECT.md, and Phase 1 state)

### Locked Decisions
- Phase 2 must stay visually close to `main.jpg`; screenshot fidelity is a primary product requirement, not optional polish
- The app is Android-only and Compose-first; Phase 2 should extend the existing single-activity Compose architecture rather than introduce a second UI stack
- The launch flow already creates a `LaunchSession` with a current batch and current index; Phase 2 should build on that state instead of replacing it
- The main screen must show one primary current photo at a time, with supporting thumbnail context and visible primary actions
- Left swipe stages a photo for deletion, right swipe skips it, and those two actions are the only required decision mutations in this phase
- Swipe behavior should feel smooth on realistic photo sizes, so rendering and gesture logic must be planned with large image content in mind
- Undo and proceed behavior belong to Phase 3; Phase 2 may render those affordances for fidelity, but should not force Phase 3 logic to be solved early
- Session continuity across configuration changes already matters because Phase 1 established `UX-03`; Phase 2 state additions must preserve that behavior

### Claude's Discretion
- Exact composable breakdown for toolbar, thumbnail rail, metadata chips, hero image, bottom actions, and proceed affordance
- Whether the thumbnail strip is purely informational in Phase 2 or already tap-selectable
- Exact animation values for drag threshold, off-screen dismissal distance, snap-back, and card rotation
- Whether `Proceed` is disabled, no-op, or wired behind a temporary callback until Phase 3

### Deferred Ideas (OUT OF SCOPE)
- Undo stack semantics and proceed-to-review navigation
- Permanent delete logic and review-state editing
- Empty-state or failure-state hardening beyond what Phase 1 already owns
- Full process-death persistence beyond the existing active-session scope

</user_constraints>

<research_summary>
## Summary

Phase 2 should be planned as a focused expansion of the existing `LaunchSession` flow into a real swipe surface, not as a standalone screen rewrite. The current code already has the right seams: `LaunchViewModel` owns lifecycle-safe session state, `AppNavGraph` routes `LaunchUiState.Ready` into `MainRoute`, and the main route is still a placeholder. The safest planning move is to keep state ownership above Compose, introduce a dedicated main-screen state/reducer layer for swipe decisions, and keep gesture math inside the UI layer where it can animate the active card without corrupting session state during partial drags.

The screenshot target in `main.jpg` implies a specific hierarchy: compact top bar, thumbnail strip, metadata row, large centered photo card, three round bottom actions, a low-emphasis promo/banner row, and a right-aligned `PROCEED` affordance. That hierarchy should drive the plan structure. Phase 2 should deliberately separate screenshot-faithful static layout from interactive photo rendering, then add swipe mutation logic last. This matches the roadmap's 02-01 / 02-02 / 02-03 split and prevents gesture work from being blocked by unfinished visual composition.

**Primary recommendation:** Plan Phase 2 around a `MainViewModel` or equivalent main-flow state holder that derives a current-photo presentation model from the existing launch session, use Coil Compose for photo/thumbnail rendering, implement the active-card swipe with `Animatable` plus Compose drag gesture APIs, and validate both reducer correctness and drag responsiveness before Phase 3 adds undo/proceed behavior on top.
</research_summary>

<standard_stack>
## Standard Stack

The established libraries/tools for this domain:

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Existing Kotlin / AGP / Compose baseline | Keep project baseline from Phase 1 | Avoid churn while building the first real screen | The project is already on a modern Compose stack |
| Lifecycle ViewModel + SavedStateHandle | Existing project baseline | Own active swipe session and survive configuration change | Matches official state-hoisting guidance and Phase 1 architecture |
| Jetpack Compose Foundation gestures | Existing Compose foundation APIs | Drag/swipe interaction and animation orchestration | Standard Compose-native gesture path |
| Jetpack Compose Animation APIs | Existing Compose baseline | Snap-back, dismiss, and subtle rotation/alpha feedback | Native animation primitives integrate cleanly with gesture state |
| Coil 3 Compose (`io.coil-kt.coil3:coil-compose`) | Current stable compatible with project baseline | Decode and render large `Uri`-backed photos and thumbnails | Standard image-loading choice for Compose Android apps |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Material 3 | Existing project baseline | Buttons, typography tokens, base dark-theme scaffolding | Use for structure, then customize to match screenshot tone |
| Compose UI Test | Existing project baseline | Verify swipe semantics and visual affordance presence | Needed for Phase 2 regression protection |
| Macrobenchmark / Profileable build | Optional later hardening | Measure frame pacing on real image loads | Useful if swipe jank appears, but not required to start Phase 2 |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Coil Compose | Manual bitmap decoding / `BitmapFactory` | Too much custom lifecycle, sizing, and memory handling for v1 |
| Custom gesture framework | `anchoredDraggable` | Better for fixed anchors like sheets; less natural than free drag for a dismissible photo card |
| Gesture-owned state only inside composables | ViewModel-owned decision state with UI-local drag offset | Simpler at first, but unsafe once undo/proceed and config changes arrive |

**Installation:**
```bash
# Keep existing Compose/Lifecycle stack from Phase 1.
# Add Coil Compose when implementing real photo rendering in Phase 2.
```
</standard_stack>

<architecture_patterns>
## Architecture Patterns

### Recommended Project Structure
```text
app/src/main/java/com/jimmymacmini/wishdtmf/
├── feature/main/
│   ├── MainRoute.kt              # Route + state collection
│   ├── MainViewModel.kt          # Session decisions + current-photo presentation
│   ├── MainUiState.kt            # Screen state model for Phase 2 / Phase 3
│   ├── MainScreen.kt             # High-level layout matching main.jpg
│   ├── SwipePhotoCard.kt         # Active image surface + drag animation
│   ├── ThumbnailStrip.kt         # Supporting thumbnail row
│   └── MainActionBar.kt          # Delete / undo / skip / proceed affordances
├── domain/
│   ├── LaunchSession.kt          # Extend carefully or add companion decision model
│   └── SwipeDecisionReducer.kt   # Pure mutation logic for stage / skip progression
└── data/media/
    └── LocalPhoto                # Already sufficient as image identity input
```

### Pattern 1: Split Persistent Decision State from Ephemeral Drag State
**What:** Keep staged/skip decisions and current index in a state holder, but keep in-progress drag offset, rotation, and transient alpha in composable-local animation state.
**When to use:** Swipe surfaces where the user can partially drag, cancel, or fling a card without committing the decision yet.
**Why:** Planning gets cleaner when the reducer only handles committed decisions and the UI owns gesture interpolation.
**Example:**
```kotlin
data class MainUiState(
    val photos: List<LocalPhoto>,
    val currentIndex: Int,
    val stagedPhotoIds: Set<Long>,
    val lastDecision: SwipeDecision? = null,
)

enum class SwipeDirection { Left, Right }

data class SwipeDecision(
    val photoId: Long,
    val direction: SwipeDirection,
)
```

### Pattern 2: Introduce a Reducer Before Adding Undo
**What:** Create pure functions for `stageCurrentPhoto()`, `skipCurrentPhoto()`, and `peekNextPhoto()` now, even though undo is Phase 3.
**When to use:** Multi-phase flows where future behavior depends on the same mutation history.
**Why:** If Phase 2 mutates `LaunchSession` ad hoc inside gesture callbacks, Phase 3 will have to unwind UI-specific logic later.
**Example:**
```kotlin
fun MainUiState.commitSwipe(direction: SwipeDirection): MainUiState {
    val currentPhoto = photos[currentIndex]
    val nextIndex = (currentIndex + 1).coerceAtMost(photos.lastIndex)
    return copy(
        currentIndex = nextIndex,
        stagedPhotoIds = if (direction == SwipeDirection.Left) {
            stagedPhotoIds + currentPhoto.id
        } else {
            stagedPhotoIds
        },
        lastDecision = SwipeDecision(currentPhoto.id, direction),
    )
}
```

### Pattern 3: Layer the Screen by Visual Hierarchy, Not by Data Source
**What:** Build `MainScreen` from stable visual sections that mirror `main.jpg`: top bar, thumbnail rail, metadata chips, hero card, bottom actions, footer/banner.
**When to use:** Screenshot-faithful UI where layout accuracy matters as much as behavior.
**Why:** This keeps fidelity work local and prevents the swipe card from absorbing unrelated layout code.
**Expected sections from `main.jpg`:**
- Top app row with back affordance, title/month label, and overflow
- Horizontal thumbnail strip showing surrounding items and active selection highlight
- Metadata chip row with size / type / index context
- Large portrait-oriented hero image surface with a small utility icon overlay
- Bottom circular action controls: delete, undo, skip/confirm-style action
- Low emphasis banner row and right-aligned `PROCEED`

### Pattern 4: Precompute Neighbors for Thumbnail Context
**What:** Derive visible thumbnail items from the current index rather than rendering all 30 at full cost.
**When to use:** A fixed-size session where only nearby context is visible on-screen.
**Why:** Rendering every thumbnail is possible, but planning a small visible window keeps recomposition and image work predictable.
**Example:**
```kotlin
fun MainUiState.visibleThumbnails(radius: Int = 3): List<LocalPhoto> {
    val start = (currentIndex - radius).coerceAtLeast(0)
    val end = (currentIndex + radius).coerceAtMost(photos.lastIndex)
    return photos.subList(start, end + 1)
}
```

### Pattern 5: Animate the Card, Commit on Threshold Crossing
**What:** Translate and slightly rotate the current card during drag, then either snap back or animate it off-screen and commit the decision at animation completion.
**When to use:** Tinder-style swipe interactions with a single active item.
**Why:** Committing on threshold crossing during an unfinished drag is brittle; commit after the dismiss animation begins or completes.
**Implementation note:** Use a horizontal threshold derived from card width, not a hardcoded dp value, so the behavior scales across devices.

### Anti-Patterns to Avoid
- **Reusing `LaunchViewModel.advanceToNextPhoto()` for Phase 2:** that method only increments an index and has no concept of staged decisions, future undo, or derived thumbnail state
- **Committing stage/skip during raw pointer movement:** state changes should happen once per accepted swipe, not per pixel moved
- **Letting the swipe card decode original-size images without size awareness:** large photos will hurt responsiveness and memory use
- **Tying screenshot fidelity to hardcoded screen-size assumptions:** use constraints and aspect-ratio-aware containers so the layout survives typical Android device variance
</architecture_patterns>

<dont_hand_roll>
## Don't Hand-Roll

Problems that look simple but have existing solutions:

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| `Uri` image loading and caching | Manual bitmap decode + in-memory cache | Coil Compose `AsyncImage` / `SubcomposeAsyncImage` only where truly needed | Handles sizing, threading, caching, and lifecycle correctly |
| Fixed-anchor swipe system | Reviving deprecated `swipeable` patterns or building a generic swipe framework | A focused `Animatable` + drag gesture implementation for the active card | The app needs one dismissible card, not a reusable gesture engine |
| Screen-state ownership in composables | Local mutable lists of staged/kept photos | ViewModel/state-holder-backed reducer | Required for config safety and future Phase 3 undo |
| Screenshot-fidelity color tuning by trial in every composable | Ad hoc color literals everywhere | A small set of main-screen tokens in theme or feature-level constants | Makes visual tuning and later polish tractable |

**Key insight:** Phase 2 should hand-roll only the narrow interaction that is actually product-specific: the dismissible photo card and its decision reducer. Everything else should lean on standard Compose, Coil, and ViewModel patterns.
</dont_hand_roll>

<common_pitfalls>
## Common Pitfalls

### Pitfall 1: Making the Swipe Surface the State Owner
**What goes wrong:** The card can animate, but staged decisions, current index, and future undo behavior become entangled with composable lifecycle.
**Why it happens:** It is tempting to keep everything next to the gesture code because the UI is visually central.
**How to avoid:** Keep drag offset local, but route committed stage/skip actions through a reducer or `MainViewModel`.
**Warning signs:** Rotation resets staged decisions or the next photo depends on composable-local lists.

### Pitfall 2: Loading Hero Images at the Wrong Size
**What goes wrong:** Drags stutter, images flash, or the app allocates more memory than needed.
**Why it happens:** `Uri` images from `MediaStore` can be much larger than the on-screen hero container.
**How to avoid:** Use Coil Compose with constraint-aware sizing and avoid painter patterns that always load at original dimensions.
**Warning signs:** Noticeable frame drops when the current photo changes or when the user starts dragging.

### Pitfall 3: Over-Scoping Thumbnail Behavior
**What goes wrong:** Phase 2 expands into a mini gallery implementation with selection, scrolling rules, and duplicate state handling.
**Why it happens:** The screenshot includes thumbnails, so it is easy to assume they need full interaction immediately.
**How to avoid:** Treat the thumbnail rail as visual context first. Only add tap-select if it materially helps fidelity and stays within the phase boundary.
**Warning signs:** Planning starts introducing secondary navigation rules not tied to SWIPE-01/02/03.

### Pitfall 4: Solving Undo Too Early
**What goes wrong:** Phase 2 gets blocked by history-stack questions that belong to Phase 3.
**Why it happens:** The screenshot shows an undo control in the bottom action row.
**How to avoid:** Render the affordance for visual fidelity, but keep the actual reversible-decision contract scoped to the next phase.
**Warning signs:** The reducer is redesigned around multi-step history before left/right swipe is stable.

### Pitfall 5: Using Device-Independent Thresholds for All Screens
**What goes wrong:** Swipe feels too sensitive on small devices and too stubborn on large ones.
**Why it happens:** Planning uses a hardcoded dp threshold without considering card width.
**How to avoid:** Derive threshold from measured card width or container width and tune with manual device checks.
**Warning signs:** Testers need very different gesture lengths across emulators/devices.
</common_pitfalls>

## Validation Architecture

Phase 2 should add validation in the same layered way the implementation is planned:

- Unit tests for the decision reducer:
  Validate left swipe stages the current photo, right swipe does not, current index advances correctly, and the final photo edge case is well-defined.
- Unit tests for thumbnail derivation:
  Validate visible-window logic around the start, middle, and end of a 30-photo session.
- Compose UI tests for affordance presence:
  Verify the main screen exposes the expected core sections and action controls when given a ready session.
- Compose UI tests for swipe mutation:
  Use `performTouchInput` drag/swipe interactions against the hero card and assert the current photo and staged count/state update correctly.
- Manual screenshot fidelity review:
  Compare emulator/device output to `main.jpg` for hierarchy, spacing, color balance, and action placement.
- Manual performance pass on realistic images:
  Run the screen with large local photos on at least one real device or high-resolution emulator and confirm the drag feels responsive.

Nyquist implication: Wave 0 is already complete from Phase 1, so Phase 2 planning should require automated reducer tests and at least one swipe UI test in the first implementation wave rather than deferring all interaction verification to the end.

<code_examples>
## Code Examples

Verified patterns from official sources:

### Hoist screen state to a ViewModel
```kotlin
// Source: https://developer.android.com/develop/ui/compose/state-hoisting
@Composable
fun ConversationScreen(
    conversationViewModel: ConversationViewModel = viewModel()
) {
    val messages by conversationViewModel.messages.collectAsStateWithLifecycle()
}
```

### Track drag gestures in Compose
```kotlin
// Source: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/drag-swipe-fling
Modifier.pointerInput(Unit) {
    detectDragGestures(
        onDragEnd = { /* settle or dismiss */ }
    ) { change, dragAmount ->
        change.consume()
        // update offset
    }
}
```

### Load images with constraint-aware `AsyncImage`
```kotlin
// Source: https://coil-kt.github.io/coil/compose/
AsyncImage(
    model = ImageRequest.Builder(LocalPlatformContext.current)
        .data(photoUri)
        .crossfade(true)
        .build(),
    contentDescription = null,
)
```

### Drive swipe assertions from Compose UI tests
```kotlin
// Source: https://developer.android.com/develop/ui/compose/testing
composeTestRule
    .onNodeWithTag("main_photo_card")
    .performTouchInput { swipeLeft() }
```
</code_examples>

<sota_updates>
## State of the Art (2024-2026)

What's changed recently:

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Accompanist / ad hoc image loading choices | Coil 3 Compose is the straightforward standard path for Compose Android image rendering | Current Compose-era practice | Phase 2 should not spend planning time inventing image infrastructure |
| Deprecated `swipeable`-style mental model for every drag UI | Compose now differentiates anchored drags from free drag gesture handling | Ongoing Compose foundation evolution | A dismissible card should use a focused drag implementation, not a generic anchored component |
| Composable-local state as default screen owner | State-hoisting guidance strongly favors screen-level state holders and ViewModels | Current official guidance | Important because Phase 2 feeds directly into Phase 3 undo/review handoff |

**New tools/patterns to consider:**
- Constraint-aware image requests so the hero photo and thumbnails do not decode at unnecessary sizes
- Feature-level reducer tests for decision logic before adding more navigation complexity
- Measured threshold-based dismiss logic rather than fixed dp thresholds

**Deprecated/outdated:**
- Building new code around deprecated `swipeable` APIs
- Treating the hero image as just another `Image` with no loading/caching strategy
- Putting future undo/review data in transient composable state
</sota_updates>

## Planning Implications

What the phase plan should explicitly decide before execution:

1. Whether to extend `LaunchSession` directly or introduce a separate `MainUiState` / `SwipeSessionState` wrapper that holds staged decisions and future undo metadata.
2. Whether thumbnails are display-only in Phase 2 or already tap-selectable.
3. What exact swipe threshold, rotation range, and snap-back/dismiss animation durations will be used as the default tuning constants.
4. Whether `Proceed` is rendered disabled/no-op in Phase 2 or wired to a placeholder callback without real navigation.
5. Which visual tokens need to be added to match `main.jpg`: dark background, muted chip surfaces, red destructive action, green skip/confirm action, and active-thumbnail highlight.
6. Which test IDs/semantics need to be added up front so swipe UI tests remain stable through Phase 3.

## Recommended Plan Shape

- **02-01 Layout fidelity:** Introduce main-screen tokens and composables that reproduce the hierarchy of `main.jpg` using placeholder/stub data from the ready session.
- **02-02 Media rendering:** Add Coil-backed hero image and thumbnail rendering, tune cropping/scaling behavior, and stabilize the screen on realistic local photos.
- **02-03 Swipe behavior:** Add drag/dismiss animation, reducer-backed stage/skip commits, and automated/manual validation for responsiveness.

## RESEARCH COMPLETE
