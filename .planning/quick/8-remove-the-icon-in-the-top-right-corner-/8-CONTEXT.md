---
name: Quick Task 8 - Remove icon and fix image cropping
description: Context for removing top-right icon overlay and switching to uncropped image display with black fill
type: project
---

# Quick Task 8: Remove the icon in the top-right corner of the large preview image and fix image cropping - Context

**Gathered:** 2026-03-15
**Status:** Ready for planning

<domain>
## Task Boundary

Remove the `"◪"` icon badge overlaid in the top-right corner of the large preview image (`CurrentPhotoCard`). The icon has no tap handler and is unused.

Also switch the image from `ContentScale.Crop` (currently crops/fills the card) to `ContentScale.Fit` so the full original image is visible without any cropping.

</domain>

<decisions>
## Implementation Decisions

### Empty space fill
- Fill letterbox/pillarbox bars with **black** background behind the image

### Card aspect ratio
- Keep the existing fixed `heroAspectRatio` (0.74–0.8f); do not adapt it to the photo's aspect ratio (Claude's discretion)

### Claude's Discretion
- Exact modifier order for black background behind the AsyncImage
- Whether to use `Modifier.background(Color.Black)` on the image or the parent Box

</decisions>

<specifics>
## Specific Ideas

- File: `app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/CurrentPhotoCard.kt`
- Remove the entire `Box` block at `Alignment.TopEnd` (lines ~56–70) that renders the `"◪"` icon
- Change `contentScale = ContentScale.Crop` → `contentScale = ContentScale.Fit`
- Add `Modifier.background(Color.Black)` so bars appear black

</specifics>
