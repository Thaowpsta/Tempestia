# 🌤️ Tempestia

Tempestia is a beautiful, modern, and highly responsive Android weather application built entirely with **Jetpack Compose** and **Kotlin**. It provides highly accurate real-time weather data, comprehensive offline support, and an advanced background alerting system.

## ✨ Key Features

* **Real-Time Forecasts:** Accurate current, hourly (24h), and daily (6-day) weather forecasts using the OpenWeatherMap API.
* **Advanced Weather Alerts:** Users can subscribe to custom weather alerts (e.g., "Severe Thunderstorm", "Morning Summary"). 
    * Uses **WorkManager** for battery-efficient background syncing.
    * Uses **AlarmManager** to trigger urgent, full-screen lock-screen alarms.
* **Offline First:** Weather data is cached locally using **Room**. If the user loses internet connection, the app seamlessly loads the last known forecast.
* **Dynamic Localization:** Native, on-the-fly support for **English** and **Arabic (RTL)** without requiring an app restart.
* **Location Flexibility:** Fetch location automatically via GPS (`FusedLocationProviderClient`) or manually drop a pin using **Google Maps Compose**.
* **Favorite Cities:** Save, search, and manage multiple cities with smooth swipe-to-dismiss animations.
* **Custom Theming:** Built-in Light, Dark, and System theme support with a highly customized color palette and animated particle Canvas backgrounds.
* **Modern Settings:** Easily toggle between Celsius/Fahrenheit and 12h/24h time formats, saved instantly via **Jetpack DataStore**.

## 🛠️ Tech Stack & Architecture

Tempestia follows modern Android development best practices and the **MVVM (Model-View-ViewModel)** architectural pattern.

**UI & Design**
* [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern declarative UI toolkit.
* [Material 3](https://m3.material.io/) - The latest Material Design system (including advanced `PullToRefreshBox`).
* Custom Canvas Animations & Transitions.

**Architecture & Reactive Programming**
* **MVVM** Architecture with Repository Pattern.
* [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & **Flow** (`StateFlow`, `SharedFlow`) for asynchronous, reactive data streams.

**Local Data & Caching**
* [Room Database](https://developer.android.com/training/data-storage/room) - For offline weather caching and favorite cities.
* [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - For type-safe user preference storage.

**Networking**
* [Retrofit2](https://square.github.io/retrofit/) & OkHttp - For RESTful API calls.
* Gson - JSON parsing and serialization for offline cache.

**Background Processing**
* [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - For periodic, reliable background weather checks.
* **AlarmManager & BroadcastReceivers** - For exact-time wake-up alarms.

**Location & Maps**
* Google Play Services Location (`FusedLocationProviderClient`).
* Google Maps Compose (`com.google.maps.android:maps-compose`).

## 🚀 Getting Started

To build and run this project locally, you will need to provide your own API keys for OpenWeatherMap and Google Maps.

### 1. Clone the repository
```bash
git clone [https://github.com/Thaowpsta/Tempestia.git](https://github.com/Thaowpsta/Tempestia.git)
