package uk.ac.tees.mad.carease.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import uk.ac.tees.mad.carease.data.repository.CarDetailsRepository
import javax.inject.Inject

@HiltViewModel
class CarDetailsViewModel @Inject constructor(
    private val repository: CarDetailsRepository
) : ViewModel() {

}