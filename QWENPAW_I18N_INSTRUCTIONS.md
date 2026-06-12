# i18n String Extraction Instructions for X-OmniClaw

## Objective
Extract all user-facing hardcoded Chinese strings from the Android app into `strings.xml` resources, replacing them with `R.string.xxx` or `stringResource()` references. Do NOT touch internal logic (tool descriptions, debug logs, comments).

---

## File Scope (FOCUS ON THESE)

### Compose UI (highest priority)
- `app/src/main/java/com/shijing/xomniclaw/ui/compose/ChatScreen.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/compose/VoiceRecordButton.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/compose/CameraPreviewOverlay.kt`

### Traditional Activity + XML (high priority)
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/ModelSetupActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/ModelConfigActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/ConfigActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/SkillsActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/SttProviderConfigActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/VlmProviderConfigActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/DiscordChannelActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/FeishuChannelActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/ChannelListActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/PermissionsActivity.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/activity/ResultActivity.kt`

### XML Layouts
- `app/src/main/res/layout/activity_*.xml`
- `app/src/main/res/layout/item_*.xml`
- `app/src/main/res/layout/dialog_*.xml`
- `app/src/main/res/layout/layout_*.xml`

### Other UI Components
- `app/src/main/java/com/shijing/xomniclaw/ui/view/ChatWindowView.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/floatwindow/*.kt`
- `app/src/main/java/com/shijing/xomniclaw/ui/viewmodel/ChatViewModel.kt`

---

## SKIP THESE (internal logic, not user-facing)
- `agent/tools/` - Tool function descriptions, internal skill logic
- `agent/skills/` - Skill definitions, internal processing
- `voice/` - Voice processing internals (NOT ScreenCompanionController.kt UI)
- `deeplink/` - Deep link handling logic
- `core/` - Application bootstrapping
- `gateway/` - WebSocket server internals
- `scheduler/` - Task scheduling logic
- `config/` - Configuration loading/parsing
- `providers/` - API provider adapters
- `logging/` - Log files and loggers
- Comments (`//`, `/* */`, `/** */`) - Do NOT extract these

---

## How to Extract Strings

### 1. Identify User-Facing Chinese Text
Look for Chinese characters in:
- `Text("...")` - Compose Text composable
- `contentDescription = "..."` - Accessibility descriptions
- `title = { Text("...") }` - Dialog/AppBar titles
- `android:text="..."` - XML layout attributes
- `Toast.makeText(context, "...", Toast.LENGTH_SHORT)` - Toast messages
- `Snackbar.make(view, "...", Snackbar.LENGTH_SHORT)` - Snackbar messages
- `setTitle("...")` - Activity/Dialog titles
- `setMessage("...")` - Dialog messages
- `setText("...")` - Programmatically set text
- AlertDialog.Builder().setTitle("...").setMessage("...")

### 2. Add to strings.xml (English is default)
File: `app/src/main/res/values/strings.xml`

Format: Use descriptive snake_case keys. Prefix with screen/section.

```xml
<!-- ChatScreen -->
<string name="chat_new_conversation">New Conversation</string>
<string name="chat_history_sessions">History Sessions</string>
<string name="chat_collapse">Collapse</string>
<string name="chat_expand">Expand</string>
<string name="chat_thinking">Thinking...</string>
<string name="chat_delete_session_title">Delete Session</string>

<!-- ModelSetupActivity -->
<string name="setup_welcome_title">Welcome to X-OmniClaw</string>
<string name="setup_api_key_hint">Enter your API Key</string>
```

### 3. Add to values-zh/strings.xml (Chinese translation)
File: `app/src/main/res/values-zh/strings.xml`

Copy the same keys, but with Chinese text:
```xml
<string name="chat_new_conversation">新对话</string>
<string name="chat_history_sessions">历史会话</string>
<string name="chat_collapse">收起</string>
<string name="chat_expand">展开</string>
<string name="chat_thinking">思考中...</string>
<string name="chat_delete_session_title">删除会话</string>
```

### 4. Replace in Code

#### For Compose files (ChatScreen.kt, etc.):
```kotlin
// BEFORE
Text("新对话")

// AFTER
Text(stringResource(R.string.chat_new_conversation))
```

```kotlin
// BEFORE
contentDescription = "发送消息"

// AFTER
contentDescription = stringResource(R.string.chat_send_message)
```

Make sure to add import if not present:
```kotlin
import androidx.compose.ui.res.stringResource
import com.shijing.xomniclaw.R
```

#### For traditional activities (ModelSetupActivity.kt, etc.):
```kotlin
// BEFORE
titleText.setText("欢迎")
toast = Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT)

// AFTER
titleText.setText(R.string.setup_welcome_title)
toast = Toast.makeText(this, R.string.setup_save_success, Toast.LENGTH_SHORT)
```

#### For XML layouts:
```xml
<!-- BEFORE -->
<TextView android:text="欢迎" />

<!-- AFTER -->
<TextView android:text="@string/setup_welcome_title" />
```

---

## Naming Conventions for String Keys

Prefix with the screen/section, then descriptive name:

| Screen | Prefix Example | Key |
|---|---|---|
| ChatScreen | `chat_` | `chat_new_conversation` |
| ChatScreen | `chat_` | `chat_delete_confirm` |
| ModelSetup | `setup_` | `setup_welcome_title` |
| ModelConfig | `model_config_` | `model_config_add_manually` |
| Config | `config_` | `config_save_path` |
| Skills | `skills_` | `skills_builtin_label` |
| STT Config | `stt_` | `stt_provider_title` |
| VLM Config | `vlm_` | `vlm_same_as_agent` |
| Common/Generic | `common_` | `common_save`, `common_cancel` |

For truly generic strings (Save, Cancel, Delete, OK, Error, etc.), check if they already exist in `strings.xml` first. Reuse existing keys instead of creating duplicates.

---

## Workflow (Do This in Batches)

### Batch 1: ChatScreen.kt (Compose)
1. Extract all Chinese strings from ChatScreen.kt
2. Add English keys to `values/strings.xml`
3. Add Chinese translations to `values-zh/strings.xml`
4. Replace all hardcoded strings with `stringResource(R.string.xxx)`
5. Build: `./gradlew :app:compileDebugKotlin`
6. Fix any compilation errors

### Batch 2: Main Activities (XML + Kotlin)
Do ModelSetupActivity, ModelConfigActivity, ConfigActivity, SkillsActivity in sequence. Same steps as above, but use `getString(R.string.xxx)` and `android:text="@string/xxx"`.

### Batch 3: XML Layouts
Go through `res/layout/activity_*.xml` files and extract hardcoded Chinese text. Most will be in `android:text` and `android:hint` attributes.

### Batch 4: Remaining UI Components
Float windows, custom views, dialogs, etc.

---

## Build Verification After Each Batch

Run this after every batch to catch errors early:

```bash
# Windows PowerShell
$env:JAVA_HOME = "$env:USERPROFILE\jdk-17.0.14.7-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :app:compileDebugKotlin
```

If the build fails, fix the errors before proceeding.

---

## Important Rules

1. **DO NOT touch internal logic** - Only extract strings that are shown to users (UI labels, buttons, dialog text, Toast messages).
2. **DO NOT extract comments** - Chinese comments should stay as-is.
3. **Reuse existing keys** - Before creating a new key, check if an existing one fits (e.g., `common_save`, `common_cancel`).
4. **Keep the same Chinese text** - When adding to `values-zh/strings.xml`, use the EXACT same Chinese text that was hardcoded. Do not translate or modify it.
5. **Test builds after each batch** - Do not do all files at once without building. Errors compound and become hard to debug.
6. **For Compose**: Use `stringResource(R.string.xxx)` inside `@Composable` functions.
7. **For traditional**: Use `getString(R.string.xxx)` or pass `R.string.xxx` directly to `setText()`.
8. **For XML**: Use `android:text="@string/xxx"`.
