package uk.ac.tees.mad.carease.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import uk.ac.tees.mad.carease.data.models.Addon
import uk.ac.tees.mad.carease.data.models.BookingPayload
import uk.ac.tees.mad.carease.data.models.ServiceSelection
import uk.ac.tees.mad.carease.viewmodels.CarDetailsUiState
import uk.ac.tees.mad.carease.viewmodels.CarDetailsViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    modifier: Modifier = Modifier,
    serviceSelection: ServiceSelection,
    onNavigateBack: () -> Unit,
    onProceedToBooking: (BookingPayload) -> Unit,
    viewModel: CarDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Initialize Cloudinary one time
    LaunchedEffect(Unit) {
        viewModel.initializeCloudinary(context)
    }

    // Photo picker state
    var showPhotoOptions by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            viewModel.setCarPhotoUri(tempImageUri)
            viewModel.uploadImageToCloudinary()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setCarPhotoUri(it)
            viewModel.uploadImageToCloudinary()
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Create temp file for camera
            val imageFile = File(context.cacheDir, "car_photo_${System.currentTimeMillis()}.jpg")
            tempImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )
            cameraLauncher.launch(tempImageUri!!)
        }
    }

    CarDetailScreenContent(
        uiState = uiState,
        serviceSelection = serviceSelection,
        onNavigateBack = onNavigateBack,
        onShowPhotoOptions = { showPhotoOptions = true },
        onCarMakeChange = viewModel::updateCarMake,
        onCarModelChange = viewModel::updateCarModel,
        onRegChange = viewModel::updateRegistrationNumber,
        onColorChange = viewModel::updateCarColor,
        onNotesChange = viewModel::updateCarNotes,
        onAddonToggle = viewModel::toggleAddon,
        clearError = viewModel::clearError,
        onProceed = {
            val vehicle = viewModel.getVehicleData()
            val payload = BookingPayload(
                serviceSelection = serviceSelection,
                vehicleData = vehicle,
                totalPrice = serviceSelection.estimatedPrice + vehicle.addonsPrice
            )
            onProceedToBooking(payload)
        }
    )
    // Photo Options Dialog
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Add Photo") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoOptions = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo")
                    }

                    TextButton(
                        onClick = {
                            showPhotoOptions = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreenContent(
    uiState: CarDetailsUiState,
    serviceSelection: ServiceSelection,
    onNavigateBack: () -> Unit = {},
    onShowPhotoOptions: () -> Unit = {},
    onCarMakeChange: (String) -> Unit = {},
    onCarModelChange: (String) -> Unit = {},
    onRegChange: (String) -> Unit = {},
    onColorChange: (String) -> Unit = {},
    onNotesChange: (String) -> Unit = {},
    onAddonToggle: (String) -> Unit = {},
    clearError:()->Unit={},
    onProceed: () -> Unit = {}
){
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Car Details") },
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
                text = "Step 2 of 3: Vehicle Details",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Service Summary Card
            ServiceSummaryCard(serviceSelection = serviceSelection)

            Spacer(modifier = Modifier.height(24.dp))

            // Photo Upload Section
            Text(
                text = "Vehicle Photo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onClick = { onShowPhotoOptions() },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3F4F6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uiState.isUploadingImage -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Uploading to Cloudinary...")
                            }
                        }

                        uiState.carPhotoUrl != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(uiState.carPhotoUrl),
                                contentDescription = "Car Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        uiState.carPhotoUri != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(uiState.carPhotoUri),
                                contentDescription = "Car Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF6B7280)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to add photo",
                                    color = Color(0xFF6B7280)
                                )
                                Text(
                                    text = "(Optional but recommended)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vehicle Details Form
            Text(
                text = "Vehicle Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Car Make
            OutlinedTextField(
                value = uiState.carMake,
                onValueChange = { onCarMakeChange(it) },
                label = { Text("Make *") },
                placeholder = { Text("e.g., Toyota") },
                leadingIcon = {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Car Model
            OutlinedTextField(
                value = uiState.carModel,
                onValueChange = {  onCarModelChange(it) },
                label = { Text("Model *") },
                placeholder = { Text("e.g., Camry") },
                leadingIcon = {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Registration Number
            OutlinedTextField(
                value = uiState.registrationNumber,
                onValueChange = {   onRegChange(it) },
                label = { Text("Registration Number *") },
                placeholder = { Text("e.g., AB12 CDE") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Car Color
            OutlinedTextField(
                value = uiState.carColor,
                onValueChange = {   onColorChange(it) },
                label = { Text("Color *") },
                placeholder = { Text("e.g., Silver") },
                leadingIcon = {
                    Icon(Icons.Default.ColorLens, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = uiState.carNotes,
                onValueChange = { onNotesChange(it) },
                label = { Text("Additional Notes") },
                placeholder = { Text("Any special instructions...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Add-ons Section
            Text(
                text = "Add-ons",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            uiState.availableAddons.forEach { addon ->
                AddonCheckboxItem(
                    addon = addon,
                    onToggle = {  onAddonToggle(addon.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price Summary
            PriceSummaryCard(
                serviceSelection = serviceSelection,
                addonsPrice = uiState.availableAddons.filter { it.isSelected }.sumOf { it.price }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Proceed Button
            Button(
                onClick = {
                    onProceed()
//                    val vehicleData = viewModel.getVehicleData()
//                    val bookingPayload = BookingPayload(
//                        serviceSelection = serviceSelection,
//                        vehicleData = vehicleData,
//                        totalPrice = serviceSelection.estimatedPrice + vehicleData.addonsPrice
//                    )
//                    onProceedToBooking(bookingPayload)
                },
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
                    text = "Continue to Booking",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Color(0xFFDC2626),
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { clearError() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
fun CarDetailsPreview() {

    val serviceSelection = ServiceSelection(
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

    val previewUiState = CarDetailsUiState(
        carMake = "Toyota",
        carModel = "Corolla",
        registrationNumber = "AB12 CDE",
        carColor = "Silver",
        carNotes = "Please focus on the wheels.",
        carPhotoUri = null,
        carPhotoUrl = null,
        isUploadingImage = false,
        availableAddons = listOf(
            Addon(
                id = "1",
                name = "Wax Polish",
                description = "Glossy shine polish",
                price = 5.0,
                isSelected = false
            ),
            Addon(
                id = "2",
                name = "Interior Vacuum",
                description = "Deep interior cleaning",
                price = 7.0,
                isSelected = true
            )
        ),
        canProceed = true,
        errorMessage = null
    )

    CarDetailScreenContent(
        uiState = previewUiState,
        serviceSelection = serviceSelection
    )
}



@Composable
fun ServiceSummaryCard(serviceSelection: ServiceSelection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDCF2FF)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = serviceSelection.serviceName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = serviceSelection.areaName,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                Text(
                    text = "£${"%.2f".format(serviceSelection.estimatedPrice)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )
            }
        }
    }
}

@Composable
fun AddonCheckboxItem(
    addon: uk.ac.tees.mad.carease.data.models.Addon,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (addon.isSelected) Color(0xFFDCF2FF) else Color.White
        ),
        border = BorderStroke(
            width = if (addon.isSelected) 2.dp else 1.dp,
            color = if (addon.isSelected) Color(0xFF3B82F6) else Color(0xFFE5E7EB)
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
                Text(
                    text = addon.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = addon.description,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "+£${addon.price}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )
                Checkbox(
                    checked = addon.isSelected,
                    onCheckedChange = null
                )
            }
        }
    }
}

@Composable
fun PriceSummaryCard(
    serviceSelection: ServiceSelection,
    addonsPrice: Double
) {
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
                text = "Price Breakdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Service:", color = Color(0xFF6B7280))
                Text(
                    "£${"%.2f".format(serviceSelection.estimatedPrice)}",
                    fontWeight = FontWeight.Medium
                )
            }

            if (addonsPrice > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Add-ons:", color = Color(0xFF6B7280))
                    Text("+£${"%.2f".format(addonsPrice)}", fontWeight = FontWeight.Medium)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    "£${"%.2f".format(serviceSelection.estimatedPrice + addonsPrice)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )
            }
        }
    }
}