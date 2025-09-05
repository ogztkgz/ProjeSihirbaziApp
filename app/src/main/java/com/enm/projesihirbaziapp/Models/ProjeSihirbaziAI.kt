// Models/ProjeSihirbaziAI.kt
package com.enm.projesihirbaziapp.Models

data class ProjeSihirbaziAI(
    val id: Int,
    val userId: Int,
    val chatHistoryJson: String,
    val createdDateTime: String,
    val lastDateTime: String,
    val model: String
)
