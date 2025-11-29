<<<<<<< HEAD
package auth
=======
package com.musicapp.auth
>>>>>>> api2

import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object AuthService {
    private const val secret = "SUPER_SECRET_KEY_KTOR"
    private const val issuer = "ktor.musicapp"
    private const val audience = "spotify.clone"
<<<<<<< HEAD
    private const val validityInMs = 36_000_000L // 10 horas
    private val algorithm = Algorithm.HMAC256(secret)

    // Roles
=======
    private const val validityInMs = 36_000_000L

    private val algorithm = Algorithm.HMAC256(secret)

    // Mapa de usuarios válidos
>>>>>>> api2
    private val validUsers = mapOf(
        "admin" to "pass123",
        "user" to "pass123"
    )

<<<<<<< HEAD
=======

>>>>>>> api2
    fun validateCredentials(username: String, password: String): String? {
        if (validUsers[username] == password) {
            val role = if (username == "admin") "ADMIN" else "USER"
            return generateToken(username, role)
        }
        return null
    }

<<<<<<< HEAD
=======

>>>>>>> api2
    private fun generateToken(username: String, role: String): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("username", username)
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(algorithm)

<<<<<<< HEAD
    // Configuración para la validación de JWT
    fun configureJwt(config: JWTAuthenticationProvider.Config) {
        config.realm = "Music App"
=======

    fun configureJwt(config: JWTAuthenticationProvider.Config) {
        config.realm = "Music App"

>>>>>>> api2
        config.verifier(
            JWT
                .require(algorithm)
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
        )
<<<<<<< HEAD
=======

        // Función de validación
>>>>>>> api2
        config.validate { credential ->
            if (credential.payload.audience.contains(audience)) {
                JWTPrincipal(credential.payload)
            } else {
                null
            }
        }
    }

    fun getSecret(): String = secret
    fun getIssuer(): String = issuer
    fun getAudience(): String = audience
<<<<<<< HEAD
}
=======
}
>>>>>>> api2
