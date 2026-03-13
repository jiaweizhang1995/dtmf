# Project Research Summary

**Project:** Android Photo Swipe Cleaner
**Domain:** Android local-gallery swipe cleaner
**Researched:** 2026-03-14
**Confidence:** HIGH

## Executive Summary

This product fits a narrow but well-understood Android utility pattern: fast gallery cleanup through high-speed swipe triage plus a final destructive-action review. The strongest implementation path is a native Android app using Kotlin, Jetpack Compose, and `MediaStore`, because the app depends on local media access, high-fidelity custom UI, and OS-mediated delete confirmation rather than cross-platform reach or backend complexity.

Research suggests the user's requested scope is appropriately focused for v1. Category leaders use simple swipe semantics, stage items before permanent deletion, and emphasize local privacy. The main risks are not product-definition risks but execution risks: choosing the wrong Android permission model, making deletion feel unsafe, and shipping a swipe UI that looks right but janks on real devices.

## Key Findings

### Recommended Stack

Use a native Android stack: Kotlin, current stable Android SDK (`compileSdk` / `targetSdk` 36), Jetpack Compose + Material 3, lifecycle-aware `ViewModel`s, and `MediaStore` for gallery access and deletion. This keeps the implementation aligned with current Android guidance and best supports a screenshot-faithful swipe experience.

**Core technologies:**
- Kotlin: primary language — best Jetpack support
- Jetpack Compose: native UI and gesture/animation toolkit — best fit for the two target screens
- MediaStore: local gallery query and delete flow — required for shared-media operations
- ViewModel: state ownership — avoids losing swipe/review state during lifecycle changes

### Expected Features

The category's must-haves are fast swipe triage, clear keep/delete semantics, a second review step before destructive deletion, and local-only trust cues. For this project specifically, the differentiator is not AI or duplicate detection; it is the app's deliberately small scope plus the exact 30-photo random batch loop the user requested.

**Must have (table stakes):**
- Swipe-driven triage of local photos
- Mandatory review before permanent deletion
- Clear delete / skip semantics
- Undo within the current session

**Should have (competitive):**
- Random 30-photo session on each launch
- Screenshot-faithful dark UI
- Batch re-selection in review before delete

**Defer (v2+):**
- Duplicate / similar-photo detection
- Video cleanup
- Full gallery filter suite

### Architecture Approach

The cleanest structure is a single-activity Compose app with two feature packages (`swipe` and `review`), a repository over `MediaStore`, and small use cases for session building and deletion coordination. This keeps platform-sensitive media operations out of the UI while still moving quickly.

**Major components:**
1. Permission + launch gate — gets the app into a valid media-access state
2. Swipe session flow — shows the 30-photo batch and stages delete/skip decisions
3. Review + delete flow — lets the user unselect items and confirm permanent deletion
4. Media repository / delete coordinator — owns `MediaStore` queries and OS delete requests

### Critical Pitfalls

1. **Wrong permission model** — build around granular media permissions and `MediaStore`
2. **Unsafe destructive UX** — keep mandatory review and undo in the core flow
3. **Swipe performance problems** — size image loading correctly and profile gestures early
4. **Lifecycle state loss** — keep session state out of transient composables

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Android Foundation + Media Access
**Rationale:** Nothing else works until gallery enumeration and lifecycle-safe app structure exist.
**Delivers:** App scaffold, permissions, local image query, random 30-photo session generation.
**Addresses:** Core local-photo access requirements.
**Avoids:** Wrong permission model and state-loss pitfalls.

### Phase 2: Main Swipe Experience
**Rationale:** The primary value is the screenshot-faithful swipe screen.
**Delivers:** Main screen layout, image rendering, left/right swipe actions, undo, proceed behavior.
**Uses:** Compose gestures, animation, image loading.
**Implements:** Swipe-session feature module.

### Phase 3: Review + Permanent Delete
**Rationale:** The product is incomplete until staged photos can be reviewed and deleted safely.
**Delivers:** Review grid, batch reselection, system delete confirmation, post-delete reconciliation.
**Uses:** `MediaStore.createDeleteRequest()` and result handling.
**Implements:** Review feature and delete coordinator.

### Phase 4: Polish, Edge Cases, and Validation
**Rationale:** A destructive utility must survive real-device, performance, and lifecycle edge cases.
**Delivers:** Empty states, error states, rotation/background handling, visual polish, tests.
**Uses:** Compose UI tests and device validation.
**Implements:** Hardening across both user flows.

### Phase Ordering Rationale

- Media access and architecture must come before UI fidelity work.
- Swipe flow should be built before destructive deletion so staging semantics can be validated safely.
- Edge cases and OEM/device variance need a dedicated hardening phase because the app is destructive.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 1:** exact permission branching by Android version and chosen `minSdk`
- **Phase 3:** delete confirmation result handling and OEM media refresh behavior

Phases with standard patterns (skip research-phase):
- **Phase 2:** Compose screen construction and gesture-state architecture are standard

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Anchored to official Android docs |
| Features | MEDIUM | Product-category patterns are partly based on competitor/product sources |
| Architecture | HIGH | Standard modern Android app architecture |
| Pitfalls | HIGH | Rooted in official platform behavior and predictable destructive-flow risks |

**Overall confidence:** HIGH

### Gaps to Address

- Exact library versions should be locked during Phase 1 scaffold against the then-current Android Studio stable toolchain.
- Whether staged selections must survive full process death should be confirmed during phase planning; v1 can reasonably choose either behavior if documented.

## Sources

### Primary (HIGH confidence)
- https://developer.android.com/tools/releases/platforms — stable Android SDK baseline
- https://developer.android.com/compose — Compose / Material 3 guidance
- https://developer.android.com/topic/libraries/architecture/viewmodel — state ownership guidance
- https://developer.android.com/training/data-storage/room — local persistence guidance
- https://developer.android.com/about/versions/13/behavior-changes-13#granular-media-permissions — media permission model
- https://developer.android.com/reference/android/provider/MediaStore — delete/trash/write request APIs

### Secondary (MEDIUM confidence)
- https://slidebox.co/faq-android.html — staged trash vs permanent delete behavior in adjacent Android product
- https://swipewipe.app/ — swipe-cleaner product framing
- https://picly.app/ — privacy / swipe / duplicate-cleaner category cues

---
*Research completed: 2026-03-14*
*Ready for roadmap: yes*
