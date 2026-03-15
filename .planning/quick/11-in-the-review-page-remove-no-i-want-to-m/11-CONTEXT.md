# Quick Task 11: Remove "No, i want to move to trash" and "Decide Later" buttons from review page - Context

**Gathered:** 2026-03-15
**Status:** Ready for planning

<domain>
## Task Boundary

In the review page, remove the "No, i want to move to trash" and "Decide Later" buttons entirely.

</domain>

<decisions>
## Implementation Decisions

### Remaining Button Layout
- The "Delete forever" button should be centered since it is now the only button left in that area. Other elements stay as they were.

### Code Cleanup Scope
- Remove all dead code associated with the deleted buttons: callbacks, click handlers, state, and any logic exclusively tied to these two buttons.

### Claude's Discretion
- Exact centering approach (e.g., `Alignment.Center`, `fillMaxWidth` modifier, etc.) — use whatever is idiomatic in the existing codebase.

</decisions>

<specifics>
## Specific Ideas

- Only the review page is in scope — no other screens affected.
- "Delete forever" button remains and should be centered after the other two are removed.

</specifics>
