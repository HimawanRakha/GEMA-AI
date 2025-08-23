package com.example.stecu.data.repository

import android.graphics.Bitmap
import com.example.stecu.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName

data class OcrResult(
    @SerializedName("title")
    val title: String = "Unknown Title",
    @SerializedName("role")
    val role: String = "Not specified"
)

class GeminiRepository {

    private val visionModel = GenerativeModel(
        modelName = "gemini-1.5-flash", // Model ini mendukung input gambar dan teks
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.2f
            responseMimeType = "application/json" // Meminta output JSON
        }
    )

    suspend fun extractCertificateInfoFromImage(image: Bitmap): Result<OcrResult> {
        return try {
            val prompt = """
                You are an expert OCR model specializing in extracting information from certificates of achievement or participation.
                Given the following image, extract the title of the event/competition and the role or achievement of the person (e.g., 'Juara 1', 'Peserta', 'Panitia', 'Gold Medal').
                Return the result as a single, valid JSON object with two keys: "title" and "role".
                If you cannot find specific information, provide a sensible default like "Sertifikat Partisipasi" for the title or "Peserta" for the role.
                Do not include any other text, explanations, or markdown syntax.
            """.trimIndent()

            val inputContent = content {
                image(image)
                text(prompt)
            }

            val response = visionModel.generateContent(inputContent)

            response.text?.let { jsonString ->
                // Parsing JSON dengan aman
                val ocrResult = try {
                    Gson().fromJson(jsonString, OcrResult::class.java)
                } catch (e: JsonSyntaxException) {
                    // Jika JSON tidak valid, kembalikan default
                    OcrResult("Gagal Parsing Judul", "Gagal Parsing Peran")
                }
                Result.success(ocrResult)
            } ?: Result.failure(Exception("Empty response from Vision API."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Model untuk respons sekali jalan (seperti di AssistantScreen)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        // Prompt General untuk asisten suara
        systemInstruction = content(role = "model") {
            text("Anda adalah GEMA, asisten AI yang ramah, membantu, dan ringkas dari Indonesia. Jawab semua pertanyaan dalam Bahasa Indonesia.")
        }
    )

    // Model untuk mode chat
    private val chatModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
        },
        systemInstruction = content(role = "model") {
            text("Anda adalah GEMA, asisten AI yang ramah dan membantu dari Indonesia. Jawab semua pertanyaan dalam Bahasa Indonesia dalam format percakapan.")
        }
    )

    private val careerPlanModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.4f // Sedikit lebih deterministik untuk JSON
            responseMimeType = "application/json" // Minta output JSON secara eksplisit
        },
        systemInstruction = content(role = "model") {
            text("""
                BUAT DALAM BAHASA INDONESIA
                Anda adalah AI Career Plan Generator. Tugas Anda adalah membuat rencana karir dalam format JSON yang valid berdasarkan input pengguna.
                - JSON harus berisi keys: "goal", dan "milestones".
                - Setiap milestone harus memiliki "id", "title", "duration_weeks", dan "quests".
                - Setiap quest harus memiliki "id", "title", "steps" (minimal 3), dan "resources".
                - Key "resources" HARUS berupa array of objects, di mana setiap object memiliki key "title" (string) dan "url" (string) pastikan url ada dan nyata.
                - JANGAN tambahkan teks, penjelasan, atau markdown apa pun di luar objek JSON.
                - Output HANYA JSON.
            """.trimIndent())
        }
    )

    // Fungsi untuk memproses teks sekali jalan (voice assistant)
    suspend fun processText(text: String): Result<String> {
        return try {
            val response = generativeModel.generateContent(text)
            response.text?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Empty response from API."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fungsi untuk mengirim pesan dalam mode chat
    suspend fun sendMessage(history: List<com.google.ai.client.generativeai.type.Content>, message: String): Result<String> {
        return try {
            val chat = chatModel.startChat(history = history)
            val response = chat.sendMessage(message)
            response.text?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Empty response from API."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateCareerPlanJson(goal: String, duration: String, background: String): Result<String> {
        return try {
            val userPrompt = """
                Buatkan rencana karir dengan detail berikut:
                - Tujuan (goal): "$goal"
                - Durasi (timeframe): "$duration"
                - Latar Belakang Pengguna (background): "$background"
            """.trimIndent()

            // Kita tambahkan tag pembungkus secara manual di sini
            val response = careerPlanModel.generateContent(userPrompt)
            response.text?.let { jsonResponse ->
                val wrappedJson = "<CAREER_PLAN_JSON>\n$jsonResponse\n</CAREER_PLAN_JSON>"
                Result.success(wrappedJson)
            } ?: Result.failure(Exception("Empty JSON response from API."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}