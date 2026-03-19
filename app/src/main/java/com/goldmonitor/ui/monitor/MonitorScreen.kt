package com.goldmonitor.ui.monitor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goldmonitor.domain.model.AlertRule
import com.goldmonitor.domain.model.AlertType
import com.goldmonitor.ui.theme.Gold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel = hiltViewModel()
) {
    val rules by viewModel.rules.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingRule by viewModel.editingRule.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提醒规则") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddRuleDialog() },
                containerColor = Gold
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加规则")
            }
        }
    ) { paddingValues ->
        if (rules.isEmpty()) {
            EmptyRulesContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    AlertRuleCard(
                        rule = rule,
                        onToggle = { viewModel.toggleRuleEnabled(rule) },
                        onEdit = { viewModel.showEditRuleDialog(rule) },
                        onDelete = { viewModel.deleteRule(rule) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddEditRuleDialog(
                editingRule = editingRule,
                onDismiss = { viewModel.dismissDialog() },
                onSave = { type, vibrate, ringtone, tts, wakeScreen, desc ->
                    viewModel.saveRule(type, vibrate, ringtone, tts, wakeScreen, desc)
                }
            )
        }
    }
}

@Composable
fun EmptyRulesContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "暂无提醒规则",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击右下角按钮添加规则",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertRuleCard(
    rule: AlertRule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rule.vibrate) AlertMethodChip("振动")
                    if (rule.ringtone) AlertMethodChip("铃声")
                    if (rule.tts) AlertMethodChip("语音")
                    if (rule.wakeScreen) AlertMethodChip("亮屏")
                }
            }
            
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() }
            )
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AlertMethodChip(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleDialog(
    editingRule: AlertRule?,
    onDismiss: () -> Unit,
    onSave: (AlertType, Boolean, Boolean, Boolean, Boolean, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(0) } // 0: upper, 1: lower, 2: market, 3: custom
    var priceInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("09:00") }
    var vibrate by remember { mutableStateOf(true) }
    var ringtone by remember { mutableStateOf(true) }
    var tts by remember { mutableStateOf(false) }
    var wakeScreen by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(editingRule) {
        editingRule?.let { rule ->
            when (val type = rule.type) {
                is AlertType.UpperLimit -> {
                    selectedType = 0
                    priceInput = type.price.toString()
                }
                is AlertType.LowerLimit -> {
                    selectedType = 1
                    priceInput = type.price.toString()
                }
                is AlertType.MarketOpen -> {
                    selectedType = 2
                }
                is AlertType.CustomTime -> {
                    selectedType = 3
                    timeInput = type.time
                }
            }
            vibrate = rule.vibrate
            ringtone = rule.ringtone
            tts = rule.tts
            wakeScreen = rule.wakeScreen
            description = rule.description
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingRule != null) "编辑规则" else "添加规则") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rule type selection
                Text("规则类型", style = MaterialTheme.typography.labelLarge)
                Column {
                    listOf("上限提醒", "下限提醒", "开盘提醒", "定时提醒").forEachIndexed { index, label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedType == index,
                                onClick = { selectedType = index }
                            )
                            Text(label)
                        }
                    }
                }

                // Price input for upper/lower limit
                if (selectedType in 0..1) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("阈值价格 (元/克)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Time input for custom time
                if (selectedType == 3) {
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = { Text("提醒时间 (HH:mm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Alert methods
                Text("提醒方式", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = vibrate,
                        onClick = { vibrate = !vibrate },
                        label = { Text("振动") }
                    )
                    FilterChip(
                        selected = ringtone,
                        onClick = { ringtone = !ringtone },
                        label = { Text("铃声") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = tts,
                        onClick = { tts = !tts },
                        label = { Text("语音") }
                    )
                    FilterChip(
                        selected = wakeScreen,
                        onClick = { wakeScreen = !wakeScreen },
                        label = { Text("亮屏") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val alertType = when (selectedType) {
                        0 -> AlertType.UpperLimit(priceInput.toDoubleOrNull() ?: 0.0)
                        1 -> AlertType.LowerLimit(priceInput.toDoubleOrNull() ?: 0.0)
                        2 -> AlertType.MarketOpen
                        else -> AlertType.CustomTime(timeInput)
                    }
                    onSave(alertType, vibrate, ringtone, tts, wakeScreen, description)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
