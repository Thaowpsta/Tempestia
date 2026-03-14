package com.example.tempestia.ui.home.view.components

import androidx.compose.runtime.Composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors

@Composable
fun AtmosphericCard(title: String, value: String, subtext: String, icon: String, modifier: Modifier = Modifier) {
    val colors = LocalTempestiaColors.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(colors.glass)
            .border(1.dp, colors.glassBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = icon, fontSize = 28.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            color = colors.text3,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = colors.text1,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtext,
            color = colors.text3,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
