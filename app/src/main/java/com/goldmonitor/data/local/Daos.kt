package com.goldmonitor.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for gold price history
 */
@Dao
interface GoldPriceDao {

    @Query("SELECT * FROM gold_price_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPrice(): GoldPriceEntity?

    @Query("SELECT * FROM gold_price_history ORDER BY timestamp DESC LIMIT 1")
    fun getLatestPriceFlow(): Flow<GoldPriceEntity?>

    @Query("SELECT * FROM gold_price_history WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    suspend fun getPriceHistory(startTime: Long): List<GoldPriceEntity>

    @Query("SELECT * FROM gold_price_history WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    fun getPriceHistoryFlow(startTime: Long): Flow<List<GoldPriceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: GoldPriceEntity): Long

    @Query("DELETE FROM gold_price_history WHERE timestamp < :beforeTime")
    suspend fun deleteOldPrices(beforeTime: Long)

    @Query("SELECT COUNT(*) FROM gold_price_history")
    suspend fun getCount(): Int
}

/**
 * DAO for alert rules
 */
@Dao
interface AlertRuleDao {

    @Query("SELECT * FROM alert_rules ORDER BY createdAt DESC")
    fun getAllRulesFlow(): Flow<List<AlertRuleEntity>>

    @Query("SELECT * FROM alert_rules ORDER BY createdAt DESC")
    suspend fun getAllRules(): List<AlertRuleEntity>

    @Query("SELECT * FROM alert_rules WHERE isEnabled = 1")
    suspend fun getEnabledRules(): List<AlertRuleEntity>

    @Query("SELECT * FROM alert_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): AlertRuleEntity?

    @Query("SELECT * FROM alert_rules WHERE type = :type AND isEnabled = 1")
    suspend fun getEnabledRulesByType(type: String): List<AlertRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AlertRuleEntity): Long

    @Update
    suspend fun updateRule(rule: AlertRuleEntity)

    @Delete
    suspend fun deleteRule(rule: AlertRuleEntity)

    @Query("DELETE FROM alert_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("UPDATE alert_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE alert_rules SET lastTriggeredAt = :timestamp WHERE id = :id")
    suspend fun updateLastTriggered(id: Long, timestamp: Long)
}
