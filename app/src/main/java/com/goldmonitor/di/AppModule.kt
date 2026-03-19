package com.goldmonitor.di

import android.content.Context
import androidx.room.Room
import com.goldmonitor.data.local.AlertRuleDao
import com.goldmonitor.data.local.GoldPriceDao
import com.goldmonitor.data.local.GoldPriceDatabase
import com.goldmonitor.data.remote.GoldPriceApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GoldPriceApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGoldPriceApiService(retrofit: Retrofit): GoldPriceApiService {
        return retrofit.create(GoldPriceApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GoldPriceDatabase {
        return Room.databaseBuilder(
            context,
            GoldPriceDatabase::class.java,
            GoldPriceDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideGoldPriceDao(database: GoldPriceDatabase): GoldPriceDao {
        return database.goldPriceDao()
    }

    @Provides
    @Singleton
    fun provideAlertRuleDao(database: GoldPriceDatabase): AlertRuleDao {
        return database.alertRuleDao()
    }
}
