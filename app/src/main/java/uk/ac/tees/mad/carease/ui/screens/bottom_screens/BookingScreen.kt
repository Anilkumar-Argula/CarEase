package uk.ac.tees.mad.carease.ui.screens.bottom_screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.carease.data.models.BookingPayload
import uk.ac.tees.mad.carease.data.models.ServiceSelection
import uk.ac.tees.mad.carease.data.models.VehicleData
import uk.ac.tees.mad.carease.viewmodels.BookingUiState
import uk.ac.tees.mad.carease.viewmodels.BookingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    modifier: Modifier = Modifier,
    bookingPayload: BookingPayload,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // When booking succeeds
    if (uiState.bookingCreated) {
        BookingSuccessDialog(
            bookingId = uiState.createdBookingId ?: "",
            onDismiss = {
                viewModel.resetBookingState()
                onBookingSuccess()
            }
        )
    }

    BookingScreenContent(
        uiState = uiState,
        bookingPayload = bookingPayload,
        datePickerState = datePickerState,
        showDatePicker = showDatePicker,
        onNavigateBack = onNavigateBack,
        onChooseDateClicked = { showDatePicker = true },
        onDateConfirm = {
            datePickerState.selectedDateMillis?.let(viewModel::selectDate)
            showDatePicker = false
        },
        onDateDismiss = { showDatePicker = false },
        onTimeSlotClick = {
            viewModel.selectTimeSlot(it, bookingPayload.serviceSelection.areaId)
        },
        onPaymentSelected = viewModel::selectPaymentMethod,
        onConfirmBooking = { viewModel.createBooking(bookingPayload) },
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreenContent(
    uiState: BookingUiState,
    bookingPayload: BookingPayload,
    datePickerState: DatePickerState,
    showDatePicker: Boolean,
    onNavigateBack: () -> Unit = {},
    onChooseDateClicked: () -> Unit = {},
    onDateConfirm: () -> Unit = {},
    onDateDismiss: () -> Unit = {},
    onTimeSlotClick: (String) -> Unit = {},
    onPaymentSelected: (String) -> Unit = {},
    onConfirmBooking: () -> Unit = {},
    onClearError: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Booking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3B82F6),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {

            Text(
                "Step 3 of 3: Confirm Booking",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(24.dp))

            BookingSummaryCard(bookingPayload)

            Spacer(Modifier.height(24.dp))

            // DATE PICKER BUTTON
            OutlinedButton(
                onClick = onChooseDateClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (uiState.selectedDate != null)
                        Color(0xFFDCF2FF)
                    else Color.White
                )
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF3B82F6))
                Spacer(Modifier.width(8.dp))
                Text(
                    uiState.selectedDate?.let { formatDate(it) } ?: "Choose a date",
                    color = Color(0xFF1E3A8A)
                )
            }

            // TIME SLOTS
            if (uiState.selectedDate != null) {
                Spacer(Modifier.height(24.dp))
                Text("Select Time Slot", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Spacer(Modifier.height(12.dp))

                uiState.availableTimeSlots.forEach { slot ->
                    TimeSlotCard(
                        timeSlot = slot,
                        isSelected = uiState.selectedTimeSlot == slot,
                        isChecking = uiState.isCheckingAvailability && uiState.selectedTimeSlot == slot,
                        isAvailable = if (uiState.selectedTimeSlot == slot)
                            uiState.isSlotAvailable
                        else true,
                        onClick = { onTimeSlotClick(slot) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // PAYMENT OPTIONS
            if (uiState.selectedTimeSlot != null && uiState.isSlotAvailable) {
                Spacer(Modifier.height(24.dp))
                Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Spacer(Modifier.height(12.dp))

                PaymentMethodCard(
                    method = "PAY_AT_SERVICE",
                    label = "Pay at Service",
                    description = "Pay when the service is completed",
                    icon = Icons.Default.Payment,
                    isSelected = uiState.paymentMethod == "PAY_AT_SERVICE",
                    onClick = { onPaymentSelected("PAY_AT_SERVICE") }
                )

                Spacer(Modifier.height(8.dp))

                PaymentMethodCard(
                    method = "PAY_IN_APP",
                    label = "Pay in App",
                    description = "Pay now using card or wallet",
                    icon = Icons.Default.CreditCard,
                    isSelected = uiState.paymentMethod == "PAY_IN_APP",
                    onClick = { onPaymentSelected("PAY_IN_APP") }
                )

                Spacer(Modifier.height(24.dp))

                // CONFIRM BOOKING
                Button(
                    onClick = onConfirmBooking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.canProceed && !uiState.isCreatingBooking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color(0xFFE5E7EB)
                    )
                ) {
                    if (uiState.isCreatingBooking) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Confirm Booking")
                    }
                }
            }

            // ERROR
            uiState.errorMessage?.let { msg ->
                Spacer(Modifier.height(16.dp))
                ErrorCard(message = msg, onClear = onClearError)
            }
        }
    }

    // DATE PICKER DIALOG
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = onDateDismiss,
            confirmButton = {
                TextButton(onClick = onDateConfirm) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = onDateDismiss) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


@Composable
fun ErrorCard(
    message: String,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEE2E2)
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
            Text(
                text = message,
                color = Color(0xFFDC2626),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClear) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFFDC2626)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {

    val fakeServiceSelection = ServiceSelection(
        serviceId = "1",
        serviceName = "Full Wash",
        serviceType = "WASH",
        basePrice = 20.0,
        duration = 45,
        areaId = "A1",
        areaName = "Central City",
        areaPriceMultiplier = 1.2,
        promoCode = "SAVE10",
        discount = 2.0
    )

    val fakeVehicle = VehicleData(
        make = "Toyota",
        model = "Corolla",
        registrationNumber = "AB12 CDE",
        color = "Silver",
        notes = "Please clean wheels properly",
        addonsPrice = 7.0,
        selectedAddons = listOf("Interior Vacuum")
    )

    val payload = BookingPayload(
        serviceSelection = fakeServiceSelection,
        vehicleData = fakeVehicle,
        totalPrice = fakeServiceSelection.estimatedPrice + fakeVehicle.addonsPrice
    )

    val previewState = BookingUiState(
        selectedDate = System.currentTimeMillis(),
        selectedTimeSlot = "10:00 AM",
        availableTimeSlots = listOf("9:00 AM", "10:00 AM", "11:00 AM"),
        isCheckingAvailability = false,
        isSlotAvailable = true,
        paymentMethod = "PAY_AT_SERVICE",
        userName = "John Doe",
        userEmail = "john@example.com",
        canProceed = true,
        isCreatingBooking = false,
        bookingCreated = false,
        createdBookingId = null
    )

    BookingScreenContent(
        uiState = previewState,
        bookingPayload = payload,
        datePickerState = rememberDatePickerState(),
        showDatePicker = false
    )
}



@Composable
fun BookingSummaryCard(bookingPayload: BookingPayload) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Booking Summary",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Service
            SummaryRow(
                icon = Icons.Default.Build,
                label = "Service",
                value = bookingPayload.serviceSelection.serviceName
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle
            SummaryRow(
                icon = Icons.Default.DirectionsCar,
                label = "Vehicle",
                value = "${bookingPayload.vehicleData.make} ${bookingPayload.vehicleData.model}"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Registration
            SummaryRow(
                icon = Icons.Default.Badge,
                label = "Registration",
                value = bookingPayload.vehicleData.registrationNumber
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Area
            SummaryRow(
                icon = Icons.Default.LocationOn,
                label = "Area",
                value = bookingPayload.serviceSelection.areaName
            )

            if (bookingPayload.vehicleData.selectedAddons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                SummaryRow(
                    icon = Icons.Default.AddCircle,
                    label = "Add-ons",
                    value = bookingPayload.vehicleData.selectedAddons.joinToString(", ")
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Total Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = "£${"%.2f".format(bookingPayload.totalPrice)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )
            }
        }
    }
}

@Composable
fun SummaryRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E3A8A)
            )
        }
    }
}

@Composable
fun TimeSlotCard(
    timeSlot: String,
    isSelected: Boolean,
    isChecking: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isAvailable && isSelected -> Color(0xFFFEE2E2)
                isSelected -> Color(0xFFDCF2FF)
                else -> Color.White
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = when {
                !isAvailable && isSelected -> Color(0xFFEF4444)
                isSelected -> Color(0xFF3B82F6)
                else -> Color(0xFFE5E7EB)
            }
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = if (isSelected && !isAvailable) Color(0xFFEF4444) else Color(0xFF3B82F6)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = timeSlot,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E3A8A)
                    )
                    if (isSelected && !isAvailable) {
                        Text(
                            text = "Slot full",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            when {
                isChecking -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                isSelected && isAvailable -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF10B981)
                )
                isSelected && !isAvailable -> Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Not available",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: String,
    label: String,
    description: String,
    icon: ImageVector,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = description,
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
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun BookingSuccessDialog(
    bookingId: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(64.dp)
            )
        },
        title = {
            Text(
                text = "Booking Confirmed!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
        },
        text = {
            Column {
                Text(
                    text = "Your booking has been successfully created.",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Booking ID: ${bookingId.take(8).uppercase()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF3B82F6)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You'll receive a confirmation email shortly.",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                )
            ) {
                Text("View My Bookings")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}
