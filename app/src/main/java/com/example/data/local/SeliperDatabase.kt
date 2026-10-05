package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import android.content.Context
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val modeId: String,
    val ftaTopicId: String,
    val providerId: String,
    val modelId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: String, // "user" or "assistant"
    val content: String,
    val modeId: String,
    val ftaTopicId: String? = null,
    val modelUsed: String? = null,
    val attachedImageUri: String? = null,
    val attachedFileName: String? = null,
    val attachedFileContent: String? = null,
    val toolLogsJson: String? = null, // JSON of List<ConnectedToolExecution> when real tools ran
    val reasoningSummary: String? = null, // Concise step summary ("King of Best Thinking")
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SeliperDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET title = :newTitle, updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun renameConversation(conversationId: Long, newTitle: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesListSync(conversationId: Long): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearMessagesInConversation(conversationId: Long)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId AND timestamp >= :fromTimestamp")
    suspend fun deleteMessagesFromTimestamp(conversationId: Long, fromTimestamp: Long)
}

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SeliperDatabase : RoomDatabase() {
    abstract fun seliperDao(): SeliperDao

    companion object {
        @Volatile
        private var INSTANCE: SeliperDatabase? = null

        fun getDatabase(context: Context): SeliperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SeliperDatabase::class.java,
                    "seliper_ai_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
