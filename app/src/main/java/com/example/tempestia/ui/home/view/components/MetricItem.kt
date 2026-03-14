package com.example.tempestia.ui.home.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors

@Composable
fun MetricItem(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalTempestiaColors.current

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = colors.text3,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = colors.text2,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}