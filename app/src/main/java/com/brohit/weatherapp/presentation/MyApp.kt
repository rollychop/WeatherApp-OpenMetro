package com.brohit.weatherapp.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brohit.weatherapp.presentation.ui.theme.DarkBlue
import com.brohit.weatherapp.presentation.ui.theme.DeepBlue
import com.brohit.weatherapp.presentation.ui.theme.WeatherAppTheme

@Composable
fun MyApp(
    viewModel: WeatherViewModel
) {
    MyAppImpl(
        onAction = viewModel::handleAction,
        locationState = viewModel.locations,
        state = viewModel.state,
        searchQuery = { viewModel.searchQuery }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyAppImpl(
    onAction: (WeatherAction) -> Unit,
    locationState: SearchCityState,
    state: WeatherState,
    searchQuery: () -> String
) {
    WeatherAppTheme(darkTheme = true) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            var searchToggle by rememberSaveable {
                mutableStateOf(false)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBlue)
                    .systemBarsPadding()
            ) {
                AnimatedVisibility(visible = searchToggle) {
                    SearchBar(
                        query = searchQuery(),
                        onQueryChange = { onAction(WeatherAction.SearchQuery(it)) },
                        onSearch = {},
                        active = searchToggle,
                        onActiveChange = { searchToggle = it },
                        placeholder = { Text(text = "Search city or by zip code") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            IconButton(onClick = { searchToggle = false }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search city or by zip code"
                                )
                            }
                        },
                        trailingIcon = {
                            if (locationState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    ) {
                        LazyColumn {
                            locationState.cities.forEachIndexed { index, it ->
                                item(key = it) {
                                    Surface(
                                        onClick = {
                                            onAction(WeatherAction.Load(it.second, it.third))
                                            searchToggle = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text(text = it.first)
                                    }
                                    if (index != locationState.cities.lastIndex) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            searchToggle = true
                        }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "search")
                    }
                }
                WeatherCard(
                    state = state,
                    backgroundColor = DeepBlue
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeatherForecast(state = state)
            }
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            state.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}