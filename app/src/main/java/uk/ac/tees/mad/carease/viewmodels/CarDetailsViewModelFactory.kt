package uk.ac.tees.mad.carease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uk.ac.tees.mad.carease.data.repository.CarDetailsRepository

class CarDetailsViewModelFactory(
    private val repository: CarDetailsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarDetailsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
