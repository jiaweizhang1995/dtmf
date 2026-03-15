---
name: Quick Task 10 - Top bar with More options menu
description: Context for adding top bar with DtMF title and three-dots menu with Enable thumbnails toggle
type: project
---

# Quick Task 10: Add top bar with three-dots More options menu containing Enable thumbnails toggle - Context

**Gathered:** 2026-03-15
**Status:** Ready for planning

<domain>
## Task Boundary

Add a top bar to the main screen with:
- A title label "DtMF" in yellow font on black background
- A three-dots (⋮) "More options" icon in the top-right corner
- Tapping the icon opens a dropdown menu with one item: "Enable thumbnails" as a checkable toggle

When "Enable thumbnails" is toggled OFF, the ThumbnailStrip is hidden. When ON, it is shown.

</domain>

<decisions>
## Implementation Decisions

### Toggle default state
- **ON** by default — thumbnails are shown when the app starts

### Toggle persistence
- **Session-only** — resets to default (ON) on each app launch; no SharedPreferences/DataStore needed

### Top bar appearance
- Title: **"DtMF"** displayed in **yellow font** on **black background**
- Three-dots icon positioned at **top-right**
- Black background matches the existing dark theme of the app

### Claude's Discretion
- Exact yellow color value (use a warm yellow that reads well on black, e.g. Color(0xFFFFD600) or similar)
- Whether to use Material3 TopAppBar or a custom Row composable (prefer whatever fits the existing pattern — the app previously used a custom Row)
- Menu implementation: use Material3 DropdownMenu + DropdownMenuItem with a leading Checkbox or trailing checkmark

</decisions>

<specifics>
## Specific Ideas

- Existing pattern reference: `MainTopBar` was a custom Row composable (now deleted in quick-9); recreate as a similar Row or use TopAppBar
- ThumbnailStrip is rendered in `MainScreen.kt` — its visibility should be conditioned on the toggle state
- Toggle state can live as a `remember { mutableStateOf(true) }` in the MainScreen composable (session-only, no VM needed)
- The three-dots icon: use `Icons.Default.MoreVert` from material-icons (already available)

</specifics>
