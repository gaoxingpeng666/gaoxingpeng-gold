package com.goldmonitor.ui.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldmonitor.data.repository.AlertRuleRepository
import com.goldmonitor.domain.model.AlertRule
import com.goldmonitor.domain.model.AlertType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val alertRuleRepository: AlertRuleRepository
) : ViewModel() {

    val rules: StateFlow<List<AlertRule>> = alertRuleRepository.observeAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingRule = MutableStateFlow<AlertRule?>(null)
    val editingRule: StateFlow<AlertRule?> = _editingRule.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    fun showAddRuleDialog() {
        _editingRule.value = null
        _showAddDialog.value = true
    }

    fun showEditRuleDialog(rule: AlertRule) {
        _editingRule.value = rule
        _showAddDialog.value = true
    }

    fun dismissDialog() {
        _editingRule.value = null
        _showAddDialog.value = false
    }

    fun saveRule(
        type: AlertType,
        vibrate: Boolean,
        ringtone: Boolean,
        tts: Boolean,
        wakeScreen: Boolean,
        description: String
    ) {
        viewModelScope.launch {
            val existingRule = _editingRule.value
            val rule = AlertRule(
                id = existingRule?.id ?: 0,
                type = type,
                isEnabled = existingRule?.isEnabled ?: true,
                vibrate = vibrate,
                ringtone = ringtone,
                tts = tts,
                wakeScreen = wakeScreen,
                description = description,
                createdAt = existingRule?.createdAt ?: System.currentTimeMillis(),
                lastTriggeredAt = existingRule?.lastTriggeredAt
            )
            alertRuleRepository.saveRule(rule)
            dismissDialog()
        }
    }

    fun toggleRuleEnabled(rule: AlertRule) {
        viewModelScope.launch {
            alertRuleRepository.setRuleEnabled(rule.id, !rule.isEnabled)
        }
    }

    fun deleteRule(rule: AlertRule) {
        viewModelScope.launch {
            alertRuleRepository.deleteRule(rule.id)
        }
    }
}
