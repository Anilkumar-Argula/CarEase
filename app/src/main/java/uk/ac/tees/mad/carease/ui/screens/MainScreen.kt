package uk.ac.tees.mad.carease.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.carease.AppContainer
import uk.ac.tees.mad.carease.navigation.bottom_navigation.BottomNavigationScreen
import uk.ac.tees.mad.carease.navigation.bottom_navigation.bottomNavItems
import uk.ac.tees.mad.carease.ui.screens.bottom_screens.HomeScreen
import uk.ac.tees.mad.carease.ui.screens.bottom_screens.ProfileScreen
import uk.ac.tees.mad.carease.viewmodels.HomeViewModel
import uk.ac.tees.mad.carease.viewmodels.HomeViewModelFactory
import uk.ac.tees.mad.carease.viewmodels.ProfileViewModel
import uk.ac.tees.mad.carease.viewmodels.ProfileViewModelFactory

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    container: AppContainer,
    navigateToServiceScreen: () -> Unit,
    logout: () -> Unit,
) {

    val bottomNavController = rememberNavController()
    val navBackStackEntry = bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: ""

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = {
                            Text(screen.title)
                        },
                        icon = {
                            Icon(screen.icon, contentDescription = screen.title)
                        }
                    )
                }
            }
        }
    )
    { innerPadding ->


        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavigationScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(BottomNavigationScreen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(
                        repository = container.homeRepository
                    )
                )

                HomeScreen(
                    viewModel = homeViewModel,
                    navigateToServiceScreen = navigateToServiceScreen
                )
            }

            composable(BottomNavigationScreen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(
                        auth = container.auth,
                        firestore = container.firestore,
                        bookingDao = container.bookingDao
                    )
                )
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToLogin = logout,

                    )
            }

        }
    }

}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    val bottomNavController = rememberNavController()
    val navBackStackEntry = bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: ""

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = {
                            Text(screen.title)
                        },
                        icon = {
                            Icon(screen.icon, contentDescription = screen.title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavigationScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavigationScreen.Home.route) {
                //  placeholder
                Text(
                    "Home Screen",
                    modifier = Modifier.padding(16.dp)
                )
            }

            composable(BottomNavigationScreen.Profile.route) {
                Text(
                    "Profile Screen",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}