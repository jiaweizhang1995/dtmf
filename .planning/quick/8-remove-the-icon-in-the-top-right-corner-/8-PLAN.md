---
phase: quick-8
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/CurrentPhotoCard.kt
autonomous: true
requirements: [QUICK-8]
must_haves:
  truths:
    - "No icon badge is visible in the top-right corner of the hero image"
    - "The full original image is visible without cropping (letterbox/pillarbox bars are black)"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/CurrentPhotoCard.kt"
      provides: "Updated hero card without icon overlay and with Fit content scale"
  key_links:
    - from: "CurrentPhotoCard.kt"
      to: "AsyncImage"
      via: "contentScale = ContentScale.Fit"
      pattern: "ContentScale\\.Fit"
---

<objective>
Remove the unused "◪" icon badge from the top-right corner of CurrentPhotoCard and switch the hero AsyncImage from ContentScale.Crop to ContentScale.Fit with a black background so the full original photo is displayed without cropping.

Purpose: The icon is unused and adds visual noise. Crop silently cuts off photo edges; Fit preserves the entire original image.
Output: Updated CurrentPhotoCard.kt with icon Box removed and image displayed uncropped with black letterbox/pillarbox fill.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: Remove icon overlay and switch to uncropped image display</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/CurrentPhotoCard.kt</files>
  <action>
Make two targeted edits to CurrentPhotoCard.kt:

1. Delete the entire icon Box block (lines 56–70): the `Box` starting at `modifier = Modifier.align(Alignment.TopEnd)` through its closing brace — this removes the "◪" badge entirely.

2. On the AsyncImage (currently line 44–50):
   - Change `contentScale = ContentScale.Crop` to `contentScale = ContentScale.Fit`
   - Add `Modifier.background(Color.Black)` before `.fillMaxSize()` on the AsyncImage modifier so letterbox/pillarbox areas are black. Use the parent Box's existing black-ish background (`Color(0xFF2A261F)`) or override it to `Color.Black` — either is acceptable. The simplest approach is to set the outer Box background to `Color.Black` (replacing `Color(0xFF2A261F)`) and leave AsyncImage modifier as `Modifier.fillMaxSize()`.

Per user decisions: fill bars with black, keep existing heroAspectRatio unchanged.

Do NOT remove the gradient overlay Box or the bottom-start Text label — those stay.

After editing, verify `ContentScale` import remains (it is already present), and remove `Modifier.size` and any import that becomes unused after deleting the icon block (specifically `androidx.compose.foundation.layout.size` if nothing else uses it, and `androidx.compose.ui.text.font.FontWeight` and `androidx.compose.material3.Text` — BUT Text is still used for the bottom label, so keep that import. Only `size` and `FontWeight` and `FontWeight` become unused).

Unused imports to remove after the icon block is deleted:
- `androidx.compose.foundation.layout.size` (used only by heroOverlaySize)
- `androidx.compose.ui.text.font.FontWeight` (used only by icon Text)
  </action>
  <verify>
    <automated>cd /Users/jimmymacmini/Desktop/codex-project/wish-dtmf && ./gradlew :app:compileDebugKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>
- No "◪" icon Box exists in CurrentPhotoCard.kt
- AsyncImage uses ContentScale.Fit
- Outer Box background is Color.Black (or equivalent pure black)
- Kotlin compilation succeeds with no warnings about unused imports
  </done>
</task>

</tasks>

<verification>
After the task completes, build the debug APK and visually verify on device/emulator:
- Open the main swipe screen
- Confirm no icon badge in the top-right corner of the hero card
- Confirm the full photo is visible with black bars on sides or top/bottom depending on photo orientation
</verification>

<success_criteria>
- CurrentPhotoCard.kt compiles cleanly
- Icon overlay code is absent from the file
- contentScale is ContentScale.Fit
- Black fill visible in letterbox/pillarbox areas
</success_criteria>

<output>
After completion, create `.planning/quick/8-remove-the-icon-in-the-top-right-corner-/8-SUMMARY.md`
</output>
