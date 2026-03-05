package com.example.tempestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tempestia.repository.WeatherRepository
import com.example.tempestia.ui.home.view.HomeScreen
import com.example.tempestia.ui.home.viewModel.WeatherViewModel
import com.example.tempestia.ui.home.viewModel.WeatherViewModelFactory
import com.example.tempestia.ui.navigations.AppDestinations
import com.example.tempestia.ui.onboarding.view.DarkTempestiaColors
import com.example.tempestia.ui.onboarding.view.LightTempestiaColors
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.ui.onboarding.view.MapScreen
import com.example.tempestia.ui.onboarding.view.OnboardingScreen
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModel
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModelFactory
import com.example.tempestia.ui.theme.TempestiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TempestiaTheme {
                val isSystemDark = isSystemInDarkTheme()
                val colors = if (isSystemDark) DarkTempestiaColors else LightTempestiaColors

                val context = LocalContext.current
                val repository = remember { WeatherRepository(context) }

                CompositionLocalProvider(LocalTempestiaColors provides colors) {

                    val onboardingViewModel: OnboardingViewModel = viewModel(
                    factory = OnboardingViewModelFactory(repository)
                    )
                    val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsState(
                        initial = null
                    )

                    var showMap by rememberSaveable { mutableStateOf(false) }

                    if (isOnboardingCompleted == null)
                        Surface(modifier = Modifier.fillMaxSize()) {}
                    else if (showMap) {
                        MapScreen(
                            onBack = {
                                showMap = false
                            },
                            onLocationSelected = { lat, lng ->
                                onboardingViewModel.saveLocation(lat, lng)

                                showMap = false
                                onboardingViewModel.completeOnboarding()
                            }
                        )
                    } else if (isOnboardingCompleted == true) {
                        TempestiaApp(repository, onboardingViewModel)
                    } else {
                        OnboardingScreen(
                            onFinished = {
                                onboardingViewModel.completeOnboarding()
                            },
                            onOpenMap = {
                                showMap = true
                            }
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun TempestiaApp(repository: WeatherRepository, onboardingViewModel: OnboardingViewModel) {
    val colors = LocalTempestiaColors.current
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.bgDeep,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                NavigationBar(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .border(1.dp, colors.glassBorder, RoundedCornerShape(32.dp)),
                    containerColor = colors.bgCard.copy(alpha = 1f),
                    contentColor = colors.text1,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0.dp)
                ) {
                    AppDestinations.entries.forEach { destination ->
                        val isSelected = currentDestination == destination

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    fontWeight = if (isSelected) Bold else Normal
                                )
                            },
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            // 3. Custom colors for the selected/unselected states
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.purpleBright,
                                selectedTextColor = colors.purpleBright,
                                indicatorColor = colors.purpleCore.copy(alpha = 0.15f), // Soft pill behind selected icon
                                unselectedIconColor = colors.text3,
                                unselectedTextColor = colors.text3
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentAlignment = Alignment.Center
        ) {
            when (currentDestination) {
                AppDestinations.HOME -> {
                    val weatherViewModel: WeatherViewModel = viewModel(
                        factory = WeatherViewModelFactory(repository)
                    )
                    HomeScreen(weatherViewModel = weatherViewModel, onboardingViewModel = onboardingViewModel)
                }

                AppDestinations.FAVORITES -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Favorites Coming Soon", color = colors.text1, fontSize = 24.sp)
                    }
                }

                AppDestinations.PROFILE -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Profile Coming Soon", color = colors.text1, fontSize = 24.sp)
                    }
                }

                AppDestinations.SETTINGS -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Settings Coming Soon", color = colors.text1, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}