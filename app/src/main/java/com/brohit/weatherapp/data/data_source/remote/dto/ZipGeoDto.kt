package com.brohit.weatherapp.data.data_source.remote.dto

data class ZipGeoDto(
    val zip: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
)
