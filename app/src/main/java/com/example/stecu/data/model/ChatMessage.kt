package com.example.stecu.data.model

import androidx.compose.ui.text.AnnotatedString

enum class MessageAuthor {
    USER, MODEL
}
sealed interface MessageContent {
    data class StyledText(val annotatedString: AnnotatedString) : MessageContent
    data class Code(val code: String) : MessageContent
    data class CareerPlanAction(val buttonText: String, val jsonData: String) : MessageContent
}
data class ChatMessage(
    val content: List<MessageContent>,
    val author: MessageAuthor,
    val isLoading: Boolean = false
)
