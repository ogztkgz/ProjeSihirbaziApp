package com.enm.projesihirbaziapp.Models

data class Academician(
    val title: String,
    val name: String,
    val section: String,
    val keywords: String,
    val imageUrl: String,
    val university: String,
    val province: String
) {
    val id: String get() = name
}
