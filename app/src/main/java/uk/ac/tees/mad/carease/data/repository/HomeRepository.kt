package uk.ac.tees.mad.carease.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.carease.data.api.WeatherApiService
import uk.ac.tees.mad.carease.data.models.*
import uk.ac.tees.mad.carease.utils.AreaCoordinates
import uk.ac.tees.mad.carease.utils.LocationManager
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val weatherApi: WeatherApiService,
    private val locationManager: LocationManager
) {

    suspend fun getServices(): Result<List<Service>> {
        return try {
            val snapshot = firestore.collection("services")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val services = snapshot.documents.mapNotNull { doc ->

                Log.d("HR", "getServices: $doc and ${doc.id} ")
                doc.toObject(Service::class.java)?.copy(id = doc.id)
            }
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }

    }


    suspend fun getAreas(): Result<List<Area>> {
        return try {
            val snapshot = firestore.collection("areas")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val areas = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Area::class.java)?.copy(id = doc.id)
            }
            Result.success(areas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch user profile
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val profile = snapshot.toObject(UserProfile::class.java)
            if (profile != null) {
                Result.success(profile)
            } else {
                Result.failure(Exception("Profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch weather for a specific area
    suspend fun getWeather(areaId: String): Result<Weather> {
        return try {
            val snapshot = firestore.collection("weather_cache")
                .document(areaId)
                .get()
                .await()

            val weather = snapshot.toObject(Weather::class.java)
            if (weather != null) {
                Result.success(weather)
            } else {
                Result.failure(Exception("Weather data not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getWeatherByCurrentLocation(context: Context): Result<Weather> {
        return try {
            // Get current location
            val locationResult = locationManager.getCurrentLocation(context)

            if (locationResult.isFailure) {
                // Fallback to area-based weather if location fails
                return Result.failure(locationResult.exceptionOrNull()!!)
            }

            val (latitude, longitude) = locationResult.getOrThrow()

            // Fetch weather from API
            val response = weatherApi.getCurrentWeather(
                latitude = latitude,
                longitude = longitude
            )

            val weather = Weather(
                areaId = "current_location",
                temperature = response.current.temperature_2m,
                condition = WeatherCodeMapper.getCondition(response.current.weather_code),
                bestTimeForWash = WeatherCodeMapper.getBestTimeForWash(response.current.weather_code),
                worstTimeForWash = WeatherCodeMapper.getWorstTimeForWash(response.current.weather_code),
                lastUpdated = Timestamp.now()
            )

            Result.success(weather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Keep the area-based method as fallback
    suspend fun getRealTimeWeather(areaId: String): Result<Weather> {
        return try {
            val coordinates = AreaCoordinates.getCoordinates(areaId)
                ?: return Result.failure(Exception("Area coordinates not found"))

            val response = weatherApi.getCurrentWeather(
                latitude = coordinates.first,
                longitude = coordinates.second
            )

            val weather = Weather(
                areaId = areaId,
                temperature = response.current.temperature_2m,
                condition = WeatherCodeMapper.getCondition(response.current.weather_code),
                bestTimeForWash = WeatherCodeMapper.getBestTimeForWash(response.current.weather_code),
                worstTimeForWash = WeatherCodeMapper.getWorstTimeForWash(response.current.weather_code),
                lastUpdated = Timestamp.now()
            )

            Result.success(weather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentBookings(): Result<List<Booking>> {
        return try {
            val userId = auth.currentUser?.uid
            // never happen
            if (userId == null) {
                return Result.failure(Exception("User not found"))
            }
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("userId", userId)
//                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(4)
                .get()
                .await()
            val booking = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)?.copy(bookingId = doc.id)
            }

            Log.d("HR", "getRecentBookings: $booking")
            Result.success(booking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}