package com.enm.projesihirbaziapp.DataAccess

import com.enm.projesihirbaziapp.Models.Academician
import com.enm.projesihirbaziapp.Abstraction.AcademicianService

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class AcademicianDataAccess : AcademicianService {

    // Swift'teki NetworkError karşılığı (Exception türevleri)
    sealed class NetworkError(message: String) : Exception(message) {
        object InvalidURL : NetworkError("Invalid URL")
        object NoData : NetworkError("No data")
        class DecodingError(detail: String) : NetworkError("Decoding error: $detail")
    }

    // Parametresiz init eşleniği zaten default var
    override fun getAcademics(
        currentPage: Int,
        selectedName: String,
        selectedProvince: String,
        selectedUniversity: String,
        selectedKeywords: String,
        completion: (Result<Pair<List<Academician>, Int>>) -> Unit
    ) {
        // Swift: APIEndpoints.getAcademicsURL(...)
        val urlString = APIEndpoints.getAcademicsURL(
            currentPage = currentPage,
            selectedName = selectedName,
            selectedProvince = selectedProvince,
            selectedUniversity = selectedUniversity,
            selectedKeywords = selectedKeywords
        )

        // Arka planda çalıştır (main thread ağ izni yok)
        Thread {
            try {
                val url = try {
                    URL(urlString)
                } catch (_: Exception) {
                    completion(Result.failure(NetworkError.InvalidURL))
                    return@Thread
                }

                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                if (stream == null) {
                    completion(Result.failure(NetworkError.NoData))
                    conn.disconnect()
                    return@Thread
                }

                val body = stream.bufferedReader(Charsets.UTF_8).use(BufferedReader::readText)
                conn.disconnect()

                // Swift: JSONDecoder().decode(ResponseAcademican.self, from: data)
                try {
                    val resp = Gson().fromJson(body, ResponseAcademican::class.java)
                    // Swift: completion(.success((response.items, response.totalPages)))
                    completion(Result.success(resp.items to resp.totalPages))
                } catch (e: JsonSyntaxException) {
                    completion(Result.failure(NetworkError.DecodingError(e.localizedMessage ?: "JsonSyntax")))
                } catch (e: Exception) {
                    completion(Result.failure(NetworkError.DecodingError(e.localizedMessage ?: "Unknown JSON error")))
                }
            } catch (e: Exception) {
                // Swift: completion(.failure(error))
                completion(Result.failure(e))
            }
        }.start()
    }
}

/**
 * Swift’teki Codable struct karşılığı.
 * Alan adları backend JSON’una göre Swift ile aynı tutuldu:
 *  currentPage, pageSize, totalItems, totalPages, items
 */
data class ResponseAcademican(
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<Academician>
)
