# Quick Task 15: Move Proceed button to top bar left of three-dot menu - Context

**Gathered:** 2026-03-16
**Status:** Ready for planning

<domain>
## Task Boundary

Remove the blue Proceed button from the bottom of the main screen and add it to the top bar, to the LEFT of the three-dot (more options) menu. Blue background, white text, rounded shape.

</domain>

<decisions>
## Implementation Decisions

### Button shape & size
- Rounded pill/button shape. Blue background, white text. Should feel like a distinct action button in the top bar row.

### Bottom area cleanup
- Undo and Skip buttons stay exactly where they are — no changes to that area.
- Remove the `ProceedAffordance` composable / the bottom Proceed button entirely.

### Dead code cleanup
- Revert/remove the `navigationBarsPadding()` changes introduced in quick tasks 13 and 14 that were specifically for the bottom Proceed button (now irrelevant since the button moves to the top bar).
- Keep `statusBarsPadding()` fixes from task 12 (those fix the top bar/status bar inset and are still needed).

### Claude's Discretion
- Exact Compose implementation for a rounded button in the TopBar row (e.g., `Button` with `shape = RoundedCornerShape`, or a styled `TextButton`) — use whatever is idiomatic and visually clean.
- Precise blue color value — use the existing app blue used for the old Proceed button if available, otherwise a Material blue that matches the original button.

</decisions>

<specifics>
## Specific Ideas

- Top bar currently has: "DtMF" title on the left, three-dot ⋮ menu on the right.
- New layout: "DtMF" title on the left, then [Proceed button] [⋮ three-dot] on the right.
- The Proceed button should be visually compact enough to fit comfortably next to the three-dot icon.

</specifics>
