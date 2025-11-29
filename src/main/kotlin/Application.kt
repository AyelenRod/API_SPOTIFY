package com.musicapp

import io.ktor.http.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.musicapp.auth.AuthService
import com.musicapp.database.DatabaseFactory
import com.musicapp.routes.authRouting
import com.musicapp.routes.contentRouting
import com.musicapp.services.ContentService
import com.musicapp.services.S3Service
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
    // NICIALIZACIÓN DE BASE DE DATOS
    val dbUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:postgresql://localhost:5432/spotify_db"
    val dbUser = environment.config.propertyOrNull("database.user")?.getString()
        ?: "postgres"
    val dbPassword = environment.config.propertyOrNull("database.password")?.getString()
        ?: "postgres"

    DatabaseFactory.init(
        jdbcUrl = dbUrl,
        username = dbUser,
        password = dbPassword
    )

    println("Base de datos conectada: $dbUrl")

    // INICIALIZACIÓN DE SERVICIOS
    val s3Service = S3Service(environment.config)
    val contentService = ContentService(s3Service)

    // PLUGINS
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }

    // Plugin de CORS
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    // Plugin de autenticación JWT
    install(Authentication) {
        jwt("auth-jwt") {
            AuthService.configureJwt(this)
        }
    }

    //RUTAS
    routing {
        authRouting()
        contentRouting(contentService)
    }

    println("Servidor iniciado en http://0.0.0.0:8080")
}