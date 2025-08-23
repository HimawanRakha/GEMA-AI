package com.example.stecu.ui.screen

import android.os.Message
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stecu.R
import com.example.stecu.data.model.ChatMessage
import com.example.stecu.data.model.MessageAuthor
import com.example.stecu.data.model.MessageContent
import com.example.stecu.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    navController: NavHostController,
    onNavigateBackToAssistant: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    // LaunchedEffect untuk auto-scroll (sudah benar)
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { route ->
            navController.navigate(route)
        }
    }

    Scaffold(
        topBar = {
            // DIUBAH: Gunakan CenterAlignedTopAppBar
            CenterAlignedTopAppBar(
                title = {
                },
                actions = {
                    // BARU: Ikon titik tiga di kanan
                    IconButton(onClick = { /* TODO: Tambahkan aksi untuk menu, misal dropdown */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu Opsi",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                // Jika ya, tampilkan UI sapaan.
                // Modifier.weight(1f) penting agar sapaan mengisi ruang
                // dan mendorong Input Bar ke bawah.
                EmptyChatGreeting(modifier = Modifier.weight(1f))
            } else {
                // Daftar Pesan (Tidak ada perubahan)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageBubble(
                            message = message,
                            onCopy = { textToCopy ->
                                clipboardManager.setText(AnnotatedString(textToCopy))
                            },
                            onReload = {
                                // Anda perlu membuat fungsi ini di ViewModel
                                viewModel.regenerateLastResponse()
                            },
                            onCareerPlanClick = { jsonData ->
                                viewModel.createCareerPlanAndNavigate(jsonData)
                            }
                        )
                    }
                    if (uiState.isModelLoading) {
                        item {
                            MessageBubble(
                                message = ChatMessage(
                                    content = emptyList(), // Teks dikosongkan karena akan diganti animasi
                                    MessageAuthor.MODEL,
                                    isLoading = true
                                ),
                                onCopy = {},
                                onReload = {},
                                onCareerPlanClick = {}
                            )
                        }
                    }
                }
            }

            // Input Bar (Tidak ada perubahan)
            GradientChatInputBar(
                viewModel = viewModel,
                onNavigateBackToAssistant = onNavigateBackToAssistant
            )
        }
    }
}

@Composable
fun EmptyChatGreeting(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(), // Mengisi ruang yang tersedia
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Gambar Sapaan
            Image(
                painter = painterResource(id = R.drawable.bg_gemma),
                contentDescription = "Gema Assistant Greeting",
                modifier = Modifier.size(150.dp)
            )

            // 2. Teks Bold
            Text(
                text = "Halo! saya Gema",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black
            )

            // Spacer antara dua teks, sudah benar
            Spacer(modifier = Modifier.height(4.dp))

            // 3. Teks Abu-abu
            Text(
                text = "Apa nih yang kamu sedang bingung dengan karir?",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 64.dp)
            )
        }
    }
}

@Composable
fun GradientChatInputBar(
    viewModel: ChatViewModel,
    onNavigateBackToAssistant: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val isTextEmpty = inputText.isBlank()
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF6000), Color(0xFFFC8F33))
    )
    // Warna untuk ikon di dalam tombol putih, diambil dari gradien
    val iconTint = Color(0xFFFF6000)

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp) // Padding luar
            .shadow(
                elevation = 8.dp, // semakin besar semakin tebal bayangan
                shape = RoundedCornerShape(28.dp),
                clip = false
            )
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(
                width = 2.dp, // ketebalan border
                color = Color(0XFFF5F5F5),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp), // Padding dalam
        contentAlignment = Alignment.Center
    ) {Column {
        // ELEMEN PERTAMA: Input Teks
        BasicTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 8.dp), // Beri jarak ke tombol di bawahnya
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(Color.Black),
            decorationBox = { innerTextField ->
                // Box ini untuk mengatur placeholder
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (isTextEmpty) {
                        Text(
                            text = "Ask anything",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField() // Ini adalah area ketik yang sebenarnya
                }
            }
        )

        // ELEMEN KEDUA: Baris yang berisi tombol-tombol
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Plus di kiri
            IconButton(
                onClick = { /* TODO: Logika untuk menambahkan file */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0XFFF5F5F5), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah File",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Spacer untuk mendorong tombol berikutnya ke ujung kanan
            Spacer(modifier = Modifier.weight(1f))

            // Tombol Kirim atau Mikrofon di kanan
            IconButton(
                onClick = {
                    if (isTextEmpty) {
                        onNavigateBackToAssistant()
                    } else {
                        viewModel.sendMessage(inputText.trim())
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0XFFB0E7FF), shape = CircleShape)
            ) {
                if (isTextEmpty) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = "Mode Suara",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Kirim",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
    }
}


@Composable
fun MessageBubble(
    message: ChatMessage,
    onCopy: (String) -> Unit,
    onReload: () -> Unit,
    onCareerPlanClick: (String) -> Unit
) {
    val isFromUser = message.author == MessageAuthor.USER

    // --- LOGIKA ANIMASI BARU DIMULAI DI SINI ---

    // BARU: State untuk mengontrol visibilitas dan memicu animasi.
    // Dimulai dari 'false' agar bisa dianimasikan menjadi 'true'.
    var visible by remember { mutableStateOf(false) }

    // BARU: LaunchedEffect hanya akan berjalan sekali saat composable ini pertama kali masuk ke layar.
    // Ini akan mengubah 'visible' menjadi true, yang memulai animasi.
    LaunchedEffect(Unit) {
        visible = true
    }

    // DIUBAH: Kita akan membungkus bubble AI dengan AnimatedVisibility.
    // Bubble pengguna akan ditampilkan seperti biasa tanpa animasi.
    if (isFromUser) {
        // Tampilkan bubble pengguna secara langsung
        BubbleContent(message = message, onCopy = onCopy, onReload = onReload, onCareerPlanClick = onCareerPlanClick)
    } else {
        // Tampilkan bubble AI dengan animasi
        AnimatedVisibility(
            visible = visible,
            // Definisikan animasi "masuk"
            enter = fadeIn(
                animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic)
            ) + slideInVertically(
                initialOffsetY = { -40 }, // Mulai 40px di atas posisi akhir
                animationSpec = tween(durationMillis = 500, easing = EaseOutCubic)
            )
        ) {
            BubbleContent(message = message, onCopy = onCopy, onReload = onReload, onCareerPlanClick = onCareerPlanClick)
        }
    }
}



@Composable
private fun BubbleContent(
    message: ChatMessage,
    onCopy: (String) -> Unit,
    onReload: () -> Unit,
    onCareerPlanClick: (String) -> Unit
) {
    val isFromUser = message.author == MessageAuthor.USER
    val alignment = if (isFromUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isFromUser) Modifier.fillMaxWidth(0.9f) else Modifier),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start) {
            val userBubbleBrush = Color(0XFFD9F4FF)
            val modelBubbleColor = Color.Transparent
            val shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromUser) 16.dp else 0.dp,
                bottomEnd = if (isFromUser) 0.dp else 16.dp
            )

            Box(
                modifier = Modifier
                    .clip(shape)
                    .then(
                        if (isFromUser) Modifier.background(color = userBubbleBrush)
                        else Modifier.background(color = modelBubbleColor)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    if (message.isLoading) {
                        TypingIndicatorAnimation()
                    } else {
                        message.content.forEach { content ->
                            when (content) {
                                is MessageContent.StyledText -> {
                                    Text(
                                        text = content.annotatedString,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }
                                is MessageContent.Code -> {
                                    CodeBlock(code = content.code)
                                }
                                is MessageContent.CareerPlanAction -> {
                                    Button(
                                        onClick = { onCareerPlanClick(content.jsonData) },
                                        shape = RoundedCornerShape(12.dp), // Membuat sudut lebih tumpul
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0XFF0086BF), // Warna latar oranye
                                            contentColor = Color.White // Warna teks putih
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 4.dp,
                                            pressedElevation = 2.dp
                                        )
                                    ) {
                                        Text(content.buttonText)
                                    }
                                }

                            }
                        }
                    }
                }
            }

            if (!isFromUser && !message.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = {
                            val textToCopy = message.content.joinToString(separator = "\n") {
                                when (it) {
                                    is MessageContent.StyledText -> it.annotatedString.text
                                    is MessageContent.Code -> "```${it.code}```"
                                    is MessageContent.CareerPlanAction -> it.buttonText

                                }
                            }
                            onCopy(textToCopy)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_copy),
                            contentDescription = "Copy",
                            tint = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onReload,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun TypingIndicatorAnimation(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = Color.Gray,
    spaceBetween: Dp = 4.dp,
    bounceHeight: Dp = 8.dp
) {
    val dots = listOf(remember { Animatable(0f) }, remember { Animatable(0f) }, remember { Animatable(0f) })
    val bounceHeightPx = with(LocalDensity.current) { bounceHeight.toPx() }

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 120L) // Delay untuk setiap titik agar berurutan
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 800
                        0f at 0
                        -bounceHeightPx at 200
                        0f at 400
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(spaceBetween)) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset(y = animatable.value.dp)
                    .background(color = dotColor, shape = CircleShape)
            )
        }
    }
}
@Composable
fun CodeBlock(code: String) {
    val clipboardManager = LocalClipboardManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        Column {
            // Header untuk blok kode (misal: tombol copy)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { clipboardManager.setText(AnnotatedString(code)) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = "Copy Code",
                        tint = Color.White
                    )
                }
            }
            // Teks kode yang bisa di-scroll horizontal
            Text(
                text = code.trim(),
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState())
            )
        }
    }
}

