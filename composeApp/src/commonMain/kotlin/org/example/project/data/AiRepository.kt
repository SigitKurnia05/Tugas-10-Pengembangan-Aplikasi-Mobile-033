package org.example.project.data

import kotlinx.coroutines.flow.Flow

// ─── Error Types ──────────────────────────────────────────────────────────────
sealed class AiError : Exception() {
    data class RateLimited(val retryAfter: Int = 60) : AiError() {
        override val message = "Terlalu banyak request. Coba lagi dalam $retryAfter detik."
    }
    data class Unauthorized(override val message: String = "API key tidak valid.") : AiError()
    data class ServerError(override val message: String = "Server AI sedang bermasalah.") : AiError()
    data class NetworkError(override val message: String = "Tidak ada koneksi internet.") : AiError()
    data class EmptyResponse(override val message: String = "AI tidak memberikan respons.") : AiError()
    data class Unknown(override val message: String) : AiError()
}

// ─── Interface ────────────────────────────────────────────────────────────────
interface AiRepository {
    fun chatStream(message: String): Flow<String>
    suspend fun chat(message: String): Result<String>
    suspend fun summarize(noteContent: String): Result<String>
    suspend fun analyzeImage(base64: String, mimeType: String, prompt: String): Result<String>
    fun clearHistory()
    fun getHistorySize(): Int
}

// ─── Implementation ───────────────────────────────────────────────────────────
class AiRepositoryImpl(
    private val geminiService: GeminiService
) : AiRepository {

    override fun chatStream(message: String): Flow<String> =
        geminiService.chatStream(message)

    override suspend fun chat(message: String): Result<String> =
        safeAiCall { geminiService.chat(message).getOrThrow() }

    override suspend fun summarize(noteContent: String): Result<String> =
        safeAiCall { geminiService.summarize(noteContent).getOrThrow() }

    override suspend fun analyzeImage(
        base64: String,
        mimeType: String,
        prompt: String
    ): Result<String> = safeAiCall {
        geminiService.analyzeImage(base64, mimeType, prompt).getOrThrow()
    }

    override fun clearHistory() = geminiService.clearHistory()
    override fun getHistorySize() = geminiService.getHistorySize()
}

// ─── Safe Call Wrapper ────────────────────────────────────────────────────────
private suspend fun <T> safeAiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        val message = e.message ?: "Unknown error"
        val aiError = when {
            message.contains("401") || message.contains("API key") -> AiError.Unauthorized()
            message.contains("429") || message.contains("quota") ||
                    message.contains("RESOURCE_EXHAUSTED")                -> AiError.RateLimited()
            message.contains("500") || message.contains("503")    -> AiError.ServerError()
            message.contains("Unable to resolve") ||
                    message.contains("SocketException") ||
                    message.contains("timeout")                           -> AiError.NetworkError()
            message.contains("kosong")                            -> AiError.EmptyResponse()
            else                                                   -> AiError.Unknown(message)
        }
        Result.failure(aiError)
    }
}