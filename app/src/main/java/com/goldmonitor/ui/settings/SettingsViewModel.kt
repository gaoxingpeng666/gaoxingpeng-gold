package com.goldmonitor.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldmonitor.data.repository.UserSettingsRepository
import com.goldmonitor.domain.model.UserSettings
import com.goldmonitor.service.GoldPriceMonitorService
import com.goldmonitor.worker.GoldPriceSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val settings: StateFlow<UserSettings> = userSettingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    fun updateMonitorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateMonitorEnabled(enabled)
            if (enabled) {
                GoldPriceMonitorService.start(context)
                GoldPriceSyncWorker.enqueue(context)
            } else {
                GoldPriceMonitorService.stop(context)
            }
        }
    }

    fun updateRefreshInterval(interval: Int) {
        viewModelScope.launch {
            userSettingsRepository.updateRefreshInterval(interval)
        }
    }

    fun updateNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateNotificationEnabled(enabled)
        }
    }

    fun updateVibrateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateVibrateEnabled(enabled)
        }
    }

    fun updateRingtoneEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateRingtoneEnabled(enabled)
        }
    }

    fun updateTtsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateTtsEnabled(enabled)
        }
    }

    fun updateWakeScreenEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateWakeScreenEnabled(enabled)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            userSettingsRepository.updateThemeMode(mode)
        }
    }
}
