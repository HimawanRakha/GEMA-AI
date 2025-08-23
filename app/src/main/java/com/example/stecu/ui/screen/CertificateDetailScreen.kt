package com.example.stecu.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.stecu.data.db.CertificateEntity
import com.example.stecu.data.repository.CertificateRepository
import kotlinx.coroutines.launch

// ViewModel khusus untuk layar detail (Tidak ada perubahan di sini)
class CertificateDetailViewModel(application: Application, private val certificateId: Int) : ViewModel() {
    private val repository = CertificateRepository(application)
    var certificate by mutableStateOf<CertificateEntity?>(null)
        private set

    init {
        viewModelScope.launch {
            certificate = repository.getCertificate(certificateId)
        }
    }
}

// Composable untuk menampilkan Modal
@Composable
fun CertificateDetailModal( // Nama diubah agar lebih jelas
    onDismissRequest: () -> Unit,
    certificateId: Int,
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: CertificateDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CertificateDetailViewModel(application, certificateId) as T
            }
        }
    )

    val certificate = viewModel.certificate

    // Menggunakan Dialog untuk efek pop-up
    Dialog(onDismissRequest = onDismissRequest) {
        // Card sebagai background putih modal
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (certificate == null) {
                // Tampilan loading
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Box untuk menampung konten dan tombol close
                Box(modifier = Modifier.padding(8.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp)) // Beri ruang untuk tombol close

                        // Gambar dengan sudut rounded
                        AsyncImage(
                            model = certificate.imageUri,
                            contentDescription = certificate.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                                .aspectRatio(16 / 9f) // Rasio gambar sertifikat
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Teks Judul
                        Text(
                            text = certificate.title,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Teks Peran
                        Text(
                            text = certificate.role,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Tombol Close di kanan atas
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.align(Alignment.TopEnd) .padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        }
    }
}