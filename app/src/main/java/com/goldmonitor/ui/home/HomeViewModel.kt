package com.goldmonitor.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldmonitor.data.repository.GoldPriceRepository
import com.goldmonitor.data.repository.UserSettingsRepository
import com.goldmonitor.domain.model.GoldPrice
import com.goldmonitor.domain.model.PriceChartData
import com.goldmonitor.domain.model.PriceEntry
import com.goldmonitor.domain.model.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goldPriceRepository: GoldPriceRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _chartData = MutableStateFlow<PriceChartData?>(null)
    val chartData: StateFlow<PriceChartData?> = _chartData.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.DAY)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    val isMonitorEnabled = userSettingsRepository.settingsFlow
        .map { it.monitorEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadPrice()
        observePriceChanges()
    }

    fun loadPrice() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            goldPriceRepository.fetchLatestPrice()
                .onSuccess { price ->
                    _uiState.value = HomeUiState.Success(price)
                    loadChartData(_selectedTimeRange.value)
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "获取金价失败")
                }
        }
    }

    fun refresh() {
        loadPrice()
    }

    fun selectTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
        loadChartData(range)
    }

    private fun observePriceChanges() {
        viewModelScope.launch {
            goldPriceRepository.observeLatestPrice().collect { price ->
                if (price != null && _uiState.value is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Success(price)
                }
            }
        }
    }

    private fun loadChartData(timeRange: TimeRange) {
        viewModelScope.launch {
            val startTime = when (timeRange) {
                TimeRange.DAY -> System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                TimeRange.WEEK -> System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
                TimeRange.MONTH -> System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            }
            
            val history = goldPriceRepository.getPriceHistory(startTime)
            if (history.isNotEmpty()) {
                val entries = history.map { PriceEntry(it.timestamp, it.price) }
                val minPrice = history.minOf { it.price }
                val maxPrice = history.maxOf { it.price }
                _chartData.value = PriceChartData(entries, timeRange, minPrice, maxPrice)
            }
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val price: GoldPrice) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
