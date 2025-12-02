package uk.ac.tees.mad.carease.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY createdAt DESC LIMIT 4")
    suspend fun getRecentBookings(userId: String): List<BookingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Query("DELETE FROM bookings WHERE userId = :userId")
    suspend fun clearUserBookings(userId: String)
}