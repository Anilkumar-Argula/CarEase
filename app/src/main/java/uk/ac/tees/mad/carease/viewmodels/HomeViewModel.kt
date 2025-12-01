package uk.ac.tees.mad.carease.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.carease.data.models.Booking
import uk.ac.tees.mad.carease.data.models.Service
import uk.ac.tees.mad.carease.data.models.UserProfile
import uk.ac.tees.mad.carease.data.models.Weather
import uk.ac.tees.mad.carease.data.repository.HomeRepository
import uk.ac.tees.mad.carease.utils.NetworkObserver
import javax.inject.Inject


data class HomeUiState(
    val isLoading: Boolean = true,
    val isOnline: Boolean = true,
    val userProfile: UserProfile? = null,
    val services: List<Service> = emptyList(),
    val weather: Weather? = null,
    val suggestedService: Service? = null,
    val recentBookings: List<Booking> = emptyList(),
    val errorMessage: String? = null,
//    val locationPermissionNeeded:Boolean=false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()


    init {
        loadHomeData()
    }

    fun loadHomeData() {

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // fetch    user Profile
            repository.getUserProfile().onSuccess { profile ->
                _uiState.value = _uiState.value.copy(userProfile = profile)

//                // Fetch weather for user's preferred area
//                if (profile.preferredArea.isNotBlank()) {
//                    repository.getWeather(profile.preferredArea).onSuccess { weather ->
//                        _uiState.value = _uiState.value.copy(weather = weather)
//                    }
//                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to load profile"
                )
            }

            // Fetch services
            repository.getServices().onSuccess { services ->
                _uiState.value = _uiState.value.copy(
                    services = services,
                    suggestedService = services.firstOrNull()
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to load services"
                )
            }

//            Log.d(TAG, "loadHomeData: ")
            //  recent bookings
            repository.getRecentBookings().onSuccess { bookings ->
                _uiState.value = _uiState.value.copy(recentBookings = bookings)
            }.onFailure { error ->
                // Don't show error if no bookings, just log it
                Log.d("HomeViewModel", "No bookings found: ${error.message}")
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }

    }

    fun checkNetworkStatus(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isOnline =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        _uiState.value = _uiState.value.copy(isOnline = isOnline)
    }

    fun observeNetwork(context: Context) {
        viewModelScope.launch {
            NetworkObserver.observe(context).collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
            }
        }
    }


    fun refreshData(context:Context) {
        loadHomeData()
        loadWeatherByLocation(context = context)
    }

    fun loadWeatherByLocation(context: Context) {
        viewModelScope.launch {
            repository.getWeatherByCurrentLocation(context).onSuccess { weather ->
                _uiState.value = _uiState.value.copy(weather = weather)
            }.onFailure { error ->
                // Try fallback to area-based weather
                val preferredArea = _uiState.value.userProfile?.preferredArea
                if (!preferredArea.isNullOrBlank()) {
                    repository.getRealTimeWeather(preferredArea).onSuccess { weather ->
                        _uiState.value = _uiState.value.copy(weather = weather)
                    }
                }
            }
        }
    }


//    fun requestLocationPermission() {
//        _uiState.value = _uiState.value.copy(locationPermissionNeeded = true)
//    }

//    fun skipLocationPermission() {
//        _uiState.value = _uiState.value.copy(
//            locationPermissionNeeded = false
//        )
//        // Use fallback to preferredArea weather
//        viewModelScope.launch {
//            val preferredArea = _uiState.value.userProfile?.preferredArea
//            if (!preferredArea.isNullOrBlank()) {
//                repository.getRealTimeWeather(preferredArea).onSuccess { weather ->
//                    _uiState.value = _uiState.value.copy(weather = weather)
//                }
//            }
//        }
//    }
}