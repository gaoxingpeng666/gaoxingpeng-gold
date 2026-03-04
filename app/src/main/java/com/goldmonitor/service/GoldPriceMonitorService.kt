package com.goldmonitor.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.goldmonitor.GoldMonitorApp
import com.goldmonitor.MainActivity
import com.goldmonitor.R
import com.goldmonitor.data.repository.AlertRuleRepository
import com.goldmonitor.data.repository.GoldPriceRepository
import com.goldmonitor.data.repository.UserSettingsRepository
import com.goldmonitor.domain.model.AlertType
import com.goldmonitor.domain.model.GoldPrice
import com.goldmonitor.domain.model.PriceDirection
import com.goldmonitor.notification.AlertNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class GoldPriceMonitorService : Service() {

    @Inject
    lateinit var goldPriceRepository: GoldPriceRepository

    @Inject
    lateinit var alertRuleRepository: AlertRuleRepository

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var alertNotificationManager: AlertNotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitorJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification(null))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        serviceScope.cancel()
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                try {
                    val settings = userSettingsRepository.settingsFlow.first()
                    val result = goldPriceRepository.fetchLatestPrice()
                    
                    result.onSuccess { price ->
                        updateNotification(price)
                        checkAlertRules(price)
                    }

                    delay(settings.refreshInterval * 1000L)
                } catch (e: Exception) {
                    delay(30_000L) // Retry after 30 seconds on error
                }
            }
        }
    }

    private suspend fun checkAlertRules(price: GoldPrice) {
        val rules = alertRuleRepository.getEnabledPriceAlertRules()
        for (rule in rules) {
            val shouldAlert = when (val type = rule.type) {
                is AlertType.UpperLimit -> price.price >= type.price
                is AlertType.LowerLimit -> price.price <= type.price
                else -> false
            }
            if (shouldAlert) {
                alertNotificationManager.sendPriceAlert(rule, price)
                alertRuleRepository.updateLastTriggered(rule.id)
            }
        }
    }

    private fun updateNotification(price: GoldPrice) {
        val notification = createNotification(price)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(price: GoldPrice?): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val priceText = price?.let {
            val arrow = when (it.direction) {
                PriceDirection.UP -> "↑"
                PriceDirection.DOWN -> "↓"
                PriceDirection.FLAT -> "→"
            }
            "¥${it.formattedPrice} $arrow${it.formattedChangePercent}"
        } ?: getString(R.string.loading)

        return NotificationCompat.Builder(this, GoldMonitorApp.CHANNEL_MONITOR)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.gold_price_label))
            .setContentText(priceText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.goldmonitor.action.START"
        const val ACTION_STOP = "com.goldmonitor.action.STOP"

        fun start(context: Context) {
            val intent = Intent(context, GoldPriceMonitorService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, GoldPriceMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
