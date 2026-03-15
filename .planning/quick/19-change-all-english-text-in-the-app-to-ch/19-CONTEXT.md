---
name: Quick Task 19 - Localize app to Chinese
description: Context for converting all English UI text to Simplified Chinese
type: project
---

# Quick Task 19: Change all English text in the app to Chinese - Context

**Gathered:** 2026-03-15
**Status:** Ready for planning

<domain>
## Task Boundary

Change all English text in the app to Chinese. The app is intended for Chinese users. Keep the app name "DtMF" unchanged, but convert all other visible text to Chinese.

</domain>

<decisions>
## Implementation Decisions

### Script Variant
- Use **Simplified Chinese** (简体中文) — the standard for mainland China

### App Name
- Keep "DtMF" unchanged — do not translate or modify the app name

### Claude's Discretion
- Scope of text: translate all strings in strings.xml plus any hardcoded strings found in Kotlin/XML layout files
- Translation approach: Claude translates directly from English to Simplified Chinese
- Play Store metadata / comments / code strings (non-UI) are out of scope

</decisions>

<specifics>
## Specific Ideas

- Target locale: zh-CN (Simplified Chinese)
- Primary string resource file: `app/src/main/res/values/strings.xml`
- Check for hardcoded strings in layout XML files and Kotlin source files
- Preserve all string resource keys — only change the values

</specifics>
