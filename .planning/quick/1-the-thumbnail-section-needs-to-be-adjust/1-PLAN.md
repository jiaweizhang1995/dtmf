---
phase: quick-1
plan: 1
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailStrip.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
autonomous: true
requirements: [QUICK-1]
must_haves:
  truths:
    - "All photos appear as thumbnails in the strip, none hidden behind an ellipsis"
    - "The strip is horizontally scrollable from first to last thumbnail"
    - "When the active photo changes, the strip scrolls so the current thumbnail is visible"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailStrip.kt"
      provides: "LazyRow-based thumbnail strip with animated scroll-to-current"
  key_links:
    - from: "MainScreen.kt"
      to: "ThumbnailStrip"
      via: "uiState.photos (full list) passed as photos param"
      pattern: "ThumbnailStrip\\(photos"
    - from: "ThumbnailStrip"
      to: "LazyListState"
      via: "LaunchedEffect(activeIndex) -> lazyListState.animateScrollToItem"
      pattern: "animateScrollToItem"
---

<objective>
Replace the fixed-width windowed Row in ThumbnailStrip with a horizontally scrollable LazyRow that shows every photo. When the active photo advances, the strip animates to bring the current thumbnail into view.

Purpose: Users can see all photos in the session at a glance by scrolling, and the strip always keeps the active thumbnail visible without any ellipsis truncation.
Output: Updated ThumbnailStrip.kt using LazyRow + animateScrollToItem; MainScreen.kt passes the full photos list.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@.planning/ROADMAP.md

@app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailStrip.kt
@app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
@app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainUiState.kt
@app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreenTokens.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Replace ThumbnailStrip with horizontally scrollable LazyRow</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailStrip.kt</files>
  <action>
Rewrite ThumbnailStrip.kt. Remove all BoxWithConstraints windowing logic (maxCount, slotsForPhotos, showEllipsis, visiblePhotos computation) and the ellipsis Box. Delete the unused imports that supported those (BoxWithConstraints, Row, Text, TextAlign, Arrangement).

New signature — add an activeIndex parameter so the composable can scroll to it:

```kotlin
@Composable
fun ThumbnailStrip(
    photos: List<MainPhotoUiModel>,
    activeIndex: Int,
    modifier: Modifier = Modifier,
)
```

Body:
1. Create a `LazyListState`: `val lazyListState = rememberLazyListState()`
2. Add a `LaunchedEffect(activeIndex)` block that calls `lazyListState.animateScrollToItem(activeIndex)`. This fires on first composition (showing photo 0 aligned left) and on every subsequent index change.
3. Replace the Row with a `LazyRow` using `state = lazyListState`, `horizontalArrangement = Arrangement.spacedBy(8.dp)`, `contentPadding = PaddingValues(horizontal = 0.dp)`. Iterate items with `items(photos) { photo -> ThumbnailBox(photo = photo) }`.
4. Keep ThumbnailBox unchanged.

Required new imports: `androidx.compose.foundation.lazy.LazyRow`, `androidx.compose.foundation.lazy.items`, `androidx.compose.foundation.lazy.rememberLazyListState`, `androidx.compose.foundation.layout.PaddingValues`, `androidx.compose.runtime.LaunchedEffect`.

Remove imports that are no longer needed: `BoxWithConstraints`, `Row`, `Arrangement`, `Text`, `TextAlign`, `alpha` (keep if still used — it IS still used in ThumbnailBox so keep it).

The `modifier` is applied to the LazyRow directly (replace the BoxWithConstraints modifier application).
  </action>
  <verify>
    <automated>./gradlew :app:compileDebugKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>ThumbnailStrip compiles with LazyRow, no BoxWithConstraints or ellipsis Box, animateScrollToItem called on activeIndex change.</done>
</task>

<task type="auto">
  <name>Task 2: Update MainScreen call-site to pass activeIndex and full photos list</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt</files>
  <action>
Find the ThumbnailStrip call site (line ~79):

```kotlin
ThumbnailStrip(photos = uiState.visibleThumbnails)
```

Change it to:

```kotlin
ThumbnailStrip(
    photos = uiState.photos,
    activeIndex = uiState.activePhotoIndex,
)
```

Using `uiState.photos` (the full list, all photos) instead of `uiState.visibleThumbnails` (which was the windowed subset). `uiState.activePhotoIndex` is already an Int field on MainUiState — no ViewModel changes needed.

No other changes to MainScreen.kt.
  </action>
  <verify>
    <automated>./gradlew :app:assembleDebug 2>&1 | tail -20</automated>
  </verify>
  <done>App builds successfully. ThumbnailStrip at call site passes full photos list and active index. `uiState.visibleThumbnails` is no longer referenced in MainScreen.</done>
</task>

</tasks>

<verification>
After both tasks, run the full debug build:

```
./gradlew :app:assembleDebug
```

Then install on device/emulator and verify manually:
- Open the app with a session of more than 4 photos — all thumbnails should be visible by scrolling the strip
- The strip should start showing the first thumbnail (index 0) aligned at the left
- As you swipe through photos, the strip should scroll to keep the active (yellow-bordered) thumbnail visible
- No ellipsis item appears anywhere in the strip
</verification>

<success_criteria>
- `./gradlew :app:assembleDebug` exits 0
- ThumbnailStrip uses LazyRow with animateScrollToItem(activeIndex)
- All photos are shown in the strip (no windowing, no ellipsis)
- Strip scrolls to active thumbnail on photo advance
</success_criteria>

<output>
After completion, create `.planning/quick/1-the-thumbnail-section-needs-to-be-adjust/1-SUMMARY.md`
</output>
