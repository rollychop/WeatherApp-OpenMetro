package com.brohit.weatherapp.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.weatherapp.domain.location.LocationTracker
import com.brohit.weatherapp.domain.repository.WeatherRepository
import com.brohit.weatherapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class WeatherAction {
    class Load(val lat: Double, val lon: Double) : WeatherAction()
    data class SearchQuery(val query: String) : WeatherAction()
    data object CanLoadFromGeoLocation : WeatherAction()
}


data class SearchCityState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cities: List<Triple<String, Double, Double>> = emptyList()
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {

    var state by mutableStateOf(WeatherState())
        private set
    var locations by mutableStateOf(SearchCityState())
        private set

    var searchQuery by mutableStateOf("")
        private set

    private fun initLoading() {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )
            val (lat, lon) = locationTracker.getCurrentLocation()?.run { latitude to longitude }
                ?: run {
                    val myIpGeoLocation = repository.getMyIpGeoLocation()
                    if ((myIpGeoLocation is Resource.Success && myIpGeoLocation.data != null)) {
                        myIpGeoLocation.data
                    } else {
                        90.0 to 23.0
                    }
                }
            state = state.copy(
                isLoading = false,
                error = null
            )
            loadWeatherInfo(lat, lon)
        }
    }

    private fun loadWeatherInfo(lat: Double, lon: Double) {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )
            when (val result = repository.getWeatherData(lat, lon)) {
                is Resource.Success -> {
                    state = state.copy(
                        weatherInfo = result.data,
                        isLoading = false,
                        error = null
                    )
                }

                is Resource.Error -> {
                    state = state.copy(
                        weatherInfo = null,
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    private var searchJob: Job? = null
    private fun onSearchQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            locations = locations.copy(
                isLoading = true,
                error = null
            )
            if (query.isBlank()) {
                locations = SearchCityState()
                return@launch
            }
            delay(1000)
            locations = when (val r = repository.searchCity(query)) {
                is Resource.Error -> {
                    locations.copy(
                        isLoading = false,
                        error = r.message
                    )
                }

                is Resource.Success -> {
                    locations.copy(
                        isLoading = false,
                        error = null,
                        cities = r.data ?: emptyList()
                    )
                }
            }
        }
    }


    fun handleAction(action: WeatherAction) {
        when (action) {
            is WeatherAction.Load -> {
                loadWeatherInfo(action.lat, action.lon)
            }

            is WeatherAction.SearchQuery -> {
                onSearchQueryChange(action.query)
            }

            WeatherAction.CanLoadFromGeoLocation -> {
                initLoading()
            }
        }
    }

}