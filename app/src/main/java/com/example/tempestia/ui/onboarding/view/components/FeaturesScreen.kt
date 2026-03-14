package com.example.tempestia.ui.onboarding.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.tempestia.R
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors

@Composable
fun FeaturesScreen(onSkip: () -> Unit, onNext: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val slides = listOf(
        Triple("⛈️", stringResource(R.string.slide1_title), stringResource(R.string.slide1_desc)),
        Triple("🗺", stringResource(R.string.slide2_title), stringResource(R.string.slide2_desc)),
        Triple("🔔", stringResource(R.string.slide3_title), stringResource(R.string.slide3_desc))
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.skip),
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
                text = if (pagerState.currentPage == 2) stringResource(R.string.get_started) else stringResource(R.string.next),
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
