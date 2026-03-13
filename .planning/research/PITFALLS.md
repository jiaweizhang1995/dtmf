# Pitfalls Research

**Domain:** Android local-gallery swipe cleaner
**Researched:** 2026-03-14
**Confidence:** HIGH

## Critical Pitfalls

### Pitfall 1: Wrong Permission Model for Gallery Access

**What goes wrong:**
The app cannot enumerate local photos consistently, or works on some Android versions and breaks on newer ones.

**Why it happens:**
Developers rely on deprecated broad storage assumptions instead of Android's granular media permission model.

**How to avoid:**
Design the media-access layer around `READ_MEDIA_IMAGES` on Android 13+ and version-aware compatibility handling for older devices. Treat photo-picker-only approaches as insufficient for app-driven random sampling.

**Warning signs:**
Works only after manual testing on one device; media queries return empty or inconsistent sets on Android 13+.

**Phase to address:**
Phase 1

---

### Pitfall 2: Destructive Delete Flow Feels Unsafe or Confusing

**What goes wrong:**
Users lose trust because they are not sure what gets deleted, or they accidentally delete something with no obvious second chance.

**Why it happens:**
The app optimizes for speed but under-designs confirmation, undo, and review semantics.

**How to avoid:**
Keep a mandatory staging area, provide clear delete/skip labels, and support undo during the swipe loop before review.

**Warning signs:**
Users hesitate during swiping, ask what left/right means, or abandon the app before tapping proceed.

**Phase to address:**
Phase 2

---

### Pitfall 3: Swipe UI Looks Right but Performs Poorly

**What goes wrong:**
The app drops frames, flashes when images change, or stutters while swiping high-resolution photos.

**Why it happens:**
Large image decode work or state mutation is happening on the UI thread, or the app renders full-size assets unnecessarily.

**How to avoid:**
Use thumbnail-appropriate loading, profile Compose performance early, and keep image loading / session mutation off the main thread.

**Warning signs:**
Visible lag on mid-range devices, jank during quick consecutive swipes, memory spikes when loading the next photo.

**Phase to address:**
Phase 2

---

### Pitfall 4: Session State Breaks on Rotation or Process Death

**What goes wrong:**
The user loses staged choices, undo history, or the current card position after the app is backgrounded or recreated.

**Why it happens:**
State is stored only inside composables or transient objects with no lifecycle-aware owner.

**How to avoid:**
Keep screen state in `ViewModel`; decide explicitly whether v1 needs persistence beyond process lifetime and implement Room if yes.

**Warning signs:**
Returning to the app restarts the batch unexpectedly or review selections disappear.

**Phase to address:**
Phase 1

---

### Pitfall 5: OEM / Device Variance in Media Behavior

**What goes wrong:**
Delete confirmation or media indexing behaves differently across vendor devices, causing “deleted but still visible” or “not found” confusion.

**Why it happens:**
Gallery-related flows depend on platform media indexing and OEM gallery integrations.

**How to avoid:**
Test on multiple Android versions / OEMs, reconcile post-delete state from fresh queries, and design the UI to handle eventual gallery refresh delays gracefully.

**Warning signs:**
Deleted items remain visible until rescan, staged review shows stale thumbnails, or one OEM behaves differently from the emulator.

**Phase to address:**
Phase 3

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Keep all state in composables | Faster initial coding | Fragile lifecycle behavior and hard-to-test destructive flows | Never for core swipe / review state |
| Skip Room entirely | Less code | Harder recovery if restart persistence becomes required | Acceptable only if v1 explicitly tolerates session reset on process death |
| Use raw file paths | Feels straightforward | Breaks against modern shared-storage assumptions | Never |

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| MediaStore query | Querying too much metadata too early | Start with only fields needed for list / preview rendering |
| Delete request | Assuming app can silently delete everything | Use `MediaStore.createDeleteRequest()` and handle user approval / cancellation |
| Permissions | Using a single code path for all Android versions | Branch by API level for permission behavior |

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Full-resolution image decode in swipe loop | Frame drops and memory churn | Load screen-sized or thumbnail-appropriate assets | Breaks immediately on large libraries / mid-range devices |
| Re-querying gallery on every swipe | Input lag | Build a session batch once, then mutate in memory | Breaks during quick swipe sessions |
| Rebuilding heavy UI state each recomposition | Jank and visual instability | Keep derived state minimal and stable | Breaks under rapid gesture input |

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Over-requesting storage access | User distrust and policy risk | Request only image access actually required by the app |
| Ambiguous destructive wording | Unintentional permanent deletion | Use explicit permanent-delete language and confirmation |
| Logging media identifiers carelessly | Privacy leakage in debug logs | Keep logs minimal and strip sensitive output in release builds |

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Left/right directions are unclear | Users mis-swipe and lose confidence | Use labels, icons, and consistent color semantics |
| Review screen does not show selection state clearly | Users cannot tell what will be deleted | Use obvious checked/unchecked visual state per item |
| Proceed is available with zero staged items but unclear outcome | Confusion and dead-end feeling | Define and communicate empty-state behavior explicitly |

## "Looks Done But Isn't" Checklist

- [ ] **Permissions:** Verify behavior on Android 13+ and pre-13 paths
- [ ] **Swipe loop:** Verify undo works after multiple rapid swipes
- [ ] **Review screen:** Verify unselected items are never included in deletion request
- [ ] **Delete flow:** Verify canceled system confirmation leaves local state intact
- [ ] **Visual fidelity:** Verify the two target screens still match screenshot structure after edge-to-edge / inset handling

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Wrong permission model | MEDIUM | Refactor repository access, retest across API levels, update onboarding |
| Unsafe delete UX | HIGH | Redesign flow, add review/undo affordances, re-validate with manual testing |
| Poor swipe performance | MEDIUM | Profile, reduce image load size, simplify recomposition hotspots |
| OEM variance | MEDIUM | Add device-specific regression tests and refresh-state reconciliation |

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| Wrong permission model | Phase 1 | Media query works across target Android versions |
| Unsafe delete UX | Phase 2 | Manual UAT confirms clear swipe / review / confirmation semantics |
| Poor swipe performance | Phase 2 | Swipe flow remains smooth under realistic image sizes |
| Session state loss | Phase 1 | Rotation / recreation tests preserve intended state behavior |
| OEM variance | Phase 3 | Real-device delete flow matches expected state after confirmation |

## Sources

- https://developer.android.com/about/versions/13/behavior-changes-13#granular-media-permissions
- https://developer.android.com/reference/android/provider/MediaStore
- https://developer.android.com/about/versions/15/behavior-changes-15
- https://slidebox.co/faq-android.html

---
*Pitfalls research for: Android local-gallery swipe cleaner*
*Researched: 2026-03-14*
