package com.musicapp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.musicapp.auth.AuthService
import com.musicapp.database.DatabaseFactory
import com.musicapp.routes.authRouting
import com.musicapp.routes.contentRouting
import com.musicapp.services.ContentService
import com.musicapp.services.S3Service
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    println("Iniciando aplicación Spotify Clone API...")

    val dbUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:postgresql://localhost:5432/spotify_db"
    val dbUser = environment.config.propertyOrNull("database.user")?.getString()
        ?: "ktor_user"
    val dbPassword = environment.config.propertyOrNull("database.password")?.getString()
        ?: "aplicacionesweb"

    DatabaseFactory.init(
        jdbcUrl = dbUrl,
        username = dbUser,
        password = dbPassword
    )

    println("Base de datos conectada: $dbUrl")

    val s3Service = S3Service(environment.config)
    println("AWS S3 configurado")

    val contentService = ContentService(s3Service)

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
    println("Content Negotiation (JSON) configurado")

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }
    println("CORS configurado")

    install(Authentication) {
        jwt("auth-jwt") {
            AuthService.configureJwt(this)
        }
    }
    println("Autenticación JWT configurada")

    routing {
        authRouting()
        contentRouting(contentService)
    }
    println("Rutas configuradas")

    println("Servidor iniciado en http://0.0.0.0:8080")
}