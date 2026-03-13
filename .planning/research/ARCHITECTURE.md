# Architecture Research

**Domain:** Android local-gallery swipe cleaner
**Researched:** 2026-03-14
**Confidence:** HIGH

## Standard Architecture

### System Overview

```text
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                            │
├─────────────────────────────────────────────────────────────┤
│  SwipeScreen   ReviewScreen   PermissionGate   Dialogs      │
├─────────────────────────────────────────────────────────────┤
│                    Presentation Layer                       │
├─────────────────────────────────────────────────────────────┤
│  SwipeViewModel        ReviewViewModel      Nav state       │
├─────────────────────────────────────────────────────────────┤
│                     Domain / App Logic                      │
├─────────────────────────────────────────────────────────────┤
│ SessionBuilder   StagingManager   DeleteCoordinator         │
├─────────────────────────────────────────────────────────────┤
│                        Data Layer                           │
├─────────────────────────────────────────────────────────────┤
│ MediaStoreRepository   SessionStore(Room or memory)         │
└─────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| Swipe screen | Render current image, thumbnail strip, swipe actions, undo, proceed | Compose screen with gesture/animation state and a `ViewModel` state holder |
| Review screen | Show staged images, let user unselect, confirm deletion | Compose grid/list backed by staged-selection state |
| Media repository | Query local images and resolve `Uri` / metadata | `ContentResolver` + `MediaStore` |
| Session builder | Randomly pick 30 candidates for the current launch session | Pure Kotlin use case over repository results |
| Delete coordinator | Issue system-mediated deletion requests and reconcile results | `MediaStore.createDeleteRequest()` orchestration |
| Session store | Preserve staged decisions, undo history, and active batch | In-memory first; Room if restart resilience is required |

## Recommended Project Structure

```text
app/
├── src/main/java/.../app/           # Application, navigation, DI wiring
├── src/main/java/.../data/          # MediaStore access and local persistence
│   ├── media/                       # Queries, mappers, repository
│   └── session/                     # Session and staged-delete storage
├── src/main/java/.../domain/        # Use cases
│   ├── session/                     # Build random 30-photo sessions
│   └── deletion/                    # Review and delete orchestration
├── src/main/java/.../feature/swipe/ # Main screenshot-matching flow
├── src/main/java/.../feature/review/# Delete staging area flow
├── src/main/java/.../ui/            # Theme, shared components, icons
└── src/androidTest/                 # Compose UI tests
```

### Structure Rationale

- **`feature/` packages:** align directly to the two screenshots and keep the code easy to reason about during v1.
- **`data/` and `domain/`:** separate platform media operations from UI logic, which matters for testing destructive flows safely.

## Architectural Patterns

### Pattern 1: Single-Activity Compose App

**What:** One activity hosts Compose navigation for the whole v1 flow.
**When to use:** Small Android apps with a compact number of screens.
**Trade-offs:** Faster to build and reason about; less ceremony than multi-activity apps.

### Pattern 2: Unidirectional UI State

**What:** UI emits intents; `ViewModel` updates immutable screen state; Compose renders from state.
**When to use:** Gesture-heavy screens with undo, selection, and async permission / delete results.
**Trade-offs:** Slightly more boilerplate, but much easier to test than ad hoc mutable UI state.

### Pattern 3: Repository + Use Case Boundary

**What:** `MediaStore` access lives behind a repository; random-session and deletion logic live in use cases.
**When to use:** Any app where platform APIs are destructive or permission-sensitive.
**Trade-offs:** More files than inline logic, but avoids tying business rules to Android framework calls.

## Data Flow

### Request Flow

```text
App launch
    ↓
Permission gate
    ↓
MediaStoreRepository queries image URIs + metadata
    ↓
SessionBuilder picks random 30
    ↓
Swipe screen renders batch
    ↓
User swipes left/right or taps undo
    ↓
StagingManager updates session state
    ↓
User taps proceed
    ↓
Review screen shows staged items
    ↓
DeleteCoordinator requests system delete confirmation
    ↓
System returns approval / cancellation result
    ↓
UI reconciles remaining items and session summary
```

### State Management

```text
ViewModel state
    ↓
Compose UI
    ↔ user gestures / button taps
Intent handlers / use cases
    ↓
Repository + session store
    ↓
new immutable UI state
```

### Key Data Flows

1. **Launch session creation:** media query -> random selection -> UI-ready card stack.
2. **Swipe decisioning:** gesture/button -> staged or skipped mutation -> next-card state.
3. **Deletion confirmation:** review selection -> pending delete request -> OS confirmation -> local state cleanup.

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| Single user / self-use | In-memory session state plus optional Room for recovery is enough |
| Broader Play release | Add analytics hooks, stronger error handling, and wider device-form-factor testing |
| Expanded feature set | Split into additional packages/modules only when duplicate detection, filters, or backup features justify it |

### Scaling Priorities

1. **First bottleneck:** image rendering smoothness on large photos — fix with thumbnail sizing, async decode, and Compose performance profiling.
2. **Second bottleneck:** deletion / permission edge cases across OEM galleries — fix with integration tests on real devices and defensive result handling.

## Anti-Patterns

### Anti-Pattern 1: Letting Composables Own Destructive Business Logic

**What people do:** put query / stage / delete logic directly in UI composables.
**Why it's wrong:** makes destructive flows hard to test and easy to break during recomposition.
**Do this instead:** keep logic in `ViewModel` + use-case classes.

### Anti-Pattern 2: Treating Media Files as Raw Paths

**What people do:** pass file paths around and assume direct filesystem access.
**Why it's wrong:** Android shared storage and permissions are `Uri`-centric.
**Do this instead:** use `MediaStore` IDs / `Uri`s end to end.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| Android permission system | Runtime permission request | Needs Android-version-aware behavior |
| MediaStore | `ContentResolver` query and delete requests | Core integration surface for v1 |
| System delete confirmation UI | `PendingIntent` / result callback | Must reconcile partial approval or cancellation paths |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| `feature/swipe` ↔ `domain/session` | Direct use-case calls | Keeps random selection and staging rules testable |
| `feature/review` ↔ `domain/deletion` | Direct use-case calls | Lets delete confirmation logic stay outside UI |
| `domain/*` ↔ `data/media` | Repository interface | Makes MediaStore interactions mockable in tests |

## Sources

- https://developer.android.com/compose — UI toolkit and Material 3 support
- https://developer.android.com/topic/libraries/architecture/viewmodel — lifecycle-aware presentation/state guidance
- https://developer.android.com/training/data-storage/room — local structured persistence guidance
- https://developer.android.com/reference/android/provider/MediaStore — shared media access and delete request APIs
- https://developer.android.com/about/versions/15/behavior-changes-15 — edge-to-edge default implications on modern Android

---
*Architecture research for: Android local-gallery swipe cleaner*
*Researched: 2026-03-14*
