package com.goldmonitor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GoldMonitorApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Monitor channel - for foreground service
            val monitorChannel = NotificationChannel(
                CHANNEL_MONITOR,
                getString(R.string.channel_monitor),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_monitor_desc)
                setShowBadge(false)
            }

            // Alert channel - for price alerts
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT,
                getString(R.string.channel_alert),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_alert_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            // Schedule channel - for scheduled reminders
            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULE,
                getString(R.string.channel_schedule),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_schedule_desc)
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(monitorChannel, alertChannel, scheduleChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_MONITOR = "gold_monitor_channel"
        const val CHANNEL_ALERT = "gold_alert_channel"
        const val CHANNEL_SCHEDULE = "gold_schedule_channel"
    }
}
