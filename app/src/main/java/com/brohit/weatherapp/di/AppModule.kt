package com.brohit.weatherapp.di

import android.app.Application
import androidx.room.Room
import com.brohit.weatherapp.data.data_source.local.AppDatabase
import com.brohit.weatherapp.data.data_source.local.dao.KeyValueDao
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.brohit.weatherapp.data.data_source.remote.WeatherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun providesRoomDb(app: Application): AppDatabase = Room.databaseBuilder(
        context = app,
        klass = AppDatabase::class.java,
        name = "weather.db",
    ).build()


    @Provides
    @Singleton
    fun providesKeyValueDao(db: AppDatabase): KeyValueDao {
        return db.keyValueDao
    }


    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }
}