package com.brohit.weatherapp.data.repository

import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.brohit.weatherapp.data.data_source.local.dao.KeyValueDao
import com.brohit.weatherapp.data.data_source.local.entity.KeyValueEntity
import com.brohit.weatherapp.data.data_source.remote.WeatherApi
import com.brohit.weatherapp.data.data_source.remote.WeatherService
import com.brohit.weatherapp.data.mappers.toModel
import com.brohit.weatherapp.data.mappers.toWeatherInfo
import com.brohit.weatherapp.data.utils.checkAndGetBody
import com.brohit.weatherapp.data.utils.runCatchingCustom
import com.brohit.weatherapp.domain.location.LocationTracker
import com.brohit.weatherapp.domain.repository.WeatherRepository
import com.brohit.weatherapp.domain.util.Resource
import com.brohit.weatherapp.domain.weather.WeatherInfo
import com.brohit.weatherapp.domain.weather.WeatherModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val openWeatherService: WeatherService,
    private val keyValueDao: KeyValueDao,
    private val locationTracker: LocationTracker,
    private val application: Application
) : WeatherRepository {

    override suspend fun getOpenWeather(lat: Double, lon: Double): Result<WeatherModel> {
        return runCatchingCustom {
            openWeatherService.getWeather(lat, lon).checkAndGetBody().toModel()
        }
    }

    override suspend fun searchWithGps(): Result<Pair<WeatherInfo, String>> {
        return runCatchingCustom {
            locationTracker.getCurrentLocation()?.let { l ->
                val address = l.cityName()
                getWeatherData(l.latitude, l.longitude, address).getOrThrow() to address
            } ?: throw Exception("No Location found")
        }
    }

    override suspend fun getWeatherData(
        lat: Double,
        long: Double,
        address: String,
    ): Result<WeatherInfo> {
        return runCatchingCustom {
            withContext(Dispatchers.IO) {
                keyValueDao.put(
                    KeyValueEntity("lat", lat.toString()),
                    KeyValueEntity("lon", long.toString()),
                    KeyValueEntity("location", address),
                    KeyValueEntity("time", Instant.now().toEpochMilli().toString()),
                )
            }
            api.getWeatherData(
                lat = lat,
                long = long
            ).toWeatherInfo()

        }
    }


    override suspend fun getMyIpGeoLocation(): Resource<Triple<Double, Double, String>> {
        return try {
            withContext(Dispatchers.IO) {
                val savedIp = keyValueDao.get("ip")
                val myCurrentIp = getMyCurrentIp().getOrNull()
                if (savedIp != myCurrentIp && myCurrentIp != null) {
                    val response =
                        api.getMyIpGeoLocation("https://ipapi.co/$myCurrentIp/json/").body()!!
                    keyValueDao.put(KeyValueEntity("ip", myCurrentIp))
                    keyValueDao.put(
                        KeyValueEntity(
                            "ip_lat",
                            response.getValue("latitude").toString()
                        ),
                        KeyValueEntity("ip_lon", response.getValue("longitude").toString()),
                        KeyValueEntity(
                            "ip_location",
                            "${response.getValue("city")}, ${response.getValue("region")}"
                        )
                    )
                }
                val data = Triple(
                    keyValueDao.get("ip_lat")!!.toDouble(),
                    keyValueDao.get("ip_lon")!!.toDouble(),
                    keyValueDao.get("ip_location") ?: "IP Location"
                )
                Resource.Success(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            keyValueDao.remove("ip")
            Resource.Error(e.message ?: "An unknown error occurred.")
        }
    }


    private suspend fun getMyCurrentIp(): Result<String?> {
        return kotlin.runCatching {
            withContext(Dispatchers.IO) {
                URL("https://cloudflare.com/cdn-cgi/trace").run {
                    val c = openConnection() as java.net.HttpURLConnection
                    c.requestMethod = "GET"
                    c.connect()
                    val body = c.inputStream.bufferedReader().use { it.readText() }
                    c.disconnect()
                    body
                }
                    .toString()
                    .lines()
                    .map { it.split("=") }
                    .filter { it.size == 2 }
                    .associate { (key, value) ->
                        key.trim() to value.trim()
                    }["ip"]
            }
        }
    }

    override suspend fun getCurrentLocation(): Result<Triple<Double, Double, String>> =
        runCatchingCustom {
            withContext(Dispatchers.IO) {
                val lat = keyValueDao.get("lat")
                val lon = keyValueDao.get("lon")
                val address = keyValueDao.get("location")
                val time = keyValueDao.get("time")
                if (lat == null || lon == null || address == null ||
                    ((time == null || Instant.now().toEpochMilli()
                        .minus(15 * 60 * 1000) > time.toLong()))
                ) {
                    locationTracker.getCurrentLocation()?.let { currentLocation ->
                        keyValueDao.put(
                            KeyValueEntity("lat", currentLocation.latitude.toString()),
                            KeyValueEntity("lon", currentLocation.longitude.toString()),
                            KeyValueEntity("location", currentLocation.cityName()),
                            KeyValueEntity("time", Instant.now().toEpochMilli().toString()),
                            KeyValueEntity("type", "gps")
                        )
                    } ?: kotlin.run {
                        val myIpGeoLocation = getMyIpGeoLocation()
                        if (myIpGeoLocation is Resource.Success && myIpGeoLocation.data != null) {
                            keyValueDao.put(
                                KeyValueEntity("lat", myIpGeoLocation.data.first.toString()),
                                KeyValueEntity("lon", myIpGeoLocation.data.second.toString()),
                                KeyValueEntity("location", myIpGeoLocation.data.third),
                                KeyValueEntity("time", Instant.now().toEpochMilli().toString()),
                                KeyValueEntity("type", "ip")
                            )
                        }
                        myIpGeoLocation.data
                    }
                }
                val lat1 = keyValueDao.get("lat")
                val lon1 = keyValueDao.get("lon")
                val address1 = keyValueDao.get("location")
                if (lat1 == null || lon1 == null || address1 == null) {
                    throw Exception("No Location found")
                }
                Triple(lat1.toDouble(), lon1.toDouble(), address1)
            }


        }

    private suspend fun Location.cityName(): String {
        return suspendCoroutine { cont ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Geocoder(application, Locale.getDefault())
                    .getFromLocation(
                        latitude,
                        longitude,
                        1
                    ) {
                        it.firstOrNull()?.let { location ->
                            cont.resume(
                                location.getAddressLine(0)
                                    ?: "${location.locality}, ${location.adminArea} ${location.countryName}"
                            )
                        } ?: cont.resume("Your Location")
                    }
            } else {
                cont.resume(
                    Geocoder(application, Locale.getDefault())
                        .getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()?.let { location ->
                            location.getAddressLine(0)
                                ?: "${location.locality}, ${location.adminArea} ${location.countryName}"
                        } ?: "Your Location"
                )
            }
        }
    }

    override suspend fun searchCity(query: String): Resource<List<Triple<String, Double, Double>>> {
        return try {
            val local = Locale.getDefault()
            val lang = "${local.language}-${local.country}"

            val json = JSONArray().put(JSONObject().apply {
                put("name", "getSunV3LocationSearchUrlConfig")
                put("params", JSONObject().apply {
                    put("query", query)
                    put("language", lang)
                    put("locationType", "locale")
                })
            })
            val response = api.searchPlacesByQuery(
                "https://weather.com/api/v1/p/redux-dal",
                json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            )

            val body = checkNotNull(response.body()) { "No Result found for '$query'" }
            val locations = body.asJsonObject
                .get("dal").asJsonObject
                .get("getSunV3LocationSearchUrlConfig").asJsonObject
                .get("language:$lang;locationType:locale;query:$query").asJsonObject
                .get("data").asJsonObject
                .get("location").asJsonObject

            val size = locations.get("address").asJsonArray.size()
            check(size > 0) { "No Result found for '$query'" }
            Resource.Success(List(size) {
                Triple(
                    locations.get("address").asJsonArray[it].asString,
                    locations.get("latitude").asJsonArray[it].asDouble,
                    locations.get("longitude").asJsonArray[it].asDouble,
                )
            })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred.")
        }
    }
}