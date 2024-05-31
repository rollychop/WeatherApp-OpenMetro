package com.brohit.weatherapp.domain.repository

import com.brohit.weatherapp.domain.util.Resource
import com.brohit.weatherapp.domain.weather.WeatherInfo

interface WeatherRepository : OpenWeatherRepository {
    suspend fun getWeatherData(lat: Double, long: Double, address: String): Result<WeatherInfo>
    suspend fun getMyIpGeoLocation(): Resource<Triple<Double, Double, String>>

    suspend fun searchCity(query: String): Resource<List<Triple<String, Double, Double>>>
    suspend fun getCurrentLocation(): Result<Triple<Double, Double, String>>

    suspend fun searchWithGps(): Result<Pair<WeatherInfo, String>>
}