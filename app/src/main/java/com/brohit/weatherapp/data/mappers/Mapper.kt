package com.brohit.weatherapp.data.mappers

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.brohit.weatherapp.data.data_source.remote.CityGeoDto
import com.brohit.weatherapp.data.data_source.remote.OpenWeatherDto
import com.brohit.weatherapp.data.data_source.remote.Weather1Dto
import com.brohit.weatherapp.domain.weather.CloudsModel
import com.brohit.weatherapp.domain.weather.CoordModel
import com.brohit.weatherapp.domain.weather.GeoCityModel
import com.brohit.weatherapp.domain.weather.MainModel
import com.brohit.weatherapp.domain.weather.RainModel
import com.brohit.weatherapp.domain.weather.SysModel
import com.brohit.weatherapp.domain.weather.Weather1Model
import com.brohit.weatherapp.domain.weather.WeatherModel
import com.brohit.weatherapp.domain.weather.WindModel

fun CityGeoDto.toModel() = GeoCityModel(
    name = name,
    lat = lat,
    lon = lon,
    country = country,
)


fun OpenWeatherDto.toModel() = WeatherModel(
    coord = CoordModel(
        coord.lon,
        coord.lat
    ),
    weather = weather.map { it.toModel() },
    base = base,
    main = MainModel(
        main.temp,
        main.feelsLike,
        main.tempMin,
        main.tempMax,
        main.pressure,
        main.humidity,
        main.seaLevel,
        main.grndLevel
    ),
    visibility = visibility,
    wind = wind?.let {
        WindModel(
            wind.speed,
            wind.deg,
            wind.gust
        )
    },
    clouds = clouds?.let {
        CloudsModel(
            clouds.all
        )
    },
    dt = dt,
    sys = sys?.let {
        SysModel(
            sys.type,
            sys.id,
            sys.country,
            sys.sunrise,
            sys.sunset
        )
    },
    timezone = timezone,
    id = id,
    name = name,
    cod = cod,
    rain = rain?.let {
        RainModel(rain.n1h)
    }

)

fun Weather1Dto.toModel() = Weather1Model(
    id = id,
    main = main,
    description = description,
    icon = icon
)

fun hexColorToColor(hexCode: String): Color {
    val c = android.graphics.Color.parseColor(hexCode)
    return Color(c.red, c.green, c.blue, c.alpha)
}
