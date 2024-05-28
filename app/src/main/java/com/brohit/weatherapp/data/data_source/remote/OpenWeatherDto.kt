package com.brohit.weatherapp.data.data_source.remote

import com.google.gson.annotations.SerializedName

data class OpenWeatherDto(
    val coord: CoordDto,
    val weather: List<Weather1Dto>,
    val base: String,
    val main: MainDto,
    val visibility: Long,
    val wind: WindDto?,
    val rain: RainDto?,
    val clouds: CloudsDto?,
    val dt: Long,
    val sys: SysDto?,
    val timezone: Long,
    val id: Long,
    val name: String,
    val cod: Long,
)

data class CoordDto(
    val lon: Double,
    val lat: Double,
)

data class Weather1Dto(
    val id: Long,
    val main: String,
    val description: String,
    val icon: String,
)

data class MainDto(
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

data class WindDto(
    val speed: Double,
    val deg: Long,
    val gust: Double,
)

data class RainDto(
    @SerializedName("1h")
    val n1h: Double,
)

data class CloudsDto(
    val all: Long,
)

data class SysDto(
    val type: Long,
    val id: Long,
    val country: String,
    val sunrise: Long,
    val sunset: Long,
)