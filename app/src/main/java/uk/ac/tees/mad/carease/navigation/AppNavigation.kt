package uk.ac.tees.mad.carease.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import uk.ac.tees.mad.carease.data.models.BookingPayload
import uk.ac.tees.mad.carease.data.models.ServiceSelection
import uk.ac.tees.mad.carease.ui.screens.*
import uk.ac.tees.mad.carease.ui.screens.bottom_screens.BookingScreen
import uk.ac.tees.mad.carease.viewmodels.AuthViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {

    val navController = rememberNavController()
    val authViewModel = hiltViewModel<AuthViewModel>()

    val gson = remember { Gson() }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                navigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(
            route = Screen.SignUp.route
        ) {
            SignUpScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(
            route = Screen.Main.route
        ) {
            MainScreen(
                logout = {
                    authViewModel.logOut {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                },
                navigateToServiceScreen = {
                    navController.navigate(Screen.SelectService.route)
                }
            )
        }

        composable(
            route = Screen.SelectService.route
        ) {
            SelectServiceScreen(
                onProceedToCarDetails = { serviceSelection ->
                    // Serialize to JSON and encode for URL safety
                    val json = gson.toJson(serviceSelection)
                    val encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8.toString())
                    navController.navigate(Screen.CarDetail.createRoute(encodedJson))
//                    navController.navigate(Screen.CarDetail.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


        // We have to implement the navigation nav arguments
        composable(
            route = Screen.CarDetail.route,
            arguments = listOf(
                navArgument("serviceSelection") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val encodedJson = backStackEntry.arguments?.getString("serviceSelection")
            val json = URLDecoder.decode(encodedJson, StandardCharsets.UTF_8.toString())
            val serviceSelection = gson.fromJson(json, ServiceSelection::class.java)

            CarDetailScreen(
                serviceSelection = serviceSelection,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProceedToBooking = { bookingPayload ->
                    // Similar encoding for next screen
                    val bookingJson = gson.toJson(bookingPayload)
                    val encodedBookingJson =
                        URLEncoder.encode(bookingJson, StandardCharsets.UTF_8.toString())
                    navController.navigate(Screen.Booking.createRoute(encodedBookingJson))
                }
            )
        }



        composable(
            route = Screen.Booking.route,
            arguments = listOf(
                navArgument("bookingPayload") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val encodedJson = backStackEntry.arguments?.getString("bookingPayload")
            val json = URLDecoder.decode(encodedJson, StandardCharsets.UTF_8.toString())
            val bookingPayload = gson.fromJson(json, BookingPayload::class.java)

            BookingScreen(
                bookingPayload = bookingPayload,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onBookingSuccess = {
                    // Navigate to home or bookings screen
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }

    }
}


