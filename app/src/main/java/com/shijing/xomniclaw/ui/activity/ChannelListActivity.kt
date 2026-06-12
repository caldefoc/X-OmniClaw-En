/**
 * OmniClaw Source Reference:
 * - ../omniclaw/src/gateway/(all)
 *
 * OmniClaw adaptation: Android UI layer.
 */
package com.shijing.xomniclaw.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.shijing.xomniclaw.R
import com.tencent.mmkv.MMKV

/**
 * Channel list page
 */
class ChannelListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ChannelListScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configLoader = remember { com.shijing.xomniclaw.config.ConfigLoader(context) }

    // Read channel enabled status from xomniclaw.json instead of MMKV
    val config = remember { configLoader.loadOmniClawConfig() }
    var feishuEnabled by remember {
        mutableStateOf(config.channels.feishu.enabled)
    }

    var discordEnabled by remember {
        mutableStateOf(config.channels.discord?.enabled ?: false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Channels") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.channels_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Feishu Channel card
            ChannelCard(
                name = stringResource(R.string.channels_feishu_name),
                description = stringResource(R.string.channels_feishu_desc),
                enabled = feishuEnabled,
                onClick = {
                    // Navigate to Feishu configuration page
                    val intent = Intent(context, FeishuChannelActivity::class.java)
                    context.startActivity(intent)
                }
            )

            // Discord Channel card
            ChannelCard(
                name = stringResource(R.string.channels_discord_name),
                description = stringResource(R.string.channels_discord_desc),
                enabled = discordEnabled,
                onClick = {
                    val intent = Intent(context, DiscordChannelActivity::class.java)
                    context.startActivity(intent)
                }
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelCard(
    name: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (enabled) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.channels_enabled),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
