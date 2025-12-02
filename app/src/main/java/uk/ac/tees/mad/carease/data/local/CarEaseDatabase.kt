package uk.ac.tees.mad.carease.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BookingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CarEaseDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
}