package com.example.tempestia.ui.favorites.viewModel

import com.example.tempestia.data.favorites.model.FavoriteCity

data class FavoriteWeatherState(
    val city: FavoriteCity,
    val temp: Double? = null,
    val condition: String? = null,
    val iconCode: String? = null,
    val humidity: Int? = null,
    val windSpeed: Double? = null,
    val uvi: Double? = null,
    val isLoading: Boolean = true
)
