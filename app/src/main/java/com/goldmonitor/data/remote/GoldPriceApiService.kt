package com.goldmonitor.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API service for fetching gold prices
 */
interface GoldPriceApiService {

    /**
     * Get current gold price from primary API
     * Using a free gold price API
     */
    @GET("api/gold")
    suspend fun getGoldPrice(): Response<GoldPriceResponse>

    /**
     * Backup API endpoint
     */
    @GET("api/goldprice")
    suspend fun getGoldPriceBackup(
        @Query("key") apiKey: String = ""
    ): Response<GoldPriceSimpleResponse>

    companion object {
        const val BASE_URL = "https://api.jisuapi.com/"
        const val BACKUP_BASE_URL = "https://apis.tianapi.com/"
    }
}
