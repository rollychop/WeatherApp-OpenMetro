package com.brohit.weatherapp.domain.repository

import com.brohit.weatherapp.domain.weather.WeatherModel


interface OpenWeatherRepository {

    //    suspend fun searchCity(cityName: String): Result<List<GeoCityModel>>
    suspend fun getOpenWeather(lat: Double, lon: Double,): Result<WeatherModel>
}