package uk.ac.tees.mad.carease.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navigateToServiceScreen:()->Unit,
    logout: () -> Unit,
) {

    val bottomNavController= rememberNavController()
    val navBackStackEntry=bottomNavController.currentBackStackEntryAsState()
    val currentRoute=navBackStackEntry.value?.destination?.route ?: ""


    val homeViewModel= hiltViewModel<HomeViewModel>()
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
    { innerPadding->


        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavigationScreen.Home.route,
            modifier=Modifier.padding(innerPadding)
        ){

            composable(BottomNavigationScreen.Home.route){
                HomeScreen(
                    viewModel = homeViewModel,
                    navigateToServiceScreen = navigateToServiceScreen
                )
            }

            composable(BottomNavigationScreen.Profile.route){
                ProfileScreen()
            }

        }
    }

}