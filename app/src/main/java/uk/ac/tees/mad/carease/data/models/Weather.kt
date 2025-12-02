package uk.ac.tees.mad.carease.data.models

import com.google.firebase.Timestamp


// Weather Model
data class Weather(
    val areaId: String = "",
    val temperature: Double = 0.0,
    val condition: String = "", // "Sunny", "Rainy", "Cloudy"
    val bestTimeForWash: String = "",
    val worstTimeForWash: String = "",
    val lastUpdated: Timestamp? = null
)
