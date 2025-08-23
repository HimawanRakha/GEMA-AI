package com.example.stecu.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stecu.data.model.MessageAuthor

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long, // Foreign key ke ConversationEntity
    val author: MessageAuthor,
    val contentJson: String, // Kita simpan List<MessageContent> sebagai JSON string
    val timestamp: Long = System.currentTimeMillis()
)