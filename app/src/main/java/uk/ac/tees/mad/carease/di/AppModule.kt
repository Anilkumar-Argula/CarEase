package uk.ac.tees.mad.carease.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.ac.tees.mad.carease.data.api.WeatherApiService
import uk.ac.tees.mad.carease.data.repository.BookingRepository
import uk.ac.tees.mad.carease.data.repository.CarDetailsRepository
import uk.ac.tees.mad.carease.data.repository.HomeRepository
import uk.ac.tees.mad.carease.utils.LocationManager
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth= FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore= FirebaseFirestore.getInstance()


    @Provides
    @Singleton
    fun provideHomeRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        weatherApiService: WeatherApiService,
        locationManager: LocationManager
    ): HomeRepository {
        return HomeRepository(firestore, auth,weatherApiService, locationManager)
    }

    @Provides
    @Singleton
    fun provideCarDetailsRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CarDetailsRepository {
        return CarDetailsRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideBookingRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): BookingRepository {
        return BookingRepository(firestore, auth)
    }


    @Provides
    @Singleton
    fun provideLocationManager(): LocationManager {
        return LocationManager()
    }


    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }
}