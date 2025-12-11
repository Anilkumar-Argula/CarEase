package uk.ac.tees.mad.carease.ui.screens

import android.R.attr.enabled
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.ac.tees.mad.carease.data.models.Area
import uk.ac.tees.mad.carease.data.models.Service
import uk.ac.tees.mad.carease.data.models.ServiceSelection
import uk.ac.tees.mad.carease.viewmodels.SelectServiceUiState
import uk.ac.tees.mad.carease.viewmodels.SelectServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectServiceScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onProceedToCarDetails: (ServiceSelection) -> Unit,
    viewModel: SelectServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SelectServiceScreenContent(
        uiState = uiState,
        onSelectService = viewModel::selectService,
        onSelectArea = viewModel::selectArea,
        onPromoChange = viewModel::updatePromoCode,
        onValidatePromo = viewModel::validatePromoCode,
        onProceed = {
            viewModel.getServiceSelection()?.let { onProceedToCarDetails(it) }
        },
        onNavigateBack = onNavigateBack
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectServiceScreenContent(
    uiState: SelectServiceUiState,
    onSelectService: (Service) -> Unit = {},
    onSelectArea: (Area) -> Unit = {},
    onPromoChange: (String) -> Unit = {},
    onValidatePromo: () -> Unit = {},
    onProceed: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {

    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Service") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3B82F6),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Progress Indicator
            Text(
                text = "Step 1 of 3: Service & Area",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Service Type Selection
                Text(
                    text = "Choose Service Type",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                uiState.services.forEach { service ->
                    ServiceCard(
                        service = service,
                        isSelected = uiState.selectedService?.id == service.id,
                        onClick = { onSelectService(service) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Area Selection
                Text(
                    text = "Select Your Area",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Area Dropdown
                var expandedArea by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedArea,
                    onExpandedChange = { expandedArea = !expandedArea }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedArea?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Area") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (expandedArea) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedArea,
                        onDismissRequest = { expandedArea = false }
                    ) {
                        uiState.areas.forEach { area ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = area.name,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (area.pricingMultiplier != 1.0) {
                                            Text(
                                                text = "Price multiplier: ${area.pricingMultiplier}x",
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectArea(area)
                                    expandedArea = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Promo Code Section
                Text(
                    text = "Promo Code (Optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.promoCode,
                        onValueChange = { onPromoChange(it) },
                        label = { Text("Enter code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { onValidatePromo() },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply")
                    }
                }

                // Promo Message
                if (uiState.promoMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.promoMessage!!,
                        fontSize = 14.sp,
                        color = if (uiState.isPromoValid == true)
                            Color(0xFF10B981)
                        else
                            Color(0xFFEF4444)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Price Summary
                if (uiState.selectedService != null && uiState.selectedArea != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3F4F6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Price Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val basePrice = uiState.selectedService!!.price
                            val multiplier = uiState.selectedArea!!.pricingMultiplier
                            val adjustedPrice = basePrice * multiplier
                            val discount = uiState.discount
                            val finalPrice = adjustedPrice - discount

                            // Base Price
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Service:", color = Color(0xFF6B7280))
                                Text("£$basePrice", fontWeight = FontWeight.Medium)
                            }

                            // Area Adjustment
                            if (multiplier != 1.0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Area adjustment:", color = Color(0xFF6B7280))
                                    Text("×$multiplier", fontWeight = FontWeight.Medium)
                                }
                            }

                            // Discount
                            if (discount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Discount:", color = Color(0xFF10B981))
                                    Text(
                                        "-£$discount",
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            // Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Estimated Total:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    "£${"%.2f".format(finalPrice)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3B82F6)
                                )
                            }

                            Text(
                                text = "ETA: ${uiState.selectedService!!.duration} mins",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Proceed Button
                Button(
                    onClick = onProceed
//                    {
//                        val selection = viewModel.getServiceSelection()
//                        if (selection != null) {
//                            // Convert to JSON and pass
//                            // For now, just navigate with encoded data
//                            onProceedToCarDetails(selection)
//                        }
//                    }
                    ,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.canProceed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6),
                        disabledContainerColor = Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Proceed to Car Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }

                // Error Message
                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEE2E2)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Color(0xFFDC2626),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
fun SelectServiceScreenPreview() {

    val fakeServices = listOf(
        Service(id="1", name="Car Wash", description="Basic wash", price=10.0, duration=20, type="WASH"),
        Service(id="2", name="Full Service", description="Inside + outside", price=25.0, duration=45, type="SERVICE")
    )

    val fakeAreas = listOf(
        Area(id="A1", name="Central", pricingMultiplier = 1.0),
        Area(id="A2", name="Airport", pricingMultiplier = 1.3)
    )

    val previewState = SelectServiceUiState(
        isLoading = false,
        services = fakeServices,
        areas = fakeAreas,
        selectedService = fakeServices[0],
        selectedArea = fakeAreas[1],
        promoCode = "SAVE10",
        promoMessage = "10% discount applied",
        discount = 3.0,
        errorMessage = null,
        isPromoValid = true,
        canProceed = true
    )

    SelectServiceScreenContent(uiState = previewState)
}


@Composable
fun ServiceCard(
    service: Service,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFDCF2FF) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF3B82F6) else Color(0xFFE5E7EB)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Service Icon based on type
                val icon = when (service.type) {
                    "WASH" -> "💧"
                    "SERVICE" -> "🔧"
                    "BOTH" -> "💧🔧"
                    else -> "🚗"
                }

                Text(
                    text = "$icon ${service.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = service.description,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "£${service.price}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )
                    Text(
                        text = "${service.duration} mins",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}