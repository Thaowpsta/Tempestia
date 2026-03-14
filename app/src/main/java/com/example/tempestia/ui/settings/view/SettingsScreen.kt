package com.example.tempestia.ui.settings.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.tempestia.R
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.ui.settings.viewModel.LocationFetchState
import com.example.tempestia.ui.settings.viewModel.SettingsViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.tempestia.utils.AnimatedParticleBackground

fun Context.currentConnectivityState(): Flow<Boolean> = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { trySend(true) }
        override fun onLost(network: Network) { trySend(false) }
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            trySend(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        }
    }
    val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    connectivityManager.registerNetworkCallback(request, callback)
    val activeNetwork = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    trySend(networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
    awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
}.distinctUntilChanged()

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigateToFavorites: () -> Unit = {}) {
    val colors = LocalTempestiaColors.current
    val context = LocalContext.current

    val prefs by viewModel.preferences.collectAsState()
    val fetchState by viewModel.locationFetchState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isOnline by context.currentConnectivityState().collectAsState(initial = true)

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, context.getString(it), Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val hasLoc = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (hasLoc) viewModel.fetchLocation()
            else viewModel.showToast(R.string.toast_loc_denied)
        }
    )

    if (prefs.showMapDialog) {
        Dialog(
            onDismissRequest = { viewModel.setShowMapDialog(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            com.example.tempestia.ui.onboarding.view.MapScreen(
                onBack = { viewModel.setShowMapDialog(false) },
                onLocationSelected = { lat, lng ->
                    viewModel.saveLocation(lat, lng)
                    viewModel.showToast(R.string.toast_map_success)
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bgDeep)) {
        AnimatedParticleBackground()

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    color = colors.text1,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.location_header),
                    color = colors.text3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )

                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.MyLocation,
                        title = stringResource(R.string.location_method),
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingsChip(stringResource(R.string.gps), prefs.locationMethod == "GPS") {
                                    if (isOnline) {
                                        viewModel.setLocationMethod("GPS")
                                        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                                        if (hasFine || hasCoarse) viewModel.fetchLocation()
                                        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    } else viewModel.showToast(R.string.no_internet)
                                }
                                SettingsChip(stringResource(R.string.manual_map), prefs.locationMethod == "Map") {
                                    if (isOnline) viewModel.setShowMapDialog(true)
                                    else viewModel.showToast(R.string.no_internet)
                                }
                            }
                        }
                    )

                    HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.5f))

                    val locationSubtitle = when (fetchState) {
                        is LocationFetchState.Fetching -> stringResource(R.string.locating)
                        is LocationFetchState.Success -> (fetchState as LocationFetchState.Success).locationName
                        is LocationFetchState.Error -> stringResource((fetchState as LocationFetchState.Error).messageResId)
                        is LocationFetchState.Idle -> prefs.locationName
                    }

                    SettingsRow(
                        icon = Icons.Filled.Map,
                        title = stringResource(R.string.current_location),
                        subtitle = locationSubtitle,
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.change_btn), color = colors.text2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = colors.text2, modifier = Modifier.size(12.dp))
                            }
                        },
                        onClick = {
                            if (isOnline) {
                                if (prefs.locationMethod == "GPS") onNavigateToFavorites()
                                else viewModel.setShowMapDialog(true)
                            } else viewModel.showToast(R.string.no_internet)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                Text(text = stringResource(R.string.preferences_header), color = colors.text3, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))

                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Thermostat,
                        title = stringResource(R.string.temperature_unit),
                        subtitle = stringResource(R.string.temperature_desc),
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingsChip("°C", prefs.isCelsius) { viewModel.setCelsius(true) }
                                SettingsChip("°F", !prefs.isCelsius) { viewModel.setCelsius(false) }
                            }
                        }
                    )
                    HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Filled.AccessTime,
                        title = stringResource(R.string.time_format),
                        subtitle = stringResource(R.string.time_desc),
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingsChip("12h", !prefs.is24Hour) { viewModel.set24Hour(false) }
                                SettingsChip("24h", prefs.is24Hour) { viewModel.set24Hour(true) }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                Text(text = stringResource(R.string.appearance_header), color = colors.text3, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))

                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Palette,
                        title = stringResource(R.string.app_theme),
                        subtitle = stringResource(R.string.theme_desc),
                        trailingContent = {
                            Column(modifier = Modifier.width(160.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SettingsChip(stringResource(R.string.light_theme), prefs.themeMode == "Light", Modifier.weight(1f)) { viewModel.setThemeMode("Light") }
                                    SettingsChip(stringResource(R.string.dark_theme), prefs.themeMode == "Dark", Modifier.weight(1f)) { viewModel.setThemeMode("Dark") }
                                }
                                SettingsChip(stringResource(R.string.system_theme), prefs.themeMode == "System", Modifier.fillMaxWidth()) { viewModel.setThemeMode("System") }
                            }
                        }
                    )
                    HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Filled.Language,
                        title = stringResource(R.string.language),
                        subtitle = stringResource(R.string.language_desc),
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingsChip("EN", prefs.language == "en") { viewModel.setLanguage("en") }
                                SettingsChip("AR", prefs.language == "ar") { viewModel.setLanguage("ar") }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.app_footer),
                    color = colors.text3,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 120.dp)
                )
            }

    }
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalTempestiaColors.current
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.glass).border(1.dp, colors.glassBorder, RoundedCornerShape(22.dp))) { content() }
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String? = null, trailingContent: @Composable () -> Unit, onClick: (() -> Unit)? = null) {
    val colors = LocalTempestiaColors.current
    Row(modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable { onClick() } else Modifier).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(colors.purpleBright.copy(alpha = 0.15f)).border(1.dp, colors.purpleBright.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = colors.purpleBright, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.text1, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = colors.text3, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        trailingContent()
    }
}

@Composable
fun SettingsChip(text: String, isActive: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val bgColor by animateColorAsState(if (isActive) colors.purpleCore else colors.glass, label = "bg")
    val textColor by animateColorAsState(if (isActive) Color.White else colors.text2, label = "text")
    val borderColor by animateColorAsState(if (isActive) Color.Transparent else colors.glassBorder, label = "border")

    Box(modifier = modifier.clip(RoundedCornerShape(50.dp)).background(bgColor).border(1.dp, borderColor, RoundedCornerShape(50.dp)).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}