# Requirements: Android Photo Swipe Cleaner

**Defined:** 2026-03-14
**Core Value:** Make bulk photo cleanup fast and low-friction without losing the safety of a final review step before permanent deletion.

## v1 Requirements

### Media Access

- [x] **MEDIA-01**: User can grant image-library access needed for the app to read local photos, and can retry if permission was denied
- [x] **MEDIA-02**: App can query local-device images and start a new launch session with 30 randomly selected photos
- [ ] **MEDIA-03**: User sees a clear empty or unavailable state when the device has no eligible photos or the app cannot build a valid session

### Swipe Flow

- [ ] **SWIPE-01**: User can view one current photo at a time on the main screen with supporting thumbnails and primary action controls
- [x] **SWIPE-02**: User can swipe left on the current photo to add it to delete staging
- [x] **SWIPE-03**: User can swipe right on the current photo to skip it without modifying the photo
- [x] **SWIPE-04**: User can undo the most recent swipe decision during the current session
- [x] **SWIPE-05**: User can tap proceed from the main screen to enter the delete staging area

### Review & Delete

- [ ] **REVW-01**: User can view all currently staged photos in the delete staging area
- [ ] **REVW-02**: User can deselect staged photos they want to keep before deletion
- [ ] **REVW-03**: User can permanently delete the currently selected staged photos only after explicit confirmation
- [ ] **REVW-04**: If deletion is cancelled or partially fails, the app preserves non-deleted items and reflects the final result accurately

### Experience & Fidelity

- [ ] **UX-01**: The main swipe screen closely matches the layout, hierarchy, and action placement shown in `main.jpg`
- [ ] **UX-02**: The delete staging screen closely matches the layout, hierarchy, and action affordances shown in `Delete-staging-area..jpg`
- [x] **UX-03**: The current swipe/review session survives configuration changes during the active app session

## v2 Requirements

### Safety Options

- **SAFE-01**: User can choose trash vs permanent delete mode before final deletion
- **SAFE-02**: User can resume an unfinished cleanup session after fully closing and reopening the app

### Smarter Cleanup

- **SMART-01**: User can filter candidate photos by month, album, or media category before starting a batch
- **SMART-02**: User can review duplicate or similar-photo suggestions
- **SMART-03**: User can include videos in cleanup sessions

## Out of Scope

| Feature | Reason |
|---------|--------|
| User accounts or sync | v1 is a single-user local-device utility |
| Cloud photo sources | The requested workflow starts from the on-device gallery only |
| Full gallery-management suite | Would dilute the narrow cleanup loop the user wants |
| Auto-delete without review | Conflicts with the required safety step before destructive deletion |
| Cross-platform release | Current project scope is Android only |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| MEDIA-01 | Phase 1 | Complete |
| MEDIA-02 | Phase 1 | Complete |
| MEDIA-03 | Phase 5 | Pending |
| SWIPE-01 | Phase 2 | Pending |
| SWIPE-02 | Phase 2 | Complete |
| SWIPE-03 | Phase 2 | Complete |
| SWIPE-04 | Phase 3 | Complete |
| SWIPE-05 | Phase 3 | Complete |
| REVW-01 | Phase 4 | Pending |
| REVW-02 | Phase 4 | Pending |
| REVW-03 | Phase 4 | Pending |
| REVW-04 | Phase 5 | Pending |
| UX-01 | Phase 2 | Pending |
| UX-02 | Phase 4 | Pending |
| UX-03 | Phase 1 | Complete |

**Coverage:**
- v1 requirements: 15 total
- Mapped to phases: 15
- Unmapped: 0

---
*Requirements defined: 2026-03-14*
*Last updated: 2026-03-14 after project initialization*
