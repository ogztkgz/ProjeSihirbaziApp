package com.enm.projesihirbaziapp.DataAccess

import android.content.Context
import com.enm.projesihirbaziapp.Abstraction.UserService
import com.enm.projesihirbaziapp.Models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UserDataAccess(private val context: Context) : UserService {

    private val prefs = context.getSharedPreferences("proje_sihirbazi_prefs", Context.MODE_PRIVATE)

    // ----------------------------- GET USER DATA -----------------------------
    override suspend fun getUserData(token: String): User? = withContext(Dispatchers.IO) {
        val url = URL(APIEndpoints.UPDATE_AND_DATA)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = 15000
            readTimeout = 15000
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code in 200..299) parseUser(JSONObject(text)) else null
        } catch (e: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    // ----------------------------- LOGIN -----------------------------
    override suspend fun logIn(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val url = URL(APIEndpoints.LOGIN)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code in 200..299) {
                val json = JSONObject(text)
                val access = json.optString("accessToken", "")
                val refresh = json.optString("refreshToken", "")
                if (access.isNotEmpty() && refresh.isNotEmpty()) {
                    saveTokens(access, refresh)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        } finally {
            conn.disconnect()
        }
    }

    // ----------------------------- UPDATE USER DATA -----------------------------
    override suspend fun updateUserData(
        token: String,
        name: String,
        surname: String,
        imageFile: String,
        password: String
    ): User? = withContext(Dispatchers.IO) {
        val url = URL(APIEndpoints.UPDATE_AND_DATA)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }
        val body = JSONObject().apply {
            put("name", name)
            put("surname", surname)
            put("imageFile", imageFile)
            put("password", password)
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code in 200..299) parseUser(JSONObject(text)) else null
        } catch (e: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    // ----------------------------- FORGOT PASSWORD -----------------------------
    override suspend fun forgetPassword(email: String): String = withContext(Dispatchers.IO) {
        val url = URL(APIEndpoints.FORGET_PASSWORD)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }
        val body = JSONObject().apply { put("email", email) }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code in 200..299) text else "Hata: $code — $text"
        } catch (e: Exception) {
            "Hata oluştu: ${e.localizedMessage ?: "Bilinmeyen hata"}"
        } finally {
            conn.disconnect()
        }
    }

    // ----------------------------- REFRESH TOKEN -----------------------------
    override suspend fun refreshToken(): Boolean = withContext(Dispatchers.IO) {
        val refresh = getRefreshToken() ?: return@withContext false
        val url = URL(APIEndpoints.REFRESH_TOKEN)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }
        val body = JSONObject().apply { put("refreshToken", refresh) }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code in 200..299) {
                val json = JSONObject(text)
                val newAccess = json.optString("accessToken", "")
                val newRefresh = json.optString("refreshToken", "")
                if (newAccess.isNotEmpty() && newRefresh.isNotEmpty()) {
                    saveTokens(newAccess, newRefresh)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        } finally {
            conn.disconnect()
        }
    }

    // ----------------------------- Helpers -----------------------------
    private fun parseUser(json: JSONObject): User {
        return User(
            id = json.optInt("id", 0),
            name = json.optString("name", ""),
            surname = json.optString("surname", ""),
            email = json.optString("email", ""),
            phone = json.optString("phone", ""),
            imageFile = json.optString("imageFile", ""),
            role = json.optString("role", "")
        )
    }

    private fun saveTokens(access: String, refresh: String) {
        prefs.edit()
            .putString("accessToken", access)
            .putString("refreshToken", refresh)
            .apply()
    }

    private fun getRefreshToken(): String? = prefs.getString("refreshToken", null)
    fun getAccessToken(): String? = prefs.getString("accessToken", null)
}
