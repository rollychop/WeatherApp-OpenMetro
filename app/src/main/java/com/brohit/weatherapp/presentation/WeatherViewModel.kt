package com.brohit.weatherapp.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.weatherapp.domain.location.LocationException
import com.brohit.weatherapp.domain.repository.WeatherRepository
import com.brohit.weatherapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class WeatherAction {
    class Load(
        val lat: Double,
        val lon: Double,
        val address: String
    ) : WeatherAction()

    data object TriggerSearch : WeatherAction()
    data object CloseSearch : WeatherAction()
    data class SearchQuery(val query: String) : WeatherAction()
    data object CanLoadFromGeoLocation : WeatherAction()

    data object SearchWithMyLocation : WeatherAction()
    data object Reload : WeatherAction()
}


enum class ErrorType {
    NO_LOCATION_PERMISSION,
    NO_GPS
}

data class SearchCityState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cities: List<Triple<String, Double, Double>> = emptyList(),
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    var state by mutableStateOf(WeatherState())
        private set
    var locations by mutableStateOf(SearchCityState())
        private set

    var searchQuery by mutableStateOf("")
        private set

    var triggerSearch by mutableStateOf(false)
        private set

    private fun initLoading() {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )
            val result = repository.getCurrentLocation()
            val (lat, lon, address) = result.getOrNull() ?: kotlin.run {
                state = state.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Please Search for location",
                    errorType = when (result.exceptionOrNull()) {
                        is LocationException.LocationPermissionDeniedException -> ErrorType.NO_LOCATION_PERMISSION
                        is LocationException.GPSDisabledException -> ErrorType.NO_GPS
                        else -> null
                    }
                )
                triggerSearch = true
                return@launch
            }
            state = state.copy(
                isLoading = false,
                error = null
            )
            loadWeatherInfo(lat, lon, address)
            searchQuery = address
        }
    }

    private fun loadWeatherInfo(lat: Double, lon: Double, address: String) {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )
            repository.getWeatherData(lat, lon, address).fold(
                onSuccess = {
                    state = state.copy(
                        weatherInfo = it,
                        isLoading = false,
                        error = null
                    )
                },

                onFailure = {
                    state = state.copy(
                        weatherInfo = null,
                        isLoading = false,
                        error = it.message
                    )
                })
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
                loadWeatherInfo(action.lat, action.lon, action.address)
                searchQuery = action.address
            }

            is WeatherAction.SearchQuery -> {
                onSearchQueryChange(action.query)
            }

            WeatherAction.CanLoadFromGeoLocation -> {
                initLoading()
            }

            WeatherAction.CloseSearch -> {
                triggerSearch = false
            }

            WeatherAction.TriggerSearch -> {
                triggerSearch = true
            }

            WeatherAction.SearchWithMyLocation -> {
                viewModelScope.launch {
                    state = state.copy(
                        isLoading = true,
                        error = null
                    )
                    repository.searchWithGps().fold(
                        onSuccess = { (it, add) ->
                            state = state.copy(
                                weatherInfo = it,
                                isLoading = false,
                                error = null
                            )
                            searchQuery = add
                        },

                        onFailure = {
                            state = state.copy(
                                weatherInfo = null,
                                isLoading = false,
                                error = it.message,
                                errorType = when (it) {
                                    is LocationException.LocationPermissionDeniedException -> ErrorType.NO_LOCATION_PERMISSION
                                    is LocationException.GPSDisabledException -> ErrorType.NO_GPS
                                    else -> null
                                }
                            )
                        })
                }
            }

            WeatherAction.Reload -> {
                initLoading()
            }
        }
    }

}