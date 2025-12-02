package uk.ac.tees.mad.carease.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val bookingId: String,
    val userId: String,
    val serviceName: String,
    val serviceType: String,
    val scheduledDate: Long, // Convert Timestamp to Long
    val timeSlot: String,
    val totalPrice: Double,
    val status: String,
    val carMake: String,
    val carModel: String,
    val areaName: String,
    val createdAt: Long // Convert Timestamp to Long
)