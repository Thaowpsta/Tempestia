package com.example.tempestia.ui.onboarding.view

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme // Detects system theme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.tempestia.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

data class TempestiaColors(
    val bgDeep: Color,
    val bgSurface: Color,
    val bgCard: Color,
    val glass: Color,
    val glassBorder: Color,
    val purpleCore: Color,
    val purpleBright: Color,
    val purpleSoft: Color,
    val goldLight: Color,
    val text1: Color,
    val text2: Color,
    val text3: Color,
    val isDark: Boolean
)

val LightTempestiaColors = TempestiaColors(
    bgDeep = Color(0xFFF0ECE8),
    bgSurface = Color(0xFFDDD5F0),
    bgCard = Color(0xD9FFFFFF),
    glass = Color(0x0F7C3AED),
    glassBorder = Color(0x2E7C3AED),
    purpleCore = Color(0xFF7C3AED),
    purpleBright = Color(0xFF9333EA),
    purpleSoft = Color(0xFF7C3AED),
    goldLight = Color(0xFFF59E0B),
    text1 = Color(0xFF1E1040),
    text2 = Color(0xFF4C1D95),
    text3 = Color(0xFF6D5A9E),
    isDark = false
)

val DarkTempestiaColors = TempestiaColors(
    bgDeep = Color(0xFF0d0a1a),
    bgSurface = Color(0xFF231848),
    bgCard = Color(0xD91D113C),
    glass = Color(0x147C3AED),
    glassBorder = Color(0x38A855F7),
    purpleCore = Color(0xFF7C3AED),
    purpleBright = Color(0xFFA855F7),
    purpleSoft = Color(0xFFC4B5FD),
    goldLight = Color(0xFFFCD34D),
    text1 = Color(0xFFF3F0FF),
    text2 = Color(0xFFE9D5FF),
    text3 = Color(0xFF8B7EC8),
    isDark = true
)

val LocalTempestiaColors = staticCompositionLocalOf { LightTempestiaColors }


@Composable
fun OnboardingScreen(onFinished: (Double?, Double?) -> Unit, onOpenMap: () -> Unit) {
    val isSystemDark = isSystemInDarkTheme()
    val colors = if (isSystemDark) DarkTempestiaColors else LightTempestiaColors

    CompositionLocalProvider(LocalTempestiaColors provides colors) {

        var currentScreen by remember { mutableIntStateOf(0) }
        val currentColors = LocalTempestiaColors.current

        Box(modifier = Modifier.fillMaxSize().background(currentColors.bgDeep)) {
            AnimatedParticleBackground()

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(500)) + slideInVertically(
                        initialOffsetY = { 50 }, animationSpec = tween(500)
                    )).togetherWith(fadeOut(animationSpec = tween(350)))
                },
                label = "OnboardingTransition"
            ) { screen ->
                when (screen) {
                    0 -> SplashScreen(onEnter = { currentScreen = 1 })
                    1 -> FeaturesScreen(
                        onSkip = { currentScreen = 2 },
                        onNext = { currentScreen = 2 }
                    )
                    2 -> PermissionsScreen(
                        onBack = { currentScreen = 1 },
                        onFinish = onFinished,
                        onOpenMap = onOpenMap
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    onFinish: (Double?, Double?) -> Unit,
    onOpenMap: () -> Unit
) {
    val colors = LocalTempestiaColors.current
    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    @SuppressLint("MissingPermission")
    fun fetchLocationAndFinish() {
        isFetchingLocation = true
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                isFetchingLocation = false
                if (location != null) {
                    // Success! Pass coords to MainActivity to save & finish
                    onFinish(location.latitude, location.longitude)
                } else {
                    // Failed (Usually because GPS is physically turned off in quick settings)
                    Toast.makeText(context, "Could not get GPS signal. Please use map.", Toast.LENGTH_LONG).show()
                    onOpenMap()
                }
            }
            .addOnFailureListener {
                isFetchingLocation = false
                Toast.makeText(context, "Failed to get location. Please use map.", Toast.LENGTH_LONG).show()
                onOpenMap()
            }
    }

    // Auto-fetch if permissions are already granted (like on an app reinstall!)
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
                // If they clicked allow, fetch the GPS instantly!
                fetchLocationAndFinish()
            } else {
                Toast.makeText(context, "Location required. Please use Map.", Toast.LENGTH_SHORT).show()
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
            Text("Local Weather Alerts", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.text1)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Enable permissions so Tempestia can track storms in your exact area and send critical alerts.",
                fontSize = 14.sp,
                color = colors.text3,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            PermissionRow(Icons.Filled.LocationOn, "Location Access", "To provide accurate local weather.")
            Spacer(modifier = Modifier.height(12.dp))
            PermissionRow(Icons.Filled.Notifications, "Push Notifications", "To alert you of incoming storms.")

            Spacer(modifier = Modifier.height(40.dp))

            PulsingButton(
                // 🚨 NEW: Show loading text while searching for GPS
                text = if (isFetchingLocation) "Getting Location..." else "Enable Permissions",
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
                text = "Use map selection instead",
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

@Composable
fun SplashScreen(onEnter: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -24f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    val spinSlow by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing)),
        label = "spin_slow"
    )
    val spinReverse by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)),
        label = "spin_reverse"
    )

    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -1000f, targetValue = 2000f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "shimmer"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(240.dp).offset(y = floatY.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = spinReverse }) {
                drawCircle(
                    color = colors.purpleCore.copy(alpha = 0.2f),
                    style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), 0f))
                )
            }
            Box(modifier = Modifier.size(220.dp).graphicsLayer { rotationZ = spinSlow }) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = colors.purpleCore.copy(alpha = 0.3f), style = Stroke(width = 3f))
                    drawCircle(color = colors.purpleBright, radius = 8f, center = Offset(size.width / 2, 0f))
                }
            }

            Image(
                painter = painterResource(if (colors.isDark) R.drawable.tempestia_dark else R.drawable.tempestia_light),
                contentDescription = "Tempestia Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(200.dp).centerGlow(
                        color = colors.purpleBright,
                        radiusDp = 24.dp,
                        cornerRadiusDp = 100.dp).clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "TEMPESTIA",
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = listOf(colors.goldLight, colors.purpleCore, colors.text1),
                    start = Offset(shimmerX, 0f),
                    end = Offset(shimmerX + 800f, 0f)
                ),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp
            )
        )
        Text(
            text = "BECAUSE NATURE NEVER WARNS TWICE",
            color = colors.text3,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 60.dp)
        )

        PulsingButton(text = "Begin the Journey →", onClick = onEnter)
    }
}

@Composable
fun FeaturesScreen(onSkip: () -> Unit, onNext: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val slides = listOf(
        Triple("⛈️", "Accurate Forecasts", "Track storms, monitor radar, and get up-to-the-minute weather alerts in a beautifully designed interface."),
        Triple("🗺", "Live Radar", "Visualize incoming precipitation and severe weather cells in real-time on our interactive map."),
        Triple("🔔", "Severe Alerts", "Stay ahead of danger with instant push notifications for lightning, hail, and extreme conditions.")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Skip",
                color = colors.text3,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSkip)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(140.dp)
                        .background(colors.glass, RoundedCornerShape(40.dp))
                        .border(1.dp, colors.glassBorder, RoundedCornerShape(40.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slides[page].first,
                        fontSize = 64.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = slides[page].second,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = colors.text1
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = slides[page].third,
                    fontSize = 15.sp,
                    color = colors.text3,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    val isActive = pagerState.currentPage == index
                    Box(
                        modifier = Modifier.height(8.dp).width(if (isActive) 24.dp else 8.dp)
                            .background(if (isActive) colors.purpleBright else colors.bgSurface, RoundedCornerShape(4.dp))
                    )
                }
            }

            PulsingButton (
                text = if (pagerState.currentPage == 2) "Get Started →" else "Next →",
                onClick =  {
                    if (pagerState.currentPage < 2) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onNext()
                    }
                }
            )
        }
    }
}

@Composable
fun PermissionsScreen(onBack: () -> Unit, onFinish: () -> Unit, onOpenMap: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        var hasNotifications = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotifications = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        if ((hasFineLocation || hasCoarseLocation) && hasNotifications) {
            onFinish()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { _ ->
            onFinish()
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
                    .padding(8.dp).clickable(onClick = onBack)
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
            Text("Local Weather Alerts", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.text1)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Enable permissions so Tempestia can track storms in your exact area and send critical alerts.",
                fontSize = 14.sp,
                color = colors.text3,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            PermissionRow(Icons.Filled.LocationOn, "Location Access", "To provide accurate local weather.")
            Spacer(modifier = Modifier.height(12.dp))
            PermissionRow(Icons.Filled.Notifications, "Push Notifications", "To alert you of incoming storms.")

            Spacer(modifier = Modifier.height(40.dp))

            PulsingButton(
                text = "Enable Permissions",
                onClick = {
                    val permissionsToRequest = mutableListOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
                    }

                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Use map selection instead",
                color = colors.purpleBright,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    onOpenMap()
                }
            )
        }
    }
}

@Composable
fun PermissionRow(icon: ImageVector, title: String, desc: String) {
    val colors = LocalTempestiaColors.current
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(colors.glass, RoundedCornerShape(16.dp))
            .border(1.dp, colors.glassBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp)
                .background(
                    Brush.linearGradient(listOf(colors.purpleCore.copy(alpha = 0.2f), colors.purpleBright.copy(alpha = 0.1f))),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.purpleCore, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, color = colors.text1, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = desc, color = colors.text3, fontSize = 13.sp)
        }
    }
}

fun Modifier.centerGlow(
    color: Color,
    radiusDp: Dp,
    cornerRadiusDp: Dp
) = this.drawBehind {
    this.drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        frameworkPaint.color = android.graphics.Color.WHITE
        frameworkPaint.setShadowLayer(
            radiusDp.toPx(),
            0f,
            0f,
            color.copy(alpha = 0.8f).toArgb()
        )

        canvas.drawRoundRect(
            0f, 0f, size.width, size.height,
            cornerRadiusDp.toPx(), cornerRadiusDp.toPx(),
            paint
        )
    }
}

@Composable
fun PulsingButton(text: String, onClick: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "btn")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOut), RepeatMode.Reverse),
        label = "btn_pulse_glow"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.centerGlow(
            color = colors.purpleSoft,
            radiusDp = pulseGlow.dp,
            cornerRadiusDp = 50.dp
        )
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(colors.purpleCore, colors.purpleBright)), RoundedCornerShape(50.dp))
                .padding(horizontal = 40.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun AnimatedParticleBackground() {
    val colors = LocalTempestiaColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)),
        label = "time"
    )

    val particles = remember {
        val random = Random(42)
        List(40) {
            floatArrayOf(
                random.nextFloat(),
                random.nextFloat() * 1.5f + 0.5f,
                random.nextFloat(),
                random.nextFloat() * 3f + 1f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerGlowColors = if (colors.isDark) {
            listOf(Color(0xFF1E1136), Color(0xFF130D26), colors.bgDeep)
        } else {
            listOf(Color(0xFFD8CCF5), Color(0xFFEDE8F8), colors.bgDeep)
        }

        drawRect(
            brush = Brush.radialGradient(
                colors = centerGlowColors,
                center = Offset(size.width / 2, 0f),
                radius = size.height * 0.7f
            )
        )

        particles.forEach { p ->
            val localTime = (time * p[1] + p[2]) % 1f
            val currentY = size.height * (1f - localTime)
            val currentX = (size.width * p[0]) + (localTime * size.width * 0.15f)

            val alpha = when {
                localTime < 0.2f -> localTime / 0.2f
                localTime > 0.8f -> (1f - localTime) / 0.2f
                else -> 1f
            }

            drawCircle(
                color = colors.purpleCore.copy(alpha = alpha * 0.3f),
                radius = p[3],
                center = Offset(currentX, currentY)
            )
        }
    }
}