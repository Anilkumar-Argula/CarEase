package uk.ac.tees.mad.carease.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import uk.ac.tees.mad.carease.data.models.OpenMeteoResponse

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weather_code",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}