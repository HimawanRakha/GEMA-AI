package com.example.stecu.ui.screen

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi // CHANGE 1: Import for combinedClickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable // CHANGE 2: Import for combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.stecu.BuildConfig
import com.example.stecu.R
import com.example.stecu.data.db.CareerPlanEntity
import com.example.stecu.data.db.CertificateEntity
import com.example.stecu.data.navigation.Screen
import com.example.stecu.viewmodel.AmberPointsViewModel
import com.example.stecu.viewmodel.CareerDetailViewModel
import com.example.stecu.viewmodel.CareerReportViewModel
import java.io.File
import androidx.activity.result.PickVisualMediaRequest


private fun createImageUri(context: Context): Uri {
    val imageFile = File.createTempFile(
        "JPEG_${System.currentTimeMillis()}_",
        ".jpg",
        context.cacheDir // Menggunakan cache directory, tidak perlu izin khusus
    )
    return FileProvider.getUriForFile(
        context,
        // Pastikan authority ini SAMA PERSIS dengan yang di AndroidManifest.xml
        "${BuildConfig.APPLICATION_ID}.provider",
        imageFile
    )
}

@Composable
fun AmberPointsScreen(
    navController: NavHostController,
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AmberPointsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AmberPointsViewModel(application) as T
            }
        }
    )

    var searchQuery by remember { mutableStateOf("") }
    val certificateList by viewModel.certificates.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var certificateToDelete by remember { mutableStateOf<CertificateEntity?>(null) }

    var showChoiceDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    var showDetailModal by remember { mutableStateOf(false) }
    var selectedCertificateId by remember { mutableStateOf<Int?>(null) }

    // --- Panggil modal jika state-nya true ---
    if (showDetailModal && selectedCertificateId != null) {
        CertificateDetailModal( // Panggil modal yang baru kita buat
            onDismissRequest = {
                showDetailModal = false
                selectedCertificateId = null
            },
            certificateId = selectedCertificateId!!
        )
    }

    // Launcher untuk memilih gambar dari galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.addCertificate(it) }
        }
    )

    // Launcher untuk mengambil foto dari kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { viewModel.addCertificate(it) }
            }
        }
    )

    // Snackbar untuk menampilkan error
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearErrorMessage()
        }
    }
    if (showChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showChoiceDialog = false },
            title = { Text("Add Certificate") },
            text = {
                Column {
                    DialogChoiceItem(
                        icon = painterResource(R.drawable.ic_camera),
                        text = "Take Photo",
                        onClick = {
                            showChoiceDialog = false
                            val uri = createImageUri(context)
                            tempImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DialogChoiceItem(
                        icon = painterResource(R.drawable.ic_photo),
                        text = "Choose from Gallery",
                        onClick = {
                            showChoiceDialog = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

                            )
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showChoiceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog && certificateToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                certificateToDelete = null
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete the certificate for '${certificateToDelete!!.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCertificate(certificateToDelete!!.id)
                        showDeleteDialog = false
                        certificateToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showDeleteDialog = false
                    certificateToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showChoiceDialog = true
                },
                shape = CircleShape,
                containerColor = Color(0XFF0086BF),
                contentColor = Color.White,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = "Add Certificate",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                // 1. Judul
                Text(
                    text = "Amber Points",
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Search Bar (menggunakan yang lama, CustomSearchBar2)
                CustomSearchBar2(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    height = 48.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isProcessing) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        CircularProgressIndicator()
                    }
                } else {
                    // 3. Grid 2 kolom
                    val filteredList = certificateList.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.role.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredList.isEmpty()){
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center){
                            Text("No certificates found.", textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredList, key = { it.id }) { certificate ->
                                CertificateItem(
                                    certificate = certificate,
                                    onItemClick = {
                                        selectedCertificateId = certificate.id
                                        showDetailModal = true
                                    },
                                    onItemLongClick = {
                                        certificateToDelete = certificate
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogChoiceItem(
    icon: Painter,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CertificateItem(
    certificate: CertificateEntity,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .aspectRatio(0.75f) // Rasio aspek kartu
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = Uri.parse(certificate.imageUri),
                contentDescription = certificate.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Gambar mengambil ruang lebih banyak
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// CustomSearchBar remains the same, no changes needed here.
@Composable
fun CustomSearchBar2(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp, // Tambahkan parameter tinggi
    placeholderText: String = "Search"
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF6000), Color(0xFFFC8F33))
    )
    var isFocused by remember { mutableStateOf(false) }

    val borderModifier = if (isFocused) {
        Modifier.border(
            width = 2.dp,
            color = Color(0X9B9B9B80),
            shape = RoundedCornerShape(50)
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = Color(0X9B9B9B80),
            shape = RoundedCornerShape(50)
        )
    }

    // Menggunakan BasicTextField untuk kontrol penuh
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .onFocusChanged { isFocused = it.isFocused },
        singleLine = true,
        // Atur warna cursor dengan brush
        cursorBrush = SolidColor(Color.Black),
        textStyle = LocalTextStyle.current.copy(color = Color.Black),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .height(height) // Gunakan tinggi dari parameter
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .then(borderModifier)
                    .padding(start = 16.dp, end = 8.dp), // Padding internal untuk konten
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    // Jika teks kosong, tampilkan placeholder
                    if (query.isEmpty()) {
                        Text(
                            text = placeholderText,
                            color = Color(0X9B9B9B80)
                        )
                    }
                    // Ini adalah tempat teks input akan muncul
                    innerTextField()
                }

                // Ikon di sebelah kanan
                Box(
                    modifier = Modifier
                        .size(height * 0.8f) // Ukuran ikon dinamis berdasarkan tinggi
                        .clip(CircleShape)
                        .background(Color(0XFFB0E7FF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.White,
                        modifier = Modifier.size(height * 0.4f)
                    )
                }
            }
        }
    )
}