package com.example.tempestia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tempestia.ui.home.view.HomeScreen
import com.example.tempestia.ui.onboarding.view.OnboardingScreen
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModel
import com.example.tempestia.ui.theme.TempestiaTheme

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            TempestiaTheme {
//                val onboardingViewModel: OnboardingViewModel = viewModel()
//                val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsState(initial = null)
//
//                if (isOnboardingCompleted == null) {
//                    Surface(modifier = Modifier.fillMaxSize()) {}
//                } else {
//                    if (isOnboardingCompleted == true) {
//                        TempestiaApp()
//                    } else {
//                        OnboardingScreen(
//                            onFinished = {
//                                onboardingViewModel.completeOnboarding()
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TempestiaTheme {
                val onboardingViewModel: OnboardingViewModel = viewModel()

                var showOnboarding by rememberSaveable { mutableStateOf(true) }
                var showMap by rememberSaveable { mutableStateOf(false) }

                if (showMap) {
                    com.example.tempestia.ui.onboarding.view.MapScreen(
                        onBack = {
                            showMap = false
                            showOnboarding = true
                        },
                        onLocationSelected = { lat, lng ->
                            // TODO: Save location to ViewModel
                            Log.i("TAG", "Location: $lat, $lng")

                            onboardingViewModel.saveLocation(lat, lng)

                            showMap = false
                            showOnboarding = false
                            onboardingViewModel.completeOnboarding()
                        }
                    )
                } else if (showOnboarding) {
                    OnboardingScreen(
                        onFinished = {
                            onboardingViewModel.completeOnboarding()
                            showOnboarding = false
                        },
                        onOpenMap = {
                            showOnboarding = false
                            showMap = true
                        }
                    )
                } else {
                    TempestiaApp()
                }
            }
        }
    }
}

@Composable
fun TempestiaApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HomeScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}