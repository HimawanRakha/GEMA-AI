package com.example.stecu.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stecu.data.db.AppDatabase
import com.example.stecu.data.db.CareerPlanEntity
import com.example.stecu.data.db.ChatMessageEntity
import com.example.stecu.data.db.ConversationEntity
import com.example.stecu.data.model.ChatMessage
import com.example.stecu.data.model.MessageAuthor
import com.example.stecu.data.model.MessageContent
import com.example.stecu.data.navigation.Screen
import com.example.stecu.data.repository.GeminiRepository
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isModelLoading: Boolean = false
)

sealed class ConversationMode {
    object Normal : ConversationMode()
    data class CollectingCareerInfo(val info: CareerPlanInfo) : ConversationMode()
}

data class CareerPlanInfo(
    val goal: String? = null,
    val duration: String? = null,
    val background: String? = null
) {
    // Properti untuk memeriksa apakah semua data sudah terisi
    val isComplete: Boolean
        get() = goal != null && duration != null && background != null

    // Fungsi untuk mendapatkan pertanyaan berikutnya
    fun getNextQuestion(): String {
        return when {
            goal == null -> "Tentu, dengan senang hati! Apa tujuan karir atau pekerjaan impian yang ingin kamu capai?"
            duration == null -> "Oke, tercatat. Kira-kira berapa lama waktu yang kamu siapkan untuk mencapai tujuan itu? (Contoh: 6 bulan, 1 tahun)"
            background == null -> "Sip. Terakhir, ceritakan sedikit tentang latar belakangmu saat ini. Misalnya, apakah kamu pelajar, fresh graduate, atau sudah bekerja di bidang lain?"
            else -> "Semua informasi sudah lengkap! Aku akan siapkan career tree untukmu sekarang..."
        }
    }
}



// DIUBAH: Menggunakan AndroidViewModel untuk mendapatkan Application Context
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GeminiRepository()

    // Inisialisasi DAO dari database
    private val chatDao = AppDatabase.getDatabase(application).chatDao()
    private val careerPlanDao = AppDatabase.getDatabase(application).careerPlanDao()
    private val gson = Gson()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    // Event untuk navigasi, dipindahkan ke dalam class
    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var currentConversationId: Long? = null

    private var currentMode: ConversationMode = ConversationMode.Normal

    fun loadOrCreateConversation(conversationId: Long?) {
        // Jika ID null atau -1L, kita berada dalam chat baru yang belum disimpan
        if (conversationId == null || conversationId == -1L) {
            currentConversationId = null
            _uiState.update { it.copy(messages = emptyList()) }
        } else {
            // Jika ada ID, muat pesan dari database
            currentConversationId = conversationId
            viewModelScope.launch {
                val messageEntities = chatDao.getMessagesForConversation(conversationId).first()
                val chatMessages = messageEntities.map { entity ->
                    ChatMessage(
                        // Kita parse ulang konten mentah dari DB untuk merekonstruksi UI
                        content = parseAiResponse(entity.contentJson),
                        author = entity.author,
                        isLoading = false
                    )
                }
                _uiState.update { it.copy(messages = chatMessages) }
            }
        }
    }
    private suspend fun ensureConversationId(userInput: String) {
        if (currentConversationId == null) {
            val newConversation = ConversationEntity(title = userInput.take(40))
            currentConversationId = chatDao.insertConversation(newConversation)
        }
    }

    fun sendMessage(userInput: String) {
        if (userInput.isBlank()) return

        val userMessage = ChatMessage(
            content = listOf(MessageContent.StyledText(AnnotatedString(userInput))),
            author = MessageAuthor.USER
        )

        // Update UI secara optimis (langsung tampilkan pesan user)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isModelLoading = true
            )
        }

        viewModelScope.launch {
            ensureConversationId(userInput)
            val conversationId = currentConversationId ?: return@launch // ID sekarang dijamin ada

            // Simpan pesan user ke database, apa pun modenya.
            chatDao.insertMessage(
                ChatMessageEntity(
                    conversationId = conversationId,
                    author = userMessage.author,
                    contentJson = userInput
                )
            )

            when (val mode = currentMode) {
                is ConversationMode.Normal -> {
                    // Cek apakah pengguna memulai flow career plan
                    if (userInput.contains("career tree", true) ||
                        userInput.contains("rencana karir", true) ||
                        userInput.contains("perjalanan karir", true)) {
                        startCareerPlanCollection()
                    } else {
                        // Lanjutkan percakapan normal
                        proceedWithNormalChat(userInput)
                    }
                }
                is ConversationMode.CollectingCareerInfo -> {
                    // Lanjutkan proses pengumpulan data
                    proceedWithCareerPlanCollection(userInput, mode.info)
                }
            }
        }
    }

    private fun startCareerPlanCollection() {
        val newInfo = CareerPlanInfo()
        currentMode = ConversationMode.CollectingCareerInfo(newInfo)
        askNextCareerQuestion(newInfo)
    }

    private fun proceedWithCareerPlanCollection(userInput: String, currentInfo: CareerPlanInfo) {
        val updatedInfo = when {
            currentInfo.goal == null -> currentInfo.copy(goal = userInput)
            currentInfo.duration == null -> currentInfo.copy(duration = userInput)
            currentInfo.background == null -> currentInfo.copy(background = userInput)
            else -> currentInfo // Seharusnya tidak terjadi
        }

        currentMode = ConversationMode.CollectingCareerInfo(updatedInfo)

        if (updatedInfo.isComplete) {
            generateCareerPlan(updatedInfo)
        } else {
            askNextCareerQuestion(updatedInfo)
        }
    }

    private fun askNextCareerQuestion(info: CareerPlanInfo) {
        val question = info.getNextQuestion()
        val modelMessage = ChatMessage(
            content = listOf(MessageContent.StyledText(AnnotatedString(question))),
            author = MessageAuthor.MODEL
        )
        viewModelScope.launch {
            currentConversationId?.let { convId ->
                chatDao.insertMessage(
                    ChatMessageEntity(
                        conversationId = convId,
                        author = modelMessage.author,
                        contentJson = question
                    )
                )
            }
        }
        _uiState.update { it.copy(messages = it.messages + modelMessage, isModelLoading = false) }
    }

    private fun generateCareerPlan(info: CareerPlanInfo) {
        viewModelScope.launch {
            // Tampilkan pesan loading
            val loadingMessage = "Semua informasi sudah lengkap! Aku akan siapkan career tree untukmu sekarang..."
            _uiState.update {
                it.copy(
                    messages = it.messages + ChatMessage(
                        content = listOf(MessageContent.StyledText(AnnotatedString(loadingMessage))),
                        author = MessageAuthor.MODEL
                    ),
                    isModelLoading = true
                )
            }
            currentConversationId?.let { convId ->
                chatDao.insertMessage(
                    ChatMessageEntity(
                        conversationId = convId,
                        author = MessageAuthor.MODEL, // Gunakan author yang benar
                        contentJson = loadingMessage // Gunakan variabel String yang benar
                    )
                )
            }

            // Panggil repository dengan data yang sudah lengkap
            repository.generateCareerPlanJson(
                goal = info.goal!!,
                duration = info.duration!!,
                background = info.background!!
            ).onSuccess { modelReply ->
                val parsedContent = parseAiResponse(modelReply)
                val modelMessage = ChatMessage(content = parsedContent, author = MessageAuthor.MODEL)
                currentConversationId?.let { convId ->
                    chatDao.insertMessage(
                        ChatMessageEntity(
                            conversationId = convId,
                            author = modelMessage.author,
                            contentJson = modelReply // Simpan JSON mentah agar tombol bisa dirender ulang
                        )
                    )
                }
                _uiState.update {
                    it.copy(
                        messages = it.messages + modelMessage,
                        isModelLoading = false
                    )
                }
                // Kembalikan ke mode normal setelah selesai
                currentMode = ConversationMode.Normal

            }.onFailure {
                // ... (handle error seperti biasa)
                val errorText = "Maaf, terjadi kesalahan saat membuat rencana. Coba lagi nanti."
                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            content = listOf(MessageContent.StyledText(AnnotatedString(errorText))),
                            author = MessageAuthor.MODEL
                        ),
                        isModelLoading = false
                    )
                }
                currentMode = ConversationMode.Normal
            }
        }
    }
    private fun proceedWithNormalChat(userInput: String) {
        _uiState.update { it.copy(isModelLoading = true) }

        viewModelScope.launch {
            // Jika ini pesan pertama dalam percakapan, buat entri baru di DB
            if (currentConversationId == null) {
                val newConversation = ConversationEntity(title = userInput.take(40)) // Judul dari 40 char pertama
                currentConversationId = chatDao.insertConversation(newConversation)
            }
            val conversationId = currentConversationId ?: return@launch

            // Buat history dari state UI saat ini untuk dikirim ke API
            val history = uiState.value.messages
                .filter { !it.isLoading }
                .map { message ->
                    content(if (message.author == MessageAuthor.USER) "user" else "model") {
                        text(message.content.toCompleteString())
                    }
                }.toList()

            // Kirim ke API Gemini
            repository.sendMessage(history, userInput).onSuccess { modelReply ->
                val parsedContent = parseAiResponse(modelReply)
                val modelMessage = ChatMessage(
                    content = parsedContent,
                    author = MessageAuthor.MODEL
                )

                // Simpan balasan model ke database
                chatDao.insertMessage(
                    ChatMessageEntity(
                        conversationId = conversationId,
                        author = modelMessage.author,
                        contentJson = modelReply // Simpan teks mentah dari model
                    )
                )

                _uiState.update {
                    it.copy(
                        // Ganti pesan user terakhir (yang sudah ada) dengan pesan model
                        // Ini akan menangani kasus di mana pesan model mungkin masuk setelah user mengirim pesan lain
                        // Cara yang lebih aman adalah dengan menghapus placeholder loading jika ada
                        messages = it.messages + modelMessage,
                        isModelLoading = false
                    )
                }
            }.onFailure { exception ->
                Log.e("ChatViewModel", "API call failed", exception)
                val errorText = "Maaf, terjadi kesalahan. Mohon coba lagi."
                val errorMessage = ChatMessage(
                    content = listOf(MessageContent.StyledText(AnnotatedString(errorText))),
                    author = MessageAuthor.MODEL
                )
                // Simpan pesan error ke DB agar tetap muncul jika chat dibuka kembali
                chatDao.insertMessage(
                    ChatMessageEntity(
                        conversationId = conversationId,
                        author = errorMessage.author,
                        contentJson = errorText
                    )
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isModelLoading = false
                    )
                }
            }
        }
    }

    fun createCareerPlanAndNavigate(jsonData: String) {
        viewModelScope.launch {
            try {
                val jsonObject = gson.fromJson(jsonData, JsonObject::class.java)
                val goal = jsonObject.get("goal").asString

                val newPlan = CareerPlanEntity(goal = goal, fullJsonData = jsonData)
                val newId = careerPlanDao.insertCareerPlan(newPlan)

                _navigationEvent.emit(Screen.CareerDetail.createRoute(newId.toString()))
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Gagal membuat career plan", e)
            }
        }
    }

    fun regenerateLastResponse() {
        val lastUserMessage = _uiState.value.messages
            .filter { it.author == MessageAuthor.USER }
            .lastOrNull()

        if (lastUserMessage != null) {
            val messagesWithoutLastModelResponse = _uiState.value.messages.dropLastWhile { it.author == MessageAuthor.MODEL }

            _uiState.update {
                it.copy(messages = messagesWithoutLastModelResponse)
            }
            sendMessage(lastUserMessage.content.toCompleteString())
        }
    }

    private fun List<MessageContent>.toCompleteString(): String {
        return this.joinToString(separator = "") { content ->
            when (content) {
                is MessageContent.StyledText -> content.annotatedString.text
                is MessageContent.Code -> "```${content.code}```"
                // DIPERBARUI: Tambahkan case untuk action agar history lebih akurat
                is MessageContent.CareerPlanAction -> content.buttonText
            }
        }
    }
}


// Fungsi helper ini bisa tetap berada di luar class karena tidak bergantung pada state ViewModel (pure functions)
private fun buildAnnotatedStringFromMarkdown(markdownText: String): AnnotatedString {
    val markdownRegex = """(?<!\*)\*\*(?!\*)(.*?)(?<!\*)\*\*(?!\*)|(?<!\*)\*(?!\*)(.*?)(?<!\*)\*(?!\*)|`(.*?)`""".toRegex()

    val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
    val italicStyle = SpanStyle(fontStyle = FontStyle.Italic)
    val inlineCodeStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = Color(0XFFD9F4FF),
        color = Color.Black
    )

    return buildAnnotatedString {
        var currentIndex = 0
        markdownRegex.findAll(markdownText).forEach { matchResult ->
            val startIndex = matchResult.range.first
            if (startIndex > currentIndex) {
                append(markdownText.substring(currentIndex, startIndex))
            }
            val (content, style) = when {
                matchResult.groups[1] != null -> Pair(matchResult.groupValues[1], boldStyle)
                matchResult.groups[2] != null -> Pair(matchResult.groupValues[2], italicStyle)
                matchResult.groups[3] != null -> Pair(matchResult.groupValues[3], inlineCodeStyle)
                else -> Pair("", null)
            }
            if (style != null) {
                withStyle(style) {
                    append(content)
                }
            }
            currentIndex = matchResult.range.last + 1
        }
        if (currentIndex < markdownText.length) {
            append(markdownText.substring(currentIndex))
        }
    }
}

private fun parseAiResponse(rawText: String): List<MessageContent> {
    val planRegex = "<CAREER_PLAN_JSON>(.*?)</CAREER_PLAN_JSON>".toRegex(RegexOption.DOT_MATCHES_ALL)
    val match = planRegex.find(rawText)

    if (match != null) {
        val jsonData = match.groupValues[1].trim()
        val plainTextPart = rawText.replace(match.value, "").trim()
        val contentList = mutableListOf<MessageContent>()
        if (plainTextPart.isNotEmpty()) {
            contentList.add(MessageContent.StyledText(buildAnnotatedStringFromMarkdown(plainTextPart)))
        }
        contentList.add(
            MessageContent.CareerPlanAction(
                buttonText = "Buat Career Tree",
                jsonData = jsonData
            )
        )
        return contentList
    } else {
        // Logika parsing markdown biasa jika tidak ada JSON plan
        val contentList = mutableListOf<MessageContent>()
        val pattern = Pattern.compile("```(.*?)```", Pattern.DOTALL)
        val matcher = pattern.matcher(rawText)
        var lastIndex = 0

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                val textPart = rawText.substring(lastIndex, matcher.start())
                if (textPart.trim().isNotEmpty()) {
                    contentList.add(MessageContent.StyledText(buildAnnotatedStringFromMarkdown(textPart)))
                }
            }
            val codePart = matcher.group(1) ?: ""
            contentList.add(MessageContent.Code(codePart))
            lastIndex = matcher.end()
        }

        if (lastIndex < rawText.length) {
            val remainingText = rawText.substring(lastIndex)
            if (remainingText.trim().isNotEmpty()) {
                contentList.add(MessageContent.StyledText(buildAnnotatedStringFromMarkdown(remainingText)))
            }
        }

        if (contentList.isEmpty() && rawText.isNotEmpty()) {
            contentList.add(MessageContent.StyledText(buildAnnotatedStringFromMarkdown(rawText)))
        }

        return contentList
    }
}