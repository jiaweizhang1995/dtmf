# Phase 05 — Visual Acceptance & Launch-Readiness Checklist

**Reference images:** `main.jpg` and `Delete-staging-area..jpg` (project root)
**Device used for comparison:** Real Android device (Phase 05-02 validation device)
**Android version:** Android 11+ (minSdk 30)
**Date:** 2026-03-15

---

## Screen 1: Main Swipe Screen vs. `main.jpg`

Open the app on the device. Start a session. Open `main.jpg` in a second window for side-by-side comparison.

| # | Element | Expected (from main.jpg) | Result | Notes |
|---|---------|--------------------------|--------|-------|
| 1.1 | Background | Dark (near-black) fills full viewport | PASS | |
| 1.2 | Top bar | Back chevron (left), centered title text, overflow icon (right) | PASS | |
| 1.3 | Thumbnail strip | Horizontal strip below top bar; active photo highlighted; other thumbnails visible and proportional | PASS | |
| 1.4 | Metadata chip row | Info icon, file size, MIME type, position indicator (e.g. "1 / 30") visible below thumbnail strip | PASS | |
| 1.5 | Hero photo card | Rounded corners; occupies the dominant vertical space; no content clipping | PASS | |
| 1.6 | Action row | Four icons in correct order: delete (left), undo (middle-left), skip (middle-right), confirm (right); undo dimmed when unavailable | PASS | |
| 1.7 | Proceed affordance | Blue "Proceed" pill/button at bottom-right with supporting copy below or alongside | PASS | |
| 1.8 | Overall hierarchy | No element obviously out of place compared to main.jpg | PASS | |

---

## Screen 2: Review / Delete Staging vs. `Delete-staging-area..jpg`

Swipe all 30 photos left, tap Proceed, enter the review screen.

| # | Element | Expected (from Delete-staging-area..jpg) | Result | Notes |
|---|---------|------------------------------------------|--------|-------|
| 2.1 | Background | Dark (near-black) fills full viewport | PASS | |
| 2.2 | App bar | Back arrow `<` at left; `REVIEW` title text | PASS | |
| 2.3 | Destructive prompt | Left teal border; "Permanently delete N items?" in bold | PASS | |
| 2.4 | Helper link | Teal-colored helper text below the destructive prompt | PASS | |
| 2.5 | Photo grid | Two-column grid; teal border on selected tiles; checkmark badge (top-left) on selected tiles | PASS | |
| 2.6 | Deselected tile treatment | Deselected tiles visibly dimmed (alpha reduced) compared to selected tiles | PASS | |
| 2.7 | Bottom helper copy | Subtle-colored helper text above the CTA row | PASS | |
| 2.8 | Decide Later CTA | Full-width grey button at bottom-left | PASS | |
| 2.9 | Delete forever CTA | Full-width teal button at bottom-right when items selected; dimmed/disabled when none selected | PASS | |
| 2.10 | Overall hierarchy | No element obviously out of place compared to Delete-staging-area..jpg | PASS | |

---

## Section 3: Edge-State Visual Checks

| # | Scenario | Expected | Result | Notes |
|---|----------|----------|--------|-------|
| 3.1 | EntryScreen — Empty state (use emulator with no photos) | Icon + "No photos to clean up" title + explanatory body + "Scan again" button visible | PASS | |
| 3.2 | EntryScreen — NeedsPermission with showSettingsHint | Copy + "Try permission again" button + "Open app settings" button visible | PASS | |
| 3.3 | EntryScreen — LoadingBatch | CircularProgressIndicator + "Preparing a fresh photo batch..." text visible | PASS | |
| 3.4 | Review — empty grid (dev/test path with zero staged IDs) | "No photos staged for deletion." text visible in grid area | PASS | |

---

## Section 4: Launch Readiness

| # | Item | Status |
|---|------|--------|
| 4.1 | All Phase 5 automated tests pass (`./gradlew testDebugUnitTest connectedDebugAndroidTest`) | PASS |
| 4.2 | 05-02 lifecycle checklist signed with no unresolved FAIL items | PASS |
| 4.3 | Main screen visually accepted (Section 1 all PASS) | PASS |
| 4.4 | Review screen visually accepted (Section 2 all PASS) | PASS |
| 4.5 | Edge states visually accepted (Section 3 all PASS) | PASS |
| 4.6 | No open blocker items in STATE.md | PASS |
| 4.7 | REQUIREMENTS.md v1 requirements: all 13 marked complete | PASS |

---

## Sign-Off

**v1 launch-ready:** YES

**Remaining work before launch (if CONDITIONAL or NO):**

None.

**Signed:** approved - v1 launch-ready
**Date:** 2026-03-15
