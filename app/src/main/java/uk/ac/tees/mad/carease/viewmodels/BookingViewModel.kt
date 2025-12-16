package uk.ac.tees.mad.carease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.carease.data.repository.BookingRepository
import java.util.*
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val selectedDate: Long? = null,
    val selectedTimeSlot: String? = null,
    val availableTimeSlots: List<String> = generateTimeSlots(),
    val paymentMethod: String = "PAY_AT_SERVICE", // Default
    val isCheckingAvailability: Boolean = false,
    val isSlotAvailable: Boolean = true,
    val isCreatingBooking: Boolean = false,
    val bookingCreated: Boolean = false,
    val createdBookingId: String? = null,
    val errorMessage: String? = null,
    val canProceed: Boolean = false
)

fun generateTimeSlots(): List<String> {
    return listOf(
        "09:00 - 11:00",
        "11:00 - 13:00",
        "13:00 - 15:00",
        "15:00 - 17:00",
        "17:00 - 19:00"
    )
}

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getUserProfile().onSuccess { (name, email) ->
                _uiState.value = _uiState.value.copy(
                    userName = name,
                    userEmail = email,
                    isLoading = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                )
            }
        }
    }

    fun selectDate(dateInMillis: Long) {
        _uiState.value = _uiState.value.copy(
            selectedDate = dateInMillis,
            selectedTimeSlot = null, // Reset time slot when date changes
            canProceed = false
        )
    }

    fun selectTimeSlot(timeSlot: String, areaId: String) {
        val date = _uiState.value.selectedDate ?: return

        _uiState.value = _uiState.value.copy(
            selectedTimeSlot = timeSlot,
            isCheckingAvailability = true
        )

        viewModelScope.launch {
            repository.checkTimeSlotAvailability(areaId, date, timeSlot)
                .onSuccess { isAvailable ->
                    _uiState.value = _uiState.value.copy(
                        isSlotAvailable = isAvailable,
                        isCheckingAvailability = false,
                        canProceed = isAvailable
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isCheckingAvailability = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun selectPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun createBooking(
        bookingPayload: uk.ac.tees.mad.carease.data.models.BookingPayload
    ) {
        val date = _uiState.value.selectedDate
        val timeSlot = _uiState.value.selectedTimeSlot

        if (date == null || timeSlot == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select date and time"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreatingBooking = true,
                errorMessage = null
            )

            repository.createBooking(
                bookingPayload = bookingPayload,
                scheduledDate = date,
                timeSlot = timeSlot,
                paymentMethod = _uiState.value.paymentMethod
            ).onSuccess { bookingId ->
                _uiState.value = _uiState.value.copy(
                    isCreatingBooking = false,
                    bookingCreated = true,
                    createdBookingId = bookingId
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isCreatingBooking = false,
                    errorMessage = error.message ?: "Failed to create booking"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetBookingState() {
        _uiState.value = BookingUiState()
        loadUserProfile()
    }
}