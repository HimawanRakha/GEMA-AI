package com.example.stecu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.stecu.data.navigation.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stecu.data.navigation.AppNavigation
import com.example.stecu.data.parser.AndroidVoiceToTextParser
import com.example.stecu.ui.components.CustomDrawerItem
import com.example.stecu.ui.components.CustomNavigationDrawerItem
import com.example.stecu.ui.theme.STECUTheme
import com.example.stecu.viewmodel.AssistantViewModel
import com.example.stecu.viewmodel.AssistantViewModelFactory
import com.example.stecu.viewmodel.HistoryViewModel
import com.example.stecu.viewmodel.TtsCommand
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private val viewModel: AssistantViewModel by viewModels {
        AssistantViewModelFactory(AndroidVoiceToTextParser(application))
    }
//    private val historyViewModel: HistoryViewModel by viewModels {
//        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
//    }



    private lateinit var tts: TextToSpeech

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }
    private lateinit var historyViewModel: HistoryViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historyViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(HistoryViewModel::class.java)

        tts = TextToSpeech(this, this)

        setContent {
            STECUTheme {
                val uiState by viewModel.uiState.collectAsState()
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val conversations by historyViewModel.conversations.collectAsState()
                var selectedConversationId by remember { mutableStateOf<Long?>(null) }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // --- CHANGE 3: Define which routes should show the main TopAppBar with the hamburger menu ---
                val mainScreenRoutes = listOf(Screen.Chat.route, Screen.CareerReport.route, Screen.AmberPoints.route)
                LaunchedEffect(Unit) {
                    viewModel.ttsCommand.collect { command ->
                        when(command) {
                            is TtsCommand.Speak -> {
                                tts.speak(command.text, TextToSpeech.QUEUE_FLUSH, null, "ai_response")
                            }
                            TtsCommand.Stop -> {
                                tts.stop()
                            }
                        }
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = currentRoute in mainScreenRoutes,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            drawerContainerColor = Color.White,
                            windowInsets = WindowInsets(-1, 0, 0, 0)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ) {
                                // TODO: Ganti dengan Icon aplikasi Anda
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_gemma),
                                    contentDescription = "GEMA AI Icon",
                                    tint = Color.Black,
                                    modifier = Modifier.size(40.dp) .padding(end = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("GEMA AI", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Divider(color = Color.Black, modifier = Modifier.padding(horizontal = 24.dp))

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Menu", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp,))

                            // Menu Items
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 18.dp),
                                // Ubah dari 8.dp menjadi nilai yang lebih kecil, misalnya 4.dp
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                CustomNavigationDrawerItem(
                                    label = "Chat",
                                    iconResId = R.drawable.ic_chat,
                                    onClick = {
                                        selectedConversationId = null
                                        navController.navigate(Screen.Chat.newChatRoute)
                                        scope.launch { drawerState.close() }
                                    },
                                    // Atur padding vertikal menjadi lebih kecil untuk mempersempit
                                    padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                )

                                CustomNavigationDrawerItem(
                                    label = "Career Report",
                                    iconResId = R.drawable.ic_work,
                                    onClick = {
                                        navController.navigate(Screen.CareerReport.route)
                                        scope.launch { drawerState.close() } },
                                    // Gunakan padding yang sama untuk konsistensi
                                    padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                )
                                CustomNavigationDrawerItem(
                                    label = "Amber Points",
                                    iconResId = R.drawable.ic_amber,
                                    onClick = {
                                        navController.navigate(Screen.AmberPoints.route)
                                        scope.launch { drawerState.close() } },
                                    // Gunakan padding yang sama untuk konsistensi
                                    padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                )

                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Obrolan", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp,))
                            LazyColumn(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 18.dp)
                                // verticalArrangement dihapus agar spacing diatur oleh Divider
                            ) {
                                // Gunakan itemsIndexed untuk mendapatkan index setiap item
                                itemsIndexed(conversations, key = { _, conversation -> conversation.id }) { index, conversation ->
                                    // Bungkus item dan divider dalam Column
                                    Column {
                                        CustomDrawerItem(
                                            label = conversation.title,
                                            selected = conversation.id == selectedConversationId,
                                            onClick = {
                                                selectedConversationId = conversation.id
                                                navController.navigate(Screen.Chat.createRoute(conversation.id))
                                                scope.launch { drawerState.close() }
                                            },
                                            onDelete = {
                                                historyViewModel.deleteConversation(conversation.id)
                                            },
                                            padding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                        )

                                        // Tambahkan Divider jika ini BUKAN item terakhir
                                        if (index < conversations.lastIndex) {
                                            Divider(
                                                color = Color.Black.copy(alpha = 0.1f), // Warna abu-abu transparan yang tipis
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(horizontal = 16.dp) // Beri sedikit padding horizontal
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))
                            // Footer Item
                            NavigationDrawerItem(
                                label = { Text("Dzaky Ryrdi") },
                                selected = false,
                                onClick = {
                                    navController.navigate(Screen.Profile.route)
                                    scope.launch { drawerState.close() } },
                                icon = {
                                    // Kita gunakan Box sebagai container untuk avatar
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp) // Tentukan ukuran avatar
                                            .clip(CircleShape) // Membuat Box menjadi lingkaran
                                            // Beri warna latar belakang sebagai fallback jika gambar gagal dimuat
                                            .background(Color.LightGray)
                                    ) {
                                        // Di sinilah Anda akan menempatkan gambar Anda
                                        Image(
                                            painter = painterResource(id = R.drawable.bg_person), // Ganti dengan gambar Anda
                                            contentDescription = "Foto Profil Mia Gilbert",
                                            // ContentScale.Crop memastikan gambar mengisi lingkaran tanpa distorsi
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                },
                                modifier = Modifier.padding(bottom = 16.dp),
                                colors = NavigationDrawerItemDefaults.colors(unselectedTextColor = Color.Black, unselectedIconColor = Color.Black, unselectedContainerColor = Color.Transparent)
                            )
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            if (currentRoute in mainScreenRoutes) {
                                TopAppBar(
                                    title = { /* Kosongkan */ },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch { drawerState.open() }
                                        }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.Transparent
                                    )
                                )
                            }
                        },
                        containerColor = Color.White
                    ) { paddingValues ->
                            AppNavigation(
                                navController = navController,
                                assistantUiState = uiState,
                                onMicClick = { checkAndRequestPermission() },
                                onStopListeningClick = { viewModel.stopListening() },
                                onStopSpeakingClick = { viewModel.interruptPlayback() },
                                paddingValues = paddingValues
                            )
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.startListening()
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // ... (Fungsi onInit, findBestIndonesianVoice, dan listener TTS tetap sama persis seperti kode awal Anda)
            val customVoice = findBestIndonesianVoice()
            if (customVoice != null) {
                tts.voice = customVoice
            } else {
                tts.language = Locale("id", "ID")
            }
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    runOnUiThread { viewModel.startSpeaking() }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "ai_response") {
                        runOnUiThread {
                            viewModel.stopSpeaking()
                            viewModel.resetToDefault()
                        }
                    }
                }

                override fun onError(utteranceId: String?) {
                    runOnUiThread {
                        viewModel.stopSpeaking()
                        viewModel.resetToDefault()
                    }
                }
            })
        }
    }

    private fun findBestIndonesianVoice(): Voice? {
        val availableVoices: Set<Voice>? = tts.voices
        if (availableVoices.isNullOrEmpty()) return null
        val indonesianVoices = availableVoices.filter {
            it.locale.language == "id" && it.locale.country == "ID"
        }
        return indonesianVoices.maxByOrNull { voice ->
            var score = 0
            if (voice.name.contains("female", ignoreCase = true)) score += 10
            if (voice.quality > Voice.QUALITY_NORMAL) score += 5
            if (!voice.isNetworkConnectionRequired) score += 2
            score
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}


