package com.example.stecu.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val role: String,
    val imageUri: String, // Kita simpan URI dari gambar
    val timestamp: Long = System.currentTimeMillis()
)