package com.example.tempestia.ui.onboarding.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tempestia.ui.onboarding.view.components.FeaturesScreen
import com.example.tempestia.ui.onboarding.view.components.PermissionsScreen
import com.example.tempestia.ui.onboarding.view.components.SplashScreen
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModel
import com.example.tempestia.utils.AnimatedParticleBackground
import com.example.tempestia.utils.DarkTempestiaColors
import com.example.tempestia.utils.LightTempestiaColors


val LocalTempestiaColors = staticCompositionLocalOf { LightTempestiaColors }


@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onFinished: (Double?, Double?) -> Unit,
    onOpenMap: () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colors = if (isSystemDark) DarkTempestiaColors else LightTempestiaColors

    CompositionLocalProvider(LocalTempestiaColors provides colors) {

        val currentScreen by viewModel.currentScreen.collectAsState()
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
                    0 -> SplashScreen(onEnter = { viewModel.setCurrentScreen(1) })
                    1 -> FeaturesScreen(
                        onSkip = { viewModel.setCurrentScreen(2) },
                        onNext = { viewModel.setCurrentScreen(2) }
                    )
                    2 -> PermissionsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.setCurrentScreen(1) },
                        onFinish = onFinished,
                        onOpenMap = onOpenMap
                    )
                }
            }
        }
    }
}
