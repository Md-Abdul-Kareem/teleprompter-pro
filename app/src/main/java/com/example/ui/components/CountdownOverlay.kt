package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioBlue

@Composable
fun CountdownOverlay(
    remainingSeconds: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = remainingSeconds,
                transitionSpec = {
                    (scaleIn(initialScale = 1.6f) + fadeIn()).togetherWith(
                        scaleOut(targetScale = 0.5f) + fadeOut()
                    )
                },
                label = "countdown_animation"
            ) { count ->
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color(0x3338BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (count > 0) count.toString() else "GO!",
                        color = StudioBlue,
                        fontSize = if (count > 0) 72.sp else 52.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (remainingSeconds > 0) "Get Ready..." else "Start Speaking!",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(28.dp))

            ElevatedButton(
                onClick = onSkip,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color(0x33FFFFFF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.bounceClick(onClick = onSkip)
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Skip countdown",
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = " Skip Countdown",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
