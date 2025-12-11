package uk.ac.tees.mad.carease.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.carease.data.models.BookingPayload
import uk.ac.tees.mad.carease.data.models.ServiceSelection
import uk.ac.tees.mad.carease.viewmodels.CarDetailsViewModel


@Composable
fun CarDetailScreen(
    modifier: Modifier = Modifier,
    serviceSelection: ServiceSelection,
    onNavigateBack: () -> Unit,
    onProceedToBooking: (BookingPayload) -> Unit,
    viewModel: CarDetailsViewModel = hiltViewModel()
) {


}