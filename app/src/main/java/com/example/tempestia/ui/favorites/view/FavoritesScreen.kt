package com.example.tempestia.ui.favorites.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import com.example.tempestia.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.data.weather.model.GeoResponse
import com.example.tempestia.ui.favorites.viewModel.FavoriteWeatherState
import com.example.tempestia.ui.favorites.viewModel.FavoritesViewModel
import com.example.tempestia.ui.onboarding.view.AnimatedParticleBackground
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.utils.getWeatherEmoji
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onCitySelected: (Double, Double, String) -> Unit = { _, _, _ -> }
) {
    val colors = LocalTempestiaColors.current

    val currentContext = LocalContext.current
    val currentConfig = LocalConfiguration.current
    val currentLayoutDir = LocalLayoutDirection.current

    val favoriteWeather by viewModel.favoriteWeather.collectAsState()

    val localSearchQuery by viewModel.localSearchQuery.collectAsState()
    val showAddSearchDialog by viewModel.showAddSearchDialog.collectAsState()
    val cityToConfirmAdd by viewModel.cityToConfirmAdd.collectAsState()
    val cityToConfirmDelete by viewModel.cityToConfirmDelete.collectAsState()

    val filteredWeather = favoriteWeather.filter {
        it.city.cityName.contains(localSearchQuery, ignoreCase = true)
    }

    if (cityToConfirmAdd != null) {
        AlertDialog(
            onDismissRequest = { viewModel.setCityToConfirmAdd(null) },
            containerColor = colors.bgCard.copy(alpha = 1f),
            titleContentColor = colors.text1,
            textContentColor = colors.text3,
            title = { Text(stringResource(R.string.add_location_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.add_location_desc,
                        cityToConfirmAdd?.name ?: ""
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    cityToConfirmAdd?.let { viewModel.addFavorite(it) }
                    viewModel.setCityToConfirmAdd(null)
                    viewModel.setShowAddSearchDialog(false)
                    viewModel.clearApiSearch()
                }) {
                    Text(
                        stringResource(R.string.add_city_btn),
                        color = colors.purpleBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setCityToConfirmAdd(null) }) {
                    Text(
                        stringResource(R.string.cancel_btn),
                        color = colors.text3
                    )
                }
            }
        )
    }

    if (cityToConfirmDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.setCityToConfirmDelete(null) },
            containerColor = colors.bgCard.copy(alpha = 1f),
            titleContentColor = colors.text1,
            textContentColor = colors.text3,
            title = { Text(stringResource(R.string.remove_location_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.remove_location_desc,
                        cityToConfirmDelete?.cityName ?: ""
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    cityToConfirmDelete?.let { viewModel.removeCity(it) }
                    viewModel.setCityToConfirmDelete(null)
                }) {
                    Text(
                        stringResource(R.string.remove_btn),
                        color = Color(0xFFFF4B4B),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setCityToConfirmDelete(null) }) {
                    Text(
                        stringResource(R.string.cancel_btn),
                        color = colors.text3
                    )
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        AnimatedParticleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            OutlinedTextField(
                value = localSearchQuery,
                onValueChange = { viewModel.updateLocalSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(colors.glass)
                    .border(1.dp, colors.glassBorder, RoundedCornerShape(32.dp)),
                placeholder = { Text(stringResource(R.string.search_saved), color = colors.text3) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = colors.purpleBright
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = colors.text1,
                    unfocusedTextColor = colors.text1,
                    cursorColor = colors.purpleBright
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.saved_locations_count, filteredWeather.size),
                color = colors.text3,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredWeather, key = { it.city.cityName }) { state ->
                    StableSwipeToDismissCard(
                        state = state,
                        onDeleteRequest = { viewModel.setCityToConfirmDelete(state.city) },
                        onClick = {
                            onCitySelected(
                                state.city.lat,
                                state.city.lon,
                                state.city.cityName
                            )
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.setShowAddSearchDialog(true) },
            containerColor = colors.purpleBright,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp)
                .size(64.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add City",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }

    val apiSearchQuery by viewModel.apiSearchQuery.collectAsState()
    val apiSearchResults by viewModel.apiSearchResults.collectAsState()
    val isSearchingApi by viewModel.isSearchingApi.collectAsState()

    if (showAddSearchDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = {
                viewModel.setShowAddSearchDialog(false)
                viewModel.clearApiSearch()
            },
            sheetState = sheetState,
            containerColor = colors.bgDeep,
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.text3) }
        ) {
            CompositionLocalProvider(
                LocalContext provides currentContext,
                LocalConfiguration provides currentConfig,
                LocalLayoutDirection provides currentLayoutDir
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 64.dp)
                ) {
                    Text(
                        stringResource(R.string.add_new_city_title),
                        color = colors.text1,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = apiSearchQuery,
                        onValueChange = { viewModel.onApiSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(colors.glass),
                        placeholder = {
                            Text(
                                stringResource(R.string.search_openweathermap),
                                color = colors.text3
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = colors.purpleBright
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = colors.text1,
                            unfocusedTextColor = colors.text1
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSearchingApi) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.purpleBright)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                            itemsIndexed(apiSearchResults ?: emptyList()) { index, result ->
                                SearchResultRow(result) { viewModel.setCityToConfirmAdd(result) }
                                if (index < (apiSearchResults?.size ?: 0) - 1) {
                                    HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StableSwipeToDismissCard(
    state: FavoriteWeatherState,
    onDeleteRequest: () -> Unit,
    onClick: () -> Unit
) {
    //This MUST remain mutableFloatStateOf. to prevent UI jank.
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "swipe")
    val colors = LocalTempestiaColors.current
    val density = LocalDensity.current

    val iconAlpha = (-animatedOffsetX / 200f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.glass)
            .border(1.dp, colors.glassBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
    ) {
        if (animatedOffsetX < 0f) {
            val revealWidth = with(density) { (-animatedOffsetX).toDp() }

            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(revealWidth)
                        .fillMaxHeight()
                        .background(Color(0xFFFF4B4B))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(end = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .alpha(iconAlpha) // Smoothly fades in!
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -250f) {
                                onDeleteRequest()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (dragAmount < 0 || offsetX < 0) {
                                offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                            }
                        }
                    )
                }
                .clickable { onClick() }
                .padding(20.dp)
        ) {
            FavoriteCityCardContent(state)
        }
    }
}

@Composable
fun FavoriteCityCardContent(state: FavoriteWeatherState) {
    val colors = LocalTempestiaColors.current
    val city = state.city

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (city.isCurrentLocation) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Current Location",
                            tint = colors.purpleBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.current_badge),
                            color = colors.purpleBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Text(
                    text = city.cityName.uppercase(),
                    color = colors.text1,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (city.country != null) {
                    Text(
                        text = "${countryCodeToEmoji(city.country)} ${city.country}",
                        color = colors.text2,
                        fontSize = 14.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = colors.purpleBright,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getWeatherEmoji(state.iconCode ?: ""),
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${state.temp?.roundToInt() ?: "--"}°",
                            color = colors.text1,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = state.condition ?: stringResource(R.string.unknown),
                        color = colors.text3,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val windKmh = state.windSpeed?.let { (it * 3.6).roundToInt() } ?: 0

            MetricText(stringResource(R.string.humidity), "${state.humidity ?: 0}%")
            Spacer(modifier = Modifier.weight(1f))
            MetricText(stringResource(R.string.wind), "$windKmh km/h")
            Spacer(modifier = Modifier.weight(1f))
            MetricText(stringResource(R.string.uv), "${state.uvi?.roundToInt() ?: 0}")
        }
    }
}

@Composable
fun SearchResultRow(result: GeoResponse, onClick: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val locationText = listOfNotNull(result.state, result.country).joinToString(", ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.purpleBright)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = result.name,
                color = colors.text1,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (locationText.isNotEmpty()) {
                Text(text = locationText, color = colors.text3, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun MetricText(label: String, value: String) {
    val colors = LocalTempestiaColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label ", color = colors.text3, fontSize = 13.sp)
        Text(text = value, color = colors.text1, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

fun countryCodeToEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🌍"
    val firstLetter = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}