package com.brohit.weatherapp.data.repository

import com.brohit.weatherapp.data.data_source.local.dao.KeyValueDao
import com.brohit.weatherapp.data.data_source.local.entity.KeyValueEntity
import com.brohit.weatherapp.data.mappers.toWeatherInfo
import com.brohit.weatherapp.data.data_source.remote.WeatherApi
import com.brohit.weatherapp.domain.repository.WeatherRepository
import com.brohit.weatherapp.domain.util.Resource
import com.brohit.weatherapp.domain.weather.WeatherInfo
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val keyValueDao: KeyValueDao,
) : WeatherRepository {

    override suspend fun getWeatherData(lat: Double, long: Double): Resource<WeatherInfo> {
        return try {
            Resource.Success(
                data = api.getWeatherData(
                    lat = lat,
                    long = long
                ).toWeatherInfo()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "An unknown error occurred.")
        }
    }


    override suspend fun getMyIpGeoLocation(): Resource<Pair<Double, Double>> {
        return try {
            val savedIp = keyValueDao.get("ip")
            val myCurrentIp = getMyCurrentIp().getOrNull()
            if (savedIp != myCurrentIp && myCurrentIp != null) {
                keyValueDao.put(KeyValueEntity("ip", myCurrentIp))
                val response =
                    api.getMyIpGeoLocation("https://ipapi.co/$myCurrentIp/json/").body()!!

                keyValueDao.put(KeyValueEntity("ip_lat", response.getValue("latitude").toString()))
                keyValueDao.put(KeyValueEntity("ip_lon", response.getValue("longitude").toString()))
            }
            val data =
                keyValueDao.get("ip_lat")!!.toDouble() to keyValueDao.get("ip_lon")!!.toDouble()
            Resource.Success(data)

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "An unknown error occurred.")
        }
    }


    private suspend fun getMyCurrentIp(): Result<String?> {
        return kotlin.runCatching {
            api.getMyIpDetails("https://cloudflare.com/cdn-cgi/trace").body()!!
                .lines()
                .associate {
                    val (key, value) = it.split("=", limit = 2)
                    key to value
                }["ip"]
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

            val body = response.body()!!

            val locations = body.asJsonObject
                .get("dal").asJsonObject
                .get("getSunV3LocationSearchUrlConfig").asJsonObject
                .get("language:$lang;locationType:locale;query:$query").asJsonObject
                .get("data").asJsonObject
                .get("location").asJsonObject

            Resource.Success(List(locations.get("address").asJsonArray.size()) {
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