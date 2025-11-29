package com.musicapp.auth

import com.musicapp.repos.UserRepository
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object AuthService {
    private const val secret = "SUPER_SECRET_KEY_KTOR"
    private const val issuer = "ktor.musicapp"
    private const val audience = "spotify.clone"
    private const val validityInMs = 36_000_000L // 10 horas
    private val algorithm = Algorithm.HMAC256(secret)

    // Valida las credenciales del usuario y genera un token JWT si son correctas
    suspend fun validateCredentials(username: String, password: String): String? {
        val user = UserRepository.validateCredentials(username, password) ?: return null
        return generateToken(username, user.role)
    }

    // Genera un token JWT con el nombre de usuario y el rol como claims
    private fun generateToken(username: String, role: String): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("username", username)
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(algorithm)

    // Configura la autenticación JWT para Ktor
    fun configureJwt(config: JWTAuthenticationProvider.Config) {
        config.realm = "Music App"
        config.verifier(
            JWT
                .require(algorithm)
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
        )
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
}