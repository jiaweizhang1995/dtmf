# Roadmap: Android Photo Swipe Cleaner

## Overview

This roadmap takes the project from Android/media foundations to a screenshot-faithful swipe-cleaning flow, then adds the review-and-delete safety loop, and finally hardens the app against edge cases that matter for a destructive utility. The phase order follows the product's real dependency chain: no UI fidelity work before reliable media access, no deletion before staged review, and no launch-ready claim before lifecycle, error, and device-variance validation.

## Phases

- [ ] **Phase 1: Foundation & Media Access** - Create the Android scaffold, lifecycle-safe state model, permissions flow, and random 30-photo launch session
- [ ] **Phase 2: Main Swipe Experience** - Recreate the primary `main.jpg` screen with smooth photo viewing and left/right swipe behavior
- [ ] **Phase 3: Session Controls & Navigation** - Add undo, proceed behavior, and the bridge from the swipe loop into review
- [ ] **Phase 4: Review & Permanent Delete** - Build the `Delete-staging-area..jpg` flow and complete safe destructive deletion
- [ ] **Phase 5: Hardening & Launch Validation** - Cover empty states, cancellation paths, device quirks, and final UI/UX validation

## Phase Details

### Phase 1: Foundation & Media Access
**Goal**: Deliver a working Android app skeleton that can read the local image library, create a 30-photo session on launch, and preserve core session state through normal lifecycle changes.
**Depends on**: Nothing (first phase)
**Requirements**: [MEDIA-01, MEDIA-02, UX-03]
**Success Criteria** (what must be TRUE):
  1. User can grant image access and the app responds correctly to granted or denied states
  2. App launches into a valid session with 30 random local photos when enough eligible photos exist
  3. The current session state survives rotation / configuration changes without losing the active batch
**Plans**: 3 plans

Plans:
- [ ] 01-01: Scaffold the Android app, Compose shell, and lifecycle-safe state architecture
- [ ] 01-02: Implement media permission handling and gallery querying through `MediaStore`
- [ ] 01-03: Build random session generation and state restoration for the active batch

### Phase 2: Main Swipe Experience
**Goal**: Recreate the core look and feel of `main.jpg` with a smooth swipe-first main screen.
**Depends on**: Phase 1
**Requirements**: [SWIPE-01, SWIPE-02, SWIPE-03, UX-01]
**Success Criteria** (what must be TRUE):
  1. User sees a main screen whose layout and hierarchy closely match `main.jpg`
  2. User can swipe left to stage a photo for deletion and swipe right to skip it
  3. Swipe interactions feel responsive on realistic device photo sizes
**Plans**: 3 plans

Plans:
- [ ] 02-01: Implement screenshot-faithful main layout, theme, and shared UI components
- [ ] 02-02: Add image rendering, thumbnail strip, and current-card presentation
- [ ] 02-03: Implement left/right swipe gestures and stage/skip state updates

### Phase 3: Session Controls & Navigation
**Goal**: Complete the main session loop with undo and a reliable handoff into review.
**Depends on**: Phase 2
**Requirements**: [SWIPE-04, SWIPE-05]
**Success Criteria** (what must be TRUE):
  1. User can undo the most recent swipe decision without corrupting staged state
  2. User can tap proceed and arrive at review with the correct staged photo set
  3. Proceed behavior is well-defined even when the staged set is empty
**Plans**: 2 plans

Plans:
- [ ] 03-01: Add undo behavior and session-control logic to the main flow
- [ ] 03-02: Wire navigation and pass staged-review state from swipe to review

### Phase 4: Review & Permanent Delete
**Goal**: Build the delete staging area and complete the permanent-delete confirmation flow safely.
**Depends on**: Phase 3
**Requirements**: [REVW-01, REVW-02, REVW-03, UX-02]
**Success Criteria** (what must be TRUE):
  1. User can view staged photos in a review screen that closely matches `Delete-staging-area..jpg`
  2. User can deselect items they want to keep before deletion
  3. User can confirm deletion and the system delete flow removes the approved photos only
**Plans**: 3 plans

Plans:
- [ ] 04-01: Build the review grid/list, selection UI, and screenshot-faithful action area
- [ ] 04-02: Implement review-state mutations and re-selection behavior
- [ ] 04-03: Integrate system confirmation and permanent deletion through `MediaStore`

### Phase 5: Hardening & Launch Validation
**Goal**: Make the app robust for real use by covering error states, cancellation paths, and final acceptance checks.
**Depends on**: Phase 4
**Requirements**: [MEDIA-03, REVW-04]
**Success Criteria** (what must be TRUE):
  1. User sees clear behavior for empty-library, permission-denied, and zero-staged-item cases
  2. Cancelled or partial delete results leave the app in a consistent and accurate state
  3. The two core screens remain visually aligned with the provided screenshots after polish and device validation
**Plans**: 3 plans

Plans:
- [ ] 05-01: Add empty, denied, cancelled, and partial-failure states
- [ ] 05-02: Validate lifecycle, device-variance, and performance edge cases
- [ ] 05-03: Finalize polish, UI acceptance checks, and launch-readiness verification

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Foundation & Media Access | 0/3 | Not started | - |
| 2. Main Swipe Experience | 0/3 | Not started | - |
| 3. Session Controls & Navigation | 0/2 | Not started | - |
| 4. Review & Permanent Delete | 0/3 | Not started | - |
| 5. Hardening & Launch Validation | 0/3 | Not started | - |
