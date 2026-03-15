# Quick Task 12: Fix top bar system insets — back button obstructed by status bar - Context

**Gathered:** 2026-03-15
**Status:** Ready for planning

<domain>
## Task Boundary

The back button on the review screen is obstructed by the system status bar because the app's TopBar does not account for system window insets. Fix this and apply consistent inset-aware padding to ALL screens app-wide so the issue cannot recur on any screen.

</domain>

<decisions>
## Implementation Decisions

### Scope of fix
- App-wide: audit every screen and apply consistent inset padding everywhere, not just the review screen.

### TopBar style after fix
- Padded below status bar: TopBar content sits below the status bar (not overlapping it). Use `windowInsetsPadding` / `statusBarsPadding` so the TopBar is always pushed clear of the system status bar on all Android versions.

### Claude's Discretion
- Specific Compose modifier to use (`statusBarsPadding()`, `WindowInsets.statusBars`, `Scaffold` padding, etc.) — use whatever is idiomatic in the existing codebase and consistent with the Compose version in use.
- If the app already calls `enableEdgeToEdge()` in MainActivity, keep it and add the padding modifiers; if not, do not add it (keep the simpler approach).

</decisions>

<specifics>
## Specific Ideas

- Primary symptom: back button on ReviewScreen hidden behind status bar.
- Goal: every screen's top-level container respects `WindowInsets.statusBars` (or equivalent) so no UI element is ever obscured by the status bar or navigation bar.
- No visual style change intended — just correct padding/insets.

</specifics>
