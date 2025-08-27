package com.enm.projesihirbaziapp.DataAccess

object APIEndpoints {

    private const val baseUrl = "https://projesihirbaziapi.enmdigital.com"

    // --- Sabit endpoint URL'leri ---
    val LOGIN = "$baseUrl/Auth/login"
    val UPDATE_AND_DATA = "$baseUrl/Auth/me"
    val POST_FORM = "$baseUrl/ContactForm/antremakine-send-message"
    val GET_SECTORS = "$baseUrl/ScrapedProject/sectors"
    val DASHBOARD = "$baseUrl/App/dashboard"
    val PROVINCES = "$baseUrl/Academician/provinces"
    val UNIVERSITIES = "$baseUrl/Academician/universities"
    val KEYWORDS_URL = "$baseUrl/Academician/keywords"
    val GET_MODELS_URL = "$baseUrl/ProjeSihirbaziAI/models"
    val SEND_MESSAGE_URL = "$baseUrl/ProjeSihirbaziAI/ask"
    val FORGET_PASSWORD = "$baseUrl/Auth/forgot-password"
    val REFRESH_TOKEN = "$baseUrl/Auth/refresh-token"

    // --- Dinamik endpointler ---
    fun getProjectURL(
        tur: String,
        page: Int,
        sector: String,
        search: String,
        status: String,
        company: String,
        sortOrder: String
    ): String {
        return "$baseUrl/ScrapedProject/projects?tur=$tur&page=$page&sector=$sector&search=$search&submissionStatus=$status&company=$company&sortOrder=$sortOrder"
    }

    fun getKurumURL(tur: String): String {
        return "$baseUrl/ScrapedProject/companies?tur=$tur"
    }

    fun deleteChatURL(chatId: Int): String {
        return "$baseUrl/ProjeSihirbaziAI/delete-chat/$chatId"
    }

    fun getOldChatURL(id: Int): String {
        return "$baseUrl/ProjeSihirbaziAI/chats/ProjeSihirbazı-$id"
    }

    fun createNewChatURL(id: Int): String {
        return "$baseUrl/ProjeSihirbaziAI/new-chat/ProjeSihirbazı-$id"
    }

    fun getAcademicsURL(
        currentPage: Int,
        selectedName: String,
        selectedProvince: String,
        selectedUniversity: String,
        selectedKeywords: String
    ): String {
        return "$baseUrl/Academician/academicians?page=$currentPage&search=$selectedName&province=$selectedProvince&university=$selectedUniversity&keyword=$selectedKeywords"
    }
}
