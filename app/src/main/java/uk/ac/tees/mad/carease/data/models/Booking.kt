package uk.ac.tees.mad.carease.data.models

import com.google.firebase.Timestamp

data class Booking (
    val bookingId: String = "",
    val userId: String = "",
    val serviceName: String = "",
    val serviceType: String = "",
    val scheduledDate: Timestamp? = null,
    val timeSlot: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "", // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    val carMake: String = "",
    val carModel: String = "",
    val areaName: String = "",
    val createdAt: Timestamp? = null

)