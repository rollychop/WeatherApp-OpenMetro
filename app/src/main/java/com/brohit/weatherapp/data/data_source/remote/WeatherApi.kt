package com.brohit.weatherapp.data.data_source.remote

import com.google.gson.JsonElement
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface WeatherApi {

    @GET("v1/forecast?hourly=temperature_2m,weathercode,relativehumidity_2m,windspeed_10m,pressure_msl")
    suspend fun getWeatherData(
        @Query("latitude") lat: Double,
        @Query("longitude") long: Double
    ): WeatherDto


    @GET
    suspend fun getMyIpGeoLocation(@Url url: String): Response<Map<String, Any>>

    @POST
    suspend fun searchPlacesByQuery(
        @Url url: String,
        @Body body: RequestBody
    ): Response<JsonElement>


    @GET
    suspend fun getMyIpDetails(
        @Url url: String
    ): Response<String>
}