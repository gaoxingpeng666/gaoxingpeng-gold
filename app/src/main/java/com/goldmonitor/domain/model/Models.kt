package com.goldmonitor.domain.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Gold price domain model
 */
data class GoldPrice(
    val price: Double,
    val openPrice: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val change: Double,
    val changePercent: Double,
    val timestamp: Long,
    val direction: PriceDirection = PriceDirection.FLAT
) {
    val formattedPrice: String
        get() = String.format("%.2f", price)

    val formattedChange: String
        get() = String.format("%+.2f", change)

    val formattedChangePercent: String
        get() = String.format("%+.2f%%", changePercent)

    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

/**
 * Price direction enum
 */
enum class PriceDirection {
    UP, DOWN, FLAT
}

/**
 * Alert rule domain model
 */
data class AlertRule(
    val id: Long = 0,
    val type: AlertType,
    val isEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val ringtone: Boolean = true,
    val tts: Boolean = false,
    val wakeScreen: Boolean = false,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long? = null
) {
    val displayName: String
        get() = when (type) {
            is AlertType.UpperLimit -> "上限提醒: ${String.format("%.2f", type.price)}元"
            is AlertType.LowerLimit -> "下限提醒: ${String.format("%.2f", type.price)}元"
            is AlertType.MarketOpen -> "开盘提醒 (9:00)"
            is AlertType.CustomTime -> "定时提醒: ${type.time}"
        }
}

/**
 * Alert type sealed class
 */
sealed class AlertType {
    data class UpperLimit(val price: Double) : AlertType()
    data class LowerLimit(val price: Double) : AlertType()
    data object MarketOpen : AlertType()
    data class CustomTime(val time: String) : AlertType()
}

/**
 * User settings domain model
 */
data class UserSettings(
    val monitorEnabled: Boolean = false,
    val refreshInterval: Int = 30, // seconds
    val notificationEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val ringtoneEnabled: Boolean = true,
    val ttsEnabled: Boolean = false,
    val wakeScreenEnabled: Boolean = false,
    val marketOpenRemind: Boolean = false,
    val customRemindTime: String? = null,
    val themeMode: String = "SYSTEM" // LIGHT, DARK, SYSTEM
)

/**
 * Chart data model
 */
data class PriceChartData(
    val entries: List<PriceEntry>,
    val timeRange: TimeRange,
    val minPrice: Double,
    val maxPrice: Double
)

data class PriceEntry(
    val timestamp: Long,
    val price: Double
)

enum class TimeRange {
    DAY, WEEK, MONTH
}
