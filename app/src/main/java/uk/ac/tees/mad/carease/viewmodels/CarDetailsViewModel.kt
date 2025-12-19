package uk.ac.tees.mad.carease.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.carease.data.models.Addon
import uk.ac.tees.mad.carease.data.models.VehicleData
import uk.ac.tees.mad.carease.data.repository.CarDetailsRepository

data class CarDetailsUiState(
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val carMake: String = "",
    val carModel: String = "",
    val registrationNumber: String = "",
    val carColor: String = "",
    val carNotes: String = "",
    val carPhotoUri: Uri? = null,
    val carPhotoUrl: String? = null,
    val availableAddons: List<Addon> = Addon.getDefaultAddons(),
    val errorMessage: String? = null,
    val canProceed: Boolean = false
)

class CarDetailsViewModel(
    private val repository: CarDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarDetailsUiState())
    val uiState: StateFlow<CarDetailsUiState> = _uiState.asStateFlow()

    fun initializeCloudinary(context: Context) {
        repository.initializeCloudinary(context)
    }

    fun updateCarMake(make: String) {
        _uiState.value = _uiState.value.copy(
            carMake = make,
            canProceed = checkCanProceed()
        )
    }

    fun updateCarModel(model: String) {
        _uiState.value = _uiState.value.copy(
            carModel = model,
            canProceed = checkCanProceed()
        )
    }

    fun updateRegistrationNumber(regNumber: String) {
        _uiState.value = _uiState.value.copy(
            registrationNumber = regNumber,
            canProceed = checkCanProceed()
        )
    }

    fun updateCarColor(color: String) {
        _uiState.value = _uiState.value.copy(
            carColor = color,
            canProceed = checkCanProceed()
        )
    }

    fun updateCarNotes(notes: String) {
        _uiState.value = _uiState.value.copy(carNotes = notes)
    }

    fun setCarPhotoUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(carPhotoUri = uri)
    }

    fun toggleAddon(addonId: String) {
        val updatedAddons = _uiState.value.availableAddons.map { addon ->
            if (addon.id == addonId) {
                addon.copy(isSelected = !addon.isSelected)
            } else {
                addon
            }
        }
        _uiState.value = _uiState.value.copy(availableAddons = updatedAddons)
    }

    fun uploadImageToCloudinary() {
        val uri = _uiState.value.carPhotoUri ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploadingImage = true,
                errorMessage = null
            )

            repository.uploadImageToCloudinary(uri).onSuccess { url ->
                _uiState.value = _uiState.value.copy(
                    carPhotoUrl = url,
                    isUploadingImage = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isUploadingImage = false,
                    errorMessage = "Failed to upload image: ${error.message}"
                )
            }
        }
    }

    private fun checkCanProceed(): Boolean {
        val state = _uiState.value
        return state.carMake.isNotBlank() &&
                state.carModel.isNotBlank() &&
                state.registrationNumber.isNotBlank() &&
                state.carColor.isNotBlank()
    }

    fun getVehicleData(): VehicleData {
        val state = _uiState.value
        val selectedAddons = state.availableAddons.filter { it.isSelected }

        return VehicleData(
            make = state.carMake.trim(),
            model = state.carModel.trim(),
            registrationNumber = state.registrationNumber.trim().uppercase(),
            color = state.carColor.trim(),
            photoUrl = state.carPhotoUrl,
            notes = state.carNotes.trim(),
            selectedAddons = selectedAddons.map { it.name },
            addonsPrice = selectedAddons.sumOf { it.price }
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}