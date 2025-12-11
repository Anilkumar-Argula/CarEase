package uk.ac.tees.mad.carease.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.carease.data.models.ServiceSelection
import uk.ac.tees.mad.carease.viewmodels.SelectServiceViewModel

@Composable
fun SelectServiceScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onProceedToCarDetails: (ServiceSelection) -> Unit,
    viewModel: SelectServiceViewModel = hiltViewModel<SelectServiceViewModel>()
) {

}