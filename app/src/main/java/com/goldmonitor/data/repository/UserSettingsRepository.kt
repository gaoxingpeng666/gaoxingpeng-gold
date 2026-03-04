package com.goldmonitor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.goldmonitor.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val MONITOR_ENABLED = booleanPreferencesKey("monitor_enabled")
        val REFRESH_INTERVAL = intPreferencesKey("refresh_interval")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val VIBRATE_ENABLED = booleanPreferencesKey("vibrate_enabled")
        val RINGTONE_ENABLED = booleanPreferencesKey("ringtone_enabled")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val WAKE_SCREEN_ENABLED = booleanPreferencesKey("wake_screen_enabled")
        val MARKET_OPEN_REMIND = booleanPreferencesKey("market_open_remind")
        val CUSTOM_REMIND_TIME = stringPreferencesKey("custom_remind_time")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                monitorEnabled = preferences[PreferencesKeys.MONITOR_ENABLED] ?: false,
                refreshInterval = preferences[PreferencesKeys.REFRESH_INTERVAL] ?: 30,
                notificationEnabled = preferences[PreferencesKeys.NOTIFICATION_ENABLED] ?: true,
                vibrateEnabled = preferences[PreferencesKeys.VIBRATE_ENABLED] ?: true,
                ringtoneEnabled = preferences[PreferencesKeys.RINGTONE_ENABLED] ?: true,
                ttsEnabled = preferences[PreferencesKeys.TTS_ENABLED] ?: false,
                wakeScreenEnabled = preferences[PreferencesKeys.WAKE_SCREEN_ENABLED] ?: false,
                marketOpenRemind = preferences[PreferencesKeys.MARKET_OPEN_REMIND] ?: false,
                customRemindTime = preferences[PreferencesKeys.CUSTOM_REMIND_TIME],
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
            )
        }

    suspend fun updateMonitorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONITOR_ENABLED] = enabled
        }
    }

    suspend fun updateRefreshInterval(interval: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REFRESH_INTERVAL] = interval
        }
    }

    suspend fun updateNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun updateVibrateEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATE_ENABLED] = enabled
        }
    }

    suspend fun updateRingtoneEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RINGTONE_ENABLED] = enabled
        }
    }

    suspend fun updateTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TTS_ENABLED] = enabled
        }
    }

    suspend fun updateWakeScreenEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WAKE_SCREEN_ENABLED] = enabled
        }
    }

    suspend fun updateMarketOpenRemind(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MARKET_OPEN_REMIND] = enabled
        }
    }

    suspend fun updateCustomRemindTime(time: String?) {
        context.dataStore.edit { preferences ->
            if (time != null) {
                preferences[PreferencesKeys.CUSTOM_REMIND_TIME] = time
            } else {
                preferences.remove(PreferencesKeys.CUSTOM_REMIND_TIME)
            }
        }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun updateSettings(settings: UserSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONITOR_ENABLED] = settings.monitorEnabled
            preferences[PreferencesKeys.REFRESH_INTERVAL] = settings.refreshInterval
            preferences[PreferencesKeys.NOTIFICATION_ENABLED] = settings.notificationEnabled
            preferences[PreferencesKeys.VIBRATE_ENABLED] = settings.vibrateEnabled
            preferences[PreferencesKeys.RINGTONE_ENABLED] = settings.ringtoneEnabled
            preferences[PreferencesKeys.TTS_ENABLED] = settings.ttsEnabled
            preferences[PreferencesKeys.WAKE_SCREEN_ENABLED] = settings.wakeScreenEnabled
            preferences[PreferencesKeys.MARKET_OPEN_REMIND] = settings.marketOpenRemind
            if (settings.customRemindTime != null) {
                preferences[PreferencesKeys.CUSTOM_REMIND_TIME] = settings.customRemindTime
            }
            preferences[PreferencesKeys.THEME_MODE] = settings.themeMode
        }
    }
}
