import io.ktor.http.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.musicapp.auth.AuthService
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
import io.ktor.server.plugins.multipart.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    val s3Service = S3Service(environment.config)
    val contentService = ContentService(s3Service)

    // Plugins
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
    }

    install(MultiPartFormSupport)

    // Seguridad JWT
    install(Authentication) {
        jwt("auth-jwt") {
            AuthService.configureJwt(this)
        }
    }

    // Rutas
    routing {
        authRouting()
        contentRouting(contentService)
    }
}

// --- Archivo de configuración por defecto para Ktor (application.conf) ---
// src/main/resources/application.conf
// No se pide, pero es necesario para la configuración de S3.

/*
ktor {
    deployment {
        port = 8080 // Puerto por defecto, será mapeado a 80 por authbind
    }
}
aws {
    s3 {
        bucketName = "mi-clon-spotify-s3-bucket" // Cambiar a tu bucket
        region = "us-east-1" // Cambiar a tu región
    }
}
*/