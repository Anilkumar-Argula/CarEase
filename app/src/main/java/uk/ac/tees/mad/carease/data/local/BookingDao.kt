package uk.ac.tees.mad.carease.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.carease.data.models.Booking

@Dao
interface BookingDao {

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY scheduledDate DESC LIMIT 4")
    fun getRecentBookings(userId: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE userId = :userId AND status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS') ORDER BY scheduledDate ASC")
    fun getUpcomingBookings(userId: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE userId = :userId AND status IN ('COMPLETED', 'CANCELLED') ORDER BY scheduledDate DESC")
    fun getPastBookings(userId: String): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<Booking>)

    @Query("UPDATE bookings SET status = :status WHERE bookingId = :bookingId")
    suspend fun updateStatus(bookingId: String, status: String)

    @Query("DELETE FROM bookings WHERE userId = :userId")
    suspend fun clearUser(userId: String)
}