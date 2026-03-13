# Stack Research

**Domain:** Android local-gallery swipe cleaner
**Researched:** 2026-03-14
**Confidence:** HIGH

## Recommended Stack

### Core Technologies

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Kotlin | 2.x current stable | Primary app language | Default modern Android language with first-class Jetpack support and strong coroutine ergonomics |
| Android SDK | `compileSdk` / `targetSdk` 36, `minSdk` 30 | Platform baseline | API 36 is current stable, and API 30+ keeps media deletion flows simpler because `MediaStore.createDeleteRequest()` is available |
| Jetpack Compose + Material 3 | Current stable Compose BOM at implementation time | Native UI, gestures, animation, theming | Best fit for reproducing screenshot-faithful custom UI with swipe gestures, transitions, and edge-to-edge handling |
| Android Architecture Components | Current stable Jetpack releases | UI state and lifecycle management | `ViewModel`, lifecycle-aware coroutines, and Navigation Compose are the standard maintainable stack for stateful single-activity apps |
| MediaStore APIs | Platform APIs | Query local photos and request deletion | Native shared-media access path for on-device galleries, including system-mediated delete confirmation flows |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Navigation Compose | Current stable | Screen transitions between swipe flow and review flow | Use for the two-screen v1 flow and future settings / permission screens |
| Room | Current stable | Persist session state, staged deletions, and recovery-safe metadata | Use if staged items or unfinished sessions must survive process death and app restarts |
| Coil 3 for Compose | Current stable | Efficient image loading from `Uri` into Compose | Use for rendering gallery images and thumbnails without building a custom decode layer |
| Coroutines + Flow | Current stable Kotlin / Jetpack | Async media queries and reactive UI state | Use for media loading, batch actions, and screen state updates |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| Android Studio stable | Build, preview, profiling, emulator | Stay on current stable channel aligned with SDK 36 tooling |
| Gradle Kotlin DSL | Build configuration | Keep build files idiomatic Kotlin and easier to refactor |
| JUnit + Compose UI Test + Macrobenchmark | Unit, UI, and performance coverage | Needed because swipe UX can look correct but still drop frames or lose state |

## Installation

```bash
# Project bootstrap will be defined during Phase 1
# Core choices:
# - Kotlin
# - Android SDK 36
# - Jetpack Compose + Material 3
# - Navigation Compose
# - Lifecycle ViewModel
# - MediaStore APIs
```

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| Native Android with Kotlin + Compose | Flutter | Use Flutter only if cross-platform becomes a real requirement; current scope is Android-only |
| `READ_MEDIA_IMAGES` + MediaStore queries | Photo Picker only | Use photo picker only if user manually selects candidate photos; it does not fit app-driven random sampling across the gallery |
| `minSdk` 30 | Lower `minSdk` with compatibility branches | Use lower minSdk only if older-device reach matters enough to justify more storage / deletion edge cases |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| `READ_EXTERNAL_STORAGE` as primary strategy | Android 13+ moved to granular media permissions | `READ_MEDIA_IMAGES` on Android 13+ and compatibility handling for older devices |
| Rebuilding gallery storage logic with raw file paths | Shared storage behavior and permissions are more fragile with path-based assumptions | Query `MediaStore` with content `Uri`s |
| Exact screenshot chrome replication for system bars | Android 15+ edge-to-edge behavior is platform-driven and screenshot chrome varies by device | Recreate the app UI faithfully while letting system bars follow platform defaults |
| Over-modularizing v1 into many Gradle modules | Small utility app scope does not justify the coordination cost yet | Start with one app module and feature-oriented packages; modularize later if scope expands |

## Stack Patterns by Variant

**If v1 stays single-purpose and offline-only:**
- Use a single-activity Compose app with feature packages
- Because it keeps velocity high and avoids architecture ceremony

**If the app later adds duplicate detection, filters, or history:**
- Introduce Room-backed session persistence and a dedicated domain/data layer
- Because media decisions and analytics-like summaries become long-lived state

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| Android SDK 36 | Current stable AGP / Kotlin selected by latest stable Android Studio | Validate exact versions during Phase 1 scaffold |
| Compose BOM current stable | Material 3 / Navigation Compose / Lifecycle Compose | Keep Jetpack libraries aligned through the BOM and current Android Studio templates |
| `minSdk` 30 | `MediaStore.createDeleteRequest()` | Simplifies staged permanent-delete implementation |

## Sources

- https://developer.android.com/tools/releases/platforms — verified Android 16 / API 36 stable availability
- https://developer.android.com/compose — verified Compose as the current native Android UI toolkit with Material 3 support
- https://developer.android.com/topic/libraries/architecture/viewmodel — verified ViewModel lifecycle/state guidance
- https://developer.android.com/training/data-storage/room — verified Room as the standard local structured persistence layer
- https://developer.android.com/about/versions/13/behavior-changes-13#granular-media-permissions — verified `READ_MEDIA_IMAGES` guidance for Android 13+
- https://developer.android.com/reference/android/provider/MediaStore — verified `createDeleteRequest()` / `createTrashRequest()` availability for shared-media operations

---
*Stack research for: Android local-gallery swipe cleaner*
*Researched: 2026-03-14*
