package uk.ac.tees.mad.carease.ui.screens.bottom_screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import uk.ac.tees.mad.carease.data.models.Booking
import uk.ac.tees.mad.carease.data.models.Service
import uk.ac.tees.mad.carease.data.models.UserProfile
import uk.ac.tees.mad.carease.data.models.Weather
import uk.ac.tees.mad.carease.viewmodels.HomeUiState
import uk.ac.tees.mad.carease.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToServiceScreen: () -> Unit,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // Permission granted, load weather
            viewModel.loadWeatherByLocation(context)
        }
    }

    // Check network status
    LaunchedEffect(Unit) {
//        viewModel.checkNetworkStatus(context)
        viewModel.observeNetwork(context = context)
        // Check permissions first
        val hasLocationPermission = context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || context.checkSelfPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            viewModel.loadWeatherByLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

//    // Show permission dialog if needed
//    if (uiState.locationPermissionNeeded) {
//        AlertDialog(
//            onDismissRequest = { },
//            title = { Text("Location Permission") },
//            text = {
//                Text("CarEase needs your location to show local weather and recommend the best time for a car wash.")
//            },
//            confirmButton = {
//                Button(
//                    onClick = {
//                        locationPermissionLauncher.launch(
//                            arrayOf(
//                                Manifest.permission.ACCESS_FINE_LOCATION,
//                                Manifest.permission.ACCESS_COARSE_LOCATION
//                            )
//                        )
//                    }
//                ) {
//                    Text("Grant Permission")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { /* Use fallback weather */ }) {
//                    Text("Skip")
//                }
//            }
//        )
//    }

    HomeScreenContent(
        modifier = modifier,
        isLoading = uiState.isLoading,
        isOnline = uiState.isOnline,
        userProfile = uiState.userProfile,
        weather = uiState.weather,
        suggestedService = uiState.suggestedService,
        recentBookings = uiState.recentBookings,
        errorMessage = uiState.errorMessage,
        onNavigateToService = navigateToServiceScreen,
        onRefresh = { viewModel.refreshData(context) }
    )

}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isOnline: Boolean = true,
    userProfile: UserProfile? = null,
    weather: Weather? = null,
    suggestedService: Service? = null,
    recentBookings: List<Booking> = emptyList(),
    errorMessage: String? = null,
    onNavigateToService: () -> Unit = {},
    onRefresh: () -> Unit = {}
){

    val scrollState=rememberScrollState()
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(scrollState)
        ) {
            // Header with gradient
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
                Column {
                    // Offline Banner
                    if (!isOnline) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEF3C7)
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
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = "Offline",
                                    tint = Color(0xFFD97706)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "You're offline. Showing cached data.",
                                    color = Color(0xFF92400E),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Welcome Text
                    Text(
                        text = "Welcome${if (userProfile?.fullName?.isNotBlank() == true) ", ${userProfile.fullName}" else ""}!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Book your car service today",
                        fontSize = 16.sp,
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
                    // Weather Tile
                    if (weather != null) {
                        WeatherCard(weather = weather)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Suggested Service Card
                    if (suggestedService != null) {
                        SuggestedServiceCard(
                            service = suggestedService,
                            navigateToServiceScreen = onNavigateToService
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }


                    // Book a Service Button (Primary CTA)
                    Button(
                        onClick = {
                            onNavigateToService()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Book a Service",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

//                    // Quick Actions
//                    Text(
//                        text = "Quick Actions",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF1E3A8A)
//                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // recent booking section
                    Text(
                        text = "Recent Bookings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    if (recentBookings.isNotEmpty()) {

                        Spacer(modifier = Modifier.height(12.dp))

                        recentBookings.forEach { booking ->
                            RecentBookingCard(booking = booking)
//                            BookingCard(booking)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        // Add this empty state
                        Spacer(modifier = Modifier.height(12.dp))
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
                                    text = "No bookings yet. Book your first service!",
                                    color = Color(0xFF6B7280),
                                    fontSize = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))


//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        // Refresh Button
//                        OutlinedButton(
//                            onClick = { onRefresh() },
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(48.dp),
//                            shape = RoundedCornerShape(12.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Refresh,
//                                contentDescription = "Refresh",
//                                modifier = Modifier.size(20.dp)
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text("Refresh")
//                        }
//                    }

                    // Error Message
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                }
            }
        }
    }

}

@Composable
fun RecentBookingCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                    text = booking.timeSlot,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // Status Badge
            val statusColor = when (booking.status) {
                "CONFIRMED" -> Color(0xFF10B981)
                "PENDING" -> Color(0xFFF59E0B)
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
    }
}

@Composable
fun WeatherCard(weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDCFCE7)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Weather",
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = weather.condition,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                    if (weather.bestTimeForWash.isNotBlank()) {
                        Text(
                            text = "Best: ${weather.bestTimeForWash}",
                            fontSize = 12.sp,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }

            // Right Side - Temperature
            Text(
                text = "${weather.temperature.toInt()}°C",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF065F46)
            )
        }
    }
}

@Composable
fun SuggestedServiceCard(
    service: Service,
    navigateToServiceScreen: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🚗 Suggested Service",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = service.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = service.description,
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "£${service.price}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )
                    Text(
                        text = "${service.duration} mins",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Button(
                    onClick = {
                        navigateToServiceScreen()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Book Now")
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

@Preview(showBackground = true,)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(
            isLoading = false,
            isOnline = true,
            userProfile = UserProfile(
                uid = "1",
                fullName = "John Doe",
                email = "john@example.com"
            ),
            weather = Weather(
                areaId = "area1",
                temperature = 22.0,
                condition = "Sunny",
                bestTimeForWash = "Morning (8-11 AM)",
                worstTimeForWash = "Evening (5-7 PM)",
                lastUpdated = Timestamp.now()
            ),
            suggestedService = Service(
                id = "1",
                name = "Premium Wash & Wax",
                description = "Deep clean with protective wax coating",
                price = 45.0,
                duration = 60
            ),
            recentBookings = listOf(
                Booking(
                    bookingId = "1",
                    userId = "user1",
                    serviceName = "Basic Wash",
                    serviceType = "wash",
                    scheduledDate = Timestamp.now(),
                    timeSlot = "Today, 2:00 PM",
                    totalPrice = 25.0,
                    status = "CONFIRMED",
                    carMake = "Toyota",
                    carModel = "Camry",
                    areaName = "Downtown",
                    createdAt = Timestamp.now()
                ),
                Booking(
                    bookingId = "2",
                    userId = "user1",
                    serviceName = "Interior Detailing",
                    serviceType = "detailing",
                    scheduledDate = Timestamp.now(),
                    timeSlot = "Tomorrow, 10:00 AM",
                    totalPrice = 65.0,
                    status = "PENDING",
                    carMake = "Honda",
                    carModel = "Civic",
                    areaName = "City Center",
                    createdAt = Timestamp.now()
                )
            )
        )
    }
}