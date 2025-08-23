package com.example.stecu.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.stecu.data.model.MessageAuthor
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TypeConverter untuk enum MessageAuthor
class Converters {
    @TypeConverter
    fun fromMessageAuthor(value: MessageAuthor): String {
        return value.name
    }

    @TypeConverter
    fun toMessageAuthor(value: String): MessageAuthor {
        return enumValueOf(value)
    }
}


@Database(
    entities = [ConversationEntity::class, ChatMessageEntity::class, CareerPlanEntity::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun careerPlanDao(): CareerPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stecu_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate data di sini
                            CoroutineScope(Dispatchers.IO).launch {
                                val dummyJson = """
                                {
                                  "goal": "Menjadi pilot",
                                  "milestones": [
                                    { "id": "m1", "title": "Riset Profesi Pilot", "duration_weeks": 2, "quests": [
                                      { "id": "q1", "title": "Pelajari jenis-jenis pilot & jalur karier", "steps": ["Cari informasi online tentang berbagai jenis pilot (komersial, militer, pribadi)", "Identifikasi jalur pendidikan dan pelatihan yang dibutuhkan", "Teliti persyaratan kesehatan dan fisik untuk menjadi pilot"], "resources": [{"title": "Website Asosiasi Pilot Indonesia", "url": "https://example.com"}] },
                                      { "id": "q2", "title": "Evaluasi minat & kemampuan diri", "steps": ["Analisis minat dan passion terhadap penerbangan", "Identifikasi kekuatan dan kelemahan diri", "Evaluasi kesesuaian minat dan kemampuan dengan profesi pilot"], "resources": [{"title": "Tes minat dan bakat online", "url": "https://example.com"}] }
                                    ]},
                                    { "id": "m2", "title": "Mengembangkan Skill Dasar", "duration_weeks": 3, "quests": [
                                      { "id": "q3", "title": "Belajar Bahasa Inggris Aviasi", "steps": ["Daftar kursus bahasa Inggris khusus aviasi", "Mulai belajar kosakata dan frase umum dalam aviasi", "Latih kemampuan berbicara dan mendengarkan"], "resources": [{"title": "Aplikasi Duolingo", "url": "https://www.duolingo.com/"}] }
                                    ]},
                                    { "id": "m3", "title": "Eksplorasi Peluang Pendidikan", "duration_weeks": 4, "quests": [
                                      { "id": "q4", "title": "Riset sekolah penerbangan", "steps": ["Cari informasi tentang sekolah penerbangan di Indonesia dan luar negeri", "Bandingkan biaya, kurikulum, dan reputasi sekolah", "Hubungi sekolah penerbangan untuk informasi lebih lanjut"], "resources": [{"title": "Website sekolah penerbangan", "url": "https://example.com"}] }
                                    ]}
                                  ]
                                }
                            """.trimIndent()

                                INSTANCE?.careerPlanDao()?.insertCareerPlan(
                                    CareerPlanEntity(goal = "Menjadi pilot", fullJsonData = dummyJson)
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}