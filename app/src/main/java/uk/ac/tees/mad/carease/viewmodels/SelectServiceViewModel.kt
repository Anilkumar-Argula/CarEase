package uk.ac.tees.mad.carease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.carease.data.models.Area
import uk.ac.tees.mad.carease.data.models.Service
import uk.ac.tees.mad.carease.data.models.ServiceSelection
import uk.ac.tees.mad.carease.data.repository.SelectServiceRepository


data class SelectServiceUiState(
    val isLoading: Boolean = true,
    val services: List<Service> = emptyList(),
    val areas: List<Area> = emptyList(),
    val selectedService: Service? = null,
    val selectedArea: Area? = null,
    val promoCode: String = "",
    val discount: Double = 0.0,
    val isPromoValid: Boolean? = null,
    val promoMessage: String? = null,
    val errorMessage: String? = null,
    val canProceed: Boolean = false
)


class SelectServiceViewModel(
    private val repository: SelectServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectServiceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }


    // to load the setvceis areas
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Fetch services
            repository.getServices().onSuccess { services ->
                _uiState.value = _uiState.value.copy(services = services)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to load services"
                )
            }

            // Fetch areas
            repository.getAreas().onSuccess { areas ->
                _uiState.value = _uiState.value.copy(areas = areas)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to load areas"
                )
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }


    // update the select service
    fun selectService(service: Service) {
        _uiState.value = _uiState.value.copy(
            selectedService = service,
            canProceed = checkCanProceed(service, _uiState.value.selectedArea)
        )
    }

    // select area
    fun selectArea(area: Area) {
        _uiState.value = _uiState.value.copy(
            selectedArea = area,
            canProceed = checkCanProceed(_uiState.value.selectedService, area)
        )
    }

    fun updatePromoCode(code: String) {
        _uiState.value = _uiState.value.copy(
            promoCode = code,
            isPromoValid = null,
            promoMessage = null
        )
    }

    fun validatePromoCode() {
        val code = _uiState.value.promoCode.trim()
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isPromoValid = null,
                promoMessage = null,
                discount = 0.0
            )
            return
        }

        viewModelScope.launch {
            repository.validatePromoCode(code).onSuccess { discount ->
                _uiState.value = _uiState.value.copy(
                    discount = discount,
                    isPromoValid = true,
                    promoMessage = "Promo applied! £$discount off"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    discount = 0.0,
                    isPromoValid = false,
                    promoMessage = error.message ?: "Invalid promo code"
                )
            }
        }
    }

    fun clearPromoCode() {
        _uiState.value = _uiState.value.copy(
            promoCode = "",
            discount = 0.0,
            isPromoValid = null,
            promoMessage = null
        )
    }

    private fun checkCanProceed(service: Service?, area: Area?): Boolean {
        return service != null && area != null
    }


    fun getServiceSelection(): ServiceSelection? {
        val service = _uiState.value.selectedService ?: return null
        val area = _uiState.value.selectedArea ?: return null

        return ServiceSelection(
            serviceId = service.id,
            serviceName = service.name,
            serviceType = service.type,
            basePrice = service.price,
            duration = service.duration,
            areaId = area.id,
            areaName = area.name,
            areaPriceMultiplier = area.pricingMultiplier,
            promoCode = if (_uiState.value.isPromoValid == true) _uiState.value.promoCode else null,
            discount = _uiState.value.discount
        )
    }

    fun refresh() {
        loadData()
    }
}