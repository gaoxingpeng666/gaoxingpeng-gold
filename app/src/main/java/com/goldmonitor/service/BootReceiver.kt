package com.goldmonitor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.goldmonitor.data.repository.UserSettingsRepository
import com.goldmonitor.worker.GoldPriceSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val settings = userSettingsRepository.settingsFlow.first()
                if (settings.monitorEnabled) {
                    GoldPriceMonitorService.start(context)
                }
                // Always enqueue background worker
                GoldPriceSyncWorker.enqueue(context)
            }
        }
    }
}
