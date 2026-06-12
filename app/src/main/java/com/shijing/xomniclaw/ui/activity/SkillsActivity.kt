/**
 * OmniClaw Source Reference:
 * - ../xomniclaw/src/gateway/(all)
 *
 * OmniClaw adaptation: Android UI layer.
 */
package com.shijing.xomniclaw.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shijing.xomniclaw.R
import com.shijing.xomniclaw.databinding.ActivitySkillsBinding
import com.shijing.xomniclaw.databinding.ItemSkillBinding
import com.shijing.xomniclaw.agent.skills.SkillsLoader
import com.shijing.xomniclaw.agent.skills.SkillDocument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Skill display model (with source information)
 */
data class SkillDisplayModel(
    val document: SkillDocument,
    val source: String, // "bundled", "workspace", "managed", "unknown"
    val path: String
)

/**
 * Skills management interface
 * Maps to OmniClaw CLI: omniclaw skills list
 *
 * Features:
 * - View all Skills
 * - View Skill details
 * - Delete user Skills
 */
class SkillsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SkillsActivity"
    }

    private lateinit var binding: ActivitySkillsBinding
    private lateinit var skillsLoader: SkillsLoader
    private lateinit var adapter: SkillsAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.skills_title)
        }

        skillsLoader = SkillsLoader(this)
        setupRecyclerView()
        loadSkills()
    }

    private fun setupRecyclerView() {
        adapter = SkillsAdapter(
            onItemClick = { skill -> showSkillDetail(skill) },
            onDeleteClick = { skill -> confirmDeleteSkill(skill) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SkillsActivity)
            adapter = this@SkillsActivity.adapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadSkills()
        }
    }

    private fun loadSkills() {
        binding.swipeRefresh.isRefreshing = true

        scope.launch {
            try {
                val skills = withContext(Dispatchers.IO) {
                    val allSkills = skillsLoader.loadSkills()
                    val result = mutableListOf<SkillDisplayModel>()

                    // Convert Map to List and infer source
                    allSkills.forEach { (name, doc) ->
                        val (source, path) = detectSkillSource(name)
                        result.add(SkillDisplayModel(doc, source, path))
                    }

                    result
                }

                adapter.submitList(skills)
                updateStats(skills)

                Log.d(TAG, "Loaded ${skills.size} Skills")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load Skills", e)
                Toast.makeText(this@SkillsActivity, String.format(getString(R.string.skills_load_failed_msg), e.message), Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    /**
     * Detect Skill source
     */
    private fun detectSkillSource(skillName: String): Pair<String, String> {
        // Check workspace
        val workspacePath = "/sdcard/.xomniclaw/workspace/skills/$skillName"
        if (File(workspacePath).exists()) {
            return "workspace" to workspacePath
        }

        // Check managed
        val managedPath = "/sdcard/.xomniclaw/skills/$skillName"
        if (File(managedPath).exists()) {
            return "managed" to managedPath
        }

        // Default to bundled
        return "bundled" to "assets/skills/$skillName"
    }

    private fun updateStats(skills: List<SkillDisplayModel>) {
        val bundled = skills.count { it.source == "bundled" }
        val workspace = skills.count { it.source == "workspace" }
        val managed = skills.count { it.source == "managed" }

        binding.tvStats.text = getString(R.string.skills_stats, skills.size, bundled, workspace, managed)
    }

    private fun showSkillDetail(skill: SkillDisplayModel) {
        val doc = skill.document
        val meta = doc.metadata

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.skills_detail_title))
            .setMessage(buildSkillDetailMessage(skill))
            .setPositiveButton(getString(R.string.skills_view_content)) { _, _ ->
                showSkillContent(doc)
            }
            .setNegativeButton(getString(R.string.skills_close), null)
            .show()
    }

    private fun buildSkillDetailMessage(skill: SkillDisplayModel): String {
        val doc = skill.document
        val meta = doc.metadata
        return buildString {
            appendLine("${meta.emoji ?: "📄"} ${doc.name}")
            appendLine()
            appendLine(getString(R.string.skills_desc_label))
            appendLine(doc.description)
            appendLine()
            appendLine(getString(R.string.skills_source_label, getSourceLabel(skill.source)))
            appendLine(getString(R.string.skills_path_label, skill.path))
            appendLine()
            appendLine(getString(R.string.skills_autoload_label, if (meta.always) getString(R.string.skills_auto_load_yes) else getString(R.string.skills_auto_load_no)))
            if (meta.requires != null && meta.requires.hasRequirements()) {
                appendLine()
                appendLine(getString(R.string.skills_requires_label))
                if (meta.requires.bins.isNotEmpty()) {
                    appendLine("  • bins: ${meta.requires.bins.joinToString(", ")}")
                }
                if (meta.requires.env.isNotEmpty()) {
                    appendLine("  • env: ${meta.requires.env.joinToString(", ")}")
                }
                if (meta.requires.config.isNotEmpty()) {
                    appendLine("  • config: ${meta.requires.config.joinToString(", ")}")
                }
            }
            appendLine()
            appendLine(getString(R.string.skills_tokens_label, doc.estimateTokens()))
        }
    }

    private fun showSkillContent(doc: SkillDocument) {
        val content = doc.getFormattedContent()
            .take(1000) + if (doc.content.length > 1000) getString(R.string.skills_content_truncated) else ""

        AlertDialog.Builder(this)
            .setTitle("${doc.metadata.emoji ?: ""} ${doc.name}")
            .setMessage(content)
            .setPositiveButton(getString(R.string.skills_close), null)
            .show()
    }

    private fun getSourceLabel(source: String): String {
        return when (source) {
            "bundled" -> getString(R.string.skills_source_bundled)
            "workspace" -> getString(R.string.skills_source_workspace)
            "managed" -> getString(R.string.skills_source_managed)
            else -> getString(R.string.skills_source_unknown)
        }
    }

    private fun confirmDeleteSkill(skill: SkillDisplayModel) {
        if (skill.source == "bundled") {
            Toast.makeText(this, getString(R.string.skills_builtin_cannot_delete), Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.skills_delete_confirm_title))
            .setMessage(getString(R.string.skills_delete_confirm_msg, skill.document.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteSkill(skill)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteSkill(skill: SkillDisplayModel) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val skillDir = File(skill.path)
                    if (skillDir.exists()) {
                        skillDir.deleteRecursively()
                        Log.d(TAG, "Deleted: ${skill.path}")
                    }
                }
                Toast.makeText(this@SkillsActivity, getString(R.string.skills_delete_success, skill.document.name), Toast.LENGTH_SHORT).show()
                loadSkills()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete Skill", e)
                Toast.makeText(this@SkillsActivity, getString(R.string.skills_delete_failed, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        (scope.coroutineContext[Job] as? Job)?.cancel()
    }
}

/**
 * Skills list adapter
 */
class SkillsAdapter(
    private val onItemClick: (SkillDisplayModel) -> Unit,
    private val onDeleteClick: (SkillDisplayModel) -> Unit
) : RecyclerView.Adapter<SkillsAdapter.ViewHolder>() {

    private var skills = listOf<SkillDisplayModel>()

    fun submitList(newSkills: List<SkillDisplayModel>) {
        skills = newSkills
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSkillBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(skills[position])
    }

    override fun getItemCount() = skills.size

    inner class ViewHolder(private val binding: ItemSkillBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(skill: SkillDisplayModel) {
            val doc = skill.document
            val meta = doc.metadata

            binding.apply {
                // Emoji + name
                tvName.text = "${meta.emoji ?: "📄"} ${doc.name}"

                // Description
                tvDescription.text = doc.description

                // Source label
                tvSource.text = when (skill.source) {
                    "bundled" -> binding.root.context.getString(R.string.skills_source_bundled)
                    "workspace" -> binding.root.context.getString(R.string.skills_source_workspace)
                    "managed" -> binding.root.context.getString(R.string.skills_source_managed)
                    else -> binding.root.context.getString(R.string.skills_source_unknown)
                }

                // Category label (inferred from name)
                val category = when {
                    doc.name.contains("mobile") || doc.name.contains("app") -> "automation"
                    doc.name.contains("test") -> "testing"
                    doc.name.contains("data") -> "data"
                    doc.name.contains("javascript") -> "scripting"
                    doc.name.contains("browser") -> "web"
                    else -> null
                }

                if (category != null) {
                    tvCategory.text = "🏷️ $category"
                    tvCategory.visibility = View.VISIBLE
                } else {
                    tvCategory.visibility = View.GONE
                }

                // Auto-load marker
                if (meta.always) {
                    tvAutoLoad.visibility = View.VISIBLE
                } else {
                    tvAutoLoad.visibility = View.GONE
                }

                // Click events
                root.setOnClickListener { onItemClick(skill) }

                // Delete button (visible only for user Skills)
                btnDelete.visibility = if (skill.source != "bundled") View.VISIBLE else View.GONE
                btnDelete.setOnClickListener { onDeleteClick(skill) }
            }
        }
    }
}
