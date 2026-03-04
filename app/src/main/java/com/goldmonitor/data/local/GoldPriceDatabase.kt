package com.goldmonitor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for gold price app
 */
@Database(
    entities = [
        GoldPriceEntity::class,
        AlertRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GoldPriceDatabase : RoomDatabase() {

    abstract fun goldPriceDao(): GoldPriceDao
    abstract fun alertRuleDao(): AlertRuleDao

    companion object {
        const val DATABASE_NAME = "gold_price_db"
    }
}
