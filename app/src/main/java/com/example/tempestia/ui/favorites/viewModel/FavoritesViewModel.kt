package com.example.tempestia.ui.favorites.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tempestia.BuildConfig
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.data.weather.model.GeoResponse
import com.example.tempestia.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val apiKey = BuildConfig.WEATHER_API_KEY

    private val _favoriteWeather = MutableStateFlow<List<FavoriteWeatherState>>(emptyList())
    val favoriteWeather: StateFlow<List<FavoriteWeatherState>> = _favoriteWeather.asStateFlow()

    private val _apiSearchQuery = MutableStateFlow("")
    val apiSearchQuery: StateFlow<String> = _apiSearchQuery.asStateFlow()

    private val _apiSearchResults = MutableStateFlow<List<GeoResponse>?>(null)
    val apiSearchResults: StateFlow<List<GeoResponse>?> = _apiSearchResults.asStateFlow()

    private val _isSearchingApi = MutableStateFlow(false)
    val isSearchingApi: StateFlow<Boolean> = _isSearchingApi.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavoriteCities().collectLatest { cities ->
                _favoriteWeather.value = cities.map { FavoriteWeatherState(city = it, isLoading = true) }

                val weatherUpdates = cities.map { city ->
                    async {
                        try {
                            val response = repository.getWeather(city.lat, city.lon, apiKey)
                            if (response.isSuccessful && response.body() != null) {
                                val current = response.body()!!.current
                                FavoriteWeatherState(
                                    city = city,
                                    temp = current.temp,
                                    condition = current.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() },
                                    iconCode = current.weather.firstOrNull()?.icon,
                                    humidity = current.humidity,
                                    windSpeed = current.windSpeed,
                                    uvi = current.uvi,
                                    isLoading = false
                                )
                            } else {
                                FavoriteWeatherState(city, isLoading = false)
                            }
                        } catch (e: Exception) {
                            FavoriteWeatherState(city, isLoading = false)
                        }
                    }
                }.awaitAll()
                _favoriteWeather.value = weatherUpdates
            }
        }

        viewModelScope.launch {
            _apiSearchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _apiSearchResults.value = null
                        _isSearchingApi.value = false
                        return@collectLatest
                    }

                    _isSearchingApi.value = true
                    try {
                        val response = repository.getCoordinatesByName(query, apiKey, limit = 10)
                        if (response.isSuccessful) {
                            val results = response.body() ?: emptyList()
                            _apiSearchResults.value = results.distinctBy { "${it.name}-${it.country}" }.take(5)
                        } else {
                            _apiSearchResults.value = emptyList()
                        }
                    } catch (e: Exception) {
                        _apiSearchResults.value = emptyList()
                    } finally {
                        _isSearchingApi.value = false
                    }
                }
        }
    }

    fun onApiSearchQueryChanged(query: String) {
        _apiSearchQuery.value = query
    }

    fun clearApiSearch() {
        _apiSearchQuery.value = ""
        _apiSearchResults.value = null
    }

    fun removeCity(city: FavoriteCity) {
        viewModelScope.launch { repository.deleteFavorite(city) }
    }

    fun addFavorite(geoResponse: GeoResponse) {
        viewModelScope.launch {
            val newFavorite = FavoriteCity(
                cityName = geoResponse.name,
                lat = geoResponse.lat,
                lon = geoResponse.lon,
                country = geoResponse.country
            )
            repository.insertFavorite(newFavorite)
        }
    }
}

class FavoritesViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}