package com.brohit.weatherapp.domain.weather

import com.google.gson.annotations.SerializedName

data class WeatherModel(
    val coord: CoordModel,
    val weather: List<Weather1Model>,
    val base: String,
    val main: MainModel,
    val visibility: Long,
    val wind: WindModel?,
    val rain: RainModel?,
    val clouds: CloudsModel?,
    val dt: Long,
    val sys: SysModel?,
    val timezone: Long,
    val id: Long,
    val name: String,
    val cod: Long,
)

data class CoordModel(
    val lon: Double,
    val lat: Double,
)

data class Weather1Model(
    val id: Long,
    val main: String,
    val description: String,
    val icon: String,
)

data class MainModel(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    val pressure: Long,
    val humidity: Long,
    @SerializedName("sea_level")
    val seaLevel: Long,
    @SerializedName("grnd_level")
    val grndLevel: Long,
)

data class WindModel(
    val speed: Double,
    val deg: Long,
    val gust: Double,
)

data class RainModel(
    val n1h: Double,
)

data class CloudsModel(
    val all: Long,
)

data class SysModel(
    val type: Long,
    val id: Long,
    val country: String,
    val sunrise: Long,
    val sunset: Long,
)