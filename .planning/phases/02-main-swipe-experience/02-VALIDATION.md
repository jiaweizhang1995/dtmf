---
phase: 02
slug: main-swipe-experience
status: approved
nyquist_compliant: true
wave_0_complete: true
created: 2026-03-14
---

# Phase 2 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit + AndroidX Test + Compose UI Test |
| **Config file** | `app/build.gradle.kts` |
| **Quick run command** | `./gradlew testDebugUnitTest` |
| **Full suite command** | `./gradlew testDebugUnitTest connectedDebugAndroidTest` |
| **Estimated runtime** | ~180 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew testDebugUnitTest`
- **After every plan wave:** Run `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- **Before `$gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 180 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | UX-01 | build + ui | `./gradlew assembleDebug connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=*.MainScreenTest` | ❌ pending plan | ⬜ pending |
| 02-01-02 | 01 | 1 | UX-01 | ui | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=*.MainScreenTest` | ❌ pending plan | ⬜ pending |
| 02-02-01 | 02 | 2 | SWIPE-01 | unit | `./gradlew testDebugUnitTest --tests '*Thumbnail*'` | ❌ pending plan | ⬜ pending |
| 02-02-02 | 02 | 2 | SWIPE-01 | ui | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=*.MainScreenTest` | ❌ pending plan | ⬜ pending |
| 02-03-01 | 03 | 3 | SWIPE-02 | unit | `./gradlew testDebugUnitTest --tests '*SwipeDecisionReducer*'` | ❌ pending plan | ⬜ pending |
| 02-03-02 | 03 | 3 | SWIPE-03 | unit | `./gradlew testDebugUnitTest --tests '*SwipeDecisionReducer*'` | ❌ pending plan | ⬜ pending |
| 02-03-03 | 03 | 3 | SWIPE-02, SWIPE-03 | ui | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=*.SwipeGestureTest` | ❌ pending plan | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure from Phase 1 covers the framework, runner, and baseline test setup.

- [ ] `app/src/test/java/.../SwipeDecisionReducerTest.kt` — reducer coverage for stage / skip progression
- [ ] `app/src/test/java/.../ThumbnailWindowTest.kt` — thumbnail-window derivation coverage
- [ ] `app/src/androidTest/java/.../MainScreenTest.kt` — screenshot-structure and action-affordance coverage
- [ ] `app/src/androidTest/java/.../SwipeGestureTest.kt` — drag/swipe mutation coverage for the hero card

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Main screen hierarchy and spacing closely match `main.jpg` | UX-01 | Screenshot fidelity still needs human comparison | Run the app on a phone-sized emulator, capture the main screen, and compare hierarchy, spacing, and action placement against `main.jpg` |
| Swipe interaction feels responsive on realistic photo sizes | SWIPE-02, SWIPE-03 | Gesture feel and image decode smoothness are hard to fully assert in automation | Load a session with large local photos on emulator/device and verify left/right drags stay visually smooth with no obvious decode jank |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or prior infrastructure coverage
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 180s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved 2026-03-14
