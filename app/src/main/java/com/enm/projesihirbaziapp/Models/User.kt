package com.enm.projesihirbaziapp.Models

data class User(
    private val id: Int,
    private val name: String,
    private val surname: String,
    private val email: String,
    private val phone: String,
    private val imageFile: String?,
    private val role: String
) {
    fun getId(): Int = id
    fun getName(): String = name
    fun getSurname(): String = surname
    fun getEmail(): String = email
    fun getPhone(): String = phone
    fun getImageFile(): String? = imageFile
    fun getRole(): String = role
}
