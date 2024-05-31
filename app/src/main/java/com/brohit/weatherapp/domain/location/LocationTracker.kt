package com.brohit.weatherapp.domain.location

import android.location.Location
import kotlin.jvm.Throws


sealed class LocationException(message: String) : Exception(message) {
    class LocationPermissionDeniedException :
        LocationException(message = "Location permission denied")

    class GPSDisabledException : LocationException(message = "GPS is disabled")
}

interface LocationTracker {

    @Throws(LocationException::class)
    suspend fun getCurrentLocation(): Location?
    fun hasLocationPermission(): Boolean
    fun isGpsEnabled(): Boolean
}