---
phase: quick-10
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
autonomous: true
requirements: [QUICK-10]

must_haves:
  truths:
    - "A top bar appears at the top of the main screen with the title 'DtMF' in yellow on black"
    - "A three-dots icon (⋮) appears at the right of the top bar"
    - "Tapping the three-dots icon opens a dropdown menu with an 'Enable thumbnails' item and a checkmark indicating current state"
    - "Unchecking 'Enable thumbnails' hides the ThumbnailStrip; checking it shows the ThumbnailStrip"
    - "Toggle defaults to ON (thumbnails visible) on every app launch"
  artifacts:
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt"
      provides: "MainTopBar composable + showThumbnails toggle state + conditional ThumbnailStrip"
  key_links:
    - from: "MainScreen"
      to: "ThumbnailStrip"
      via: "if (showThumbnails) ThumbnailStrip(...)"
---

<objective>
Add a top bar to the main screen with a yellow "DtMF" title and a three-dots More options menu that contains an "Enable thumbnails" toggle. Toggling off hides the ThumbnailStrip; toggling on shows it. State is session-only (resets to ON on app launch).

Purpose: Give the user a header and a quick way to reclaim vertical space by hiding the thumbnail rail.
Output: Updated MainScreen.kt with MainTopBar composable, DropdownMenu, and conditional ThumbnailStrip visibility.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@.planning/quick/10-add-top-bar-with-three-dots-more-options/10-CONTEXT.md

<interfaces>
<!-- From MainScreen.kt — current structure the executor is modifying -->

MainScreen composable signature (unchanged — no new params needed):
```kotlin
@Composable
fun MainScreen(
    uiState: MainUiState,
    onStageCurrentPhoto: () -> Unit,
    onSkipCurrentPhoto: () -> Unit,
    onUndoLastDecision: () -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Current Column children order (top to bottom):
1. ThumbnailStrip(photos, activeIndex)
2. MainMetadataRow(uiState)
3. SwipePhotoCard / SessionCompleteCard
4. BottomActionRow
5. ProceedAffordance

The top bar must be added ABOVE the Column (as the first thing inside BoxWithConstraints),
or as the first item inside the Column — either works; adding it before the Column as a
separate Row at the top of BoxWithConstraints is cleanest.

Icons.Default.MoreVert is available via material-icons-extended (already in dependencies).
DropdownMenu and DropdownMenuItem are available in androidx.compose.material3.
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add MainTopBar with More options menu and conditional ThumbnailStrip</name>
  <files>app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt</files>
  <action>
Modify MainScreen.kt to add a top bar and a session-scoped thumbnails toggle:

1. **Toggle state** — At the top of the MainScreen composable body, add:
   ```kotlin
   var showThumbnails by remember { mutableStateOf(true) }
   var menuExpanded by remember { mutableStateOf(false) }
   ```

2. **Restructure layout** — Change the BoxWithConstraints content from a single Column to a Column that spans the full height, with the top bar as the first element:
   - Add a `MainTopBar` composable call as the first child of the outer Column (before ThumbnailStrip).
   - Wrap the existing ThumbnailStrip call in `if (showThumbnails) { ... }`.

3. **MainTopBar private composable** — Add this private composable to the file:
   ```kotlin
   @Composable
   private fun MainTopBar(
       menuExpanded: Boolean,
       showThumbnails: Boolean,
       onMenuOpen: () -> Unit,
       onMenuDismiss: () -> Unit,
       onToggleThumbnails: () -> Unit,
   ) {
       Row(
           modifier = Modifier
               .fillMaxWidth()
               .background(Color.Black)
               .padding(horizontal = 4.dp, vertical = 4.dp),
           verticalAlignment = Alignment.CenterVertically,
           horizontalArrangement = Arrangement.SpaceBetween,
       ) {
           Text(
               text = "DtMF",
               color = Color(0xFFFFD600),
               fontWeight = FontWeight.Bold,
               fontSize = 20.sp,
               modifier = Modifier.padding(start = 8.dp),
           )
           Box {
               IconButton(onClick = onMenuOpen) {
                   Icon(
                       imageVector = Icons.Default.MoreVert,
                       contentDescription = "More options",
                       tint = Color.White,
                   )
               }
               DropdownMenu(
                   expanded = menuExpanded,
                   onDismissRequest = onMenuDismiss,
               ) {
                   DropdownMenuItem(
                       text = { Text("Enable thumbnails") },
                       onClick = onToggleThumbnails,
                       trailingIcon = {
                           if (showThumbnails) {
                               Icon(
                                   imageVector = Icons.Default.Check,
                                   contentDescription = "Enabled",
                               )
                           }
                       },
                   )
               }
           }
       }
   }
   ```

4. **Wire MainTopBar** — In MainScreen, replace the existing Column's first child (ThumbnailStrip) with:
   ```kotlin
   MainTopBar(
       menuExpanded = menuExpanded,
       showThumbnails = showThumbnails,
       onMenuOpen = { menuExpanded = true },
       onMenuDismiss = { menuExpanded = false },
       onToggleThumbnails = {
           showThumbnails = !showThumbnails
           menuExpanded = false
       },
   )
   if (showThumbnails) {
       ThumbnailStrip(
           photos = uiState.photos,
           activeIndex = uiState.activePhotoIndex,
       )
   }
   ```

5. **Add required imports** — Ensure the following are imported (add only what is missing):
   - `androidx.compose.material3.DropdownMenu`
   - `androidx.compose.material3.DropdownMenuItem`
   - `androidx.compose.material3.Icon`
   - `androidx.compose.material3.IconButton`
   - `androidx.compose.material.icons.Icons`  (already via material3)
   - `androidx.compose.material.icons.filled.MoreVert`
   - `androidx.compose.material.icons.filled.Check`
   - `androidx.compose.runtime.getValue`
   - `androidx.compose.runtime.setValue`
   - `androidx.compose.runtime.mutableStateOf`
   - `androidx.compose.ui.unit.sp`

   Note: `Icons.Default.MoreVert` and `Icons.Default.Check` are in material-icons-extended which is already a dependency. Do NOT add a new gradle dependency.

   Note: The existing BoxWithConstraints outer modifier already applies horizontal padding via `MainScreenTokens.screenPadding`. The top bar Row uses `Modifier.fillMaxWidth()` — it will respect that padding. If you want the top bar to span edge-to-edge without the screen padding, wrap the Column body so that the top bar sits outside the padded area. The simplest approach: keep the top bar inside the Column (which already has horizontal padding from BoxWithConstraints) and accept that slight inset — this is consistent with the rest of the screen.
  </action>
  <verify>
    <automated>./gradlew :app:assembleDebug 2>&1 | tail -20</automated>
  </verify>
  <done>
    Build succeeds. The main screen has a black top bar showing "DtMF" in yellow with a ⋮ icon. Tapping ⋮ shows a dropdown with "Enable thumbnails" and a checkmark. Toggling it hides/shows the thumbnail strip. Defaults to ON on launch.
  </done>
</task>

</tasks>

<verification>
Run `./gradlew :app:assembleDebug` — must complete with BUILD SUCCESSFUL.
Install on device/emulator and confirm:
- Top bar shows "DtMF" in yellow on black background
- Three-dots icon is tappable and opens a menu
- "Enable thumbnails" menu item has a checkmark when ON
- Toggling off collapses the thumbnail strip; toggling on restores it
- Fresh launch always starts with thumbnails visible
</verification>

<success_criteria>
Build succeeds without errors. Top bar with DtMF title and functional More options thumbnail toggle is visible and working on device.
</success_criteria>

<output>
After completion, create `.planning/quick/10-add-top-bar-with-three-dots-more-options/10-SUMMARY.md` with what was changed and any notable implementation details.
</output>
