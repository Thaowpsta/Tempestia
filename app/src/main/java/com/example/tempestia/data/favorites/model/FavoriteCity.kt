package com.example.tempestia.data.favorites.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_cities")
data class FavoriteCity(
    @PrimaryKey
    var id: String = "",
    val cityName: String,
    val lat: Double,
    val lon: Double,
    val country: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val isCurrentLocation: Boolean = false,
    val cachedWeather: String? = null
) {
    init {
        if (id.isEmpty()) {
            id = "${lat}_${lon}"
        }
    }
}