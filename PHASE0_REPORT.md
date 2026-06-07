# Phase 0 — Discovery Report

## 1. Modules Containing UI Resources

| Module | UI Resources? |
|--------|---------------|
| `app/` | **YES** — main Android module with layouts, drawables, colors, themes, strings |
| `extensions/observer/` | **YES** — has own `res/layout/`, `res/values/strings.xml`, `values/themes.xml` |
| `extensions/feishu/` | No UI resources (manifest only) |
| `extensions/discord/` | No UI resources (manifest only) |
| `self-control/` | No UI resources (manifest only) |

**Conclusion:** Two modules have UI: `app/` (primary) and `extensions/observer/` (permissions page only).

## 2. Markdown Files

### Bootstrap files (`app/src/main/assets/bootstrap/`)
| File | Has Chinese? |
|------|-------------|
| `AGENTS.md` | Yes |
| `OPS_GUIDE.md` | Yes |
| `VOICE_VISION_ORCHESTRATION_PROMPT.md` | Yes |
| `VOICE_VISION_SYSTEM_PROMPT.md` | Yes — very short |
| `memory/MEMORY.md` | Yes |
| `memory/USER-PROFILE.md` | TBD |
| `memory/IMAGE-MEMORY.md` | TBD |

### Skill files (`app/src/main/assets/skills/*/SKILL.md`)
All 15 SKILL.md files likely contain Chinese — these are user-facing agent skill docs.

### Feishu extension skills (`extensions/feishu/skills/*/SKILL.md`)
9 SKILL.md files — likely Chinese.

### Root docs
| File | Has Chinese? |
|------|-------------|
| `README.md` | English |
| `README_zh.md` | Chinese (duplicate of README) |

### Self-control docs
| File | Has Chinese? |
|------|-------------|
| `self-control/ADB_USAGE.md` | TBD |
| `self-control/DUAL_MODE.md` | TBD |
| `self-control/INTEGRATION.md` | TBD |
| `self-control/PROJECT_SUMMARY.md` | TBD |
| `self-control/self-control-skill.md` | TBD |
| `self-control/skills/self-control.md` | TBD |
| `self-control/SUMMARY.md` | TBD |

### Other
| File | Has Chinese? |
|------|-------------|
| `extensions/observer/REFACTOR_PLAN.md` | TBD |

## 3. Hardcoded Chinese Strings in Layout XML

| Layout File | Hardcoded Chinese Count |
|-------------|------------------------|
| `activity_model_config.xml` | ~30 strings (model config labels, hints, section titles) |
| `activity_main.xml` | ~15 strings (platform title, permission/card labels) |
| `activity_config.xml` | ~20 strings (card titles, descriptions, button text) |
| `activity_model_setup.xml` | ~10 strings (welcome, tutorial steps) |
| `activity_stt_provider_config.xml` | ~10 strings (STT setup instructions) |
| `activity_vlm_provider_config.xml` | ~10 strings (VLM setup instructions) |
| `layout_behavior_recording_float.xml` | 3 strings |
| `layout_screen_companion_float.xml` | 3 strings |
| `layout_session_float.xml` | 1 string |

**Total:** ~100+ hardcoded Chinese strings across layout XML files.

## 4. Hardcoded Chinese Strings in Java/Kotlin UI Code

| Source File | Hardcoded Chinese |
|-------------|-------------------|
| **`SessionManager.kt`** | ~15 strings (welcome messages, "新对话" default title, "聊天记录已清空", type labels like "飞书", "其他") |
| **`ModelSetupActivity.kt`** | ~15 strings (model names like "未配置", "Kimi K2.6 (付费)", "Qwen 3.6 Flash (付费，推荐)", API key hints, expand/collapse labels) |
| **`ModelConfigActivity.kt`** | ~20 strings (status summaries like "STT：未配置", "Agent：${model}", status descriptions) |
| **`ConfigActivity.kt`** | ~15 strings (Toast messages, dialog titles, version labels, save/reset messages) |
| **`ChannelListActivity.kt`** | ~8 strings (Compose UI text: "返回", "Feishu (飞书)", "配置多渠道接入", etc.) |
| **`FeishuChannelActivity.kt`** | ~10 strings (Compose text: "保存", "配置已保存", section titles) |
| **`DiscordChannelActivity.kt`** | ~10 strings (Compose text) |
| **`SkillsActivity.kt`** | ~15 strings (Toasts, dialog titles, labels) |
| **`MainActivityCompose.kt`** | ~15 strings (Tab labels "对话"/"状态"/"设置", permission dialog text) |
| **`ChatScreen.kt`** | ~5 strings ("新对话", "删除会话", "选择会话") |
| **`ChatTimelineMapper.kt`** | Comments only — code comments in Chinese, not user-facing strings |
| **`ChatWindowView.kt`** | 1 string ("已停止生成" Toast, hint "输入指令...") |

**Total:** ~100+ hardcoded Chinese strings in Kotlin UI code.

## 5. Current Theme Setup

| Property | Value |
|----------|-------|
| Theme parent | `Theme.MaterialComponents.DayNight.DarkActionBar` |
| `values/themes.xml` | Light theme with green primary, monospace font |
| `values-night/themes.xml` | Exists — overrides surface to black, window/status bar/nav to surface |
| `values-night/colors.xml` | **DOES NOT EXIST** |
| Color palette | Minimal: green `#388E3C`, status colors (blue/yellow/green/red/gray), `text_secondary` `#666666` |
| `Activity` configChanges | **NONE** — no `uiMode` block on any Activity (correct for DayNight) |
| minSdk / targetSdk / compileSdk | 26 / 34 / 34 |

### Key findings:
- DayNight theme parent is correct; no `configChanges` flag needed
- Night theme exists but is minimal (only surface/window/bar overrides)
- **No night color palette** (`values-night/colors.xml` missing)
- Many layout files use hardcoded `#RRGGBB` colors that won't adapt to dark mode

## 6. Existing i18n Resources

| Resource | Status |
|----------|--------|
| `values/strings.xml` | **Mostly English**, some Chinese remnants |
| `values-zh/strings.xml` | Chinese translations for most strings |
| `values-night/themes.xml` | Partially done (dark surface/window background) |
| `values-night/colors.xml` | **Missing** |
| `values/arrays.xml` | Language selection array (bilingual) |

## Summary of Work Required

| Phase | Scope | Files Affected |
|-------|-------|---------------|
| **Phase 1** | String extraction + English resources | ~12 layout XML files, `values/strings.xml`, `values-zh/strings.xml` |
| **Phase 2** | Dark mode via theme attributes | `values-night/colors.xml`, drawable icons, layout hardcoded colors |
| **Phase 3** | Code-level string cleanup | ~10 Kotlin files (SessionManager, ModelSetupActivity, ModelConfigActivity, ConfigActivity, etc.) |
| **Phase 4** | Theme attribute audit | layouts/*.xml, drawable/*.xml — replace hardcoded colors with `?attr/` |
| **Phase 5** | Documentation | `CHANGES.md` at repo root |

## Risk Assessment

- **SessionManager.kt welcome messages**: These are agent-facing messages embedded in the Kotlin code. They use string templates and emoji. Extract to `strings.xml` as format strings.
- **ModelSetupActivity.kt ModelPresets**: Model names like "Kimi K2.6 (付费)" contain Chinese + English. Translate: "Kimi K2.6 (Paid)".
- **ChatTimelineMapper.kt**: Code comments only — translate comments to English but keep logic intact.
- **SKILL.md files**: Bootstrap, skill, and extension skill docs contain Chinese. Create English versions under `docs/en/` or alongside originals. **Skip full translation** if too many files — document in NOTES.md.
