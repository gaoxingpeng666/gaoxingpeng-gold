package com.goldmonitor.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Gold price response from API
 */
data class GoldPriceResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("msg")
    val message: String?,
    @SerializedName("data")
    val data: GoldPriceData?
)

data class GoldPriceData(
    @SerializedName("price")
    val price: Double,
    @SerializedName("open")
    val openPrice: Double,
    @SerializedName("high")
    val highPrice: Double,
    @SerializedName("low")
    val lowPrice: Double,
    @SerializedName("close")
    val closePrice: Double,
    @SerializedName("change")
    val change: Double,
    @SerializedName("changepercent")
    val changePercent: Double,
    @SerializedName("time")
    val time: String?,
    @SerializedName("timestamp")
    val timestamp: Long?
)

/**
 * Alternative API response format (for backup APIs)
 */
data class GoldPriceSimpleResponse(
    @SerializedName("result")
    val result: GoldPriceSimpleData?
)

data class GoldPriceSimpleData(
    @SerializedName("au9999")
    val au9999: String?,
    @SerializedName("updatetime")
    val updateTime: String?
)
