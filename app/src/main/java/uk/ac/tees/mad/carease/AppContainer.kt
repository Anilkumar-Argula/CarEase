package uk.ac.tees.mad.carease

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.ac.tees.mad.carease.data.api.WeatherApiService
import uk.ac.tees.mad.carease.data.local.CarEaseDatabase
import uk.ac.tees.mad.carease.data.repository.*
import uk.ac.tees.mad.carease.utils.LocationManager

class AppContainer(context: Context) {

    // Firebase
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Room
    private val database = Room.databaseBuilder(
        context,
        CarEaseDatabase::class.java,
        "carease_cache"
    ).build()

    val bookingDao = database.bookingDao()

    // Network
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(WeatherApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApi = retrofit.create(WeatherApiService::class.java)

    // Utils
    private val locationManager = LocationManager()

    // Repositories
    val homeRepository = HomeRepository(
        firestore,
        auth,
        weatherApi,
        locationManager,
        bookingDao
    )

    val bookingRepository = BookingRepository(firestore, auth)

    val carDetailsRepository = CarDetailsRepository(firestore, auth)

    val selectServiceRepository = SelectServiceRepository(firestore)
}
