package uk.ac.tees.mad.carease.ui.screens.bottom_screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.carease.data.models.BookingPayload
import uk.ac.tees.mad.carease.viewmodels.BookingViewModel

@Composable
fun BookingScreen(
    modifier: Modifier = Modifier,
    bookingPayload: BookingPayload,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {

}