package uk.ac.tees.mad.carease.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.ac.tees.mad.carease.data.models.Booking
import uk.ac.tees.mad.carease.data.models.Service
import uk.ac.tees.mad.carease.data.models.UserProfile
import uk.ac.tees.mad.carease.data.models.Weather
import uk.ac.tees.mad.carease.data.repository.HomeRepository
import uk.ac.tees.mad.carease.utils.NetworkObserver

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


class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()


    init {
        loadHomeData()
        observeRecentBookings()
    }

    // ADD THIS METHOD
    private fun observeRecentBookings() {
        viewModelScope.launch {
            repository.getRecentBookingsFlow()
                .catch { error ->
                    Log.e("HomeVM", "Bookings flow error: ${error.message}")
                }
                .collect { bookings ->
                    _uiState.update { it.copy(recentBookings = bookings) }
                }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Fetch user profile
            repository.getUserProfile().onSuccess { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Failed to load profile"
                    )
                }
            }

            // Fetch services
            repository.getServices().onSuccess { services ->
                _uiState.update {
                    it.copy(
                        services = services,
                        suggestedService = services.firstOrNull()
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Failed to load services"
                    )
                }
            }

            // REMOVE getRecentBookings() from here - now using Flow

            _uiState.update { it.copy(isLoading = false) }
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


    fun refreshData(context: Context) {
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