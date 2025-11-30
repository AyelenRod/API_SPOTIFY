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

    // Health Check - Verifica que la API esté funcionando
    get("/health") {
        call.respond(HttpStatusCode.OK, mapOf(
            "status" to "UP",
            "service" to "MusicApp Backend",
            "timestamp" to System.currentTimeMillis()
        ))
    }

    // Búsqueda de Tracks
    get("/search") {
        val query = call.request.queryParameters["q"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing query parameter 'q'")

        val trackDTOs = contentService.searchTracks(query)
        call.respond(SearchResponse(tracks = TracksWrapper(items = trackDTOs)))
    }

    // Listar TODOS los artistas
    get("/artists") {
        try {
            val artists = contentService.getAllArtists()
            call.respond(HttpStatusCode.OK, artists)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving artists: ${e.message}")
        }
    }

    // Obtener UN artista por ID
    get("/artists/{id}") {
        val artistId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing artist ID")

        try {
            val artist = contentService.getArtistById(artistId)
            if (artist == null) {
                call.respond(HttpStatusCode.NotFound, "Artist not found")
            } else {
                call.respond(HttpStatusCode.OK, artist)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid ID format")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving artist: ${e.message}")
        }
    }

    // Listar TODOS los álbumes
    get("/albums") {
        try {
            val albums = contentService.getAllAlbums()
            call.respond(HttpStatusCode.OK, albums)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving albums: ${e.message}")
        }
    }

    // Obtener UN álbum por ID
    get("/albums/{id}") {
        val albumId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing album ID")

        try {
            val album = contentService.getAlbumById(albumId)
            if (album == null) {
                call.respond(HttpStatusCode.NotFound, "Album not found")
            } else {
                call.respond(HttpStatusCode.OK, album)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid ID format")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving album: ${e.message}")
        }
    }

    // Listar TODOS los tracks
    get("/tracks") {
        try {
            val tracks = contentService.getAllTracks()
            call.respond(HttpStatusCode.OK, tracks)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving tracks: ${e.message}")
        }
    }

    // Obtener UNA canción por ID
    get("/tracks/{id}") {
        val trackId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing track ID")

        try {
            val track = contentService.getTrackById(trackId)
            if (track == null) {
                call.respond(HttpStatusCode.NotFound, "Track not found")
            } else {
                call.respond(HttpStatusCode.OK, track)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid ID format")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving track: ${e.message}")
        }
    }


    // RUTAS PROTEGIDAS
    authenticate("auth-jwt") {

        // POST /artists - Crear artista (ADMIN)
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
                var imageBytes: ByteArray? = null
                var imageFileName: String? = null
                var imageContentType: ContentType? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "genre" -> genre = part.value
                            }
                            part.dispose()
                        }
                        is PartData.FileItem -> {
                            if (part.name == "image") {
                                imageBytes = part.streamProvider().readBytes()
                                imageFileName = part.originalFileName
                                imageContentType = part.contentType
                            }
                            part.dispose()
                        }
                        else -> part.dispose()
                    }
                }

                if (name == null || genre == null || imageBytes == null || imageFileName == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, genre, or image file")
                }

                try {
                    val newArtist = contentService.createArtist(
                        name!!,
                        genre!!,
                        imageBytes!!,
                        imageFileName!!,
                        imageContentType?.toString()
                    )
                    call.respond(HttpStatusCode.Created, newArtist)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating artist: ${e.message}")
                }
            }
        }

        // POST /albums - Crear álbum (ADMIN)
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
                var albumArtBytes: ByteArray? = null
                var albumArtFileName: String? = null
                var albumArtContentType: ContentType? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "artistId" -> artistId = part.value
                                "year" -> year = part.value
                            }
                            part.dispose()
                        }
                        is PartData.FileItem -> {
                            if (part.name == "albumArt") {
                                albumArtBytes = part.streamProvider().readBytes()
                                albumArtFileName = part.originalFileName
                                albumArtContentType = part.contentType
                            }
                            part.dispose()
                        }
                        else -> part.dispose()
                    }
                }

                if (name == null || artistId == null || year == null || albumArtBytes == null || albumArtFileName == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, artistId, year, or albumArt file")
                }

                try {
                    val newAlbum = contentService.createAlbum(
                        name!!,
                        artistId!!,
                        year!!.toInt(),
                        albumArtBytes!!,
                        albumArtFileName!!,
                        albumArtContentType?.toString()
                    )
                    call.respond(HttpStatusCode.Created, newAlbum)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating album: ${e.message}")
                }
            }
        }

        // POST /tracks - Crear track/canción (ADMIN)
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
                var previewBytes: ByteArray? = null
                var previewFileName: String? = null
                var previewContentType: ContentType? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "albumId" -> albumId = part.value
                                "artistId" -> artistId = part.value
                                "duration" -> duration = part.value
                            }
                            part.dispose()
                        }
                        is PartData.FileItem -> {
                            if (part.name == "preview") {
                                previewBytes = part.streamProvider().readBytes()
                                previewFileName = part.originalFileName
                                previewContentType = part.contentType
                            }
                            part.dispose()
                        }
                        else -> part.dispose()
                    }
                }

                if (name == null || albumId == null || artistId == null || duration == null || previewBytes == null || previewFileName == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, albumId, artistId, duration, or preview file")
                }

                try {
                    val newTrack = contentService.createTrack(
                        name!!,
                        albumId!!,
                        duration!!.toLong(),
                        artistId!!,
                        previewBytes!!,
                        previewFileName!!,
                        previewContentType?.toString()
                    )
                    call.respond(HttpStatusCode.Created, newTrack)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating track: ${e.message}")
                }
            }
        }
    }
}