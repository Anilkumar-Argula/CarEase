package uk.ac.tees.mad.carease.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import uk.ac.tees.mad.carease.data.repository.SelectServiceRepository


@HiltViewModel
class SelectServiceViewModel  @Inject constructor(
    private val repository: SelectServiceRepository
): ViewModel(){

}