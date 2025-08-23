package com.example.stecu.ui.screen

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.stecu.R
import com.example.stecu.data.model.CheckableStep
import com.example.stecu.data.model.Milestone
import com.example.stecu.data.model.Resource
import com.example.stecu.viewmodel.CareerDetailViewModel
import kotlin.math.roundToInt

@Composable
fun ChecklistItem(item: CheckableStep) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Checkbox(
            checked = item.isChecked.value,
            onCheckedChange = { newItemState -> item.isChecked.value = newItemState },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0XFFB0E7FF),
                uncheckedColor = Color.Black,
                checkmarkColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = item.text, color = Color.Black, fontSize = 14.sp)
    }
}

// BARU: Konektor yang menampilkan durasi
@Composable
fun TimelineConnector(durationInWeeks: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Durasi di sebelah kiri (dalam alur vertikal)
        Text(
            text = "$durationInWeeks MINGGU",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            painter = painterResource(R.drawable.ic_arrowdownward),
            contentDescription = "Connector",
            tint = Color.Gray,
            modifier = Modifier.size(40.dp)
        )
    }
}

// DIPERBARUI: Card sekarang menampilkan durasi dan link resource
@Composable
fun MilestoneCard(milestone: Milestone, milestoneNumber: Int) {
    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0XFFB0E7FF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Card: Judul dan Durasi
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$milestoneNumber. ${milestone.title}",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f) // Agar teks mengambil sisa ruang
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Tampilan Durasi di dalam Card
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_schedule),
                        contentDescription = "Duration",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${milestone.duration_weeks} mgg",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

            // Daftar Quests
            milestone.quests.forEach { quest ->
                // Baris untuk setiap Quest: Judul dan Link Resource
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = quest.title,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                    // Tampilkan ikon panah jika ada resource
                    if (quest.resources.isNotEmpty()) {
                        val uriHandler = LocalUriHandler.current
                        val resource = quest.resources.first() // Ambil resource pertama
                        IconButton(onClick = { uriHandler.openUri(resource.url) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Resource",
                                tint = Color.Black
                            )
                        }
                    }
                }
                // Daftar Steps untuk setiap Quest
                quest.steps.forEach { step ->
                    ChecklistItem(item = step)
                }
            }
        }
    }
}

@Composable
fun MilestoneNode(
    milestone: Milestone,
    milestoneNumber: Int,
    isLastNode: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Baris ini tidak lagi menggunakan weight, membiarkan konten menentukan lebarnya sendiri
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- SISI KIRI (DURASI) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                DurationBranch(durationInWeeks = milestone.duration_weeks)
                Divider(modifier = Modifier.width(16.dp), color = Color.Gray)
            }

            // --- TENGAH (KARTU UTAMA) ---
            MilestoneCard(milestone = milestone, milestoneNumber = milestoneNumber)

            // --- SISI KANAN (RESOURCE) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.width(16.dp), color = Color.Gray)
                val allResources = milestone.quests.flatMap { it.resources }
                ResourceBranch(resources = allResources)
            }
        }

        // Tampilkan konektor panah ke bawah jika ini BUKAN node terakhir
        if (!isLastNode) {
            Icon(
                painter = painterResource(R.drawable.ic_arrowdownward),
                contentDescription = "Connector to next milestone",
                tint = Color.Gray,
                modifier = Modifier
                    .size(40.dp)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun OverallProgressBar(
    progress: Float, // Nilai antara 0.0f dan 1.0f
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0XFFB0E7FF))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = "Overall Progress",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress }, // Menggunakan state lambda
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0XFFB0E7FF), // Warna progress
                trackColor = Color.Gray, // Warna sisa progress bar
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).roundToInt()}%",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End) // Teks % di kanan
            )
        }
    }
}

@Composable
fun DurationBranch(durationInWeeks: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0XFFB0E7FF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_schedule),
                contentDescription = "Duration",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$durationInWeeks MINGGU",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// BARU: Composable untuk cabang resource di sebelah kanan
@Composable
fun ResourceBranch(resources: List<Resource>) {
    // Hanya tampilkan card jika ada resource
    if (resources.isNotEmpty()) {
        val uriHandler = LocalUriHandler.current
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0XFFB0E7FF))
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).width(120.dp)) {
                resources.forEach { resource ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = resource.title,
                            color = Color.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        IconButton(
                            onClick = { uriHandler.openUri(resource.url) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_link),
                                contentDescription = "Open Link",
                                tint = Color(0XFFB0E7FF)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BranchingConnector(milestone: Milestone) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // --- SISI KIRI ---
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DurationBranch(durationInWeeks = milestone.duration_weeks)
                // Garis horizontal menuju panah
                Divider(modifier = Modifier.width(16.dp), color = Color.Gray)
            }
        }

        // --- TENGAH (PANAH UTAMA) ---
        Icon(
            painter = painterResource(R.drawable.ic_arrowdownward),
            contentDescription = "Connector",
            tint = Color.Gray,
            modifier = Modifier.size(40.dp)
        )

        // --- SISI KANAN ---
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Garis horizontal dari panah
                Divider(modifier = Modifier.width(16.dp), color = Color.Gray)
                // Mengumpulkan semua resource dari semua quest dalam satu milestone
                val allResources = milestone.quests.flatMap { it.resources }
                ResourceBranch(resources = allResources)
            }
        }
    }
}

// --- SCREEN UTAMA (DENGAN PERUBAHAN PENTING) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerDetailScreen(careerId: String?, onNavigateBack: () -> Unit
                       ) {

    val application = LocalContext.current.applicationContext as Application
    val viewModel: CareerDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CareerDetailViewModel(application) as T
            }
        }
    )

    LaunchedEffect(careerId) {
        careerId?.toLongOrNull()?.let {
            viewModel.loadCareerPlan(it)
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val careerPlan = uiState.careerPlan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            uiState.isLoading -> {
                // Tampilkan loading indicator di tengah layar
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null -> {
                // Tampilkan pesan error
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.careerPlan != null -> {
                // Hanya tampilkan UI utama jika careerPlan TIDAK NULL
                val careerPlan =
                    uiState.careerPlan!! // Di sini aman menggunakan !! karena sudah dicek

                Image(
                    painter = painterResource(id = R.drawable.bg_lines),
                    contentDescription = "Background Lines",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                // Sekarang aman untuk mengakses .goal
                                Text(
                                    text = " ${careerPlan.goal}",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .size(40.dp) // Ukuran latar belakang lingkaran
                                        .clip(CircleShape)
                                        .background(Color(0XFFB0E7FF)), // Warna biru iOS sebagai contoh
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(onClick = onNavigateBack) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White // Warna ikon menjadi putih
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->

                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    // Box ini adalah viewport yang mendeteksi gestur
                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .clipToBounds()
                            .pointerInput(Unit) {
                                detectTransformGestures { centroid, pan, zoom, _ ->
                                    // Simpan skala lama sebelum diubah
                                    val oldScale = scale
                                    // Hitung skala baru dan batasi nilainya
                                    val newScale = (scale * zoom).coerceIn(0.3f, 3f)

                                    // --- INI ADALAH FORMULA KUNCI UNTUK ZOOM YANG TEPAT SASARAN ---
                                    // 1. Hitung offset baru berdasarkan zoom dan centroid
                                    // 2. Terapkan juga pergeseran (pan)
                                    offset = (offset * zoom) + centroid * (1 - zoom) + pan

                                    // Terapkan skala baru
                                    scale = newScale
                                }
                            }
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else if ((careerPlan != null)) {
                            // --- KUNCI PERBAIKAN ADA DI SINI ---
                            // Kita gunakan Layout Composable untuk memberi Column ruang vertikal tak terbatas
                            Layout(
                                content = {
                                    Column(
                                        // Kita butuh alignment di Column agar Modifier.align() pada child bekerja
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        // Beri sedikit padding horizontal pada seluruh kanvas agar
                                        // progress bar tidak menempel di tepi saat di-pan
                                    ) {
                                        // --- PENAMBAHAN PROGRESS BAR DIMULAI DI SINI ---

                                        // 1. Hitung progres setiap kali terjadi recomposition
                                        val allSteps = remember(careerPlan) {
                                            careerPlan.milestones.flatMap { it.quests }
                                                .flatMap { it.steps }
                                        }
                                        val checkedSteps = allSteps.count { it.isChecked.value }
                                        val totalSteps = allSteps.size
                                        val progress = if (totalSteps > 0) {
                                            checkedSteps.toFloat() / totalSteps.toFloat()
                                        } else {
                                            0f
                                        }

                                        // 2. Tampilkan Composable Progress Bar
                                        OverallProgressBar(
                                            progress = progress,
                                            modifier = Modifier
                                                .align(Alignment.Start) // Kunci: Sejajarkan ke kanan dalam Column
                                                .padding(bottom = 24.dp) // Beri jarak ke Milestone pertama
                                        )

                                        Spacer(modifier = Modifier.size(16.dp))

                                        // Loop menjadi lebih sederhana, hanya memanggil MilestoneNode
                                        careerPlan.milestones.forEachIndexed { index, milestone ->
                                            MilestoneNode(
                                                milestone = milestone,
                                                milestoneNumber = index + 1,
                                                isLastNode = index == careerPlan.milestones.lastIndex
                                            )
                                        }

                                        Spacer(modifier = Modifier.size(16.dp))
                                    }

                                },
                                // Terapkan transformasi (pan & zoom) ke Layout ini, BUKAN ke Column
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y,
                                        transformOrigin = TransformOrigin(0.5f, 0f)
                                    )
                            ) { measurables, constraints ->
                                // 1. Buat batasan baru dengan tinggi tak terbatas
                                val LooseConstraints = constraints.copy(
                                    minWidth = 0, // Izinkan lebar lebih kecil dari layar
                                    minHeight = 0,
                                    maxWidth = Constraints.Infinity, // Izinkan lebar MELEBIHI layar
                                    maxHeight = Constraints.Infinity
                                )
                                // 2. Ukur satu-satunya child kita (Column) dengan batasan longgar ini
                                val placeable = measurables.first().measure(LooseConstraints)

                                // 3. Atur ukuran Layout ini agar sesuai dengan batasan awal (ukuran layar)
                                layout(constraints.maxWidth, constraints.maxHeight) {
                                    // 4. Tempatkan Column (yang sekarang berukuran penuh) di dalam Layout ini
                                    // Posisikan di tengah secara horizontal dan di atas secara vertikal
                                    val x = (constraints.maxWidth - placeable.width) / 2
                                    val y = 0
                                    placeable.placeRelative(x, y)
                                }
                            }
                        } else {
                            Text(
                                "Error: ${uiState.error}",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}