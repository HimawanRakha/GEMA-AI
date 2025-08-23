package com.example.stecu.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // === Conversations ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Query("SELECT * FROM conversations ORDER BY startTime DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: Long)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversationId(conversationId: Long)

    @Transaction
    suspend fun deleteFullConversation(conversationId: Long) {
        deleteMessagesByConversationId(conversationId)
        deleteConversationById(conversationId)
    }

    // === Messages ===
    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<ChatMessageEntity>>
}