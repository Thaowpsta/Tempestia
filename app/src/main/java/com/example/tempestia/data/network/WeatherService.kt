package com.example.tempestia.data.network

import com.example.tempestia.data.weather.model.GeoResponse
import com.example.tempestia.data.weather.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/3.0/onecall")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = "minutely,alerts",
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>


    @GET("geo/1.0/reverse")
    suspend fun getCityName(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): Response<List<GeoResponse>>

    @GET("geo/1.0/direct")
    suspend fun getCoordinatesByName(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): Response<List<GeoResponse>>
}