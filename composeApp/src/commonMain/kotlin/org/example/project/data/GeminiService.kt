package org.example.project.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ─── Data Models ─────────────────────────────────────────────────────────────

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("generationConfig")
    val generationConfig: GenerationConfig? = null,
    @SerialName("systemInstruction")
    val systemInstruction: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    @SerialName("inline_data")
    val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    @SerialName("mime_type")
    val mimeType: String,
    val data: String   // base64
)

@Serializable
data class GenerationConfig(
    val temperature: Double = 0.7,
    @SerialName("maxOutputTokens")
    val maxOutputTokens: Int = 4096,
    @SerialName("topP")
    val topP: Double = 0.95
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
    val error: GeminiError? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent,
    @SerialName("finishReason")
    val finishReason: String? = null
)

@Serializable
data class GeminiError(
    val code: Int = 0,
    val message: String = "",
    val status: String = ""
)

// ─── HTTP Client ──────────────────────────────────────────────────────────────

fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.NONE
    }
}

// ─── GeminiService ────────────────────────────────────────────────────────────

class GeminiService(private val apiKey: String) {

    private val client = createHttpClient()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val model = "gemini-2.5-flash"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // System prompt
    private val systemInstruction = GeminiContent(
        role = "user",
        parts = listOf(
            GeminiPart(
                text = """
                Kamu adalah asisten catatan cerdas bernama "NoteAI" yang terintegrasi dalam aplikasi catatan pribadi.

                Kemampuanmu:
                - Merangkum catatan panjang menjadi poin-poin utama
                - Membantu menulis dan menyempurnakan isi catatan
                - Menerjemahkan teks ke bahasa yang diminta
                - Menganalisis gambar dan mendeskripsikan isinya secara detail
                - Menjawab pertanyaan umum dengan lengkap

                Aturan:
                - Jawab dalam bahasa yang sama dengan pertanyaan pengguna (Indonesia atau Inggris)
                - Respons harus LENGKAP dan TIDAK dipotong di tengah — jelaskan sampai tuntas
                - Jika diminta merangkum, gunakan format poin-poin dengan "• "
                - JANGAN PERNAH gunakan simbol asterisk (*) atau markdown bold/italic
                - Jangan pernah mengarang fakta — jika tidak tahu, katakan dengan jujur
                - Selalu ramah dan profesional
                """.trimIndent()
            )
        )
    )

    // History untuk multi-turn conversation
    private val conversationHistory = mutableListOf<GeminiContent>()

    // ── Streaming chat ────────────────────────────────────────────────────────
    fun chatStream(userMessage: String): Flow<String> = flow {
        conversationHistory.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userMessage))
            )
        )

        val request = GeminiRequest(
            contents = conversationHistory.toList(),
            systemInstruction = systemInstruction,
            generationConfig = GenerationConfig(temperature = 0.7, maxOutputTokens = 4096)
        )

        val fullResponse = StringBuilder()

        client.preparePost("$baseUrl/models/$model:streamGenerateContent?alt=sse&key=$apiKey") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.execute { response ->
            val channel: ByteReadChannel = response.bodyAsChannel()

            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break

                if (line.startsWith("data: ")) {
                    val jsonData = line.removePrefix("data: ").trim()
                    if (jsonData == "[DONE]" || jsonData.isEmpty()) continue

                    try {
                        val chunk = json.decodeFromString<GeminiResponse>(jsonData)
                        val text = chunk.candidates
                            .firstOrNull()?.content?.parts?.firstOrNull()?.text ?: continue

                        val cleaned = text
                            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
                            .replace(Regex("\\*(.+?)\\*"), "$1")
                            .replace(Regex("(?m)^\\* "), "• ")

                        fullResponse.append(cleaned)
                        emit(cleaned)
                    } catch (_: Exception) {
                        // skip malformed chunk
                    }
                }
            }
        }

        // Simpan full response ke history
        if (fullResponse.isNotEmpty()) {
            conversationHistory.add(
                GeminiContent(
                    role = "model",
                    parts = listOf(GeminiPart(text = fullResponse.toString()))
                )
            )
        }
    }

    // ── Fallback non-streaming chat (dipakai jika streaming gagal) ─────────────
    suspend fun chat(userMessage: String): Result<String> = runCatching {
        conversationHistory.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userMessage))
            )
        )

        val request = GeminiRequest(
            contents = conversationHistory.toList(),
            systemInstruction = systemInstruction,
            generationConfig = GenerationConfig(temperature = 0.7, maxOutputTokens = 4096)
        )

        val response: GeminiResponse = client.post(
            "$baseUrl/models/$model:generateContent"
        ) {
            contentType(ContentType.Application.Json)
            parameter("key", apiKey)
            setBody(request)
        }.body()

        if (response.error != null) {
            throw Exception("Gemini error ${response.error.code}: ${response.error.message}")
        }

        val assistantText = response.candidates
            .firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Response kosong dari Gemini")

        conversationHistory.add(
            GeminiContent(
                role = "model",
                parts = listOf(GeminiPart(text = assistantText))
            )
        )

        assistantText
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
            .replace(Regex("\\*(.+?)\\*"), "$1")
            .replace(Regex("(?m)^\\* "), "• ")
            .trim()
    }

    // ── Image analysis ────────────────────────────────────────────────────────
    suspend fun analyzeImage(
        base64Image: String,
        mimeType: String = "image/jpeg",
        prompt: String = "Deskripsikan isi gambar ini secara lengkap dan detail dalam bahasa Indonesia. Sebutkan objek, warna, aktivitas, teks yang terlihat, dan konteks keseluruhan gambar."
    ): Result<String> = runCatching {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = mimeType,
                                data = base64Image
                            )
                        ),
                        GeminiPart(text = prompt)
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.4, maxOutputTokens = 2048)
        )

        val response: GeminiResponse = client.post(
            "$baseUrl/models/$model:generateContent"
        ) {
            contentType(ContentType.Application.Json)
            parameter("key", apiKey)
            setBody(request)
        }.body()

        if (response.error != null) {
            throw Exception("Gemini error ${response.error.code}: ${response.error.message}")
        }

        val rawText = response.candidates
            .firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Response kosong dari Gemini")

        rawText
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
            .replace(Regex("\\*(.+?)\\*"), "$1")
            .replace(Regex("(?m)^\\* "), "• ")
            .trim()
    }

    // ── Summarize ─────────────────────────────────────────────────────────────
    suspend fun summarize(noteContent: String): Result<String> = runCatching {
        val prompt = """
            Kamu adalah asisten ringkasan catatan yang cerdas.
            
            Tugasmu adalah menganalisis dan merangkum catatan berikut secara LENGKAP dan MENDETAIL.
            
            Format jawaban:
            1. Mulai dengan satu kalimat gambaran umum isi catatan.
            2. Lanjutkan dengan poin-poin utama menggunakan "• " (minimal 3, maksimal 8 poin).
            3. Setiap poin harus informatif dan tidak dipotong setengah-setengah.
            4. Jika catatan mengandung langkah-langkah atau daftar, sertakan semua langkah penting.
            5. Tutup dengan satu kalimat kesimpulan atau saran tindak lanjut jika relevan.
            
            PENTING: Jangan gunakan simbol asterisk (*) atau markdown bold/italic.
            Tulis dengan bahasa yang sama seperti isi catatan (Indonesia atau Inggris).
            
            Catatan yang akan dirangkum:
            $noteContent
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.4, maxOutputTokens = 2048)
        )

        val response: GeminiResponse = client.post(
            "$baseUrl/models/$model:generateContent"
        ) {
            contentType(ContentType.Application.Json)
            parameter("key", apiKey)
            setBody(request)
        }.body()

        val rawText = response.candidates.first().content.parts.first().text
            ?: throw Exception("Response kosong")
        rawText
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
            .replace(Regex("\\*(.+?)\\*"), "$1")
            .replace(Regex("(?m)^\\* "), "• ")
            .trim()
    }

    fun clearHistory() { conversationHistory.clear() }
    fun getHistorySize(): Int = conversationHistory.size
}