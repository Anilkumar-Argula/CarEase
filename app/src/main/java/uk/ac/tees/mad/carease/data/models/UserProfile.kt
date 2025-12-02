package uk.ac.tees.mad.carease.data.models


import com.google.firebase.Timestamp


data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val defaultVehicle: String = "",
    val preferredArea: String = "",
    val notificationsEnabled: Boolean = true,
    val createdAt: Timestamp? = Timestamp.now()
)
