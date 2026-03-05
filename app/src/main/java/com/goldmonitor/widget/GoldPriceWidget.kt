package com.goldmonitor.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.goldmonitor.MainActivity
import com.goldmonitor.data.local.GoldPriceDatabase
import com.goldmonitor.domain.model.PriceDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoldPriceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val priceData = withContext(Dispatchers.IO) {
            try {
                val db = androidx.room.Room.databaseBuilder(
                    context,
                    GoldPriceDatabase::class.java,
                    GoldPriceDatabase.DATABASE_NAME
                ).build()
                val price = db.goldPriceDao().getLatestPrice()
                db.close()
                price
            } catch (e: Exception) {
                null
            }
        }

        provideContent {
            GoldPriceWidgetContent(
                price = priceData?.price,
                change = priceData?.change,
                changePercent = priceData?.changePercent,
                direction = when {
                    priceData == null -> PriceDirection.FLAT
                    priceData.change > 0 -> PriceDirection.UP
                    priceData.change < 0 -> PriceDirection.DOWN
                    else -> PriceDirection.FLAT
                }
            )
        }
    }
}

@Composable
fun GoldPriceWidgetContent(
    price: Double?,
    change: Double?,
    changePercent: Double?,
    direction: PriceDirection
) {
    val priceColor = when (direction) {
        PriceDirection.UP -> androidx.compose.ui.graphics.Color(0xFFE53935)
        PriceDirection.DOWN -> androidx.compose.ui.graphics.Color(0xFF43A047)
        PriceDirection.FLAT -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFFF5F5F5))
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "黄金 AU9999",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF757575))
                )
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = if (price != null) "¥${String.format("%.2f", price)}" else "---.--",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(androidx.compose.ui.graphics.Color(0xFFFFD700))
                )
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            if (changePercent != null) {
                val sign = if (changePercent >= 0) "+" else ""
                Text(
                    text = "$sign${String.format("%.2f", changePercent)}%",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(priceColor)
                    )
                )
            }
        }
    }
}

class GoldPriceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GoldPriceWidget()
}
