package uk.ac.tees.mad.carease.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.ac.tees.mad.carease.data.models.Booking

@Database(entities = [Booking::class], version = 1)
abstract class CarEaseDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
}