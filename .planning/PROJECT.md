# Android Photo Swipe Cleaner

## What This Is

An Android app for quickly triaging local gallery photos with Tinder-style swipe gestures. Each time the app opens, it randomly selects 30 photos from the device album, shows them in a swipe-based review flow, stages left-swiped photos for deletion, and then lets the user review and permanently delete them from a dedicated staging screen.

## Core Value

Make bulk photo cleanup fast and low-friction without losing the safety of a final review step before permanent deletion.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Match the provided screenshot-based UI closely for the main swipe screen and delete staging review screen
- [ ] Randomly sample 30 local-device photos on each app launch and support left-swipe to stage for deletion / right-swipe to skip
- [ ] Allow staged photos to be reviewed, batch-selected, and permanently deleted from the device after explicit confirmation

### Out of Scope

- User accounts, sync, or cloud photo sources — v1 is a single-user local-device utility
- A redesigned visual system different from the provided screenshots — the goal is screenshot-faithful UI rather than exploring a new aesthetic
- Cross-platform release targets beyond Android — the current scope is Android only

## Context

The product is intended for the user themself, not a general multi-user platform. The two provided screenshots define the primary UX target:

- `main.jpg`: dark gallery triage interface with thumbnail strip, large current photo preview, left/delete and right/skip actions, and a proceed action into review
- `Delete-staging-area..jpg`: dark review screen listing staged photos, allowing another selection pass before final deletion

The frontend should preserve the core structure, hierarchy, and visual feel of those screenshots. Motion should mimic the original swipe-based interaction as closely as practical, but Android system bars can follow platform defaults rather than reproducing the exact screenshot chrome.

The app operates only on on-device photos. The desired main behavior is:

1. Launch app
2. Randomly pull 30 photos from the local gallery
3. Swipe left to add a photo into delete staging
4. Swipe right to skip and leave the photo untouched
5. Tap proceed to enter the delete staging area
6. Review staged photos, batch choose what to delete, and permanently delete them

## Constraints

- **Platform**: Android only — the requested product is specifically an Android app
- **Design Fidelity**: Core screens should stay visually close to the provided screenshots — frontend accuracy is a primary requirement
- **Data Source**: Local device gallery only — no cloud integration or account model in v1
- **Safety**: Permanent deletion must happen only after a second review step — reduces accidental destructive actions

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Build for single-user local-device usage first | The app is intended for the user's own workflow and does not need product/account complexity | — Pending |
| Use screenshot-faithful dark UI for v1 | The user explicitly wants the frontend to match the provided screenshots closely | — Pending |
| Keep Android system bars platform-default | The user wants the app visually close to the screenshots but does not require exact system chrome reproduction | — Pending |
| Use staged review before destructive delete | The product's core safety model depends on confirming deletion in a second screen | — Pending |

---
*Last updated: 2026-03-14 after initialization*
