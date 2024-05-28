package com.brohit.weatherapp.data.data_source.remote.interceptor

import com.brohit.weatherapp.common.Constant
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        if (request.url.host == "api.openweathermap.org") {
            builder.url(
                chain.request()
                    .url.newBuilder()
                    .addQueryParameter("appId", Constant.API_KEY)
                    .build()
            )
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}