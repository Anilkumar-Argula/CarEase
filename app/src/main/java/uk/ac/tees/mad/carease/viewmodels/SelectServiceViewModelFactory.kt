package uk.ac.tees.mad.carease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uk.ac.tees.mad.carease.data.repository.SelectServiceRepository

class SelectServiceViewModelFactory(
    private val repository: SelectServiceRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectServiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SelectServiceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
