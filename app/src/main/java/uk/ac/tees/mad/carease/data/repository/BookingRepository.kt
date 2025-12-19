package uk.ac.tees.mad.carease.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.carease.data.models.BookingPayload

class BookingRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    // Check if time slot
    suspend fun checkTimeSlotAvailability(
        areaId: String,
        date: Long,
        timeSlot: String
    ): Result<Boolean> {
        return try {
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("areaDetails.areaId", areaId)
                .whereEqualTo("scheduledDate", Timestamp(date / 1000, 0))
                .whereEqualTo("timeSlot", timeSlot)
                .whereIn("status", listOf("PENDING", "CONFIRMED"))
                .get()
                .await()

            // Simple availability check we can check for rush
//            val isAvailable = snapshot.documents.size < 5
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create booking in Firestore
    suspend fun createBooking(
        bookingPayload: BookingPayload,
        scheduledDate: Long,
        timeSlot: String,
        paymentMethod: String
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val userEmail = auth.currentUser?.email ?: ""
            val userName = auth.currentUser?.displayName ?: ""

            // Create booking document
            val bookingData = hashMapOf(
                "userId" to userId,
                "userDetails" to hashMapOf(
                    "name" to userName,
                    "email" to userEmail
                ),

                // Vehicle details
                "vehicleDetails" to hashMapOf(
                    "make" to bookingPayload.vehicleData.make,
                    "model" to bookingPayload.vehicleData.model,
                    "registrationNumber" to bookingPayload.vehicleData.registrationNumber,
                    "color" to bookingPayload.vehicleData.color,
                    "photoUrl" to (bookingPayload.vehicleData.photoUrl ?: ""),
                    "notes" to bookingPayload.vehicleData.notes
                ),

                "carMake" to bookingPayload.vehicleData.make,
                "carModel" to bookingPayload.vehicleData.model,

                // Service details
                "serviceDetails" to hashMapOf(
                    "serviceId" to bookingPayload.serviceSelection.serviceId,
                    "serviceName" to bookingPayload.serviceSelection.serviceName,
                    "serviceType" to bookingPayload.serviceSelection.serviceType,
                    "basePrice" to bookingPayload.serviceSelection.basePrice
                ),

                "serviceName" to bookingPayload.serviceSelection.serviceName,
                "serviceType" to bookingPayload.serviceSelection.serviceType,

                // Area details
                "areaDetails" to hashMapOf(
                    "areaId" to bookingPayload.serviceSelection.areaId,
                    "areaName" to bookingPayload.serviceSelection.areaName
                ),

                "areaName" to bookingPayload.serviceSelection.areaName,

                // Add-ons
                "addons" to bookingPayload.vehicleData.selectedAddons,
                "addonsPrice" to bookingPayload.vehicleData.addonsPrice,

                // Scheduling
                "scheduledDate" to Timestamp(scheduledDate / 1000, 0),
                "timeSlot" to timeSlot,

                // Pricing
                "pricing" to hashMapOf(
                    "servicePrice" to bookingPayload.serviceSelection.estimatedPrice,
                    "addonsPrice" to bookingPayload.vehicleData.addonsPrice,
                    "discount" to bookingPayload.serviceSelection.discount,
                    "totalPrice" to bookingPayload.totalPrice
                ),

                "totalPrice" to bookingPayload.totalPrice,

                // Status and payment
                "status" to "PENDING",
                "paymentMethod" to paymentMethod,
                "paymentStatus" to if (paymentMethod == "PAY_AT_SERVICE") "UNPAID" else "PENDING",

                // Promo code
                "promoCode" to (bookingPayload.serviceSelection.promoCode ?: ""),

                // Timestamps
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            val docRef = firestore.collection("bookings")
                .add(bookingData)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update booking status will be used later
    suspend fun updateBookingStatus(
        bookingId: String,
        status: String
    ): Result<Unit> {
        return try {
            firestore.collection("bookings")
                .document(bookingId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user profile name
    suspend fun getUserProfile(): Result<Pair<String, String>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val name = snapshot.getString("fullName") ?: ""
            val email = snapshot.getString("email") ?: ""

            Result.success(Pair(name, email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}