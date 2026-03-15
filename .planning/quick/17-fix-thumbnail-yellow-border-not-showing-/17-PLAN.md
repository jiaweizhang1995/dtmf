---
phase: quick-17
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailStrip.kt
autonomous: true
requirements: [quick-17]
must_haves:
  truths:
    - "The active thumbnail shows a visible yellow border immediately when the active index changes"
    - "The border is drawn outside (on top of) the rounded clip shape, not masked by it"
    - "Inactive thumbnails show the dim border; the active thumbnail shows the yellow border"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailStrip.kt"
      provides: "Fixed modifier order so border renders outside the clip region"
  key_links:
    - from: "ThumbnailBox modifier chain"
      to: "Compose rendering pipeline"
      via: "border() before clip()"
      pattern: "border.*clip|clip.*border"
---

<objective>
Fix the yellow active-thumbnail border not appearing when advancing to the next image.

Purpose: In Compose, `.border()` applied after `.clip()` is masked by the clip — the border is drawn inside the already-clipped shape so it becomes invisible. Moving `.border()` before `.clip()` places it in the unclipped layer where it renders correctly.

Output: ThumbnailStrip.kt with corrected modifier order; yellow border appears immediately on the active thumbnail when `activeIndex` changes.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/PROJECT.md
@.planning/STATE.md

<interfaces>
<!-- From app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailStrip.kt -->
<!-- ThumbnailBox receives MainPhotoUiModel; isCurrent drives border color/width -->

Current (broken) modifier order in ThumbnailBox:
```kotlin
Modifier
    .size(width = ..., height = ...)
    .clip(RoundedCornerShape(10.dp))          // (1) clips content
    .background(MainScreenTokens.mutedSurface)
    .border(                                   // (2) border is INSIDE clip — masked, invisible
        width = if (photo.isCurrent) 2.dp else 1.dp,
        color = if (photo.isCurrent) MainScreenTokens.thumbnailBorder
                else MainScreenTokens.thumbnailInactiveBorder,
        shape = RoundedCornerShape(10.dp),
    )
    .semantics { ... }
    .alpha(...)
    .testTag(...)
```

Fixed modifier order:
```kotlin
Modifier
    .size(width = ..., height = ...)
    .border(                                   // (1) border drawn BEFORE clip — visible
        width = if (photo.isCurrent) 2.dp else 1.dp,
        color = if (photo.isCurrent) MainScreenTokens.thumbnailBorder
                else MainScreenTokens.thumbnailInactiveBorder,
        shape = RoundedCornerShape(10.dp),
    )
    .clip(RoundedCornerShape(10.dp))          // (2) clips content, border already rendered
    .background(MainScreenTokens.mutedSurface)
    .semantics { ... }
    .alpha(...)
    .testTag(...)
```
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: Fix modifier order in ThumbnailBox — border before clip</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/ThumbnailStrip.kt</files>
  <action>
In ThumbnailBox, reorder the Modifier chain so `.border(...)` comes BEFORE `.clip(RoundedCornerShape(10.dp))`.

Current order (broken):
  .clip(RoundedCornerShape(10.dp))
  .background(MainScreenTokens.mutedSurface)
  .border(...)

Correct order:
  .border(
      width = if (photo.isCurrent) 2.dp else 1.dp,
      color = if (photo.isCurrent) MainScreenTokens.thumbnailBorder
              else MainScreenTokens.thumbnailInactiveBorder,
      shape = RoundedCornerShape(10.dp),
  )
  .clip(RoundedCornerShape(10.dp))
  .background(MainScreenTokens.mutedSurface)

Keep all other modifiers (.size, .semantics, .alpha, .testTag) in their existing relative positions. Do not change any logic, tokens, or values — only reorder the three modifiers listed above.
  </action>
  <verify>
    <automated>./gradlew :app:compileDebugKotlin 2>&1 | tail -5</automated>
  </verify>
  <done>ThumbnailStrip.kt compiles cleanly. The border modifier appears before clip in the modifier chain. The yellow border (MainScreenTokens.thumbnailBorder = 0xFFD1AE5E) is visible on the active thumbnail immediately when activeIndex advances.</done>
</task>

</tasks>

<verification>
Build passes:
  ./gradlew :app:assembleDebug

Manual check: swipe through 2–3 photos. Each time the active index advances, the yellow border should appear on the new active thumbnail without delay.
</verification>

<success_criteria>
- ThumbnailStrip.kt modifier order: .border() before .clip()
- Active thumbnail shows yellow (0xFFD1AE5E) border at 2.dp width immediately on index change
- Inactive thumbnails show dim border (0xFF2E2E2E) at 1.dp
- No compile errors
</success_criteria>

<output>
After completion, create `.planning/quick/17-fix-thumbnail-yellow-border-not-showing-/17-SUMMARY.md`
</output>
