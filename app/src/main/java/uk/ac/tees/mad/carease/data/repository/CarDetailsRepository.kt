package uk.ac.tees.mad.carease.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.carease.utils.CloudinaryConfig
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CarDetailsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    fun initializeCloudinary(context: Context) {
        try {
            val config = hashMapOf(
                "cloud_name" to CloudinaryConfig.CLOUD_NAME,
            )
            MediaManager.init(context, config)
        } catch (e: Exception) {
            // Already initialized
        }
    }

    // Upload image to Cloudinary
    suspend fun uploadImageToCloudinary(imageUri: Uri): Result<String> =
        suspendCoroutine { continuation ->
            try {
                val requestId = MediaManager.get().upload(imageUri)
                    .unsigned(CloudinaryConfig.UPLOAD_PRESET)
                    .option("folder", "carease")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            // Upload started
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            // Progress update
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["secure_url"] as? String
                            if (imageUrl != null) {
                                continuation.resume(Result.success(imageUrl))
                            } else {
                                continuation.resume(Result.failure(Exception("Failed to get image URL")))
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            continuation.resume(Result.failure(Exception(error.description)))
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            continuation.resume(Result.failure(Exception("Upload rescheduled: ${error.description}")))
                        }
                    })
                    .dispatch()
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }


        }

    // Fetch saved vehicles for the user
    suspend fun getSavedVehicles(): Result<List<Map<String, Any>>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = firestore.collection("vehicles")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val vehicles = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(vehicles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Save vehicle to Firestore (optional - for future use)
    suspend fun saveVehicle(
        make: String,
        model: String,
        registrationNumber: String,
        color: String,
        photoUrl: String,
        notes: String
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val vehicleData = hashMapOf(
                "userId" to userId,
                "make" to make,
                "model" to model,
                "registrationNumber" to registrationNumber,
                "color" to color,
                "photoUrl" to photoUrl,
                "notes" to notes,
                "isDefault" to false,
                "createdAt" to System.currentTimeMillis()
            )

            val docRef = firestore.collection("vehicles")
                .add(vehicleData)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}