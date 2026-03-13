---
phase: 01
slug: foundation-media-access
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-14
---

# Phase 1 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit + AndroidX Test + Compose UI Test |
| **Config file** | `app/build.gradle.kts` and default Android test source sets |
| **Quick run command** | `./gradlew testDebugUnitTest` |
| **Full suite command** | `./gradlew testDebugUnitTest connectedDebugAndroidTest` |
| **Estimated runtime** | ~120 seconds after Wave 0 scaffolding |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew testDebugUnitTest`
- **After every plan wave:** Run `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- **Before `$gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 01-01-01 | 01 | 1 | MEDIA-01 | build + smoke | `./gradlew assembleDebug` | ❌ W0 | ⬜ pending |
| 01-01-02 | 01 | 1 | UX-03 | unit | `./gradlew testDebugUnitTest --tests '*LaunchState*'` | ❌ W0 | ⬜ pending |
| 01-02-01 | 02 | 2 | MEDIA-01 | unit | `./gradlew testDebugUnitTest --tests '*Permission*'` | ❌ W0 | ⬜ pending |
| 01-02-02 | 02 | 2 | MEDIA-02 | unit | `./gradlew testDebugUnitTest --tests '*PhotoRepository*'` | ❌ W0 | ⬜ pending |
| 01-03-01 | 03 | 2 | MEDIA-02 | unit | `./gradlew testDebugUnitTest --tests '*LaunchSession*'` | ❌ W0 | ⬜ pending |
| 01-03-02 | 03 | 2 | UX-03 | instrumented | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=*.EntryFlowTest` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/.../LaunchStateTest.kt` — unit coverage for launch-state transitions
- [ ] `app/src/test/java/.../LaunchSessionTest.kt` — unit coverage for batch-generation rules
- [ ] `app/src/androidTest/java/.../EntryFlowTest.kt` — UI coverage for permission / empty / ready entry states
- [ ] Android test dependencies and runner configuration — create during initial scaffold

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Runtime permission UX feels minimal and direct | MEDIA-01 | System permission sheet and first-launch flow are hard to assert fully in automation | Install app fresh, launch, verify there is no intro detour and the permission ask is immediate |
| Rotation preserves current entry state and active batch | UX-03 | Best verified on emulator/device with real configuration changes | Start a batch, rotate device, confirm current position and state are unchanged |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 120s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
