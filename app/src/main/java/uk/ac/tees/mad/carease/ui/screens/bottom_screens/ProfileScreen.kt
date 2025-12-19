package uk.ac.tees.mad.carease.ui.screens.bottom_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import uk.ac.tees.mad.carease.data.models.Booking
import uk.ac.tees.mad.carease.data.models.UserProfile
import uk.ac.tees.mad.carease.viewmodels.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

// Stateful wrapper
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToBookingDetails: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Show success/error messages
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    ProfileScreenContent(
        modifier = modifier,
        isLoading = uiState.isLoading,
        userProfile = uiState.userProfile,
        upcomingBookings = uiState.upcomingBookings,
        pastBookings = uiState.pastBookings,
        isEditMode = uiState.isEditMode,
        errorMessage = uiState.errorMessage,
        successMessage = uiState.successMessage,
        onToggleEditMode = { viewModel.toggleEditMode() },
        onUpdateProfile = { name, phone, vehicle, area ->
            viewModel.updateProfile(name, phone, vehicle, area)
        },
        onCancelBooking = { viewModel.cancelBooking(it) },
        onLogout = { viewModel.logout(onNavigateToLogin) },
        onRefresh = {
            viewModel.loadUserProfile()
//            viewModel.loadUserBookings()
//            viewModel.refre
        },
        onNavigateToBookingDetails = onNavigateToBookingDetails
    )
}

// Stateless content
@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    userProfile: UserProfile? = null,
    upcomingBookings: List<Booking> = emptyList(),
    pastBookings: List<Booking> = emptyList(),
    isEditMode: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onToggleEditMode: () -> Unit = {},
    onUpdateProfile: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onCancelBooking: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onNavigateToBookingDetails: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf<String?>(null) }

    // Edit mode states
    var editFullName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editDefaultVehicle by remember { mutableStateOf("") }
    var editPreferredArea by remember { mutableStateOf("") }

    // Update edit fields when profile loads or edit mode toggles
    LaunchedEffect(userProfile, isEditMode) {
        if (isEditMode && userProfile != null) {
            editFullName = userProfile.fullName
            editPhone = userProfile.phone
            editDefaultVehicle = userProfile.defaultVehicle
            editPreferredArea = userProfile.preferredArea
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(scrollState)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E3A8A),
                                Color(0xFF3B82F6)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Picture Placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile?.fullName ?: "Loading...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = userProfile?.email ?: "",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Success/Error Messages
                if (successMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFDCFCE7)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF059669)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = successMessage,
                                color = Color(0xFF065F46),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEE2E2)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFDC2626),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Loading State
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Profile Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Profile Information",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )

                                IconButton(onClick = onToggleEditMode) {
                                    Icon(
                                        imageVector = if (isEditMode) Icons.Default.Close else Icons.Default.Edit,
                                        contentDescription = if (isEditMode) "Cancel" else "Edit",
                                        tint = Color(0xFF3B82F6)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isEditMode) {
                                // Edit Mode
                                OutlinedTextField(
                                    value = editFullName,
                                    onValueChange = { editFullName = it },
                                    label = { Text("Full Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = editPhone,
                                    onValueChange = { editPhone = it },
                                    label = { Text("Phone") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = editDefaultVehicle,
                                    onValueChange = { editDefaultVehicle = it },
                                    label = { Text("Default Vehicle") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = editPreferredArea,
                                    onValueChange = { editPreferredArea = it },
                                    label = { Text("Preferred Area") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        onUpdateProfile(
                                            editFullName,
                                            editPhone,
                                            editDefaultVehicle,
                                            editPreferredArea
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3B82F6)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Save Changes")
                                }
                            } else {
                                // View Mode
                                ProfileInfoRow(
                                    icon = Icons.Default.Person,
                                    label = "Full Name",
                                    value = userProfile?.fullName ?: "N/A"
                                )

                                ProfileInfoRow(
                                    icon = Icons.Default.Phone,
                                    label = "Phone",
                                    value = userProfile?.phone ?: "N/A"
                                )

                                ProfileInfoRow(
                                    icon = Icons.Default.DirectionsCar,
                                    label = "Default Vehicle",
                                    value = userProfile?.defaultVehicle?.ifBlank { "Not set" } ?: "N/A"
                                )

                                ProfileInfoRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Preferred Area",
                                    value = userProfile?.preferredArea?.ifBlank { "Not set" } ?: "N/A"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Upcoming Bookings Section
                    Text(
                        text = "Upcoming Bookings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (upcomingBookings.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3F4F6)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No upcoming bookings",
                                    color = Color(0xFF6B7280),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        upcomingBookings.forEach { booking ->
                            BookingCard(
                                booking = booking,
                                onCancel = { showCancelDialog = booking.bookingId },
                                onClick = { onNavigateToBookingDetails(booking.bookingId) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Past Bookings Section
                    Text(
                        text = "Past Bookings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (pastBookings.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3F4F6)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No past bookings",
                                    color = Color(0xFF6B7280),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        pastBookings.forEach { booking ->
                            BookingCard(
                                booking = booking,
                                showCancelButton = false,
                                onClick = { onNavigateToBookingDetails(booking.bookingId) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Logout Button
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFDC2626)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Logout", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Cancel Booking Dialog
    showCancelDialog?.let { bookingId ->
        AlertDialog(
            onDismissRequest = { showCancelDialog = null },
            title = { Text("Cancel Booking") },
            text = { Text("Are you sure you want to cancel this booking?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelBooking(bookingId)
                        showCancelDialog = null
                    }
                ) {
                    Text("Cancel Booking", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = null }) {
                    Text("Keep Booking")
                }
            }
        )
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
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
                color = Color(0xFF1E3A8A),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    showCancelButton: Boolean = true,
    onCancel: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val formattedDate = booking.scheduledDate?.let { formatTimestamp(it) } ?: "N/A"
    Card(
        modifier = Modifier
            .fillMaxWidth()
//            .clickable { onClick() },
        ,colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.serviceName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${booking.carMake} ${booking.carModel}",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = booking.timeSlot,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    if (booking.areaName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = booking.areaName,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }

                // Status Badge
                val statusColor = when (booking.status) {
                    "CONFIRMED" -> Color(0xFF10B981)
                    "PENDING" -> Color(0xFFF59E0B)
                    "IN_PROGRESS" -> Color(0xFF3B82F6)
                    "COMPLETED" -> Color(0xFF6B7280)
                    else -> Color(0xFFEF4444)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = booking.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (showCancelButton && booking.status in listOf("PENDING", "CONFIRMED")) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFDC2626)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel Booking", fontSize = 14.sp)
                }
            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreenContent(
            userProfile = UserProfile(
                uid = "123",
                fullName = "John Doe",
                email = "john@example.com",
                phone = "+44 7700 900000",
                defaultVehicle = "Toyota Camry 2020",
                preferredArea = "Downtown",
                notificationsEnabled = true,
                createdAt = Timestamp.now()
            ),
            upcomingBookings = listOf(
                Booking(
                    bookingId = "1",
                    serviceName = "Premium Wash",
                    carMake = "Toyota",
                    carModel = "Camry",
                    timeSlot = "Tomorrow, 10:00 AM",
                    status = "CONFIRMED",
                    areaName = "Downtown"
                )
            ),
            pastBookings = listOf(
                Booking(
                    bookingId = "2",
                    serviceName = "Basic Wash",
                    carMake = "Honda",
                    carModel = "Civic",
                    timeSlot = "Dec 1, 2:00 PM",
                    status = "COMPLETED",
                    areaName = "City Center"
                )
            )
        )
    }
}