package com.enm.projesihirbaziapp.Abstraction

import com.enm.projesihirbaziapp.Models.User

interface UserService {
    suspend fun getUserData(token: String): User?
    suspend fun logIn(email: String, password: String): Boolean
    suspend fun updateUserData(
        token: String,
        name: String,
        surname: String,
        imageFile: String,
        password: String
    ): User?

    suspend fun forgetPassword(email: String): String
    suspend fun refreshToken(): Boolean
}
