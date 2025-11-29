package com.musicapp.routes

import com.musicapp.auth.AuthService
import com.musicapp.models.toResponse
import com.musicapp.repos.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRouting() {
    route("/auth") {

        // POST /auth/register
        post("/register") {
            val credentials = call.receive<Map<String, String>>()

            val username = credentials["username"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing username")
            val password = credentials["password"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")
            val role = credentials["role"] ?: "USER"  // Por defecto USER

            if (role !in listOf("USER", "ADMIN")) {
                return@post call.respond(HttpStatusCode.BadRequest, "Invalid role. Use USER or ADMIN")
            }

            val existingUser = UserRepository.findByUsername(username)
            if (existingUser != null) {
                return@post call.respond(HttpStatusCode.Conflict, "Username already exists")
            }

            val newUser = UserRepository.createUser(username, password, role)
            if (newUser != null) {
                call.respond(HttpStatusCode.Created, newUser.toResponse())
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Error creating user")
            }
        }

        // POST /auth/login
        post("/login") {
            val credentials = call.receive<Map<String, String>>()

            val username = credentials["username"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing username")
            val password = credentials["password"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")

            val token = AuthService.validateCredentials(username, password)

            if (token != null) {
                call.respond(mapOf("access_token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
    }
}