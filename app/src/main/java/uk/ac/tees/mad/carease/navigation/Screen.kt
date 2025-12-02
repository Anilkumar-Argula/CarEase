package uk.ac.tees.mad.carease.navigation


sealed class Screen(val route: String) {

    object Splash : Screen("splash")
    object Login:Screen("login")
    object SignUp:Screen("signup")
    object Main : Screen("main")
    object SelectService:Screen("service")


    object CarDetail:Screen("car_detail/{serviceSelection}"){
        fun createRoute(serviceSelection:String)="car_detail/$serviceSelection"
    }


    object Booking:Screen("booking/{bookingPayload}"){
        fun createRoute(bookingPayload: String)= "booking/$bookingPayload"
    }
}