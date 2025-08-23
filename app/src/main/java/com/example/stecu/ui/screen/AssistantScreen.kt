package com.example.stecu.data.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stecu.R
import com.example.stecu.ui.components.AnimatedOrb
import com.example.stecu.ui.components.ScrollableAnimatedText
import com.example.stecu.viewmodel.AssistantUiState

@Composable
fun AssistantScreen(
    uiState: AssistantUiState,
    onMicClick: () -> Unit,
    onStopListeningClick: () -> Unit, // BARU
    onStopSpeakingClick: () -> Unit,
    onNavigateToChat: () -> Unit // BARU
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            //                AnimatedOrb(
//                    isListening = uiState.isListening,
//                    isSpeaking = uiState.isSpeaking
//                )
            Spacer(modifier = Modifier.weight(0.5f))
            Box(
                modifier = Modifier
                    .size(140.dp) // Sedikit lebih besar dari ikon untuk memberikan ruang stroke
                        .border(BorderStroke(2.dp, Color.Black.copy(alpha = 0.7f)), CircleShape) // Tambahkan border putih transparan
                    .clip(CircleShape), // Jadikan bentuknya lingkaran
                contentAlignment = Alignment.Center // Agar ikon tetap di tengah
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gema),
                    contentDescription = "GEMA AI Icon",
                    tint = Color.Black,
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 150.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ScrollableAnimatedText(
                    text = uiState.displayText,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            TextButton(onClick = onNavigateToChat) {
                Text("Beralih ke mode chat", color = Color.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = Color(0XFF0086BF),
                        strokeWidth = 4.dp
                    )
                } else if (uiState.isSpeaking) {
                    IconButton(
                        onClick = onStopSpeakingClick,
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.Red.copy(alpha = 0.8f), shape = CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stop), // Pastikan Anda punya drawable ini
                            contentDescription = "Hentikan Ucapan",
                            tint = Color.White,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                } else { // Tombol Mic
                    IconButton(
                        onClick = {
                            // DIUBAH: Fungsionalitas ganda
                            if (uiState.isListening) {
                                onStopListeningClick()
                            } else {
                                onMicClick()
                            }
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = if (uiState.isListening) Color.Gray else Color(0XFF0086BF),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic), // Pastikan Anda punya drawable ini
                            contentDescription = if (uiState.isListening) "Hentikan Rekaman" else "Rekam Suara",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}