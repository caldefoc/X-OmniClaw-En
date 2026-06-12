/**
 * OmniClaw Source Reference:
 * - ../omniclaw/src/gateway/(all)
 *
 * OmniClaw adaptation: Android UI layer.
 */
package com.shijing.xomniclaw.ui.activity

import android.content.Intent
import android.net.Uri
import android.content.ComponentName
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shijing.xomniclaw.core.MyApplication
import com.shijing.xomniclaw.accessibility.AccessibilityProxy
import com.shijing.xomniclaw.util.MMKVKeys
import com.shijing.xomniclaw.R
import com.shijing.xomniclaw.databinding.ActivityMainBinding
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch
import com.shijing.xomniclaw.agent.skills.SkillsLoader
import com.shijing.xomniclaw.gateway.GatewayController
import com.shijing.xomniclaw.ui.session.SessionManager
import com.shijing.xomniclaw.updater.AppUpdater
import java.io.File

/**
 * OmniClaw Main Activity
 *
 * Maps OmniClaw CLI commands to visual interface:
 * - omniclaw status → Status cards
 * - omniclaw config → Config page
 * - omniclaw skills → Skills management
 * - omniclaw gateway → Gateway control
 * - omniclaw sessions → Session list
 */
class MainActivity : AppCompatActivity() {

    private fun launchObserverPermissionActivity() {
        try {
            startActivity(Intent().apply {
                component = ComponentName(
                    "com.shijing.xomniclaw",
                    "com.shijing.xomniclaw.accessibility.PermissionActivity"
                )
            })
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Observer PermissionActivity unavailable, fallback to local PermissionsActivity", e)
            startActivity(Intent(this, PermissionsActivity::class.java))
        }
    }

    private lateinit var binding: ActivityMainBinding
    private val mmkv by lazy { MMKV.defaultMMKV() }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_ACCESSIBILITY = 1001
        private const val REQUEST_OVERLAY = 1002
        private const val REQUEST_SCREEN_CAPTURE = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        updateStatusCards()
    }

    override fun onResume() {
        super.onResume()
        updateStatusCards()
        silentUpdateCheck()
    }

    /**
     * Silent update check on every app resume (cold + warm start).
     * Only shows dialog if update is available, no toast on "already latest".
     */
    private fun silentUpdateCheck() {
        lifecycleScope.launch {
            try {
                val updater = AppUpdater(this@MainActivity)
                val info = updater.checkForUpdate()
                if (info.hasUpdate) {
                    showUpdateDialog(updater, info)
                }
            } catch (_: Exception) {
                // Silent — don't bother user on network errors
            }
        }
    }

    private fun setupViews() {
        // Status card click events
        binding.apply {
            // Gateway card
            cardGateway.setOnClickListener {
                if (isGatewayRunning()) {
                    showGatewayInfo()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.main_gateway_not_running), Toast.LENGTH_SHORT).show()
                }
            }

            // Permissions card
            cardPermissions.setOnClickListener {
                launchObserverPermissionActivity()
            }

            // Skills card
            cardSkills.setOnClickListener {
                showSkillsDialog()
            }

            // Sessions card
            cardSessions.setOnClickListener {
                showSessionsDialog()
            }

            // Bottom navigation buttons
            btnConfig.setOnClickListener {
                startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
            }

            btnTest.setOnClickListener {
                checkForUpdate()
            }

            btnLogs.setOnClickListener {
                showLogsDialog()
            }
        }
    }

    /**
     * Update status cards
     * Maps to OmniClaw CLI: omniclaw status
     */
    private fun updateStatusCards() {
        lifecycleScope.launch {
            updateGatewayCard()
            updatePermissionsCard()
            updateSkillsCard()
            updateSessionsCard()
        }
    }

    /**
     * Update Gateway status card
     */
    private fun updateGatewayCard() {
        val isRunning = isGatewayRunning()
        binding.apply {
            tvGatewayStatus.text = if (isRunning) getString(R.string.main_gateway_running) else getString(R.string.main_gateway_stopped)
            tvGatewayStatus.setTextColor(
                if (isRunning) getColor(R.color.status_ok)
                else getColor(R.color.status_error)
            )

            if (isRunning) {
                tvGatewayDetails.text = "WebSocket: ws://0.0.0.0:8765\n" +
                        "Sessions: ${getSessionCount()}"
            } else {
                tvGatewayDetails.text = getString(R.string.main_gateway_not_started)
            }
        }
    }

    /**
     * Update permissions status card
     */
    private fun updatePermissionsCard() {
        val accessibility = AccessibilityProxy.isConnected.value == true && AccessibilityProxy.isServiceReady()
        val overlay = Settings.canDrawOverlays(this)
        val screenCapture = AccessibilityProxy.isMediaProjectionGranted()

        val allGranted = accessibility && overlay && screenCapture

        binding.apply {
            tvPermissionsStatus.text = if (allGranted) getString(R.string.main_permissions_authorized) else getString(R.string.main_permissions_needed)
            tvPermissionsStatus.setTextColor(
                if (allGranted) getColor(R.color.status_ok)
                else getColor(R.color.status_warning)
            )

            tvPermissionsDetails.text = buildString {
                append("${getString(R.string.accessibility_service)}: ${if (accessibility) "✓" else "✗"}\n")
                append("${getString(R.string.main_overlay_permission)}: ${if (overlay) "✓" else "✗"}\n")
                append("${getString(R.string.main_screen_capture)}: ${if (screenCapture) "✓" else "✗"} (${AccessibilityProxy.getMediaProjectionStatus()})")
            }
        }
    }

    /**
     * Update Skills status card
     */
    private fun updateSkillsCard() {
        try {
            val skillsLoader = SkillsLoader(this)
            val allSkills = skillsLoader.getAllSkills()
            val alwaysSkills = skillsLoader.getAlwaysSkills()
            val totalSkills = allSkills.size

            binding.apply {
                tvSkillsStatus.text = getString(R.string.total_skills, totalSkills)
                tvSkillsStatus.setTextColor(getColor(R.color.status_ok))

                tvSkillsDetails.text = buildString {
                    append("Always: ${alwaysSkills.size}\n")
                    append("On-Demand: ${totalSkills - alwaysSkills.size}\n")
                    append("Total: $totalSkills")
                }
            }
        } catch (e: Exception) {
            binding.tvSkillsStatus.text = getString(R.string.main_skills_loading_failed)
            binding.tvSkillsDetails.text = e.message ?: getString(R.string.main_unknown_error)
        }
    }

    private fun updateSessionsCard() {
        val sessionCount = getSessionCount()
        binding.apply {
            tvSessionsStatus.text = if (sessionCount > 0) {
                getString(R.string.active_sessions, sessionCount)
            } else {
                getString(R.string.no_active_sessions)
            }
            tvSessionsStatus.setTextColor(
                if (sessionCount > 0) getColor(R.color.status_ok)
                else getColor(R.color.text_secondary)
            )
            tvSessionsDetails.text = if (sessionCount > 0) {
                getString(R.string.main_view_details)
            } else {
                getString(R.string.main_no_active_agent_sessions)
            }
        }
    }

    private fun showGatewayInfo() {
        val info = buildString {
            append(getString(R.string.main_gateway_status_title) + "\n\n")
            append("WebSocket port: 8765\n")
            append("Address: ws://0.0.0.0:8765\n")
            append(getString(R.string.active_sessions, getSessionCount()) + "\n\n")
            append("RPC methods:\n")
            append("  • agent - Execute Agent task\n")
            append("  • agent.wait - Wait for task completion\n")
            append("  • health - Health check\n")
            append("  • session.list - List sessions\n")
            append("  • session.reset - Reset session\n")
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.main_gateway_info_title))
            .setMessage(info)
            .setPositiveButton(getString(R.string.skills_close), null)
            .setNeutralButton(getString(R.string.main_test_connection)) { _, _ ->
                Toast.makeText(this, if (isGatewayRunning()) getString(R.string.main_gateway_ok) else getString(R.string.main_gateway_not_running), Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showPermissionsDialog() {
        val accessibility = AccessibilityProxy.isConnected.value == true && AccessibilityProxy.isServiceReady()
        val overlay = Settings.canDrawOverlays(this)
        val screenCapture = AccessibilityProxy.isMediaProjectionGranted()
        val message = buildString {
            append(getString(R.string.main_permission_status_title) + ":\n\n")
            append("${if (accessibility) "✓" else "✗"} ${getString(R.string.accessibility_service)}\n")
            if (!accessibility) {
                append("  ${getString(R.string.main_permission_accessibility_desc)}\n\n")
            }
            append("${if (overlay) "✓" else "✗"} ${getString(R.string.main_overlay_permission)}\n")
            if (!overlay) {
                append("  ${getString(R.string.main_permission_overlay_desc)}\n\n")
            }
            append("${if (screenCapture) "✓" else "✗"} ${getString(R.string.main_screen_capture)}\n")
            if (!screenCapture) {
                append("  ${getString(R.string.main_permission_screen_desc)}\n")
                append("  ${getString(R.string.main_status_label)}: ${AccessibilityProxy.getMediaProjectionStatus()}\n")
            }
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.main_permission_manage_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.main_go_to_settings)) { _, _ -> requestPermissions() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun requestPermissions() {
        val accessibility = AccessibilityProxy.isConnected.value == true && AccessibilityProxy.isServiceReady()
        val overlay = Settings.canDrawOverlays(this)
        val screenCapture = AccessibilityProxy.isMediaProjectionGranted()
        when {
            !accessibility -> startActivityForResult(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), REQUEST_ACCESSIBILITY)
            !overlay -> startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")), REQUEST_OVERLAY)
            !screenCapture -> Toast.makeText(this, getString(R.string.main_screen_capture_managed), Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, getString(R.string.main_all_permissions_granted), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSkillsDialog() {
        try {
            val skillsLoader = SkillsLoader(this)
            val allSkills = skillsLoader.getAllSkills()
            val message = buildString {
                if (allSkills.isEmpty()) append(getString(R.string.main_no_skills_installed))
                else allSkills.forEachIndexed { index, skill ->
                    val emoji = skill.metadata.emoji ?: "📋"
                    val always = if (skill.metadata.always) " [Always]" else ""
                    append("${index + 1}. $emoji ${skill.name}$always\n")
                    append("   ${skill.description.lines().firstOrNull()?.take(50) ?: ""}\n\n")
                }
            }
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.skills_title) + " (${allSkills.size})")
                .setMessage(message)
                .setPositiveButton(getString(R.string.skills_close), null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.main_skills_loading_failed) + ": ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSessionsDialog() {
        try {
            val sessionManager = SessionManager()
            val sessions = sessionManager.getAllSessions()
            val message = buildString {
                if (sessions.isEmpty()) append(getString(R.string.no_active_sessions))
                else sessions.forEachIndexed { index, session ->
                    append("${index + 1}. ${session.title}\n")
                    append(getString(R.string.main_messages_count, session.messages.size) + "\n\n")
                }
            }
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.main_session_list_title, sessions.size))
                .setMessage(message)
                .setPositiveButton(getString(R.string.skills_close), null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.main_load_sessions_failed) + ": ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogsDialog() {
        val logDir = File("/sdcard/.xomniclaw/workspace/logs")
        if (!logDir.exists() || !logDir.isDirectory) {
            Toast.makeText(this, getString(R.string.main_no_logs), Toast.LENGTH_SHORT).show(); return
        }
        val logFiles = logDir.listFiles()?.filter { it.name.endsWith(".log") }?.sortedByDescending { it.lastModified() }?.take(20) ?: emptyList()
        if (logFiles.isEmpty()) { Toast.makeText(this, getString(R.string.main_no_logs), Toast.LENGTH_SHORT).show(); return }
        val fileNames = logFiles.map { file -> "${file.name} (${file.length() / 1024}KB)" }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.main_logs_title, logFiles.size))
            .setItems(fileNames) { _, which -> showLogContent(logFiles[which]) }
            .setPositiveButton(getString(R.string.skills_close), null)
            .show()
    }

    private fun showLogContent(file: File) {
        try {
            val content = file.readText()
            val truncated = if (content.length > 5000) content.take(5000) + "\n\n... (${content.length - 5000} ${getString(R.string.main_chars_truncated)})" else content
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(file.name)
                .setMessage(truncated)
                .setPositiveButton(getString(R.string.skills_close), null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.main_read_log_failed) + ": ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkForUpdate() {
        Toast.makeText(this, getString(R.string.checking_updates), Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            try {
                val updater = AppUpdater(this@MainActivity)
                val info = updater.checkForUpdate()
                if (info.hasUpdate) showUpdateDialog(updater, info)
                else Toast.makeText(this@MainActivity, getString(R.string.already_latest, info.currentVersion), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, getString(R.string.update_failed) + ": ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUpdateDialog(updater: AppUpdater, info: AppUpdater.UpdateInfo) {
        val sizeStr = if (info.fileSize > 0) "%.1f MB".format(info.fileSize / 1024.0 / 1024.0) else getString(R.string.main_unknown_size)
        val message = buildString {
            append(getString(R.string.update_available) + "!\n\n")
            append(getString(R.string.config_current_version, info.currentVersion) + "\n")
            append(getString(R.string.config_new_version, info.latestVersion) + "\n")
            append(getString(R.string.main_file_size_label) + ": $sizeStr\n")
            if (!info.publishedAt.isNullOrEmpty()) append(getString(R.string.main_published_label) + ": ${info.publishedAt.take(10)}\n")
            if (!info.releaseNotes.isNullOrEmpty()) append("\n${getString(R.string.main_changelog_label)}:\n${info.releaseNotes.take(300)}")
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_available) + " v${info.latestVersion}")
            .setMessage(message)
            .setPositiveButton(getString(R.string.update_now)) { _, _ ->
                if (info.downloadUrl != null) {
                    Toast.makeText(this, getString(R.string.config_downloading), Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        val success = updater.downloadAndInstall(info.downloadUrl, info.latestVersion)
                        if (!success) openUrl(info.releaseUrl)
                    }
                } else openUrl(info.releaseUrl)
            }
            .setNeutralButton(getString(R.string.config_open_browser)) { _, _ -> openUrl(info.releaseUrl) }
            .setNegativeButton(getString(R.string.update_later), null)
            .show()
    }

    private fun openUrl(url: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        catch (e: Exception) { Toast.makeText(this, getString(R.string.setup_open_browser_failed) + ": ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ACCESSIBILITY, REQUEST_OVERLAY -> {
                // Returned from permission settings, refresh status
                updateStatusCards()
            }
        }
    }

    private fun isGatewayRunning(): Boolean {
        return try {
            java.net.Socket().use { s -> s.connect(java.net.InetSocketAddress("127.0.0.1", 8765), 500); true }
        } catch (e: Exception) { false }
    }

    private fun getSessionCount(): Int {
        return try { SessionManager().getSessionCount() } catch (e: Exception) { 0 }
    }
}
