package com.musicapp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.musicapp.database.DatabaseFactory
import com.musicapp.routes.ContentRouting
import com.musicapp.services.ContentService
import com.musicapp.repos.ArtistRepository
import com.musicapp.repos.AlbumRepository
import com.musicapp.repos.TrackRepository
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    println("Iniciando aplicación Spotify Clone API (Sin Auth/S3)...")

    // 1. Configuración Base de Datos
    val dbUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: System.getenv("DATABASE_URL")
        ?: "jdbc:postgresql://localhost:5432/spotify_db"

    val dbUser = environment.config.propertyOrNull("database.user")?.getString()
        ?: System.getenv("DATABASE_USER")
        ?: "postgres" // Usuario por defecto suele ser postgres

    val dbPassword = environment.config.propertyOrNull("database.password")?.getString()
        ?: System.getenv("DATABASE_PASSWORD")
        ?: "password"

    DatabaseFactory.init(
        jdbcUrl = dbUrl,
        username = dbUser,
        password = dbPassword
    )

    // 2. Inicialización de Servicios
    val artistRepository = ArtistRepository
    val albumRepository = AlbumRepository
    val trackRepository = TrackRepository

    val contentService = ContentService(
        artistRepository = artistRepository,
        albumRepository = albumRepository,
        trackRepository = trackRepository
    )

    // 3. Plugins Ktor
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }

    install(CORS) {
        anyHost() // Desarrollo: permitir todo. En prod, especificar dominios.
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    // 4. Rutas
    routing {
        // Ruta de chequeo de salud
        get("/health") {
            call.respond(HttpStatusCode.OK, "API funcionando correctamente")
        }

        // Rutas de contenido (Artistas, Albumes, Tracks)
        ContentRouting(contentService)
    }

    println("Servidor iniciado en http://0.0.0.0:8080")
}