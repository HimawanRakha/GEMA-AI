package com.example.stecu.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: CertificateEntity)

    @Query("SELECT * FROM certificates ORDER BY timestamp DESC")
    fun getAllCertificates(): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM certificates WHERE id = :id")
    suspend fun getCertificateById(id: Int): CertificateEntity?

    @Query("DELETE FROM certificates WHERE id = :id")
    suspend fun deleteCertificate(id: Int)
}