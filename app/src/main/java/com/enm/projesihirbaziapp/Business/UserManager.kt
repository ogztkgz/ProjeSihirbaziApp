package com.enm.projesihirbaziapp.Business

import android.content.Context
import com.enm.projesihirbaziapp.Abstraction.UserService
import com.enm.projesihirbaziapp.DataAccess.UserDataAccess
import com.enm.projesihirbaziapp.Models.User

class UserManager(
    context: Context
) : UserService {

    private val userInterface = UserDataAccess(context)

    override suspend fun getUserData(token: String): User? {
        return userInterface.getUserData(token)
    }

    override suspend fun logIn(email: String, password: String): Boolean {
        return if (email.isNotBlank() && password.isNotBlank()) {
            userInterface.logIn(email, password)
        } else {
            false
        }
    }

    override suspend fun updateUserData(
        token: String,
        name: String,
        surname: String,
        imageFile: String,
        password: String
    ): User? {
        return userInterface.updateUserData(token, name, surname, imageFile, password)
    }

    override suspend fun forgetPassword(email: String): String {
        return userInterface.forgetPassword(email)
    }

    override suspend fun refreshToken(): Boolean {
        return userInterface.refreshToken()
    }
}
