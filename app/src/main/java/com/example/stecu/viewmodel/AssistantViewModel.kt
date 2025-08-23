package com.example.stecu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stecu.data.parser.AndroidVoiceToTextParser
import com.example.stecu.data.repository.GeminiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ... (AssistantUiState dan TtsCommand tetap sama) ...
data class AssistantUiState(
    val displayText: String = "Sentuh mic dan mulailah berbicara...",
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface TtsCommand {
    data class Speak(val text: String): TtsCommand
    data object Stop: TtsCommand
}

class AssistantViewModel(
    private val voiceParser: AndroidVoiceToTextParser,
    private val repository: GeminiRepository // DIUBAH
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState = _uiState.asStateFlow()

    private val _ttsCommand = MutableSharedFlow<TtsCommand>()
    val ttsCommand = _ttsCommand.asSharedFlow()

    init {
        voiceParser.state
            .onEach { state ->
                // DIUBAH: Panggil processSpokenText hanya saat user selesai bicara
                if (!state.isSpeaking && state.spokenText.isNotBlank()) {
                    processSpokenText(state.spokenText)
                }

                // DIUBAH: Tampilkan hasil parsial saat mendengarkan
                val newDisplayText = if (state.isSpeaking) {
                    state.partialSpokenText.ifBlank { "Mendengarkan..." }
                } else {
                    // Jangan ubah teks saat berhenti mendengarkan, tunggu respons AI
                    _uiState.value.displayText
                }

                _uiState.update { it.copy(
                    isListening = state.isSpeaking,
                    displayText = if(state.isSpeaking) newDisplayText else it.displayText
                ) }
            }.launchIn(viewModelScope)
    }

    fun startListening() {
        // Reset state sebelum mulai mendengarkan
        _uiState.update { it.copy(displayText = "Mendengarkan...") }
        voiceParser.startListening()
    }

    fun stopListening() {
        voiceParser.stopListening()
    }

    fun startSpeaking() {
        _uiState.update { it.copy(isSpeaking = true) }
    }

    fun stopSpeaking() {
        _uiState.update { it.copy(isSpeaking = false) }
    }

    fun resetToDefault() {
        _uiState.update {
            it.copy(
                displayText = "Sentuh mic dan mulailah berbicara...",
                isLoading = false,
                isListening = false,
                isSpeaking = false
            )
        }
    }

    fun interruptPlayback() {
        viewModelScope.launch {
            _ttsCommand.emit(TtsCommand.Stop)
        }
        stopSpeaking()
        resetToDefault()
    }

    private fun processSpokenText(text: String) {
        _uiState.update { it.copy(isLoading = true, displayText = "GEMA sedang berpikir...") }
        voiceParser.reset() // Reset parser state setelah teks diproses

        viewModelScope.launch {
            repository.processText(text).onSuccess { reply ->
                _uiState.update { it.copy(isLoading = false, displayText = reply) }
                _ttsCommand.emit(TtsCommand.Speak(reply))
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, displayText = "Maaf, ada gangguan. Coba lagi.") }
            }
        }
    }
}

class AssistantViewModelFactory(
    private val voiceParser: AndroidVoiceToTextParser
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AssistantViewModel(
            voiceParser = voiceParser,
            repository = GeminiRepository() // DIUBAH
        ) as T
    }
}