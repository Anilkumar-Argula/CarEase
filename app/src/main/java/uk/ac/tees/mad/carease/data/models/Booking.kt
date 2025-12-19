package uk.ac.tees.mad.carease.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.firebase.Timestamp


@Entity(tableName = "bookings")
@TypeConverters(TimestampConverter::class)
data class Booking(
    @PrimaryKey
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

class TimestampConverter {

    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? {
        return value?.toDate()?.time
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it / 1000, 0) }
    }
}