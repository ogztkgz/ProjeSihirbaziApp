package com.enm.projesihirbaziapp.Abstraction

import com.enm.projesihirbaziapp.Models.ChatMessage
import com.enm.projesihirbaziapp.Models.ProjeSihirbaziAI

interface ProjeSihirbaziAIService {
    suspend fun getOldChat(
        projectId: Int,
        token: String
    ): Result<List<ProjeSihirbaziAI>>

    suspend fun createNewChat(
        projectId: Int,
        token: String
    ): Result<ProjeSihirbaziAI>

    suspend fun deleteChat(
        token: String,
        chatId: Int
    ): Result<Boolean>

    suspend fun getChatWithId(
        projectId: Int,
        chatId: Int,
        token: String
    ): Result<List<ChatMessage>>

    suspend fun sendMessage(
        chatId: Int,
        message: String,
        token: String
    ): Result<Unit>
}
