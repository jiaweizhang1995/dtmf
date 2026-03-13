# Feature Research

**Domain:** Android local-gallery swipe cleaner
**Researched:** 2026-03-14
**Confidence:** MEDIUM

## Feature Landscape

### Table Stakes (Users Expect These)

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Fast photo-by-photo swipe triage | This category exists to make cleanup dramatically faster than gallery multi-select | MEDIUM | Core product loop; must feel immediate and smooth |
| Clear keep vs delete staging semantics | Users need zero ambiguity around what swipe directions do | LOW | Direction labels, icons, and undo affordances matter |
| Final review before destructive action | Comparable products stage first and only delete after explicit review | MEDIUM | Matches screenshot flow and reduces accidental loss |
| Local-only privacy model | Users expect gallery cleaner apps to work on-device without cloud uploads | LOW | Strong trust feature for this category |
| Undo / recovery within current session | Swipe cleanup is high-volume and error-prone without a safety net | MEDIUM | Screenshot already shows Undo on the main screen |

### Differentiators (Competitive Advantage)

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Random 30-photo sampling on launch | Makes cleanup feel light and repeatable instead of overwhelming | LOW | This is specific to the user's vision and should stay in v1 |
| Screenshot-faithful dark UI | Recreates a proven visual pattern the user already wants | LOW | Important for stakeholder fit, even if not broadly differentiating in market terms |
| Batch re-selection in review screen | Lets users rescue false positives before deletion | MEDIUM | Present in the target review screen |
| Monthly / album filters | Makes giant galleries more manageable | MEDIUM | Common in adjacent products, but not required for this v1 |
| Duplicate / similar-photo detection | Increases deletion yield quickly | HIGH | Common upsell / premium feature in the category, defer from v1 |

### Anti-Features (Commonly Requested, Often Problematic)

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Cloud backup / sync in v1 | Sounds safer and more “complete” | Adds account, storage, privacy, and retention complexity unrelated to the core cleaning loop | Stay local-only and rely on OS/cloud providers the user already uses |
| Auto-delete without final review | Feels faster | Too risky for a destructive utility and undermines trust | Keep a mandatory staging / confirmation step |
| Full gallery-management suite | Seems like a natural expansion | Turns a focused cleaner into a general-purpose gallery app | Keep v1 focused on triage + review + delete |

## Feature Dependencies

```text
Media permission + gallery query
    └──requires──> random 30-photo session
                         └──requires──> swipe triage UI
                                              └──requires──> staged review
                                                                   └──requires──> delete confirmation flow

Undo support ──enhances──> swipe triage UI
Batch re-selection ──enhances──> staged review
Auto-delete ──conflicts──> mandatory review safety model
```

### Dependency Notes

- **Random session requires media access:** the app cannot pick a launch batch until it can enumerate local images.
- **Staged review requires session/state persistence:** even transiently, the app must remember which photos were staged.
- **Auto-delete conflicts with the core safety model:** it removes the second-chance review that the screenshots and user both want.

## MVP Definition

### Launch With (v1)

- [ ] Request and handle local gallery access for images — essential to the product
- [ ] Randomly select 30 photos per app launch — core interaction framing
- [ ] Tinder-style swipe flow with delete / skip actions — core value delivery
- [ ] Undo during current swipe session — basic safety
- [ ] Proceed into a review screen with staged items — required safety checkpoint
- [ ] Batch confirm and permanently delete selected staged items — completes the promise

### Add After Validation (v1.x)

- [ ] Trash instead of permanent delete option — add if users want a softer safety model
- [ ] Filters by month / album / screenshots — add if random-only feels too limiting
- [ ] Resume unfinished sessions after app restart — add if users commonly leave mid-session

### Future Consideration (v2+)

- [ ] Duplicate / similar-photo detection — valuable but materially more complex
- [ ] Video triage — expands scope and storage / preview handling
- [ ] Space-saved analytics and cleanup streaks — motivational but non-core

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Random 30-photo launch session | HIGH | LOW | P1 |
| Swipe left / right triage | HIGH | MEDIUM | P1 |
| Review staged deletions | HIGH | MEDIUM | P1 |
| Permanent delete confirmation | HIGH | MEDIUM | P1 |
| Undo last swipe | HIGH | MEDIUM | P1 |
| Month / album filtering | MEDIUM | MEDIUM | P2 |
| Trash-mode option | MEDIUM | MEDIUM | P2 |
| Duplicate detection | HIGH | HIGH | P3 |

**Priority key:**
- P1: Must have for launch
- P2: Should have, add when possible
- P3: Nice to have, future consideration

## Competitor Feature Analysis

| Feature | Competitor A | Competitor B | Our Approach |
|---------|--------------|--------------|--------------|
| Swipe triage | Swipewipe markets left/right swipe cleaning | Picly markets right-to-keep / left-to-delete simplicity | Keep the same familiar mental model |
| Review before delete | Slidebox stages “trashed” items and later permanently deletes | Some cleaners offer batch review after sorting | Make review mandatory in v1 for trust |
| Local privacy | Picly emphasizes on-device / private operation | Slidebox FAQ says actions apply directly to Android Gallery | Be explicit that v1 is local-device only |
| Gallery scope | Category leaders often add albums / duplicates / screenshots tools | Products expand into full cleaners over time | Keep v1 deliberately narrower around the two target screens |

## Sources

- https://slidebox.co/faq-android.html — behavior model for staged trash vs permanent delete on Android
- https://swipewipe.app/ — category framing for swipe-based gallery cleanup
- https://picly.app/ — secondary source for privacy-first and swipe-first positioning (search snippet used due site fetch error)
- https://www.hashphotos.app/howto/organize-like-slidebox/ — secondary source showing tray/review pattern in adjacent products

---
*Feature research for: Android local-gallery swipe cleaner*
*Researched: 2026-03-14*
