package com.brohit.weatherapp.presentation

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brohit.weatherapp.R
import com.brohit.weatherapp.presentation.component.LottieAnimationItem
import com.brohit.weatherapp.presentation.ui.theme.DeepBlue
import com.brohit.weatherapp.presentation.ui.theme.WeatherAppTheme

@Composable
fun CheckGPSStatus(onGPSStatusChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(key1 = context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        onGPSStatusChanged(
            (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        )
    }
}

@Composable
fun MyApp(
    viewModel: WeatherViewModel
) {
    MyAppImpl(
        onAction = viewModel::handleAction,
        locationState = viewModel.locations,
        state = viewModel.state,
        searchQuery = { viewModel.searchQuery },
        searchToggle = viewModel.triggerSearch
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyAppImpl(
    onAction: (WeatherAction) -> Unit,
    locationState: SearchCityState,
    state: WeatherState,
    searchQuery: () -> String,
    searchToggle: Boolean
) {
    WeatherAppTheme(darkTheme = true) {
        Scaffold(topBar = {
            SearchBar(
                colors = SearchBarDefaults.colors(
                    containerColor = DeepBlue,
                    inputFieldColors = TextFieldDefaults.colors()
                ),
                query = searchQuery(),
                onQueryChange = { onAction(WeatherAction.SearchQuery(it)) },
                onSearch = {},
                active = searchToggle,
                onActiveChange = {
                    onAction(
                        if (it) WeatherAction.TriggerSearch
                        else WeatherAction.CloseSearch
                    )
                },
                placeholder = { Text(text = "Search city or by zip code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                leadingIcon = {
                    IconButton(onClick = {
                        onAction(WeatherAction.CloseSearch)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search city or by zip code"
                        )
                    }
                },
                trailingIcon = {
                    if (searchToggle) {
                        IconButton(
                            onClick = {
                                if (searchQuery().isBlank()) {
                                    onAction(WeatherAction.CloseSearch)
                                } else {
                                    onAction(WeatherAction.SearchQuery(""))
                                }

                            }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear"
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            onAction(WeatherAction.SearchWithMyLocation)
                        }) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "")
                        }
                    }
                }
            ) {
                AnimatedVisibility(
                    visible = locationState.isLoading
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimationItem(
                            lottieFile = R.raw.loading_weather,
                            modifier = Modifier.fillMaxWidth(.8f)
                        )
                    }
                }
                AnimatedVisibility(visible = locationState.error != null) {
                    Text(
                        text = locationState.error ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                LazyColumn {
                    locationState.cities.forEachIndexed { index, it ->
                        item(key = it) {
                            Surface(
                                color = DeepBlue,
                                onClick = {
                                    onAction(WeatherAction.Load(it.second, it.third, it.first))
                                    onAction(WeatherAction.CloseSearch)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(text = it.first)
                            }
                            if (index != locationState.cities.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    visible = locationState.cities.isEmpty()
                            && !locationState.isLoading
                            && locationState.error == null
                            && searchQuery().isBlank()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterVertically
                        )
                    ) {
                        LottieAnimationItem(
                            lottieFile = R.raw.city_search,
                            modifier = Modifier
                                .widthIn(max = 200.dp)
                                .aspectRatio(1f),
                            restartOnPlay = true
                        )
                        Text(
                            text = "Enter a city or zip code",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }) {
            val rs = rememberPullToRefreshState()

            if (!state.isLoading) {
                LaunchedEffect(true) {
                    rs.endRefresh()
                }
            }

            if (rs.isRefreshing) {
                LaunchedEffect(true) {
                    onAction(WeatherAction.Reload)
                }
            }

            Box(
                modifier = Modifier
                    .nestedScroll(rs.nestedScrollConnection)
                    .padding(it)
//                    .background(DarkBlue)
                    .fillMaxSize()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error?.let { error ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.error),
                            contentDescription = "Error",
                            modifier = Modifier.fillMaxSize(.8f)
                        )
                        Text(
                            text = error,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        val context = LocalContext.current
                        when (state.errorType) {
                            ErrorType.NO_GPS -> {
                                TextButton(onClick = {
                                    runCatching {
                                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                                            context.startActivity(this)
                                        }
                                    }
                                }) {
                                    Text(text = "Enable GPS")
                                }
                            }

                            ErrorType.NO_LOCATION_PERMISSION -> {
                                TextButton(onClick = {
                                    runCatching {
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts(
                                                "package",
                                                context.packageName,
                                                null
                                            )
                                            context.startActivity(this)
                                        }
                                    }
                                }) {
                                    Text(text = "Enable Location Permission")
                                }
                            }

                            else -> {
                                TextButton(onClick = {
                                    onAction(WeatherAction.CanLoadFromGeoLocation)
                                }) {
                                    Text(text = "Retry")
                                }

                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    WeatherCard(
                        state = state,
                        backgroundColor = DeepBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WeatherForecast(state = state)
                }
                PullToRefreshContainer(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = rs,
                )
            }

        }
    }
}