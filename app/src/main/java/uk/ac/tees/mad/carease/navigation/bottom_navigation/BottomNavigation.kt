package uk.ac.tees.mad.carease.navigation.bottom_navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavigationScreen(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )


    object Profile : BottomNavigationScreen(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )
}

val bottomNavItems = listOf(
    BottomNavigationScreen.Home,
    BottomNavigationScreen.Profile
)