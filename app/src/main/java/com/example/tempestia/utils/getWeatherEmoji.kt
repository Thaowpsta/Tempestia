package com.example.tempestia.utils

fun getWeatherEmoji(iconCode: String): String {
    return when (iconCode.take(2)) {
        "01" -> "☀️" // clear sky
        "02" -> "⛅" // few clouds
        "03", "04" -> "☁️" // scattered/broken clouds
        "09", "10" -> "🌧️" // shower rain / rain
        "11" -> "⛈️" // thunderstorm
        "13" -> "❄️" // snow
        "50" -> "🌫️" // mist
        else -> "⛅"
    }
}
