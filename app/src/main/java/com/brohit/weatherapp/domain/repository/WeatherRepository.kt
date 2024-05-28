package com.brohit.weatherapp.domain.repository

import com.brohit.weatherapp.domain.util.Resource
import com.brohit.weatherapp.domain.weather.WeatherInfo

interface WeatherRepository {
    suspend fun getWeatherData(lat: Double, long: Double): Resource<WeatherInfo>
    suspend fun getMyIpGeoLocation(): Resource<Pair<Double, Double>>

    suspend fun searchCity(query: String): Resource<List<Triple<String, Double, Double>>>

}