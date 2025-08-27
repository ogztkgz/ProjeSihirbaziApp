package com.enm.projesihirbaziapp.DataAccess

import com.enm.projesihirbaziapp.Abstraction.AcademicianService
import com.enm.projesihirbaziapp.Models.Academician
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class AcademicianDataAccess : AcademicianService {

    override suspend fun getAcademics(
        currentPage: Int,
        selectedName: String,
        selectedProvince: String,
        selectedUniversity: String,
        selectedKeywords: String
    ): Result<List<Academician>> = withContext(Dispatchers.IO) {

        val urlStr = APIEndpoints.getAcademicsURL(
            currentPage = currentPage,
            selectedName = selectedName,
            selectedProvince = selectedProvince,
            selectedUniversity = selectedUniversity,
            selectedKeywords = selectedKeywords
        )

        var conn: HttpURLConnection? = null
        try {
            val url = try {
                URL(urlStr)
            } catch (e: MalformedURLException) {
                return@withContext Result.failure(NetworkError.InvalidURL)
            }

            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                // Eğer endpoint Authorization istiyorsa:
                // setRequestProperty("Authorization", "Bearer $accessToken")
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (code in 200..299) {
                // {"currentPage":..,"pageSize":..,"totalItems":..,"totalPages":..,"items":[...]}
                val response = Gson().fromJson(text, ResponseAcademican::class.java)
                Result.success(response.items ?: emptyList())
            } else {
                Result.failure(IOException("HTTP $code: $text"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }
}

/** Swift’teki NetworkError karşılığı */
sealed class NetworkError(message: String? = null) : Exception(message) {
    data object InvalidURL : NetworkError("Invalid URL")
    data object NoData : NetworkError("No data")
    data class DecodingError(val detail: String) : NetworkError(detail)
}

/** API'den gelen yanıt modeli */
data class ResponseAcademican(
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<Academician>?
)
