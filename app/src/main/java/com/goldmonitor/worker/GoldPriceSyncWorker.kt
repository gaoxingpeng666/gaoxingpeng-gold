package com.goldmonitor.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.goldmonitor.data.repository.AlertRuleRepository
import com.goldmonitor.data.repository.GoldPriceRepository
import com.goldmonitor.domain.model.AlertType
import com.goldmonitor.notification.AlertNotificationManager
import com.goldmonitor.widget.GoldPriceWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class GoldPriceSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val goldPriceRepository: GoldPriceRepository,
    private val alertRuleRepository: AlertRuleRepository,
    private val alertNotificationManager: AlertNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Fetch latest price
            val priceResult = goldPriceRepository.fetchLatestPrice()

            priceResult.onSuccess { price ->
                // Check alert rules
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

                // Update widget
                GoldPriceWidget().updateAll(applicationContext)
            }

            // Clean old data periodically
            goldPriceRepository.cleanOldData()

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "gold_price_sync_work"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<GoldPriceSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
