package com.goldmonitor.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goldmonitor.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Monitor Settings
            SettingsSection(title = "监控设置") {
                SwitchSettingItem(
                    title = "启用监控服务",
                    subtitle = "在后台持续监控金价",
                    checked = settings.monitorEnabled,
                    onCheckedChange = { viewModel.updateMonitorEnabled(it) }
                )
                
                if (settings.monitorEnabled) {
                    RefreshIntervalSetting(
                        currentInterval = settings.refreshInterval,
                        onIntervalChange = { viewModel.updateRefreshInterval(it) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Notification Settings
            SettingsSection(title = "通知设置") {
                SwitchSettingItem(
                    title = "启用通知",
                    subtitle = "接收金价提醒通知",
                    checked = settings.notificationEnabled,
                    onCheckedChange = { viewModel.updateNotificationEnabled(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Default Alert Settings
            SettingsSection(title = "默认提醒方式") {
                SwitchSettingItem(
                    title = "振动",
                    checked = settings.vibrateEnabled,
                    onCheckedChange = { viewModel.updateVibrateEnabled(it) }
                )
                SwitchSettingItem(
                    title = "铃声",
                    checked = settings.ringtoneEnabled,
                    onCheckedChange = { viewModel.updateRingtoneEnabled(it) }
                )
                SwitchSettingItem(
                    title = "语音播报",
                    checked = settings.ttsEnabled,
                    onCheckedChange = { viewModel.updateTtsEnabled(it) }
                )
                SwitchSettingItem(
                    title = "亮屏提醒",
                    checked = settings.wakeScreenEnabled,
                    onCheckedChange = { viewModel.updateWakeScreenEnabled(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // About
            SettingsSection(title = "关于") {
                SettingItem(
                    title = "版本",
                    subtitle = BuildConfig.VERSION_NAME
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun RefreshIntervalSetting(
    currentInterval: Int,
    onIntervalChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val intervals = listOf(15, 30, 60, 120, 300)
    val intervalLabels = mapOf(
        15 to "15秒",
        30 to "30秒",
        60 to "1分钟",
        120 to "2分钟",
        300 to "5分钟"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "刷新间隔",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "当前: ${intervalLabels[currentInterval] ?: "${currentInterval}秒"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            TextButton(onClick = { expanded = true }) {
                Text(intervalLabels[currentInterval] ?: "${currentInterval}秒")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                intervals.forEach { interval ->
                    DropdownMenuItem(
                        text = { Text(intervalLabels[interval] ?: "${interval}秒") },
                        onClick = {
                            onIntervalChange(interval)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
