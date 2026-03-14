package com.example.tempestia.ui.favorites.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tempestia.BuildConfig
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.data.weather.model.GeoResponse
import com.example.tempestia.data.weather.model.WeatherResponse
import com.example.tempestia.data.WeatherRepository
import com.google.gson.Gson
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
    private val _localSearchQuery = MutableStateFlow("")
    val localSearchQuery: StateFlow<String> = _localSearchQuery.asStateFlow()

    private val _showAddSearchDialog = MutableStateFlow(false)
    val showAddSearchDialog: StateFlow<Boolean> = _showAddSearchDialog.asStateFlow()

    private val _cityToConfirmAdd = MutableStateFlow<GeoResponse?>(null)
    val cityToConfirmAdd: StateFlow<GeoResponse?> = _cityToConfirmAdd.asStateFlow()

    private val _cityToConfirmDelete = MutableStateFlow<FavoriteCity?>(null)
    val cityToConfirmDelete: StateFlow<FavoriteCity?> = _cityToConfirmDelete.asStateFlow()

    fun updateLocalSearchQuery(query: String) {
        _localSearchQuery.value = query
    }

    fun setShowAddSearchDialog(show: Boolean) {
        _showAddSearchDialog.value = show
    }

    fun setCityToConfirmAdd(city: GeoResponse?) {
        _cityToConfirmAdd.value = city
    }

    fun setCityToConfirmDelete(city: FavoriteCity?) {
        _cityToConfirmDelete.value = city
    }

    init {
        viewModelScope.launch {
            repository.getFavoriteCities().collectLatest { cities ->
                _favoriteWeather.value = cities.map { FavoriteWeatherState(city = it, isLoading = true) }

                val isOnline = repository.isNetworkAvailable()

                val weatherUpdates = cities.map { city ->
                    async {
                        if (isOnline) {
                            try {
                                val response = repository.getWeather(city.lat, city.lon, apiKey)
                                if (response.isSuccessful && response.body() != null) {
                                    val weather = response.body()!!
                                    val current = weather.current

                                    // 🚨 Save offline cache
                                    val json = Gson().toJson(weather)
                                    if (city.cachedWeather != json) {
                                        repository.updateFavorite(city.copy(cachedWeather = json))
                                    }

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
                                    loadFromCache(city)
                                }
                            } catch (e: Exception) {
                                loadFromCache(city)
                            }
                        } else {
                            loadFromCache(city)
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

                    if (!repository.isNetworkAvailable()) {
                        _apiSearchResults.value = emptyList()
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

    // 🚨 OFFLINE ENGINE
    private fun loadFromCache(city: FavoriteCity): FavoriteWeatherState {
        return if (city.cachedWeather != null) {
            try {
                val weather = Gson().fromJson(city.cachedWeather, WeatherResponse::class.java)
                val current = weather.current
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
            } catch(e: Exception) {
                FavoriteWeatherState(city = city, isLoading = false, condition = "Offline Error")
            }
        } else {
            FavoriteWeatherState(city = city, isLoading = false, condition = "Offline")
        }
    }

    fun onApiSearchQueryChanged(query: String) { _apiSearchQuery.value = query }
    fun clearApiSearch() { _apiSearchQuery.value = ""; _apiSearchResults.value = null }
    fun removeCity(city: FavoriteCity) { viewModelScope.launch { repository.deleteFavorite(city) } }
    fun addFavorite(geoResponse: GeoResponse) {
        viewModelScope.launch {
            val newFavorite = FavoriteCity(cityName = geoResponse.name, lat = geoResponse.lat, lon = geoResponse.lon, country = geoResponse.country)
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