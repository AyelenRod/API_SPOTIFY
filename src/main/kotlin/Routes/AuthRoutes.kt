package Routes

import com.musicapp.auth.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRouting() {
    route("/auth") {
        post("/login") {
            val credentials = call.receive<Map<String, String>>()
            val username = credentials["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing username")
            val password = credentials["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")

            val token = AuthService.validateCredentials(username, password)
            if (token != null) {
                call.respond(mapOf("access_token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
    }
}