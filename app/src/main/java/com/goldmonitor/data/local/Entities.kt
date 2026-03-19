package com.goldmonitor.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Gold price history entity for Room database
 */
@Entity(tableName = "gold_price_history")
data class GoldPriceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val price: Double,
    val openPrice: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val change: Double,
    val changePercent: Double,
    val timestamp: Long,
    val source: String = "default"
)

/**
 * Alert rule entity for Room database
 */
@Entity(tableName = "alert_rules")
data class AlertRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // UPPER_LIMIT, LOWER_LIMIT, MARKET_OPEN, CUSTOM_TIME
    val thresholdPrice: Double?,
    val reminderTime: String?, // HH:mm format
    val isEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val ringtone: Boolean = true,
    val tts: Boolean = false,
    val wakeScreen: Boolean = false,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long? = null
)
