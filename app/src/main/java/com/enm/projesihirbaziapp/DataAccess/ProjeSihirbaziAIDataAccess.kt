package com.enm.projesihirbaziapp.DataAccess

import com.enm.projesihirbaziapp.Abstraction.ProjeSihirbaziAIService
import com.enm.projesihirbaziapp.Models.ChatMessage
import com.enm.projesihirbaziapp.Models.ProjeSihirbaziAI
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Swift’teki DataAccess’in Kotlin karşılığı (suspend + Result) */
class ProjeSihirbaziAIDataAccess : ProjeSihirbaziAIService {

    private val gson = Gson()

    /* =========================
     * Helpers
     * ========================= */

    private fun makeConnection(
        urlStr: String,
        method: String,
        token: String,
        contentTypeJson: Boolean = false,
        timeout: Int = 15_000
    ): HttpURLConnection {
        val url = URL(urlStr)
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = timeout
            readTimeout = timeout
            setRequestProperty("Authorization", "Bearer $token")
            if (contentTypeJson) setRequestProperty("Content-Type", "application/json")
        }
    }

    private fun HttpURLConnection.readText(): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    /* =========================
     * API
     * ========================= */

    override suspend fun getOldChat(
        projectId: Int,
        token: String
    ): Result<List<ProjeSihirbaziAI>> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            conn = makeConnection(
                urlStr = APIEndpoints.getOldChatURL(projectId),
                method = "GET",
                token = token
            )
            val code = conn.responseCode
            val body = conn.readText()
            if (code in 200..299) {
                val list = gson.fromJson(body, Array<ProjeSihirbaziAI>::class.java)?.toList() ?: emptyList()
                Result.success(list)
            } else {
                Result.failure(IOException("HTTP $code: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    override suspend fun createNewChat(
        projectId: Int,
        token: String
    ): Result<ProjeSihirbaziAI> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            conn = makeConnection(
                urlStr = APIEndpoints.createNewChatURL(projectId),
                method = "POST",
                token = token,
                contentTypeJson = true
            )
            // Gövde yoksa bile aç-kapat (bazı sunucular için gerekli değil)
            conn.doOutput = true
            BufferedOutputStream(conn.outputStream).use { /* empty body */ }

            val code = conn.responseCode
            val body = conn.readText()
            if (code in 200..299) {
                val obj = gson.fromJson(body, ProjeSihirbaziAI::class.java)
                Result.success(obj)
            } else {
                Result.failure(IOException("HTTP $code: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    override suspend fun deleteChat(
        token: String,
        chatId: Int
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            // Swift tarafında DELETE yerine GET kullanılıyordu; API’niz öyle ise "GET" bırakın
            conn = makeConnection(
                urlStr = APIEndpoints.deleteChatURL(chatId),
                method = "GET",
                token = token
            )
            val code = conn.responseCode
            if (code in 200..299) Result.success(true)
            else Result.failure(IOException("HTTP $code: ${conn.readText()}"))
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    override suspend fun getChatWithId(
        projectId: Int,
        chatId: Int,
        token: String
    ): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        val old = getOldChat(projectId, token)

        old.fold(
            onSuccess = { chats ->
                val chat = chats.firstOrNull { it.id == chatId }
                    ?: return@withContext Result.failure(NoSuchElementException("Chat not found: $chatId"))

                try {
                    val msgs = gson.fromJson(
                        chat.chatHistoryJson,
                        Array<ChatMessage>::class.java
                    )?.toList() ?: emptyList()

                    Result.success(msgs)
                } catch (e: com.google.gson.JsonSyntaxException) {
                    Result.failure(e)
                }
            },
            onFailure = { err ->
                Result.failure(err)
            }
        )

    }

    override suspend fun sendMessage(
        chatId: Int,
        message: String,
        token: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            // Projendeki endpoint adını buna göre ayarla:
            // Eğer sende "APIEndpoints.sendMessageURL" bir String ise direkt onu kullan:
            // urlStr = APIEndpoints.sendMessageURL
            conn = makeConnection(
                urlStr = APIEndpoints.SEND_MESSAGE_URL,   // <- Gerekirse kendi sabit adına uyarlayın
                method = "POST",
                token = token,
                contentTypeJson = true
            )
            conn.doOutput = true

            val payload = mapOf(
                "chatId" to chatId,
                "message" to message,
                "fileInfo" to null
            )
            val body = gson.toJson(payload)
            BufferedOutputStream(conn.outputStream).use { out ->
                out.write(body.toByteArray(Charsets.UTF_8))
                out.flush()
            }

            val code = conn.responseCode
            if (code in 200..299) Result.success(Unit)
            else Result.failure(IOException("HTTP $code: ${conn.readText()}"))
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }
}
