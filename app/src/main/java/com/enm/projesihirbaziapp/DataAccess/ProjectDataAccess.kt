package com.enm.projesihirbaziapp.DataAccess

import com.enm.projesihirbaziapp.Abstraction.ProjectPage
import com.enm.projesihirbaziapp.Abstraction.ProjectService
import com.enm.projesihirbaziapp.Models.Projects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ProjectDataAccess : ProjectService {

    private val gson = Gson()

    override suspend fun getProject(
        tur: String,
        page: Int,
        sector: String,
        search: String,
        status: String,
        company: String,
        sortOrder: String
    ): Result<ProjectPage> = withContext(Dispatchers.IO) {
        val urlStr = APIEndpoints.getProjectURL(
            tur = tur,
            page = page,
            sector = sector,
            search = search,
            status = status,
            company = company,
            sortOrder = sortOrder
        )

        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
                // Gerekirse Authorization:
                // setRequestProperty("Authorization", "Bearer $token")
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (code in 200..299) {
                val resp = gson.fromJson(body, ResponseProject::class.java)
                Result.success(
                    ProjectPage(
                        items = resp.items ?: emptyList(),
                        totalPages = resp.totalPages ?: 1
                    )
                )
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

/** API'den dönen sayfa yapısı */
data class ResponseProject(
    val currentPage: Int?,
    val pageSize: Int?,
    val totalItems: Int?,
    val totalPages: Int?,
    val items: List<Projects>?
)
