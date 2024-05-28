package com.brohit.weatherapp.data.data_source.remote

import com.google.gson.annotations.SerializedName

data class WeatherDto(
    @SerializedName("hourly")
    val weatherData: WeatherDataDto
)
