package com.example.stecu.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabel utama
@Entity(tableName = "career_plans")
data class CareerPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goal: String, // Misal: "Menjadi pilot"
    val fullJsonData: String // Simpan seluruh JSON response di sini untuk kemudahan
)