package com.example.stecu.ui.components // Sesuaikan dengan package Anda

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScrollableAnimatedText(
    modifier: Modifier = Modifier,
    text: String
) {
    // AnimatedContent akan menangani transisi antar teks (misal: dari respons AI ke teks default)
    AnimatedContent(
        targetState = text,
        label = "full-text-animation",
        transitionSpec = {
            // Saat teks baru masuk (enter), ia akan slide dari atas dan fade in.
            // Saat teks lama keluar (exit), ia akan slide ke bawah dan fade out.
            (slideInVertically(animationSpec = tween(500)) { -it / 2 } + fadeIn(animationSpec = tween(500)))
                .togetherWith(slideOutVertically(animationSpec = tween(500)) { it / 2 } + fadeOut(animationSpec = tween(500)))
        }
    ) { currentText ->
        // Kotak dengan background dan bisa di-scroll
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0XFFF5F5F5))
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = currentText,
                fontSize = 18.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .fillMaxWidth()
            )
        }
    }
}