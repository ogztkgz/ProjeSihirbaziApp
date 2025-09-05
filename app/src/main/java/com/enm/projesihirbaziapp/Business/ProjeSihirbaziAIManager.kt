package com.enm.projesihirbaziapp.Business

import com.enm.projesihirbaziapp.Abstraction.ProjeSihirbaziAIService
import com.enm.projesihirbaziapp.DataAccess.ProjeSihirbaziAIDataAccess
import com.enm.projesihirbaziapp.Models.ChatMessage
import com.enm.projesihirbaziapp.Models.ProjeSihirbaziAI

class ProjeSihirbaziAIManager(
    private val dataAccess: ProjeSihirbaziAIDataAccess = ProjeSihirbaziAIDataAccess()
) : ProjeSihirbaziAIService {

    override suspend fun getOldChat(
        projectId: Int,
        token: String
    ): Result<List<ProjeSihirbaziAI>> =
        dataAccess.getOldChat(projectId, token)

    override suspend fun createNewChat(
        projectId: Int,
        token: String
    ): Result<ProjeSihirbaziAI> =
        dataAccess.createNewChat(projectId, token)

    override suspend fun deleteChat(
        token: String,
        chatId: Int
    ): Result<Boolean> =
        dataAccess.deleteChat(token, chatId)

    override suspend fun getChatWithId(
        projectId: Int,
        chatId: Int,
        token: String
    ): Result<List<ChatMessage>> =
        dataAccess.getChatWithId(projectId, chatId, token)

    override suspend fun sendMessage(
        chatId: Int,
        message: String,
        token: String
    ): Result<Unit> =
        dataAccess.sendMessage(chatId, message, token)
}
