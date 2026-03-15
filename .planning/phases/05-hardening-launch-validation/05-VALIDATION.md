---
phase: 5
slug: hardening-launch-validation
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-15
---

# Phase 5 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4 + AndroidX Compose UI Test |
| **Config file** | `app/build.gradle.kts` |
| **Quick run command** | `./gradlew testDebugUnitTest` |
| **Full suite command** | `./gradlew testDebugUnitTest connectedDebugAndroidTest` |
| **Estimated runtime** | ~60 seconds (unit); ~3–5 min (instrumented) |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew testDebugUnitTest`
- **After every plan wave:** Run `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- **Before `/gsd:verify-work`:** Full suite must be green
- **Max feedback latency:** ~60 seconds (unit tests)

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 5-01-01 | 01 | 1 | MEDIA-03 | androidTest | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.entry.EntryScreenTest` | ✅ exists | ⬜ pending |
| 5-01-02 | 01 | 1 | MEDIA-03 | androidTest | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.entry.EntryScreenTest` | ✅ exists | ⬜ pending |
| 5-01-03 | 01 | 1 | MEDIA-03 | androidTest | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.entry.EntryScreenTest` | ✅ exists | ⬜ pending |
| 5-01-04 | 01 | 1 | REVW-04 | unit | `./gradlew testDebugUnitTest --tests '*ReviewViewModelTest*'` | ❌ Wave 0 | ⬜ pending |
| 5-01-05 | 01 | 1 | REVW-04 | androidTest | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jimmymacmini.wishdtmf.feature.review.ReviewScreenTest` | ✅ exists | ⬜ pending |
| 5-01-06 | 01 | 1 | REVW-04 | unit | `./gradlew testDebugUnitTest --tests '*ReviewViewModelTest*'` | ✅ exists | ⬜ pending |
| 5-02-01 | 02 | 2 | MEDIA-03, REVW-04 | manual | N/A — real-device lifecycle validation | N/A | ⬜ pending |
| 5-03-01 | 03 | 3 | MEDIA-03, REVW-04 | manual | N/A — visual acceptance sign-off | N/A | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/com/jimmymacmini/wishdtmf/feature/review/ReviewViewModelTest.kt` — add test for partial-delete scenario: `onDeleteConfirmed(actuallyDeleted)` where `actuallyDeleted` is a strict subset of `selectedPhotoIds`
- [ ] `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTest.kt` — add empty-grid test case for `stagedPhotoIds = emptyList()` with `isLoading = false`
- [ ] `app/src/androidTest/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreenTest.kt` — add test verifying settings-deep-link affordance is visible when `showSettingsHint = true`

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Rotate device mid-swipe session preserves state | MEDIA-03, REVW-04 | Requires real-device configuration change | Rotate device while swiping; verify card, staged count, undo preserved |
| Background during delete dialog returns with intact state | REVW-04 | Requires real-device interaction with system dialog | Trigger delete dialog; press home; return; verify cancel works |
| Process death and restart returns to entry screen | MEDIA-03 | Requires enabling "Don't keep activities" in dev options | Enable dev option; trigger delete; re-enter; confirm entry screen shown |
| Visual alignment with main.jpg | MEDIA-03 | Screenshot comparison is manual judgment | Side-by-side comparison against project root reference image |
| Visual alignment with Delete-staging-area..jpg | REVW-04 | Screenshot comparison is manual judgment | Side-by-side comparison against project root reference image |
| Real-device validation for all states | MEDIA-03, REVW-04 | STATE.md blocker: emulator-only testing noted | Run on physical device per 05-02 checklist |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s (unit), ~5min (instrumented)
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
