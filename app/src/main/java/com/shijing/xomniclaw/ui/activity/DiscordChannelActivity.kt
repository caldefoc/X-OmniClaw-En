/**
 * OmniClaw Source Reference:
 * - ../omniclaw/src/gateway/(all)
 *
 * OmniClaw adaptation: Android UI layer.
 */
package com.shijing.xomniclaw.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.shijing.xomniclaw.R
import com.shijing.xomniclaw.config.ConfigLoader
import kotlinx.coroutines.launch

/**
 * Discord Channel 配置页面
 */
class DiscordChannelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DiscordChannelScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscordChannelScreen(
    onBack: () -> Unit,
    context: android.content.Context = androidx.compose.ui.platform.LocalContext.current
) {
    val scope = rememberCoroutineScope()
    val configLoader = remember { ConfigLoader(context) }

    // 加载配置
    val openClawConfig = remember { configLoader.loadOmniClawConfig() }
    val savedConfig = remember { openClawConfig.channels.discord }

    // 状态变量（对齐 Discord 配置）
    var enabled by remember { mutableStateOf(savedConfig?.enabled ?: false) }
    var token by remember { mutableStateOf(savedConfig?.token ?: "") }
    var name by remember { mutableStateOf(savedConfig?.name ?: "OmniClaw Bot") }
    var dmPolicy by remember { mutableStateOf(savedConfig?.dm?.policy ?: "pairing") }
    var groupPolicy by remember { mutableStateOf(savedConfig?.groupPolicy ?: "allowlist") }
    var replyToMode by remember { mutableStateOf(savedConfig?.replyToMode ?: "off") }
    var allowFrom by remember { mutableStateOf(savedConfig?.dm?.allowFrom?.joinToString("\n") ?: "") }

    var showSaveSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discord Channel") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                // 读取当前完整配置
                                val currentConfig = configLoader.loadOmniClawConfig()

                                // 构建 Discord 配置
                                val updatedDiscordConfig = com.shijing.xomniclaw.config.DiscordChannelConfig(
                                    enabled = enabled,
                                    token = token.takeIf { it.isNotBlank() },
                                    name = name.takeIf { it.isNotBlank() },
                                    dm = com.shijing.xomniclaw.config.DmPolicyConfig(
                                        policy = dmPolicy,
                                        allowFrom = allowFrom.split("\n").filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
                                    ),
                                    groupPolicy = groupPolicy,
                                    replyToMode = replyToMode
                                )

                                // 更新完整配置
                                val updatedChannelsConfig = currentConfig.channels.copy(
                                    discord = updatedDiscordConfig
                                )
                                val updatedConfig = currentConfig.copy(
                                    channels = updatedChannelsConfig
                                )

                                // 保存到 xomniclaw.json
                                configLoader.saveOmniClawConfig(updatedConfig)

                                showSaveSuccess = true
                            }
                        }
                    ) {
                        Text(stringResource(R.string.discord_save))
                    }
                }
            )
        },
        snackbarHost = {
            if (showSaveSuccess) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showSaveSuccess = false
                }
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(stringResource(R.string.discord_saved))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 启用开关
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.discord_enable_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.discord_enable_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                }
            }

            // 基础配置
            Text(
                text = stringResource(R.string.discord_basic_config),
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text(stringResource(R.string.discord_token_label)) },
                placeholder = { Text("MTxxxxxxxx.Gxxxx.xxxxxxxxxxxxxxx") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.discord_name_label)) },
                placeholder = { Text("OmniClaw Bot") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // DM 策略
            Text(
                text = stringResource(R.string.discord_dm_policy),
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = dmPolicy == "open",
                    onClick = { dmPolicy = "open" },
                    label = { Text(stringResource(R.string.discord_dm_open)) },
                    modifier = Modifier.fillMaxWidth()
                )
                FilterChip(
                    selected = dmPolicy == "pairing",
                    onClick = { dmPolicy = "pairing" },
                    label = { Text(stringResource(R.string.discord_dm_pairing)) },
                    modifier = Modifier.fillMaxWidth()
                )
                FilterChip(
                    selected = dmPolicy == "allowlist",
                    onClick = { dmPolicy = "allowlist" },
                    label = { Text(stringResource(R.string.discord_dm_allowlist)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (dmPolicy == "allowlist") {
                OutlinedTextField(
                    value = allowFrom,
                    onValueChange = { allowFrom = it },
                    label = { Text(stringResource(R.string.discord_allowlist_label)) },
                    placeholder = { Text("123456789012345678\n987654321098765432") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
            }

            // Guild (服务器) 策略
            Text(
                text = stringResource(R.string.discord_guild_policy),
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = groupPolicy == "open",
                    onClick = { groupPolicy = "open" },
                    label = { Text(stringResource(R.string.discord_guild_open)) },
                    modifier = Modifier.fillMaxWidth()
                )
                FilterChip(
                    selected = groupPolicy == "allowlist",
                    onClick = { groupPolicy = "allowlist" },
                    label = { Text(stringResource(R.string.discord_guild_allowlist)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 回复模式
            Text(
                text = stringResource(R.string.discord_reply_mode),
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = replyToMode == "off",
                    onClick = { replyToMode = "off" },
                    label = { Text(stringResource(R.string.discord_reply_off)) },
                    modifier = Modifier.fillMaxWidth()
                )
                FilterChip(
                    selected = replyToMode == "always",
                    onClick = { replyToMode = "always" },
                    label = { Text(stringResource(R.string.discord_reply_always)) },
                    modifier = Modifier.fillMaxWidth()
                )
                FilterChip(
                    selected = replyToMode == "threads",
                    onClick = { replyToMode = "threads" },
                    label = { Text(stringResource(R.string.discord_reply_threads)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 配置提示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.discord_help_title),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(R.string.discord_help_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
