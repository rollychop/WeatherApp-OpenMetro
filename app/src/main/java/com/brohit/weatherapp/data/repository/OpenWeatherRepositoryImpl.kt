package com.brohit.weatherapp.data.repository

import com.brohit.weatherapp.data.data_source.remote.WeatherService
import com.brohit.weatherapp.data.mappers.toModel
import com.brohit.weatherapp.data.utils.checkAndGetBody
import com.brohit.weatherapp.data.utils.runCatchingCustom
import com.brohit.weatherapp.domain.repository.OpenWeatherRepository
import com.brohit.weatherapp.domain.weather.GeoCityModel
import com.brohit.weatherapp.domain.weather.WeatherModel

class OpenWeatherRepositoryImpl(
    private val weatherService: WeatherService
) : OpenWeatherRepository {
    override suspend fun searchCity(cityName: String): Result<List<GeoCityModel>> {
        return runCatchingCustom {
            weatherService.getGeoLocation(cityName).checkAndGetBody().map { it.toModel() }
        }
    }

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherModel> {
        return runCatchingCustom {
            weatherService.getWeather(lat, lon).checkAndGetBody().toModel()
        }
    }
}