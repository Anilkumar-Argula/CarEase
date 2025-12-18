package uk.ac.tees.mad.carease.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.carease.navigation.bottom_navigation.BottomNavigationScreen
import uk.ac.tees.mad.carease.navigation.bottom_navigation.bottomNavItems
import uk.ac.tees.mad.carease.ui.screens.bottom_screens.HomeScreen
import uk.ac.tees.mad.carease.ui.screens.bottom_screens.ProfileScreen
import uk.ac.tees.mad.carease.viewmodels.HomeViewModel
import uk.ac.tees.mad.carease.viewmodels.ProfileViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navigateToServiceScreen: () -> Unit,
    logout: () -> Unit,
) {

    val bottomNavController = rememberNavController()
    val navBackStackEntry = bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: ""


    val homeViewModel = hiltViewModel<HomeViewModel>()
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
                HomeScreen(
                    viewModel = homeViewModel,
                    navigateToServiceScreen = navigateToServiceScreen
                )
            }

            composable(BottomNavigationScreen.Profile.route) {
                val profileViewModel: ProfileViewModel=hiltViewModel()
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