package uk.ac.tees.mad.carease.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.carease.data.local.BookingDao
import javax.inject.Inject

class BookingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val bookingDao: BookingDao
) {

//    suspend fun checkTimeSlotAvailability(
//        areaId: String,
//        date: Long,
//        timeSlot: String
//    ): Result<Boolean> {
//        return try {
//            val snapshot = firestore.collection("bookings")
//                .whereEqualTo("areaDetails.areaId", areaId)
//                .whereEqualTo("scheduledDate", Timestamp(date / 1000, 0))
//                .whereEqualTo("timeSlot", timeSlot)
//                .whereIn("status", listOf("PENDING", "CONFIRMED"))
//                .get()
//                .await()
//
//            Result.success(true)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun createBooking(
//        bookingPayload: BookingPayload,
//        scheduledDate: Long,
//        timeSlot: String,
//        paymentMethod: String
//    ): Result<String> {
//        return try {
//            val userId = auth.currentUser?.uid
//                ?: return Result.failure(Exception("User not logged in"))
//
//            val userEmail = auth.currentUser?.email ?: ""
//            val userName = auth.currentUser?.displayName ?: ""
//
//            val bookingData = hashMapOf(
//                "userId" to userId,
//                "userDetails" to hashMapOf(
//                    "name" to userName,
//                    "email" to userEmail
//                ),
//                "vehicleDetails" to hashMapOf(
//                    "make" to bookingPayload.vehicleData.make,
//                    "model" to bookingPayload.vehicleData.model,
//                    "registrationNumber" to bookingPayload.vehicleData.registrationNumber,
//                    "color" to bookingPayload.vehicleData.color,
//                    "photoUrl" to (bookingPayload.vehicleData.photoUrl ?: ""),
//                    "notes" to bookingPayload.vehicleData.notes
//                ),
//                "carMake" to bookingPayload.vehicleData.make,
//                "carModel" to bookingPayload.vehicleData.model,
//                "serviceDetails" to hashMapOf(
//                    "serviceId" to bookingPayload.serviceSelection.serviceId,
//                    "serviceName" to bookingPayload.serviceSelection.serviceName,
//                    "serviceType" to bookingPayload.serviceSelection.serviceType,
//                    "basePrice" to bookingPayload.serviceSelection.basePrice
//                ),
//                "serviceName" to bookingPayload.serviceSelection.serviceName,
//                "serviceType" to bookingPayload.serviceSelection.serviceType,
//                "areaDetails" to hashMapOf(
//                    "areaId" to bookingPayload.serviceSelection.areaId,
//                    "areaName" to bookingPayload.serviceSelection.areaName
//                ),
//                "areaName" to bookingPayload.serviceSelection.areaName,
//                "addons" to bookingPayload.vehicleData.selectedAddons,
//                "addonsPrice" to bookingPayload.vehicleData.addonsPrice,
//                "scheduledDate" to Timestamp(scheduledDate / 1000, 0),
//                "timeSlot" to timeSlot,
//                "pricing" to hashMapOf(
//                    "servicePrice" to bookingPayload.serviceSelection.estimatedPrice,
//                    "addonsPrice" to bookingPayload.vehicleData.addonsPrice,
//                    "discount" to bookingPayload.serviceSelection.discount,
//                    "totalPrice" to bookingPayload.totalPrice
//                ),
//                "totalPrice" to bookingPayload.totalPrice,
//                "status" to "PENDING",
//                "paymentMethod" to paymentMethod,
//                "paymentStatus" to if (paymentMethod == "PAY_AT_SERVICE") "UNPAID" else "PENDING",
//                "promoCode" to (bookingPayload.serviceSelection.promoCode ?: ""),
//                "createdAt" to Timestamp.now(),
//                "updatedAt" to Timestamp.now()
//            )
//
//            val docRef = firestore.collection("bookings")
//                .add(bookingData)
//                .await()
//
//            // Cache the booking locally immediately after creation
//            val localBooking = BookingEntity(
//                bookingId = docRef.id,
//                userId = userId,
//                serviceName = bookingPayload.serviceSelection.serviceName,
//                serviceType = bookingPayload.serviceSelection.serviceType,
//                scheduledDate = scheduledDate,
//                timeSlot = timeSlot,
//                totalPrice = bookingPayload.totalPrice,
//                status = "PENDING",
//                carMake = bookingPayload.vehicleData.make,
//                carModel = bookingPayload.vehicleData.model,
//                areaName = bookingPayload.serviceSelection.areaName,
//                createdAt = System.currentTimeMillis()
//            )
//            bookingDao.insertBooking(localBooking)
//
//            Result.success(docRef.id)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun updateBookingStatus(
//        bookingId: String,
//        status: String
//    ): Result<Unit> {
//        return try {
//            firestore.collection("bookings")
//                .document(bookingId)
//                .update(
//                    mapOf(
//                        "status" to status,
//                        "updatedAt" to Timestamp.now()
//                    )
//                )
//                .await()
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getUserProfile(): Result<Pair<String, String>> {
//        return try {
//            val userId = auth.currentUser?.uid
//                ?: return Result.failure(Exception("User not logged in"))
//
//            val snapshot = firestore.collection("users")
//                .document(userId)
//                .get()
//                .await()
//
//            val name = snapshot.getString("fullName") ?: ""
//            val email = snapshot.getString("email") ?: ""
//
//            Result.success(Pair(name, email))
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
}