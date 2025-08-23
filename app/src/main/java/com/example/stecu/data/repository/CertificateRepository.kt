package com.example.stecu.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.stecu.data.db.AppDatabase
import com.example.stecu.data.db.CertificateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CertificateRepository(private val context: Context) {

    private val certificateDao = AppDatabase.getDatabase(context).certificateDao()
    private val geminiRepository = GeminiRepository()

    val allCertificates: Flow<List<CertificateEntity>> = certificateDao.getAllCertificates()

    suspend fun processAndSaveCertificate(imageUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Ubah URI menjadi Bitmap
            val bitmap = uriToBitmap(imageUri)

            // 2. Kirim bitmap ke Gemini untuk OCR
            val ocrResult = geminiRepository.extractCertificateInfoFromImage(bitmap).getOrThrow()

            // 3. Simpan hasil ke database
            val newCertificate = CertificateEntity(
                title = ocrResult.title,
                role = ocrResult.role,
                imageUri = imageUri.toString() // Simpan URI sebagai String
            )
            certificateDao.insertCertificate(newCertificate)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    suspend fun getCertificate(id: Int): CertificateEntity? {
        return certificateDao.getCertificateById(id)
    }

    suspend fun deleteCertificate(id: Int) {
        certificateDao.deleteCertificate(id)
    }
}