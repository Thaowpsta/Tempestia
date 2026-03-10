package com.example.tempestia.ui.onboarding.view

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tempestia.R
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onBack: () -> Unit,
    onLocationSelected: (Double, Double) -> Unit
) {
    val colors = LocalTempestiaColors.current
    val context = LocalContext.current

    val alexandria = LatLng(31.2001, 29.9187)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(alexandria, 12f)
    }

    val addressTextState by viewModel.addressText.collectAsState()
    val hasDragged by viewModel.hasDragged.collectAsState()

    val addressText = addressTextState ?: context.getString(R.string.drag_map_instruction)

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            viewModel.setHasDragged(true)
            viewModel.setAddressText(context.getString(R.string.updating_location))
        } else if (hasDragged) {
            val target = cameraPositionState.position.target
            viewModel.fetchAddressFromLatLng(context, target.latitude, target.longitude)
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
                text = stringResource(R.string.set_custom_location),
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
                text = stringResource(R.string.confirm_location),
                onClick = {
                    val selectedLatLng = cameraPositionState.position.target
                    onLocationSelected(selectedLatLng.latitude, selectedLatLng.longitude)
                }
            )
        }
    }
}