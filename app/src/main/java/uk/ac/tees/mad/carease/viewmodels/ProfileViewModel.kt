package uk.ac.tees.mad.carease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.carease.data.local.BookingDao
import uk.ac.tees.mad.carease.data.models.Booking
import uk.ac.tees.mad.carease.data.models.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val upcomingBookings: List<Booking> = emptyList(),
    val pastBookings: List<Booking> = emptyList(),
    val errorMessage: String? = null,
    val isEditMode: Boolean = false,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val bookingDao: BookingDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
//        loadUserBookings()
        observeBookings()
    }

    private fun observeBookings() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Observe upcoming bookings
            launch {
                bookingDao.getUpcomingBookings(userId)
                    .catch { /* ignore */ }
                    .collect { bookings ->
                        _uiState.update { it.copy(upcomingBookings = bookings) }
                    }
            }

            // Observe past bookings
            launch {
                bookingDao.getPastBookings(userId)
                    .catch { /* ignore */ }
                    .collect { bookings ->
                        _uiState.update { it.copy(pastBookings = bookings) }
                    }
            }

            // Refresh from Firebase in background
            refreshBookingsFromFirebase()
        }
    }

    private suspend fun refreshBookingsFromFirebase() {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return

            val snapshot = firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val bookings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)?.copy(bookingId = doc.id)
            }

            bookingDao.insertAll(bookings)
        } catch (e: Exception) {
            // Ignore - cache will show old data
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                    return@launch
                }

                val snapshot = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val profile = snapshot.toObject(UserProfile::class.java)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userProfile = profile
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load profile: ${e.message}"
                )
            }
        }
    }

//    fun loadUserBookings() {
//        viewModelScope.launch {
//            try {
//                val userId = firebaseAuth.currentUser?.uid ?: return@launch
//
//                val snapshot = firestore.collection("bookings")
//                    .whereEqualTo("userId", userId)
////                    .orderBy("scheduledDate", Query.Direction.DESCENDING)
//                    .get()
//                    .await()
//
//                val allBookings = snapshot.documents.mapNotNull { doc ->
//                    doc.toObject(Booking::class.java)?.copy(bookingId = doc.id)
//                }
//
//                val upcoming = allBookings.filter {
//                    it.status in listOf("PENDING", "CONFIRMED", "IN_PROGRESS")
//                }
//                val past = allBookings.filter {
//                    it.status in listOf("COMPLETED", "CANCELLED")
//                }
//
//                _uiState.value = _uiState.value.copy(
//                    upcomingBookings = upcoming,
//                    pastBookings = past
//                )
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    errorMessage = "Failed to load bookings: ${e.message}"
//                )
//            }
//        }
//    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode,
            errorMessage = null,
            successMessage = null
        )
    }

    fun updateProfile(
        fullName: String,
        phone: String,
        defaultVehicle: String,
        preferredArea: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                    return@launch
                }

                val updates = hashMapOf<String, Any>(
                    "fullName" to fullName,
                    "phone" to phone,
                    "defaultVehicle" to defaultVehicle,
                    "preferredArea" to preferredArea
                )

                firestore.collection("users")
                    .document(userId)
                    .update(updates)
                    .await()

                // Reload profile
                loadUserProfile()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEditMode = false,
                    successMessage = "Profile updated successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to update profile: ${e.message}"
                )
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                // Update Firebase
                firestore.collection("bookings")
                    .document(bookingId)
                    .update("status", "CANCELLED")
                    .await()

                // Update cache
                bookingDao.updateStatus(bookingId, "CANCELLED")

                _uiState.update {
                    it.copy(
                        successMessage = "Booking cancelled successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to cancel booking: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
//                firebaseAuth.signOut()
                _uiState.value = ProfileUiState() // Reset state
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Logout failed: ${e.message}"
                )
            }
        }
    }
}