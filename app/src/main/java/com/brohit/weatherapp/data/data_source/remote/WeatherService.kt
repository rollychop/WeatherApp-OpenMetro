package com.brohit.weatherapp.data.data_source.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("geo/1.0/direct")
    suspend fun getGeoLocation(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5
    ): Response<List<CityGeoDto>>

    @GET("geo/1.0/zip")
    suspend fun getGeoLocationByZip(@Query("zip") zip: String): Response<ZipGeoDto?>

    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<OpenWeatherDto>

    @GET("data/2.5/forecast/hourly")
    suspend fun getWeatherByZip(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<Any>


}