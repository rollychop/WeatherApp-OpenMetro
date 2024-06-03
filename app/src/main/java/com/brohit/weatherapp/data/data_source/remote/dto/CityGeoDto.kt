package com.brohit.weatherapp.data.data_source.remote.dto

data class CityGeoDto(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    /*    @SerializedName("local_names")
    val localNames: Map<String, String>,*/
)