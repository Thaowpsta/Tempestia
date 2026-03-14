package com.example.tempestia.ui.onboarding.view.components

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.tempestia.R
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModel
import com.example.tempestia.utils.showToast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun PermissionsScreen(viewModel: OnboardingViewModel, onBack: () -> Unit, onFinish: (Double?, Double?) -> Unit, onOpenMap: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val isFetchingLocation by viewModel.isFetchingLocation.collectAsState()

    @SuppressLint("MissingPermission")
    fun fetchLocationAndFinish() {
        viewModel.setIsFetchingLocation(true)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                viewModel.setIsFetchingLocation(false)
                if (location != null) {
                    onFinish(location.latitude, location.longitude)
                } else {
                    context.showToast(R.string.toast_gps_signal_lost)
                    onOpenMap()
                }
            }
            .addOnFailureListener {
                viewModel.setIsFetchingLocation(false)
                context.showToast(R.string.toast_location_failed)
                onOpenMap()
            }
    }

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            fetchLocationAndFinish()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val hasLoc = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (hasLoc) {
                fetchLocationAndFinish()
            } else {
                context.showToast(R.string.toast_location_required)
                onOpenMap()
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.text2,
                modifier = Modifier.size(38.dp).background(colors.glass, CircleShape)
                    .border(1.dp, colors.glassBorder, CircleShape)
                    .padding(8.dp).clickable { if (!isFetchingLocation) onBack() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
                .background(colors.bgCard, RoundedCornerShape(32.dp))
                .border(1.dp, colors.glassBorder, RoundedCornerShape(32.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.local_weather_alerts), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.text1)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.enable_perms_desc),
                fontSize = 14.sp,
                color = colors.text3,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            PermissionRow(Icons.Filled.LocationOn, stringResource(R.string.location_access), stringResource(R.string.location_access_desc))
            Spacer(modifier = Modifier.height(12.dp))
            PermissionRow(Icons.Filled.Notifications, stringResource(R.string.push_notifications), stringResource(R.string.push_notifications_desc))

            Spacer(modifier = Modifier.height(40.dp))

            PulsingButton(
                text = if (isFetchingLocation) stringResource(R.string.getting_location) else stringResource(R.string.enable_permissions),
                onClick = {
                    if (!isFetchingLocation) {
                        val permissionsToRequest = mutableListOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
                        }

                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.use_map_instead),
                color = colors.purpleBright,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    if (!isFetchingLocation) onOpenMap()
                }
            )
        }
    }
}
