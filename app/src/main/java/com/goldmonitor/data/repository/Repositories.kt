package com.goldmonitor.data.repository

import com.goldmonitor.data.local.AlertRuleDao
import com.goldmonitor.data.local.AlertRuleEntity
import com.goldmonitor.data.local.GoldPriceDao
import com.goldmonitor.data.local.GoldPriceEntity
import com.goldmonitor.data.remote.GoldPriceApiService
import com.goldmonitor.domain.model.AlertRule
import com.goldmonitor.domain.model.AlertType
import com.goldmonitor.domain.model.GoldPrice
import com.goldmonitor.domain.model.PriceDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for gold price data
 */
@Singleton
class GoldPriceRepository @Inject constructor(
    private val apiService: GoldPriceApiService,
    private val goldPriceDao: GoldPriceDao
) {

    /**
     * Fetch latest gold price from API and cache it
     */
    suspend fun fetchLatestPrice(): Result<GoldPrice> {
        return try {
            val response = apiService.getGoldPrice()
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                val entity = GoldPriceEntity(
                    price = data.price,
                    openPrice = data.openPrice,
                    highPrice = data.highPrice,
                    lowPrice = data.lowPrice,
                    change = data.change,
                    changePercent = data.changePercent,
                    timestamp = data.timestamp ?: System.currentTimeMillis(),
                    source = "primary"
                )
                goldPriceDao.insertPrice(entity)
                Result.success(entity.toDomainModel())
            } else {
                // Try to get cached data
                val cached = goldPriceDao.getLatestPrice()
                if (cached != null) {
                    Result.success(cached.toDomainModel())
                } else {
                    Result.failure(Exception("Failed to fetch gold price"))
                }
            }
        } catch (e: Exception) {
            // Return cached data on network error
            val cached = goldPriceDao.getLatestPrice()
            if (cached != null) {
                Result.success(cached.toDomainModel())
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Get latest cached price
     */
    suspend fun getLatestCachedPrice(): GoldPrice? {
        return goldPriceDao.getLatestPrice()?.toDomainModel()
    }

    /**
     * Observe latest price changes
     */
    fun observeLatestPrice(): Flow<GoldPrice?> {
        return goldPriceDao.getLatestPriceFlow().map { it?.toDomainModel() }
    }

    /**
     * Get price history for chart
     */
    suspend fun getPriceHistory(startTime: Long): List<GoldPrice> {
        return goldPriceDao.getPriceHistory(startTime).map { it.toDomainModel() }
    }

    /**
     * Observe price history
     */
    fun observePriceHistory(startTime: Long): Flow<List<GoldPrice>> {
        return goldPriceDao.getPriceHistoryFlow(startTime).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    /**
     * Clean old price data (keep last 30 days)
     */
    suspend fun cleanOldData() {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        goldPriceDao.deleteOldPrices(thirtyDaysAgo)
    }

    private fun GoldPriceEntity.toDomainModel(): GoldPrice {
        val direction = when {
            change > 0 -> PriceDirection.UP
            change < 0 -> PriceDirection.DOWN
            else -> PriceDirection.FLAT
        }
        return GoldPrice(
            price = price,
            openPrice = openPrice,
            highPrice = highPrice,
            lowPrice = lowPrice,
            change = change,
            changePercent = changePercent,
            timestamp = timestamp,
            direction = direction
        )
    }
}

/**
 * Repository for alert rules
 */
@Singleton
class AlertRuleRepository @Inject constructor(
    private val alertRuleDao: AlertRuleDao
) {

    fun observeAllRules(): Flow<List<AlertRule>> {
        return alertRuleDao.getAllRulesFlow().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    suspend fun getAllRules(): List<AlertRule> {
        return alertRuleDao.getAllRules().map { it.toDomainModel() }
    }

    suspend fun getEnabledRules(): List<AlertRule> {
        return alertRuleDao.getEnabledRules().map { it.toDomainModel() }
    }

    suspend fun getRuleById(id: Long): AlertRule? {
        return alertRuleDao.getRuleById(id)?.toDomainModel()
    }

    suspend fun getEnabledPriceAlertRules(): List<AlertRule> {
        val upperRules = alertRuleDao.getEnabledRulesByType("UPPER_LIMIT")
        val lowerRules = alertRuleDao.getEnabledRulesByType("LOWER_LIMIT")
        return (upperRules + lowerRules).map { it.toDomainModel() }
    }

    suspend fun saveRule(rule: AlertRule): Long {
        return alertRuleDao.insertRule(rule.toEntity())
    }

    suspend fun updateRule(rule: AlertRule) {
        alertRuleDao.updateRule(rule.toEntity())
    }

    suspend fun deleteRule(id: Long) {
        alertRuleDao.deleteRuleById(id)
    }

    suspend fun setRuleEnabled(id: Long, enabled: Boolean) {
        alertRuleDao.setRuleEnabled(id, enabled)
    }

    suspend fun updateLastTriggered(id: Long) {
        alertRuleDao.updateLastTriggered(id, System.currentTimeMillis())
    }

    private fun AlertRuleEntity.toDomainModel(): AlertRule {
        val alertType = when (type) {
            "UPPER_LIMIT" -> AlertType.UpperLimit(thresholdPrice ?: 0.0)
            "LOWER_LIMIT" -> AlertType.LowerLimit(thresholdPrice ?: 0.0)
            "MARKET_OPEN" -> AlertType.MarketOpen
            "CUSTOM_TIME" -> AlertType.CustomTime(reminderTime ?: "09:00")
            else -> AlertType.MarketOpen
        }
        return AlertRule(
            id = id,
            type = alertType,
            isEnabled = isEnabled,
            vibrate = vibrate,
            ringtone = ringtone,
            tts = tts,
            wakeScreen = wakeScreen,
            description = description,
            createdAt = createdAt,
            lastTriggeredAt = lastTriggeredAt
        )
    }

    private fun AlertRule.toEntity(): AlertRuleEntity {
        val (typeStr, thresholdPrice, reminderTime) = when (type) {
            is AlertType.UpperLimit -> Triple("UPPER_LIMIT", type.price, null)
            is AlertType.LowerLimit -> Triple("LOWER_LIMIT", type.price, null)
            is AlertType.MarketOpen -> Triple("MARKET_OPEN", null, null)
            is AlertType.CustomTime -> Triple("CUSTOM_TIME", null, type.time)
        }
        return AlertRuleEntity(
            id = id,
            type = typeStr,
            thresholdPrice = thresholdPrice,
            reminderTime = reminderTime,
            isEnabled = isEnabled,
            vibrate = vibrate,
            ringtone = ringtone,
            tts = tts,
            wakeScreen = wakeScreen,
            description = description,
            createdAt = createdAt,
            lastTriggeredAt = lastTriggeredAt
        )
    }
}
