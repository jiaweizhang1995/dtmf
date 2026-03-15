---
phase: quick-19
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/res/values/strings.xml
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreen.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/LaunchViewModel.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainUiState.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/PhotoPresentationMapper.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTokens.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewUiState.kt
  - app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt
autonomous: true
requirements: [QUICK-19]

must_haves:
  truths:
    - "All visible UI text is in Simplified Chinese"
    - "The app name DtMF remains unchanged"
    - "The app builds successfully after translation"
  artifacts:
    - path: "app/src/main/res/values/strings.xml"
      provides: "App name resource (unchanged)"
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreen.kt"
      provides: "Chinese entry screen strings"
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt"
      provides: "Chinese main screen strings"
    - path: "app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTokens.kt"
      provides: "Chinese review screen copy constants"
  key_links:
    - from: "ReviewScreenTokens.kt"
      to: "ReviewScreen.kt"
      via: "T.AppBarTitle, T.HelperLinkText, T.BottomHelperText, T.DecideLaterLabel, T.DeleteForeverLabel"
      pattern: "ReviewScreenTokens\\.(AppBarTitle|HelperLinkText|BottomHelperText|DecideLaterLabel|DeleteForeverLabel)"
---

<objective>
Translate all visible English UI text to Simplified Chinese across the app. The app name "DtMF" stays unchanged. All other user-facing strings become Chinese.

Purpose: The app targets Chinese users; English UI creates friction.
Output: All 9 source files updated with Chinese strings, app builds and runs cleanly.
</objective>

<execution_context>
@/Users/jimmymacmini/.claude/get-shit-done/workflows/execute-plan.md
@/Users/jimmymacmini/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/PROJECT.md
@.planning/STATE.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: Translate strings.xml and all entry + main screen strings</name>
  <files>
    app/src/main/res/values/strings.xml,
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/EntryScreen.kt,
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/entry/LaunchViewModel.kt,
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainScreen.kt,
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/MainUiState.kt,
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/main/PhotoPresentationMapper.kt
  </files>
  <action>
    Apply the following translations exactly. Preserve all surrounding code, imports, and logic — only change quoted string values.

    strings.xml:
    - Keep `app_name` value as "DtMF" (change from "Wish DTMF" to "DtMF" — trim to brand name only per user decision)

    EntryScreen.kt line-by-line replacements:
    - "Local cleanup utility"  →  "本地清理工具"
    - "Gallery access is still required to build a fresh batch."  →  "仍需相册访问权限以生成新批次。"
    - "Allow gallery access to build a new random review batch."  →  "请允许访问相册以生成随机审核批次。"
    - "Try permission again"  →  "重试权限"
    - "Allow gallery access"  →  "允许访问相册"
    - Text("Open app settings")  →  Text("打开应用设置")
    - "Preparing a fresh photo batch..."  →  "正在准备新照片批次..."
    - "Session ready with ${uiState.session.photoCount} photos."  →  "已加载 ${uiState.session.photoCount} 张照片。"
    - "No photos to clean up"  →  "没有需要清理的照片"
    - "Only visible, non-trashed photos that are not currently uploading appear here."  →  "此处仅显示可见、未删除且未正在上传的照片。"
    - Text("Scan again")  →  Text("重新扫描")
    - Text("Retry")  →  Text("重试")

    LaunchViewModel.kt:
    - DEFAULT_ERROR_MESSAGE = "Could not prepare a photo batch."  →  "无法准备照片批次。"

    MainScreen.kt:
    - text = "All photos reviewed"  →  text = "全部照片已审阅"
    - Keep text = "DtMF" unchanged
    - Text(text = "Proceed", ...)  →  Text(text = "继续", ...)
    - Text("Enable thumbnails")  →  Text("显示缩略图")
    - label = "Undo"  →  label = "撤销"
    - label = "Skip"  →  label = "跳过"

    MainUiState.kt (5 string literals — only change quoted values, keep all interpolations intact):
    - "Review ${swipeState.stagedPhotoIds.size} selected"  →  "审核已选 ${swipeState.stagedPhotoIds.size} 张"
    - "No photos selected for review"  →  "未选择照片进行审核"
    - "Swipe left on a photo to enable review"  →  "向左滑动照片以加入审核"
    - "All photos reviewed. Proceed to review ${swipeState.stagedPhotoIds.size} selected."  →  "全部已审阅。可审核已选 ${swipeState.stagedPhotoIds.size} 张。"
    - "All photos reviewed. No photos selected for review."  →  "全部已审阅。未选择照片进行审核。"

    PhotoPresentationMapper.kt:
    - thumbnailContentDescription = "Thumbnail ${index + 1}"  →  "缩略图 ${index + 1}"
    - heroContentDescription = "Photo ${index + 1}"  →  "照片 ${index + 1}"
    - return "Unknown size"  →  return "未知大小"
  </action>
  <verify>
    <automated>cd /Users/jimmymacmini/Desktop/codex-project/wish-dtmf && ./gradlew :app:compileDebugKotlin --quiet 2>&1 | tail -20</automated>
  </verify>
  <done>All entry/main screen strings show Chinese, app compiles without errors.</done>
</task>

<task type="auto">
  <name>Task 2: Translate review screen strings</name>
  <files>
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreenTokens.kt,
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewUiState.kt,
    app/src/main/java/com/jimmymacmini/wishdtmf/feature/review/ReviewScreen.kt
  </files>
  <action>
    Apply the following translations exactly. Only change quoted string values; preserve all surrounding code.

    ReviewScreenTokens.kt (Copy section constants):
    - AppBarTitle: String = "REVIEW"  →  "审核"
    - HelperLinkText: String = "No, I want to move to trash"  →  "不，我想移到垃圾桶"
    - BottomHelperText: String = "You can unselect the one's you wish to keep"  →  "您可以取消选择想要保留的照片"
    - DecideLaterLabel: String = "Decide Later"  →  "稍后决定"
    - DeleteForeverLabel: String = "Delete forever"  →  "永久删除"

    ReviewUiState.kt (destructivePromptText property):
    - "Permanently delete 1 item?"  →  "永久删除 1 项？"
    - "Permanently delete $selectedCount items?"  →  "永久删除 ${selectedCount} 项？"

    ReviewScreen.kt (two occurrences inside DestructivePromptSection lambda):
    - "Permanently delete 1 item?"  →  "永久删除 1 项？"
    - "Permanently delete $count items?"  →  "永久删除 ${count} 项？"
    - text = "No photos staged for deletion."  →  text = "没有等待删除的照片。"
  </action>
  <verify>
    <automated>cd /Users/jimmymacmini/Desktop/codex-project/wish-dtmf && ./gradlew :app:assembleDebug --quiet 2>&1 | tail -20</automated>
  </verify>
  <done>All review screen strings show Chinese; debug APK builds successfully.</done>
</task>

</tasks>

<verification>
After both tasks complete, verify the full build passes:

    cd /Users/jimmymacmini/Desktop/codex-project/wish-dtmf && ./gradlew :app:assembleDebug

No English user-facing strings remain (grep check):

    grep -rn '"[A-Z][a-z]' app/src/main/java/com/jimmymacmini/wishdtmf/feature --include="*.kt" | grep -v "//\|testTag\|TAG\|Log\.\|require(\|navRoute\|DtMF\|stateDescription\|contentDescription.*staged"
</verification>

<success_criteria>
- All visible UI text in the app is Simplified Chinese
- "DtMF" brand name is unchanged throughout (MainScreen top bar, app_name)
- ./gradlew :app:assembleDebug exits 0
- No English copy strings remain in feature/*.kt files
</success_criteria>

<output>
After completion, create `.planning/quick/19-change-all-english-text-in-the-app-to-ch/19-SUMMARY.md`
</output>
