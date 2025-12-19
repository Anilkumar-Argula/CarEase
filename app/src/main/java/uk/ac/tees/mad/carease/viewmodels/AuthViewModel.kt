package uk.ac.tees.mad.carease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import uk.ac.tees.mad.carease.data.local.BookingDao
import uk.ac.tees.mad.carease.data.models.UserProfile

class AuthViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dao: BookingDao,
) : ViewModel() {


    fun isLoggedIn() = auth.currentUser != null

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                val errorMsg = when {
                    exception.message?.contains("network") == true ->
                        "No internet connection. Please check your network."

                    exception.message?.contains("password") == true ->
                        "Incorrect password. Please try again."

                    exception.message?.contains("user") == true ->
                        "No account found with this email."

                    else -> exception.message ?: "Login failed. Please try again."
                }
                onError(errorMsg)
            }
    }

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        defaultVehicle: String?,
        preferredArea: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener

                val userProfile = UserProfile(
                    uid = userId,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    defaultVehicle = defaultVehicle ?: "",
                    preferredArea = preferredArea ?: "",
                    notificationsEnabled = true,
                    createdAt = Timestamp.now()
                )


                // Create user profile in Firestore
//                val userProfile = hashMapOf(
//                    "uid" to userId,
//                    "fullName" to fullName,
//                    "email" to email,
//                    "defaultVehicle" to (defaultVehicle ?: ""),
//                    "preferredArea" to (preferredArea ?: ""),
//                    // notification
//                    "notificationsEnabled" to true,
//                    "createdAt" to System.currentTimeMillis()
//                )

                firestore.collection("users")
                    .document(userId)
                    .set(userProfile)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Failed to create profile")
                    }
            }
            .addOnFailureListener { exception ->
                val errorMsg = when {
                    exception.message?.contains("network") == true ->
                        "No internet connection. Please check your network."

                    exception.message?.contains("already in use") == true ->
                        "This email is already registered."

                    exception.message?.contains("weak password") == true ->
                        "Password is too weak. Use at least 6 characters."

                    exception.message?.contains("badly formatted") == true ->
                        "Invalid email format."

                    else -> exception.message ?: "Sign up failed. Please try again."
                }
                onError(errorMsg)
            }
    }

//    fun logOut(onSuccess: () -> Unit) {
//        val userId = firebaseAuth.currentUser?.uid
//        viewModelScope.launch {
//            dao.clearUser(userId = userId!!)
//        }
//        firebaseAuth.signOut()
//        onSuccess()
//    }

    fun logOut(onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid

        viewModelScope.launch {
            if (userId != null) {
                dao.clearUser(userId)
            }
            auth.signOut()
            onSuccess()
        }
    }



}