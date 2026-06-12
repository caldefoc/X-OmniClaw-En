import re

# Read files
with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'r', encoding='utf-8') as f:
    content = f.read()

with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    en_xml = f.read()

with open('app/src/main/res/values-zh/strings.xml', 'r', encoding='utf-8') as f:
    zh_xml = f.read()

# Targeted replacements: (chinese_text, english_text, existing_key_or_new_key)
# If key doesn't exist, we'll add it
replacements = [
    # Status tab
    ('权限状态：检查中...', 'Permission status: Checking...', 'main_permission_checking'),
    ('当前模型：读取中...', 'Current model: Loading...', 'main_model_loading'),
    ('权限状态：全部已授权', 'Permission status: All authorized', 'main_permission_all_authorized'),
    ('权限状态：未全部授权', 'Permission status: Not all authorized', 'main_permission_not_all_authorized'),
    ('权限状态：检查失败', 'Permission status: Check failed', 'main_permission_check_failed'),
    ('当前模型：未配置', 'Current model: Not configured', 'main_model_not_configured'),
    ('当前模型：读取失败', 'Current model: Read failed', 'main_model_read_failed'),
    ('未配置', 'Not configured', 'not_configured'),
    ('跟随 Agent', 'Follow Agent', 'main_follow_agent'),
    
    # Permission names in status tab
    ('无障碍', 'Accessibility', 'accessibility_service'),
    ('悬浮窗', 'Overlay', 'main_overlay_permission'),
    ('录屏', 'Screen capture', 'main_screen_capture'),
    ('相册', 'Album', 'main_album'),
    ('全部文件', 'All files', 'main_all_files'),
    ('摄像头', 'Camera', 'main_camera'),
    ('麦克风', 'Microphone', 'main_microphone'),
    
    # Settings tab
    ('设置', 'Settings', 'tab_settings'),
    ('模型配置', 'Model config', 'main_model_config'),
    ('配置 API Key 和模型参数', 'Configure API Key and model params', 'main_model_config_desc'),
    ('配置多渠道接入（飞书等）', 'Configure multi-channel access (Feishu, etc.)', 'main_channel_config_desc'),
    ('检查更新', 'Check update', 'main_check_update'),
    ('版本信息', 'Version info', 'main_version_info'),
    ('显示当前应用版本与构建类型；可从 GitHub Releases 检查新版本。', 'Show current app version and build type; check GitHub Releases for updates.', 'main_version_info_desc'),
    ('应用版本', 'App version', 'main_app_version'),
    ('构建类型', 'Build type', 'main_build_type'),
    ('当前页面无法执行检查更新', 'Cannot check update on current page', 'main_check_update_failed_page'),
    
    # Toast messages
    ('相册权限已授权', 'Album permission granted', 'main_album_permission_granted'),
    ('相册权限未完全授权，请在权限页手动开启', 'Album permission not fully granted, please enable manually in settings', 'main_album_permission_partial'),
    ('正在检查更新...', 'Checking for updates...', 'checking_updates'),
    ('检查更新失败', 'Update check failed', 'update_failed'),
    ('定时任务已更新', 'Scheduled task updated', 'main_task_updated'),
    ('保存失败', 'Save failed', 'main_save_failed'),
    ('定时任务已删除', 'Scheduled task deleted', 'main_task_deleted'),
    ('删除失败，任务可能已不存在', 'Delete failed, task may no longer exist', 'main_task_delete_failed'),
    
    # Dialog titles
    ('选择视觉输入', 'Select visual input', 'main_select_visual_input'),
    ('删除定时任务', 'Delete scheduled task', 'main_delete_scheduled_task'),
    ('开启相册记忆与画像', 'Enable album memory & profile', 'main_enable_album_memory'),
    
    # Common
    ('未启用', 'Not enabled', 'main_not_enabled'),
    ('尚未运行', 'Not yet running', 'main_not_yet_running'),
    ('暂无内容', 'No content', 'main_no_content'),
    ('已启用', 'Enabled', 'main_enabled'),
    ('已停用', 'Disabled', 'main_disabled'),
    ('关闭', 'Close', 'main_close'),
    ('取消', 'Cancel', 'cancel'),
    ('删除', 'Delete', 'delete'),
    ('编辑', 'Edit', 'edit'),
    ('刷新', 'Refresh', 'main_refresh'),
    ('暂无定时任务', 'No scheduled tasks', 'main_no_scheduled_tasks'),
    ('没有匹配当前搜索条件的定时任务', 'No scheduled tasks match current filter', 'main_no_matching_tasks'),
    ('启用任务', 'Enable task', 'main_enable_task'),
    ('精确闹钟', 'Exact alarm', 'main_exact_alarm'),
    ('允许息屏空闲时触发', 'Allow trigger when screen off idle', 'main_allow_idle_trigger'),
    ('保存中...', 'Saving...', 'main_saving'),
    ('保存', 'Save', 'save'),
    ('搜索任务', 'Search tasks', 'main_search_tasks'),
    ('任务名称', 'Task name', 'main_task_name'),
    ('执行指令', 'Execution command', 'main_execution_command'),
    ('重复类型', 'Repeat type', 'main_repeat_type'),
    ('执行时间', 'Execution time', 'main_execution_time'),
    ('每日时间', 'Daily time', 'main_daily_time'),
    ('每周时间', 'Weekly time', 'main_weekly_time'),
    ('周几', 'Day of week', 'main_day_of_week'),
    ('间隔分钟数', 'Interval minutes', 'main_interval_minutes'),
    ('时区（可选）', 'Timezone (optional)', 'main_timezone_optional'),
    ('快速测试', 'Quick test', 'main_quick_test'),
    ('立即扫描一次', 'Scan once now', 'main_scan_once_now'),
    ('扫描中...', 'Scanning...', 'main_scanning'),
    ('总开关', 'Master switch', 'main_master_switch'),
    ('画像加载', 'Profile loading', 'main_profile_loading'),
    ('扫描频率', 'Scan frequency', 'main_scan_frequency'),
    ('游标维护', 'Cursor maintenance', 'main_cursor_maintenance'),
    ('重置游标', 'Reset cursor', 'main_reset_cursor'),
    ('查看', 'View', 'main_view'),
    ('初始化', 'Initialize', 'main_initialize'),
    ('编辑定时任务', 'Edit scheduled task', 'main_edit_scheduled_task'),
    ('后台扫描进度', 'Background scan progress', 'main_background_scan_progress'),
    ('当前状态', 'Current status', 'main_current_status'),
    ('知识类文件', 'Knowledge files', 'main_knowledge_files'),
    ('暂无额外 memory 文件', 'No extra memory files', 'main_no_extra_memory_files'),
    ('每日日志', 'Daily logs', 'main_daily_logs'),
    ('暂无 daily log', 'No daily log', 'main_no_daily_log'),
    ('读取中...', 'Loading...', 'main_loading'),
    ('处理中...', 'Processing...', 'main_processing'),
    ('已进入屏内替身模式', 'Entered screen companion mode', 'main_entered_companion'),
    ('进入替身模式失败', 'Enter companion mode failed', 'main_enter_companion_failed'),
    ('应用上下文异常，无法进入替身模式', 'App context exception, cannot enter companion mode', 'main_companion_context_error'),
    ('需要录屏权限以采集屏幕画面', 'Need screen capture permission to capture screen', 'main_need_screen_capture'),
    ('需要摄像头权限', 'Need camera permission', 'main_need_camera'),
    ('需要录音权限', 'Need microphone permission', 'main_need_microphone'),
    ('应用上下文异常，无法开始轨迹录制', 'App context exception, cannot start trace recording', 'main_trace_context_error'),
    ('需要先启用无障碍服务', 'Need to enable accessibility service first', 'main_need_accessibility'),
    ('已开始轨迹录制', 'Trace recording started', 'main_trace_started'),
    ('开始轨迹录制失败', 'Start trace recording failed', 'main_trace_start_failed'),
    ('轨迹录制（支持 Deeplink 收藏）', 'Trace recording (supports Deeplink bookmarks)', 'main_trace_recording'),
    ('屏幕画面（截屏流）', 'Screen view (screenshot stream)', 'main_screen_view_stream'),
    ('摄像头', 'Camera', 'camera_back'),  # Reuse existing
    ('Snapshot 附加 YOLO 结果', 'Snapshot append YOLO results', 'main_snapshot_yolo'),
    ('关闭时仅返回无障碍 snapshot；开启后会并行执行 YOLO，并追加原始检测结果给大模型参考。', 'When closed, only return accessibility snapshot; when enabled, YOLO runs in parallel and appends raw detection results for LLM reference.', 'main_snapshot_yolo_desc'),
    ('Token 使用量（实时）', 'Token usage (real-time)', 'main_token_usage'),
    ('本会话累计', 'This session cumulative', 'main_session_cumulative'),
    ('全局累计', 'Global cumulative', 'main_global_cumulative'),
    ('Logcat 完整 LLM 请求', 'Logcat full LLM request', 'main_logcat_llm'),
    ('定时任务', 'Scheduled tasks', 'main_scheduled_tasks'),
    ('类型', 'Type', 'main_type'),
    ('下一次触发', 'Next trigger', 'main_next_trigger'),
    ('执行指令', 'Execution command', 'main_execution_command_2'),
    ('最近触发', 'Last triggered', 'main_last_triggered'),
    ('触发延迟', 'Trigger delay', 'main_trigger_delay'),
    ('派发耗时', 'Dispatch latency', 'main_dispatch_latency'),
    ('触发来源', 'Trigger source', 'main_trigger_source'),
    ('亮屏结果', 'Wake screen result', 'main_wake_result'),
    ('相册记忆与画像', 'Album memory & profile', 'main_album_memory_profile'),
    ('管理后台增量扫描、画像默认加载和自动同步频率', 'Manage background incremental scan, profile default loading and auto-sync frequency', 'main_album_memory_desc'),
    ('启用后默认允许后台定时增量扫描相册', 'When enabled, allows background scheduled incremental album scanning', 'main_album_scan_desc'),
    ('执行任务时默认加载 USER-PROFILE.md', 'Default load USER-PROFILE.md when executing tasks', 'main_profile_load_desc'),
    ('选择后台增量扫描相册的频率，开启总开关后生效', 'Choose background incremental album scan frequency, effective after enabling master switch', 'main_scan_freq_desc'),
    ('立即执行一次：后台最多补扫 ${state.manualSyncMaxImages} 张未写入图片，并更新画像', 'Execute once: background will backfill up to ${state.manualSyncMaxImages} unwritten images and update profile', 'main_scan_once_desc'),
    ('如果怀疑有遗漏，可重置扫描位置后重新补扫；已写入的图片会被 stableKey 过滤，不会重复写入。', 'If you suspect missing items, reset scan position to re-backfill; written images are filtered by stableKey and will not be duplicated.', 'main_cursor_reset_desc'),
    ('MEMORY.md 已存在，长度 ${snapshot.longTermMemoryLength} 字符', 'MEMORY.md exists, length ${snapshot.longTermMemoryLength} chars', 'main_memory_exists'),
    ('MEMORY.md 暂无内容', 'MEMORY.md no content', 'main_memory_no_content'),
    ('查看 MEMORY.md', 'View MEMORY.md', 'main_view_memory_md'),
    ('全局记忆进化：待处理 ${snapshot.evolutionStatus.pendingEvents} 条，最近处理 ${snapshot.evolutionStatus.processedEvents} 条，采纳 ${snapshot.evolutionStatus.acceptedCandidates} 条', 'Global memory evolution: pending ${snapshot.evolutionStatus.pendingEvents}, recently processed ${snapshot.evolutionStatus.processedEvents}, accepted ${snapshot.evolutionStatus.acceptedCandidates}', 'main_global_evolution'),
    ('最近结果', 'Recent result', 'main_recent_result'),
    ('IMAGE-MEMORY.md 已存在，长度 ${snapshot.imageMemoriesLength} 字符', 'IMAGE-MEMORY.md exists, length ${snapshot.imageMemoriesLength} chars', 'main_image_memory_exists'),
    ('IMAGE-MEMORY.md 暂无内容', 'IMAGE-MEMORY.md no content', 'main_image_memory_no_content'),
    ('查看 IMAGE-MEMORY.md', 'View IMAGE-MEMORY.md', 'main_view_image_memory_md'),
    ('USER-PROFILE.md 已存在，长度 ${snapshot.userProfileLength} 字符', 'USER-PROFILE.md exists, length ${snapshot.userProfileLength} chars', 'main_user_profile_exists'),
    ('USER-PROFILE.md 暂无内容', 'USER-PROFILE.md no content', 'main_user_profile_no_content'),
    ('查看 USER-PROFILE.md', 'View USER-PROFILE.md', 'main_view_user_profile_md'),
    ('可查看 MEMORY.md、IMAGE-MEMORY.md、USER-PROFILE.md 与按日沉淀日志', 'Can view MEMORY.md, IMAGE-MEMORY.md, USER-PROFILE.md and daily logs', 'main_view_memory_files'),
]

new_en = []
new_zh = []

for cn, en, key in replacements:
    # Check if key already exists
    if f'name="{key}"' not in en_xml:
        new_en.append(f'    <string name="{key}">{en}</string>')
        new_zh.append(f'    <string name="{key}">{cn}</string>')
    
    # Replace in content - only exact string literals
    old = f'"{cn}"'
    # For Toast / AlertDialog.Builder / regular function calls: use getString()
    # For Compose Text() / text = / title = / label = : use stringResource()
    # But we need to be careful about context
    
    # Simple approach: replace all exact occurrences
    # For now, just replace all - we'll fix compilation errors afterward
    if old in content:
        content = content.replace(old, f'getString(R.string.{key})')

# Add new strings
if new_en:
    en_insert = en_xml.rfind('</resources>')
    zh_insert = zh_xml.rfind('</resources>')
    en_xml = en_xml[:en_insert] + '\n    <!-- MainActivityCompose targeted -->\n' + '\n'.join(new_en) + '\n' + en_xml[en_insert:]
    zh_xml = zh_xml[:zh_insert] + '\n    <!-- MainActivityCompose targeted -->\n' + '\n'.join(new_zh) + '\n' + zh_xml[zh_insert:]

with open('app/src/main/res/values/strings.xml', 'w', encoding='utf-8') as f:
    f.write(en_xml)
with open('app/src/main/res/values-zh/strings.xml', 'w', encoding='utf-8') as f:
    f.write(zh_xml)
with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print(f"Added {len(new_en)} new strings")
