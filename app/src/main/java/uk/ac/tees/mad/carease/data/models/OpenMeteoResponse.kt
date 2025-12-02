package uk.ac.tees.mad.carease.data.models

// API Response models
data class OpenMeteoResponse(
    val current: CurrentWeather
)

data class CurrentWeather(
    val temperature_2m: Double,
    val weather_code: Int,
    val time: String
)

// Helper to convert weather codes to conditions
object WeatherCodeMapper {
    fun getCondition(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rainy"
            71, 73, 75 -> "Snowy"
            80, 81, 82 -> "Rain showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }

    fun getBestTimeForWash(code: Int): String {
        return when {
            code == 0 -> "Today is perfect for a wash!"
            code in 1..3 -> "Good day for a wash (partly cloudy)"
            code > 50 -> "Wait for better weather (rain/snow expected)"
            else -> "Check forecast before booking"
        }
    }

    fun getWorstTimeForWash(code: Int): String {
        return when {
            code >= 61 -> "Avoid today (rainy conditions)"
            code >= 51 -> "Not ideal (drizzle expected)"
            else -> "No major concerns"
        }
    }
}