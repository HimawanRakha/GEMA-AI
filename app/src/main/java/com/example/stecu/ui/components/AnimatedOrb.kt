package com.example.stecu.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Warna tema
val DarkPurple = Color(0xFF1A1A2E)
val NeonBlue = Color(0xFF00BFFF)
val NeonPink = Color(0xFFE94560)
val NeonGreen = Color(0xFFADFF2F)

@Composable
fun AnimatedOrb(
    modifier: Modifier = Modifier,
    isListening: Boolean,
    isSpeaking: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb-transition")

    // Animasi warna berdasarkan state
    val color1 by infiniteTransition.animateColor(
        initialValue = NeonBlue,
        targetValue = NeonPink,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = ""
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = NeonPink,
        targetValue = NeonBlue,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = ""
    )

    // Animasi pulsasi saat mendengarkan atau berbicara
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening || isSpeaking) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 400 else 800),
            repeatMode = RepeatMode.Reverse
        ), label = "orb-pulse"
    )

    val brush = Brush.radialGradient(
        colors = listOf(color1.copy(alpha = 0.6f), color2.copy(alpha = 0.4f)),
        center = Offset(60f, 60f),
        radius = 220f * pulse
    )

    Canvas(modifier = modifier.size(250.dp)) {
        drawCircle(brush = brush, radius = size.minDimension / 2 * pulse)
    }
}