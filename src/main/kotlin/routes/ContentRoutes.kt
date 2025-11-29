package com.musicapp.routes

import com.musicapp.models.SearchResponse
import com.musicapp.models.TracksWrapper
import com.musicapp.services.ContentService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.contentRouting(contentService: ContentService) {
    // Endpoint público de búsqueda de canciones
    get("/search") {
        val query = call.request.queryParameters["q"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing query parameter 'q'")

        val trackDTOs = contentService.searchTracks(query)

        call.respond(SearchResponse(tracks = TracksWrapper(items = trackDTOs)))
    }

    // Rutas protegidas con autenticación JWT
    authenticate("auth-jwt") {

        // Crea un nuevo artista con rol de ADMIN
        route("/artists") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (role != "ADMIN") {
                    return@post call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
                }

                val multipart = call.receiveMultipart()
                var name: String? = null
                var genre: String? = null
                var imagePart: PartData.FileItem? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "genre" -> genre = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "image") imagePart = part
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (name == null || genre == null || imagePart == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, genre, or image file")
                }

                try {
                    val newArtist = contentService.createArtist(name!!, genre!!, imagePart!!)
                    call.respond(HttpStatusCode.Created, newArtist)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating artist: ${e.message}")
                }
            }
        }


        // Crea un nuevo álbum con rol de ADMIN
        route("/albums") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (role != "ADMIN") {
                    return@post call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
                }

                val multipart = call.receiveMultipart()
                var name: String? = null
                var artistId: String? = null
                var year: String? = null
                var albumArtPart: PartData.FileItem? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "artistId" -> artistId = part.value
                                "year" -> year = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "albumArt") albumArtPart = part
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (name == null || artistId == null || year == null || albumArtPart == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, artistId, year, or albumArt file")
                }

                try {
                    val newAlbum = contentService.createAlbum(name!!, artistId!!, year!!.toInt(), albumArtPart!!)
                    call.respond(HttpStatusCode.Created, newAlbum)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating album: ${e.message}")
                }
            }
        }

        // Crea una nueva canción con rol de ADMIN
        route("/tracks") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (role != "ADMIN") {
                    return@post call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
                }

                val multipart = call.receiveMultipart()
                var name: String? = null
                var albumId: String? = null
                var artistId: String? = null
                var duration: String? = null
                var previewPart: PartData.FileItem? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "albumId" -> albumId = part.value
                                "artistId" -> artistId = part.value
                                "duration" -> duration = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "preview") previewPart = part
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (name == null || albumId == null || artistId == null || duration == null || previewPart == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, albumId, artistId, duration, or preview file")
                }

                try {
                    val newTrack = contentService.createTrack(name!!, albumId!!, duration!!.toLong(), artistId!!, previewPart!!)
                    call.respond(HttpStatusCode.Created, newTrack)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating track: ${e.message}")
                }
            }
        }
    }
}