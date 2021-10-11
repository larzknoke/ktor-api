package com.larsknoke.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.larsknoke.models.User
import io.ktor.config.*
import java.util.*

class TokenManager(val config: HoconApplicationConfig) {
    fun generateJWTToken(user: User): String? {
        val audience = config.property("jwt.audience").getString()
        val issuer = config.property("jwt.issuer").getString()
        val secret = config.property("jwt.secret").getString()
        val expirationDate = Date(System.currentTimeMillis() + 6000)

        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", user.username)
            .withClaim("userId", user.id)
            //.withExpiresAt(expirationDate)
            .sign(Algorithm.HMAC256(secret))
        return token
    }
}
