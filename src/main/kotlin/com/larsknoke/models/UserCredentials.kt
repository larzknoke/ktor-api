package com.larsknoke.models

import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt

@Serializable
class UserCredentials(
    val username: String,
    val password: String
){
    fun hashedPassword(): String? {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun isValidCredentials(): Boolean {
        return username.length >= 3 && password.length >= 8
    }
}
