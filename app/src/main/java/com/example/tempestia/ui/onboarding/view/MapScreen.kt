package com.example.tempestia.ui.onboarding.view

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun MapScreen(
    onBack: () -> Unit,
    onLocationSelected: (Double, Double) -> Unit
) {
    val colors = LocalTempestiaColors.current
    val context = LocalContext.current

    val alexandria = LatLng(31.2001, 29.9187)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(alexandria, 12f)
    }

    var addressText by remember {
        mutableStateOf("Drag the map to position the pin exactly where you want to track the weather.")
    }
    var hasDragged by remember { mutableStateOf(false) }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            hasDragged = true
            addressText = "Updating location..."
        } else if (hasDragged) {
            val target = cameraPositionState.position.target
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())

                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(target.latitude, target.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        addressText = addresses[0].getAddressLine(0) ?: "Unknown Location"
                    } else {
                        addressText = String.format(Locale.US, "Lat: %.4f, Lng: %.4f", target.latitude, target.longitude)
                    }
                } catch (e: Exception) {
                    addressText = String.format(Locale.US, "Lat: %.4f, Lng: %.4f", target.latitude, target.longitude)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false
            )
        )

        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Center Pin",
            tint = colors.purpleBright,
            modifier = Modifier.size(48.dp).align(Alignment.Center).offset(y = (-24).dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.text1,
                modifier = Modifier.size(42.dp).background(colors.bgCard, CircleShape)
                    .border(1.dp, colors.glassBorder, CircleShape).padding(10.dp)
                    .clickable(onClick = onBack)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp)
                .clip(RoundedCornerShape(32.dp)).background(colors.bgCard)
                .border(1.dp, colors.glassBorder, RoundedCornerShape(32.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set Custom Location",
                color = colors.text1,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = addressText,
                color = if (cameraPositionState.isMoving) colors.purpleBright else colors.text3,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            PulsingButton(
                text = "Confirm Location",
                onClick = {
                    val selectedLatLng = cameraPositionState.position.target
                    onLocationSelected(selectedLatLng.latitude, selectedLatLng.longitude)
                }
            )
        }
    }
}