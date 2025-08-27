package com.enm.projesihirbaziapp.DataAccess

import com.enm.projesihirbaziapp.Abstraction.DashboardService
import com.enm.projesihirbaziapp.Models.Dashboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DashboardDataAccess : DashboardService {

    override suspend fun fetchDashboardData(): Result<Dashboard> = withContext(Dispatchers.IO) {
        val urlStr = APIEndpoints.DASHBOARD
        val url = try {
            URL(urlStr)
        } catch (e: Exception) {
            return@withContext Result.failure(IllegalArgumentException("Invalid URL: $urlStr", e))
        }

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            // Eğer endpoint yetki istiyorsa aşağıyı açın:
            // setRequestProperty("Authorization", "Bearer ${yourAccessToken}")
        }

        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (code in 200..299) {
                val json = JSONObject(text)
                val grantCount = json.optInt("grantCount", 0)
                val academicianCount = json.optInt("academicianCount", 0)
                val tenderCount = json.optInt("tenderCount", 0)

                val dashboard = Dashboard(
                    grantCount = grantCount,
                    academicianCount = academicianCount,
                    tenderCount = tenderCount
                )
                Result.success(dashboard)
            } else {
                Result.failure(IOException("HTTP $code: $text"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn.disconnect()
        }
    }
}
