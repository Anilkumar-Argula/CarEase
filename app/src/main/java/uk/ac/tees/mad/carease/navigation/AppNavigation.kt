package uk.ac.tees.mad.carease.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import uk.ac.tees.mad.carease.AppContainer
import uk.ac.tees.mad.carease.data.models.BookingPayload
import uk.ac.tees.mad.carease.data.models.ServiceSelection
import uk.ac.tees.mad.carease.ui.screens.CarDetailScreen
import uk.ac.tees.mad.carease.ui.screens.LoginScreen
import uk.ac.tees.mad.carease.ui.screens.MainScreen
import uk.ac.tees.mad.carease.ui.screens.SelectServiceScreen
import uk.ac.tees.mad.carease.ui.screens.SignUpScreen
import uk.ac.tees.mad.carease.ui.screens.SplashScreen
import uk.ac.tees.mad.carease.ui.screens.bottom_screens.BookingScreen
import uk.ac.tees.mad.carease.viewmodels.AuthViewModel
import uk.ac.tees.mad.carease.viewmodels.AuthViewModelFactory
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    container: AppContainer
) {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            auth = container.auth,
            firestore = container.firestore,
            bookingDao = container.bookingDao
        )
    )

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
                container = container,
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
                container = container,
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
                container = container,
                serviceSelection = serviceSelection,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProceedToBooking = { bookingPayload ->
                    val bookingJson = gson.toJson(bookingPayload)
                    val encodedBookingJson =
                        URLEncoder.encode(
                            bookingJson,
                            StandardCharsets.UTF_8.toString()
                        )

                    navController.navigate(
                        Screen.Booking.createRoute(encodedBookingJson)
                    )
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
                container = container,
                bookingPayload = bookingPayload,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onBookingSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = false }
                    }
                }
            )
        }

    }
}


