package com.example.tempestia.ui.settings.viewModel

sealed class LocationFetchState {
    object Idle : LocationFetchState()
    object Fetching : LocationFetchState()
    data class Success(val locationName: String) : LocationFetchState()
    data class Error(val messageResId: Int) : LocationFetchState()
}
