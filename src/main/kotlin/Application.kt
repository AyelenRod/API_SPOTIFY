package com.musicapp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.musicapp.auth.AuthService
import com.musicapp.database.DatabaseFactory
import com.musicapp.routes.authRouting
import com.musicapp.routes.contentRouting
import com.musicapp.routes.ContentRouting
import com.musicapp.services.ContentService
import com.musicapp.repos.ArtistRepository
import com.musicapp.repos.AlbumRepository
import com.musicapp.repos.TrackRepository
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    println("Iniciando aplicación Spotify Clone API...")

    // Configuracion base de datos
    val dbUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: System.getenv("DATABASE_URL")
        ?: "jdbc:postgresql://localhost:5432/spotify_db"

    val dbUser = environment.config.propertyOrNull("database.user")?.getString()
        ?: System.getenv("DATABASE_USER")
        ?: "ktor_user"

    val dbPassword = environment.config.propertyOrNull("database.password")?.getString()
        ?: System.getenv("DATABASE_PASSWORD")
        ?: "aplicacionesweb"

    DatabaseFactory.init(
        jdbcUrl = dbUrl,
        username = dbUser,
        password = dbPassword
    )

    println("Base de datos conectada: $dbUrl")

    // Configuracion AWS S3
    val s3Service = try {
        S3Service(environment.config)
    } catch (e: Exception) {
        println("ADVERTENCIA: No se pudo leer s3.bucketName de application.conf")
        println("Intentando usar variables de entorno...")

        val bucketName = System.getenv("S3_BUCKET_NAME") ?: "amzn-s3-mispotifyapi"
        val region = System.getenv("AWS_REGION") ?: "us-east-1"

        val manualConfig = MapApplicationConfig(
            "aws.region" to region,
            "s3.bucketName" to bucketName
        )
        S3Service(manualConfig)
    }
    println("AWS S3 configurado")

    // Inicialización de repositorios
    val artistRepository = ArtistRepository
    val albumRepository = AlbumRepository
    val trackRepository = TrackRepository

    // Servicio completo con archivos (S3)
    val contentService = ContentService(
        s3Service = s3Service,
        artistRepository = artistRepository,
        albumRepository = albumRepository,
        trackRepository = trackRepository
    )

    // Servicio
    val ContentService = ContentService(
        artistRepository = artistRepository,
        albumRepository = albumRepository,
        trackRepository = trackRepository
    )

    // Json Content Negotiation
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
    println("Content Negotiation (JSON) configurado")

    // CORS
    install(CORS) {
        val allowedOrigins = System.getenv("ALLOWED_ORIGINS")?.split(",")
            ?: listOf("*")

        if (allowedOrigins.contains("*")) {
            anyHost()
        } else {
            allowedOrigins.forEach { origin ->
                allowHost(origin.trim(), schemes = listOf("http", "https"))
            }
        }

        // Headers permitidos
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)

        // Métodos HTTP permitidos
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowCredentials = true

        maxAgeInSeconds = 3600
    }
    println("CORS configurado (Origins: ${System.getenv("ALLOWED_ORIGINS") ?: "all"})")

    // Autenticación JWT
    install(Authentication) {
        jwt("auth-jwt") {
            AuthService.configureJwt(this)
        }
    }
    println("Autenticación JWT configurada")

    // Rutas
    routing {
        // Rutas de autenticación
        authRouting()

        // RUTAS SIMPLIFICADAS
        ContentRouting(ContentService)

        // RUTAS COMPLETAS
        contentRouting(contentService)
    }

    println("Servidor iniciado en http://0.0.0.0:8080")
    println("Health check disponible en: http://0.0.0.0:8080/health")
}

