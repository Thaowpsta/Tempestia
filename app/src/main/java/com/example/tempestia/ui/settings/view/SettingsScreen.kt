package com.example.tempestia.ui.settings.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.tempestia.ui.onboarding.view.AnimatedParticleBackground
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.ui.settings.viewModel.SettingsViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.tempestia.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigateToFavorites: () -> Unit = {}) {
    val colors = LocalTempestiaColors.current
    val context = LocalContext.current

    val isCelsius by viewModel.isCelsiusFlow.collectAsState(initial = true)
    val is24Hour by viewModel.is24HourFlow.collectAsState(initial = false)
    val themeMode by viewModel.themeModeFlow.collectAsState(initial = "System")

    var locationMethod by remember { mutableStateOf("GPS") }
    val locationName by viewModel.locationNameFlow.collectAsState(initial = context.getString(R.string.locating))

    var showMap by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val currentLanguage by viewModel.languageFlow.collectAsState(initial = "en")

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        isFetchingLocation = true
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                isFetchingLocation = false
                if (location != null) {
                    viewModel.saveLocation(location.latitude, location.longitude)
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_gps_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_gps_failed), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                isFetchingLocation = false
                Toast.makeText(context, context.getString(R.string.toast_loc_failed), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val hasLoc = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (hasLoc) fetchLocation()
            else Toast.makeText(context, context.getString(R.string.toast_loc_denied), Toast.LENGTH_SHORT).show()
        }
    )

    fun requestLocationAndFetch() {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) fetchLocation()
        else permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    if (showMap) {
        Dialog(
            onDismissRequest = { showMap = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            com.example.tempestia.ui.onboarding.view.MapScreen(
                onBack = {
                    showMap = false
                },
                onLocationSelected = { lat, lng ->
                    viewModel.saveLocation(lat, lng)
                    showMap = false
                    locationMethod = "Map"
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_map_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(colors.bgDeep)) {
        AnimatedParticleBackground()

        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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
                                SettingsChip(stringResource(R.string.gps), locationMethod == "GPS") {
                                    locationMethod = "GPS"
                                    requestLocationAndFetch()
                                }
                                SettingsChip(stringResource(R.string.manual_map), locationMethod == "Map") {
                                    showMap = true
                                }
                            }
                        }
                    )

                    HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.5f))

                    SettingsRow(
                        icon = Icons.Filled.Map,
                        title = stringResource(R.string.current_location),
                        subtitle = if (isFetchingLocation) stringResource(R.string.locating) else locationName,
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.change_btn),
                                    color = colors.text2,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = colors.text2,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        },
                        onClick = {
                            if (locationMethod == "GPS") {
                                onNavigateToFavorites()
                            } else {
                                showMap = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.preferences_header),
                    color = colors.text3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )

                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Thermostat,
                        title = stringResource(R.string.temperature_unit),
                        subtitle = stringResource(R.string.temperature_desc),
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingsChip("°C", isCelsius) { viewModel.setCelsius(true) }
                                SettingsChip("°F", !isCelsius) { viewModel.setCelsius(false) }
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
                                SettingsChip("12h", !is24Hour) { viewModel.set24Hour(false) }
                                SettingsChip("24h", is24Hour) { viewModel.set24Hour(true) }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.appearance_header),
                    color = colors.text3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )

                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Palette,
                        title = stringResource(R.string.app_theme),
                        subtitle = stringResource(R.string.theme_desc),
                        trailingContent = {
                            Column(
                                modifier = Modifier.width(160.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SettingsChip(
                                        stringResource(R.string.light_theme),
                                        themeMode == "Light",
                                        Modifier.weight(1f)
                                    ) { viewModel.setThemeMode("Light") }
                                    SettingsChip(
                                        stringResource(R.string.dark_theme),
                                        themeMode == "Dark",
                                        Modifier.weight(1f)
                                    ) { viewModel.setThemeMode("Dark") }
                                }
                                SettingsChip(
                                    stringResource(R.string.system_theme),
                                    themeMode == "System",
                                    Modifier.fillMaxWidth()
                                ) { viewModel.setThemeMode("System") }
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
                                SettingsChip("EN", currentLanguage == "en") { viewModel.setLanguage("en") }
                                SettingsChip("AR", currentLanguage == "ar") { viewModel.setLanguage("ar") }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = stringResource(R.string.app_footer),
                color = colors.text3,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 120.dp, top = 16.dp)
            )
        }
    }
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalTempestiaColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.glass)
            .border(1.dp, colors.glassBorder, RoundedCornerShape(22.dp))
    ) {
        content()
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalTempestiaColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.purpleBright.copy(alpha = 0.15f))
                .border(1.dp, colors.purpleBright.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.purpleBright, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.text1,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = colors.text3,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        trailingContent()
    }
}

@Composable
fun SettingsChip(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalTempestiaColors.current

    val bgColor by animateColorAsState(
        if (isActive) colors.purpleCore else colors.glass,
        label = "bg"
    )
    val textColor by animateColorAsState(
        if (isActive) Color.White else colors.text2,
        label = "text"
    )
    val borderColor by animateColorAsState(
        if (isActive) Color.Transparent else colors.glassBorder,
        label = "border"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}