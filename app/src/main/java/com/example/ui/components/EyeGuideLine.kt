package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioCyan

@Composable
fun EyeGuideLine(
    modifier: Modifier = Modifier,
    alpha: Float = 0.75f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        // Subtle gradient highlight bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            StudioCyan.copy(alpha = 0.8f),
                            StudioCyan,
                            StudioCyan.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Center Eye-Level Indicator Pill
        Row(
            modifier = Modifier
                .background(
                    color = Color(0x990F172A),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(StudioCyan, CircleShape)
            )
            Text(
                text = " EYE LEVEL ",
                color = StudioCyan,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
