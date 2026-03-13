# Phase 1: Foundation & Media Access - Context

**Gathered:** 2026-03-14
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver the Android app foundation needed to access the local image library, create a random launch batch, and preserve the active session through normal in-session lifecycle events. This phase does not build the full screenshot-faithful swipe UI or the delete review screen; it defines how the app enters those flows and what state exists when they start.

</domain>

<decisions>
## Implementation Decisions

### Launch path
- First open should go straight into a minimal permission-requesting entry flow, not an intro page and not a shell homepage
- Once permission is granted, the app should immediately generate the current batch and enter the main flow without an extra “start” step
- Reopening the app should start a new random batch by default rather than asking to resume a prior launch session
- Entry-state copy should feel like a minimal utility tool: short, calm, and not brand-heavy

### Photo eligibility for the launch batch
- The launch batch should be drawn from the full local photo library, not only recent items
- v1 should sample ordinary photos only; videos are excluded
- The candidate pool should avoid hidden / trash / non-normal gallery content where possible
- If fewer than 30 eligible photos exist, the app should proceed with the available count rather than blocking the user

### In-session continuity
- Rotation should preserve the exact current position in the active batch
- Backgrounding and returning to the app should preserve the current batch, current item, and staged decisions
- Phase 1 does not need to guarantee restoration after full process death or full app relaunch; starting a fresh batch is acceptable there
- Staged decisions only need to persist for the currently active session, not across hard restarts

### Empty and failure states
- If no eligible photos exist, show a minimal utility-style message with retry / exit behavior rather than an educational flow
- If permission is denied, stay on a minimal explanation state with a clear way to request permission again
- If gallery loading fails after permission is granted, use a short, tool-like error state rather than a verbose explanation
- The primary recovery action for abnormal entry states should be retry; do not overcomplicate the screen with multiple competing actions

### Claude's Discretion
- Exact wording of minimal entry / empty / error copy
- Whether “exit” is an explicit button or a passive back/system behavior in no-photo states
- Exact visual treatment of loading / permission states before the screenshot-faithful UI is built

</decisions>

<specifics>
## Specific Ideas

- The product rule remains: every normal app launch creates a new random set of 30 local photos
- The app should feel like a focused cleanup utility, not a branded onboarding-heavy product
- “Random 30” means true full-library random sampling, with no recent-first bias

</specifics>

<code_context>
## Existing Code Insights

### Reusable Assets
- None yet: the repository currently contains only the two reference screenshots

### Established Patterns
- No application code exists yet, so Phase 1 is free to establish the initial Android/Compose patterns
- Project-level constraints already established: Android-only, local-device-only, mandatory review before permanent delete in later phases

### Integration Points
- Phase 1 will define the app shell and gallery-access entry point that later phases build on
- The media-access/session model from this phase must feed the later swipe screen and review screen without changing the core launch semantics

</code_context>

<deferred>
## Deferred Ideas

- Cross-relaunch session restore: captured as future safety enhancement, not required in Phase 1
- Trash-mode option instead of permanent delete: later phase / future version
- Filters by month / album or smarter candidate selection: later phase / future version
- Video cleanup support: out of Phase 1 and out of v1 scope

</deferred>

---

*Phase: 01-foundation-media-access*
*Context gathered: 2026-03-14*
