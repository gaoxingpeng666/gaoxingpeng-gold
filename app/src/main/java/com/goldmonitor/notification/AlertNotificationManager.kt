package com.goldmonitor.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.goldmonitor.GoldMonitorApp
import com.goldmonitor.R
import com.goldmonitor.alert.TextToSpeechManager
import com.goldmonitor.domain.model.AlertRule
import com.goldmonitor.domain.model.AlertType
import com.goldmonitor.domain.model.GoldPrice
import com.goldmonitor.ui.alert.AlertActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ttsManager: TextToSpeechManager
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun sendPriceAlert(rule: AlertRule, price: GoldPrice) {
        val (title, message, ttsMessage) = when (rule.type) {
            is AlertType.UpperLimit -> Triple(
                context.getString(R.string.notification_upper_alert),
                "当前金价 ¥${price.formattedPrice} 已突破上限 ¥${String.format("%.2f", rule.type.price)}",
                context.getString(R.string.tts_upper_alert, rule.type.price, price.price)
            )
            is AlertType.LowerLimit -> Triple(
                context.getString(R.string.notification_lower_alert),
                "当前金价 ¥${price.formattedPrice} 已跌破下限 ¥${String.format("%.2f", rule.type.price)}",
                context.getString(R.string.tts_lower_alert, rule.type.price, price.price)
            )
            else -> return
        }

        // Vibrate
        if (rule.vibrate) {
            vibrate()
        }

        // TTS
        if (rule.tts) {
            ttsManager.speak(ttsMessage)
        }

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(context, GoldMonitorApp.CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        // Wake screen with full screen intent
        if (rule.wakeScreen) {
            val fullScreenIntent = Intent(context, AlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AlertActivity.EXTRA_TITLE, title)
                putExtra(AlertActivity.EXTRA_MESSAGE, message)
                putExtra(AlertActivity.EXTRA_PRICE, price.price)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context, 
                System.currentTimeMillis().toInt(),
                fullScreenIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            notificationBuilder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        notificationManager.notify(
            ALERT_NOTIFICATION_ID + rule.id.toInt(),
            notificationBuilder.build()
        )
    }

    fun sendScheduleReminder(title: String, message: String, price: GoldPrice?) {
        val ttsMessage = price?.let {
            context.getString(R.string.tts_market_open, it.price)
        } ?: message

        ttsManager.speak(ttsMessage)
        vibrate()

        val notification = NotificationCompat.Builder(context, GoldMonitorApp.CHANNEL_SCHEDULE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(SCHEDULE_NOTIFICATION_ID, notification)
    }

    private fun vibrate() {
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    companion object {
        private const val ALERT_NOTIFICATION_ID = 2000
        private const val SCHEDULE_NOTIFICATION_ID = 3000
    }
}
