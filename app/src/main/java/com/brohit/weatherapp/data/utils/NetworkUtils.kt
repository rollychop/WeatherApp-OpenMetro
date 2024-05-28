package com.brohit.weatherapp.data.utils

import android.util.Log
import com.brohit.weatherapp.data.repository.OpenWeatherRepositoryImpl
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

@Throws(IllegalStateException::class)
fun <T> Response<T>.checkAndGetBody(): T {

    check(isSuccessful) {
        val json = JSONObject(errorBody()?.string() ?: "")
        if (json.has("error")) {
            json.getString("error")
        } else message()
    }
    return checkNotNull(body())
}


inline fun <T, R> T.runCatchingCustom(block: (OpenWeatherRepositoryImpl) -> Unit): Result<R> {
    return try {
        val value = block()
        Result.success(value)
    } catch (e: Exception) {
        Log.e("RunCatchingCustom", "$this", e)

        if (e is CancellationException) throw e
        if (e is HttpException) {
            if (e.code() == 401) return Result.failure(
                Exception(
                    "You're not unauthorized to access. " +
                            "Please check login status"
                )
            )
            return Result.failure(Exception("Http Exception"))
        }

        if (e is ConnectException) {
            return Result.failure(Exception("Unable to connect to the server"))
        }

        if (e is SocketTimeoutException) {
            return Result.failure(
                Exception(
                    "Server is taking longer time than expected. " +
                            "Please check your connection or try again later"
                )
            )
        }

        if (e is IOException) {
            return Result.failure(Exception("Internet Error Please check your connection and try again"))
        }

        Result.failure(e)
    }
}