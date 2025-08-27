package com.enm.projesihirbaziapp.DataAccess

import com.enm.projesihirbaziapp.Abstraction.FiltreService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class FiltreDataAccess : FiltreService {

    private val gson = Gson()

    override suspend fun getKurumlar(tur: String): Result<List<String>> =
        fetchList(APIEndpoints.getKurumURL(tur))

    override suspend fun getSektorler(): Result<List<String>> =
        fetchList(APIEndpoints.GET_SECTORS)

    override suspend fun getIl(): Result<List<String>> =
        fetchList(APIEndpoints.PROVINCES)

    override suspend fun getUni(): Result<List<String>> =
        fetchList(APIEndpoints.UNIVERSITIES)

    override suspend fun getKeyword(): Result<List<String>> =
        fetchList(APIEndpoints.KEYWORDS_URL)

    /** Genel GET + JSON array<String> parse yardımcıcısı */
    private suspend fun fetchList(urlStr: String): Result<List<String>> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
                // Gerekirse Authorization header ekleyin:
                // setRequestProperty("Authorization", "Bearer $token")
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (code in 200..299) {
                val type = object : TypeToken<List<String>>() {}.type
                val list: List<String> = gson.fromJson(body, type) ?: emptyList()
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
}
