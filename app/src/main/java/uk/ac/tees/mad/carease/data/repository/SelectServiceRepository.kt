package uk.ac.tees.mad.carease.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.carease.data.models.Area
import uk.ac.tees.mad.carease.data.models.Service

class SelectServiceRepository(
    private val firestore: FirebaseFirestore
) {

    suspend fun getServices(): Result<List<Service>> {
        return try {
            val snapshot = firestore.collection("services")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val services = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Service::class.java)?.copy(id = doc.id)
            }
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch all active areas
    suspend fun getAreas(): Result<List<Area>> {
        return try {
            val snapshot = firestore.collection("areas")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val areas = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Area::class.java)?.copy(id = doc.id)
            }
            Result.success(areas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Validate promo code (optional - returns discount amount)
    suspend fun validatePromoCode(promoCode: String): Result<Double> {
        return try {
            val snapshot = firestore.collection("promoCodes")
                .whereEqualTo("code", promoCode.uppercase())
                .whereEqualTo("isActive", true)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val promo = snapshot.documents.first()
                val discountValue = promo.getDouble("discountValue") ?: 0.0
                Result.success(discountValue)
            } else {
                Result.failure(Exception("Invalid promo code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}