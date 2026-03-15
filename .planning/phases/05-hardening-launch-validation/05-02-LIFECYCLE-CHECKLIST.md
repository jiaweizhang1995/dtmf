# Phase 05 — Lifecycle & Device-Variance Validation Checklist

**Device under test:** ___________________________
**Android version:** ___________________________
**Build variant:** debug
**Date:** ___________________________

## Pre-flight

- [ ] `./gradlew assembleDebug` succeeds with no warnings
- [ ] `./gradlew testDebugUnitTest` green
- [ ] App installed on physical device via `adb install`

---

## Section 1: Configuration Changes (Rotation)

| # | Scenario | Expected | Result | Notes |
|---|----------|----------|--------|-------|
| 1.1 | Rotate device mid-swipe session (not at session boundary) | Active card, staged count, undo availability preserved; no crash | | |
| 1.2 | Rotate device with at least one photo deselected in review screen | Deselected items remain deselected after rotation; count label correct | | |
| 1.3 | Rotate device while LaunchUiState.LoadingBatch is visible | Loading indicator reappears; batch loads normally after rotation | | |
| 1.4 | Rotate device while LaunchUiState.Empty is visible | Empty state (icon + copy) reappears correctly | | |

---

## Section 2: Background / Foreground Transitions

| # | Scenario | Expected | Result | Notes |
|---|----------|----------|--------|-------|
| 2.1 | Press Home mid-swipe, return to app | Session restored to exactly the same card, staged count unchanged | | |
| 2.2 | Press Home while system delete confirmation dialog is open, return | Dialog dismissed by OS; review screen shows original selection intact (cancel path) | | |
| 2.3 | Receive a phone call mid-swipe (or simulate via notification); return | Session fully restored | | |

---

## Section 3: Process Death

Enable "Don't keep activities" in Developer Options before this section.

| # | Scenario | Expected | Result | Notes |
|---|----------|----------|--------|-------|
| 3.1 | Navigate to review screen, press Home, wait for process kill, reopen app | App returns to EntryScreen and begins a new session (process death clears back stack) | | |
| 3.2 | Complete a deletion on a fresh session post-process-death | Deletion flow works normally; refreshed session shows correct photo count | | |

Disable "Don't keep activities" after this section.

---

## Section 4: Post-Delete State Accuracy (REVW-04)

| # | Scenario | Expected | Result | Notes |
|---|----------|----------|--------|-------|
| 4.1 | Delete all selected photos from a small batch (3–5 photos) | Post-delete session starts fresh; no Coil 404 errors in Logcat for deleted IDs | | |
| 4.2 | Cancel from the system delete confirmation dialog | Review screen unchanged; all previously-selected items still selected | | |
| 4.3 | Enter review with a single photo and delete it | Post-delete: if library is now empty, EntryScreen shows "No photos to clean up" icon + copy | | |

---

## Section 5: Empty and Denied States (MEDIA-03)

| # | Scenario | Expected | Result | Notes |
|---|----------|----------|--------|-------|
| 5.1 | Deny permission at first launch | EntryScreen shows NeedsPermission copy + "Allow gallery access" button | | |
| 5.2 | Deny permission twice (permanent deny) | EntryScreen shows NeedsPermission(showSettingsHint=true) copy + "Try permission again" + "Open app settings" button | | |
| 5.3 | Tap "Open app settings" | Device Settings > App Info > Permissions opens for the app (no crash) | | |
| 5.4 | Grant permission from Settings, return to app, tap retry | App loads a new batch normally | | |

---

## Section 6: Performance

| # | Scenario | Expected | Result | Notes |
|---|----------|----------|--------|-------|
| 6.1 | Launch session with a full 30-photo batch on a mid-range device | First card visible within 2 seconds; no ANR | | |
| 6.2 | Swipe through all 30 photos rapidly | No frame drops to below 30fps sustained; no crash at session boundary | | |
| 6.3 | Open review with 30 staged photos; scroll the 2-column grid | Grid scrolls smoothly; Coil images load progressively | | |

---

## Sign-Off

**Overall result:** PASS / FAIL / CONDITIONAL PASS (circle one)

**Issues found:**

(List any FAIL items with steps to reproduce)

**Signed:** ___________________________
**Date:** ___________________________
